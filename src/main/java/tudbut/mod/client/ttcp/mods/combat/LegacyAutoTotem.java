package tudbut.mod.client.ttcp.mods.combat;

import java.util.ArrayList;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import tudbut.debug.DebugProfiler;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.tools.Lock;
import tudbut.tools.ThreadPool;

@Combat
public class LegacyAutoTotem
extends Module {
    static LegacyAutoTotem instance;
    public static DebugProfiler profiler;
    public int minCount = 0;
    public int origMinCount = 0;
    public boolean isRestockingAfterRespawn = false;
    public boolean autoStack = false;
    private boolean autoStackIgnoreCount = false;
    public int delay = 0;
    Lock swapLock = new Lock();
    ThreadPool swapThread = new ThreadPool(1, "Swap thread", true);
    private boolean noTotems = true;

    public LegacyAutoTotem() {
        instance = this;
    }

    public static LegacyAutoTotem getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createInt(0, 12, "Count", this, "origMinCount"));
        this.subComponents.add(Setting.createBoolean("AutoStack (WIP)", this, "autoStack"));
        this.subComponents.add(Setting.createInt(0, 5000, "Delay", this, "delay"));
        this.subComponents.add(new Button("AutoStack now", it -> {
            this.autoStackIgnoreCount = true;
            this.autoStack();
            this.autoStackIgnoreCount = false;
        }));
        this.subComponents.add(new Button("Actual count: " + this.minCount, it -> {}));
    }

    @Override
    public void onSubTick() {
        if (TTCp.isIngame() && !this.swapLock.isLocked()) {
            EntityPlayerSP player = TTCp.player;
            profiler.next("RestockCheck");
            if (this.isRestockingAfterRespawn() || this.isRestockingAfterRespawn) {
                return;
            }
            if (this.noTotems) {
                profiler.next("TotCountUpdate");
                this.updateTotCount();
            }
            profiler.next("AutoStack");
            if (this.autoStack) {
                this.autoStack();
            }
            profiler.next("Check");
            ItemStack stack = player.func_184592_cb();
            int minCount = this.minCount;
            if (stack.getCount() <= minCount) {
                profiler.next("Switch.GetSlot");
                Integer slot = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, minCount + 1, 64);
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
                profiler.next("Switch.Swap");
                this.swapLock.lock(2000);
                this.swapThread.run(() -> {
                    InventoryUtils.inventorySwap(slot, 45, this.delay, 300L, 100L);
                    this.swapLock.lock(1000);
                });
                profiler.next("Switch.Notify");
                Notifications.add(new Notifications.Notification("Switched to next TotemStack"));
            }
        }
        profiler.next("idle");
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
        if (InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, this.origMinCount + 1, 64) != null) {
            this.minCount = this.origMinCount;
            this.updateBinds();
            return;
        }
        Integer i = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, this.minCount + 1, 64);
        while (i == null) {
            --this.minCount;
            i = InventoryUtils.getSlotWithItem(player.field_71069_bz, Items.TOTEM_OF_UNDYING, new int[]{45}, this.minCount + 1, 64);
            this.updateBinds();
            if (this.minCount >= 0) continue;
            this.minCount = 0;
            this.updateBinds();
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

    static {
        profiler = new DebugProfiler("LegacyAutoTotem", "idle");
    }
}
