package tudbut.mod.client.ttcp.mods.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.gui.GuiTTCIngame;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.combat.PopCount;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.DebugProfilerAdapter;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.Save;
import tudbut.tools.Lock;
import tudbut.tools.ThreadPool;

@Combat
public class AutoTotem
extends Module {
    public static AutoTotem instance;
    public static DebugProfilerAdapter profiler;
    public int minCount = 0;
    @Save
    public int prepCount = 2;
    @Save
    public int origMinCount = 0;
    public boolean isRestockingAfterRespawn = false;
    @Save
    public boolean autoStack = false;
    private boolean autoStackIgnoreCount = false;
    @Save
    public boolean pingPredict = false;
    @Save
    public int sdelay = 0;
    @Save
    public int pdelay = 0;
    @Save
    public int cdelay = 0;
    @Save
    public int ldelay = 500;
    @Save
    public boolean legacy = true;
    public int swapProgress = 0;
    public int countSwapped = 0;
    public int countSwappedAt;
    public long lastSwap = 0L;
    public int fullCount = 0;
    public boolean[] slotsUsed = new boolean[45];
    public int[] slotsUsedAtCounts = new int[45];
    public long[] slotsUsedAtTime = new long[45];
    Lock swapLock;
    ThreadPool swapThread;
    public boolean panic;
    private boolean noTotems;

    public void renderTotems() {
        if (this.fullCount != 0) {
            ScaledResolution res = new ScaledResolution(mc);
            int y = res.getScaledHeight() - 32 - 3 - 8;
            int x = this.player.func_184591_cq() != EnumHandSide.LEFT ? res.getScaledWidth() / 2 - 91 - 26 : res.getScaledWidth() / 2 + 91 + 10;
            GuiTTCIngame.drawOffhandSlot(x - 3, y - 3);
            GuiTTCIngame.drawItem(x, y, 1.0f, (EntityPlayer)this.player, new ItemStack(Items.TOTEM_OF_UNDYING, this.fullCount));
        }
    }

    public int getTotemCount() {
        return InventoryUtils.getItemAmount(this.player.field_71069_bz, Items.TOTEM_OF_UNDYING);
    }

    public void panic() {
        this.panic = true;
        this.doSwitch(true);
        this.panic = false;
    }

    public AutoTotem() {
        Arrays.fill(this.slotsUsedAtCounts, Integer.MAX_VALUE);
        Arrays.fill(this.slotsUsedAtCounts, 0);
        this.swapLock = new Lock();
        this.swapThread = new ThreadPool(1, "Swap thread", true);
        this.panic = false;
        this.noTotems = true;
        instance = this;
    }

    public static AutoTotem getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createInt(0, 12, "PrepCount", this, "prepCount"));
        this.subComponents.add(Setting.createInt(0, 12, "Count", this, "origMinCount", () -> {
            this.updateTotCount();
            this.updateBinds();
        }));
        this.subComponents.add(Setting.createBoolean("AutoStack (WIP)", this, "autoStack"));
        this.subComponents.add(Setting.createBoolean("PingPredict", this, "pingPredict"));
        this.subComponents.add(Setting.createInt(0, 500, "SwitchDelay", this, "sdelay"));
        this.subComponents.add(Setting.createInt(0, 500, "PostDelay", this, "pdelay"));
        this.subComponents.add(Setting.createInt(0, 5000, "CooldownDelay", this, "cdelay"));
        this.subComponents.add(Setting.createInt(0, 1000, "LockDelay", this, "ldelay"));
        this.subComponents.add(Setting.createBoolean("Fast mode", this, "legacy"));
        this.subComponents.add(new Button("AutoStack now", it -> {
            this.autoStackIgnoreCount = true;
            this.autoStack();
            this.autoStackIgnoreCount = false;
        }));
        this.subComponents.add(new Button("Actual count: " + this.minCount, it -> {}));
        this.customKeyBinds.setIfNull("panic", new Module.KeyBind(null, this + "::panic", true));
        this.subComponents.add(Setting.createKey("Panic", (Module.KeyBind)this.customKeyBinds.get("panic")));
    }

    public void doSwitch(boolean takeMax) {
        KillAura.getInstance().noSwitch = true;
        ItemStack stack = this.player.field_71071_by.getStackInSlot(45);
        profiler.next("Switch.GetSlot");
        Integer slot = null;
        if (takeMax) {
            int biggestCount = 0;
            int slotNum = -1;
            for (int i = 0; i < 45; ++i) {
                ItemStack itemStack = (ItemStack)this.player.field_71069_bz.getInventory().get(i);
                if (!itemStack.getItem().equals(Items.TOTEM_OF_UNDYING) || itemStack.getCount() <= biggestCount) continue;
                slotNum = i;
                biggestCount = itemStack.getCount();
            }
            if (slotNum != -1 && slotNum != 45) {
                slot = slotNum;
            }
        } else {
            slot = InventoryUtils.getSlotWithItem(this.player.field_71069_bz, Items.TOTEM_OF_UNDYING, this.getUnusableSlots(), this.minCount + 1, 64);
        }
        if (slot == null) {
            profiler.next("Switch.NotifyEmpty");
            if (!this.noTotems) {
                Notifications.add(new Notifications.Notification("No more totems! Couldn't switch!"));
            }
            this.noTotems = true;
            profiler.next("idle");
            return;
        }
        this.noTotems = false;
        this.slotsUsed[slot.intValue()] = true;
        this.swapLock.lock(2000);
        int finalSlot = slot;
        this.swapThread.run(() -> {
            if (this.legacy) {
                profiler.next("Switch.Notify");
                Notifications.add(new Notifications.Notification("Switching to next TotemStack..."));
                profiler.next("Switch.Swap");
                InventoryUtils.inventorySwap(finalSlot, 45, this.sdelay, this.pdelay, this.cdelay);
                this.swapLock.lock(this.ldelay);
                profiler.next("Switch.Notify");
                Notifications.add(new Notifications.Notification("Switched to next TotemStack"));
            } else {
                this.swapProgress = 1;
                this.countSwappedAt = stack.getCount();
                this.countSwapped = TTCp.player.field_71071_by.getStackInSlot(finalSlot).getCount();
                this.slotsUsedAtTime[finalSlot] = System.currentTimeMillis();
                this.lastSwap = System.currentTimeMillis();
                profiler.next("Switch.Notify");
                Notifications.add(new Notifications.Notification("Switching to next TotemStack..."));
                profiler.next("Switch.Swap");
                InventoryUtils.inventorySwap(finalSlot, 45, this.sdelay, this.pdelay, this.cdelay);
                this.swapLock.lock(this.ldelay);
                profiler.next("Switch.Notify");
                Notifications.add(new Notifications.Notification("Switched to next TotemStack"));
                this.swapProgress = 2;
            }
            KillAura.getInstance().noSwitch = false;
        });
    }

    @Override
    public void onEverySubTick() {
        if (TTCp.isIngame()) {
            this.fullCount = this.getTotemCount();
        }
    }

    @Override
    public void onSubTick() {
        if (TTCp.isIngame() && (!this.swapLock.isLocked() || this.panic)) {
            EntityPlayerSP player = TTCp.player;
            profiler.next("RestockCheck");
            if (this.isRestockingAfterRespawn() || this.isRestockingAfterRespawn) {
                return;
            }
            if (this.noTotems || this.minCount != this.origMinCount) {
                profiler.next("TotCountUpdate");
                this.reindex();
                this.swapLock.unlock();
                this.swapProgress = 0;
            }
            profiler.next("AutoStack");
            if (this.autoStack) {
                this.autoStack();
            }
            profiler.next("Check");
            ItemStack stack = player.func_184592_cb();
            if (!(this.legacy || this.swapProgress != 2 && this.lastSwap > System.currentTimeMillis() - 2000L)) {
                PopCount.Counter counter = TTCp.getModule(PopCount.class).counters.get((EntityPlayer)TTCp.player);
                if (stack.getCount() < this.countSwapped && stack.getCount() > Math.max(this.countSwappedAt, this.minCount) || this.lastSwap <= System.currentTimeMillis() - 2000L) {
                    this.swapProgress = 0;
                    this.reindex();
                }
            }
            boolean bl = KillAura.getInstance().noSwitch = stack.getCount() <= this.prepCount + this.minCount;
            if ((this.panic || stack.getCount() <= this.minCount || this.checkPingPop() && stack.getCount() <= this.minCount + 1) && (this.swapProgress == 0 || this.legacy)) {
                this.doSwitch(false);
            }
        }
        profiler.next("idle");
    }

    public void reindex() {
        for (int i = 0; i < this.slotsUsed.length; ++i) {
            ItemStack stack = this.player.field_71071_by.getStackInSlot(i);
            this.slotsUsedAtCounts[i] = Math.min(stack.getCount(), this.slotsUsedAtCounts[i]);
            if (stack.getItem() != Items.TOTEM_OF_UNDYING || stack.getCount() <= this.slotsUsedAtCounts[i] || !this.slotsUsed[i] || this.slotsUsedAtTime[i] >= System.currentTimeMillis() - 5000L) continue;
            this.slotsUsedAtCounts[i] = stack.getCount();
            this.slotsUsed[i] = false;
        }
    }

    private int[] getUnusableSlots() {
        this.reindex();
        ArrayList<Integer> slots = new ArrayList<Integer>(Collections.singletonList(45));
        for (int i = 0; i < this.slotsUsed.length; ++i) {
            if (!this.slotsUsed[i]) continue;
            slots.add(i);
        }
        int[] theSlots = new int[slots.size()];
        for (int i = 0; i < theSlots.length; ++i) {
            theSlots[i] = slots.get(i);
        }
        return theSlots;
    }

    private boolean checkPingPop() {
        if (!this.pingPredict || this.minCount == 0) {
            return false;
        }
        PopCount popCount = TTCp.getModule(PopCount.class);
        PopCount.Counter counter = popCount.counters.get((EntityPlayer)TTCp.player);
        if (counter != null && counter.isPopping()) {
            long d = counter.predictNextPopDelay();
            return d <= Math.max(EventHandler.ping[0], 0L);
        }
        return false;
    }

    public boolean isRestockingAfterRespawn() {
        EntityPlayerSP player = TTCp.player;
        GuiScreen screen = TTCp.mc.currentScreen;
        if (!(screen instanceof GuiContainer) || screen instanceof GuiInventory || screen instanceof GuiContainerCreative) {
            this.isRestockingAfterRespawn = false;
            return false;
        }
        Integer slot0 = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[0], 1, 64);
        if (slot0 == null) {
            this.isRestockingAfterRespawn = true;
            return true;
        }
        Integer slot1 = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{slot0}, 1, 64);
        if (slot1 == null) {
            this.isRestockingAfterRespawn = true;
            return true;
        }
        return false;
    }

    public void updateTotCount() {
        EntityPlayerSP player = TTCp.player;
        this.slotsUsed = new boolean[45];
        this.slotsUsedAtCounts = new int[45];
        this.slotsUsedAtTime = new long[45];
        this.minCount = this.origMinCount;
        Integer i = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, this.minCount + 1, 64);
        while (i == null) {
            --this.minCount;
            i = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, this.minCount + 1, 64);
            if (this.minCount >= 0) continue;
            this.minCount = 0;
            return;
        }
    }

    public void autoStack() {
        if (this.minCount == 0) {
            return;
        }
        EntityPlayerSP player = TTCp.player;
        ArrayList<Integer> slots = new ArrayList<Integer>();
        int min = 2;
        int max = 24;
        for (int i = 0; i < 50; ++i) {
            Integer slot;
            int j;
            ArrayList<Integer> dropped = new ArrayList<Integer>();
            if (slots.size() != 0) {
                for (j = 0; j < 100 && (slot = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, Utils.objectArrayToNativeArray(dropped.toArray(new Integer[0])), 0, min - 1)) != null; ++j) {
                    InventoryUtils.drop(slot);
                    System.out.println("Dropped item in " + slot);
                    dropped.add(slot);
                }
            }
            if (this.origMinCount == this.minCount && !this.autoStackIgnoreCount) {
                return;
            }
            slots.clear();
            for (j = 0; j < 100 && (slot = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, Utils.objectArrayToNativeArray(slots.toArray(new Integer[0])), min, max)) != null; ++j) {
                slots.add(slot);
            }
            while (slots.size() >= 2) {
                slot = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.AIR, 0);
                if (slot == null) {
                    InventoryUtils.drop((Integer)slots.get(0));
                    slots.remove(0);
                    continue;
                }
                System.out.println("Combining " + slots.get(0) + " and " + slots.get(1) + " to " + slot);
                InventoryUtils.clickSlot((Integer)slots.get(0), ClickType.PICKUP, 0);
                InventoryUtils.clickSlot((Integer)slots.get(1), ClickType.PICKUP, 0);
                InventoryUtils.clickSlot(slot, ClickType.PICKUP, 0);
                InventoryUtils.drop((Integer)slots.get(1));
                slots.remove(0);
                slots.remove(0);
            }
        }
    }

    @Override
    public void onChat(String s, String[] args) {
        if (s.startsWith("count ")) {
            try {
                this.origMinCount = this.minCount = Integer.parseInt(s.substring("count ".length()));
                ChatUtils.print("Set!");
            }
            catch (Exception e) {
                ChatUtils.print("ERROR: NaN");
            }
        }
        if (s.startsWith("debug")) {
            ChatUtils.print(profiler.getTempResults().toString());
        }
        this.updateBinds();
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    static {
        profiler = new DebugProfilerAdapter("AutoTotem", "idle");
        TTCp.registerProfiler(profiler);
    }
}
