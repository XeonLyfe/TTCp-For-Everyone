package tudbut.mod.client.ttcp.mods.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.Slot;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiPlayerSelect;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.misc.AltControl;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.DoubleTypedObject;
import tudbut.obj.Save;
import tudbut.parsing.TudSort;
import tudbut.tools.Lock;
import tudbut.tools.Queue;

@Combat
public class HopperAura
extends Module {
    @Save
    int delay = 300;
    @Save
    int randomDelay = 0;
    @Save
    public int attack = 0;
    @Save
    boolean threadMode = false;
    @Save
    boolean swing = true;
    @Save
    boolean superAttack = false;
    @Save
    boolean batch = false;
    boolean cBatch = false;
    @Save
    boolean tpsSync = false;
    @Save
    int iterations = 1;
    @Save
    int iterationDelay = 0;
    Lock switchTimer = new Lock();
    BlockPos currentHopper = null;
    Container hopper = null;
    public static State state = State.WAITING;
    static boolean paused = false;
    static boolean hopperNeedsOpening = false;
    static boolean guiNeedsClosing = false;
    Lock placeLock = new Lock();
    Lock closeLock = new Lock();
    Lock emptyLock = new Lock();
    boolean empty = false;
    ArrayList<BlockPos> validHoppers = new ArrayList();
    ArrayList<DoubleTypedObject<BlockPos, Lock>> digging = new ArrayList();
    Queue<EntityLivingBase> toAttack = new Queue();
    public ArrayList<String> targets = new ArrayList();
    Lock threadLock = new Lock(true);
    Lock timer = new Lock();
    Thread thread = new Thread(() -> {
        while (true) {
            try {
                while (true) {
                    this.threadLock.waitHere();
                    this.timer.waitHere((int)((float)(this.delay / 6) * Utils.tpsMultiplier()));
                    if (this.enabled) {
                        this.onTick();
                        continue;
                    }
                    this.timer.lock((int)((float)(this.delay / 2) * Utils.tpsMultiplier()));
                }
            }
            catch (Exception exception) {
                continue;
            }
            break;
        }
    }, "HopperAura");
    static HopperAura instance;

    public HopperAura() {
        this.thread.start();
        this.customKeyBinds.set("select", new Module.KeyBind(null, this.toString() + "::triggerSelect", false));
        instance = this;
    }

    public static void pause() {
        paused = true;
        instance.reloadHopper();
    }

    public static void resume() {
        paused = false;
        instance.reloadHopper();
    }

    /*
     * Enabled aggressive block sorting
     */
    public void reloadHopper() {
        if (!this.enabled) return;
        if (this.currentHopper == null) {
            hopperNeedsOpening = this.createHopper();
            this.emptyLock.unlock();
            this.empty = false;
            return;
        }
        if (this.player.field_71070_bA instanceof ContainerHopper || hopperNeedsOpening) {
            Vec3d vec3d = new Vec3d((Vec3i)this.currentHopper);
            if (!(this.player.func_174824_e(0.0f).distanceTo(vec3d.addVector(0.5, 0.5, 0.5)) >= 4.0)) return;
        }
        if (HopperAura.mc.world.func_180495_p(this.currentHopper).getBlock() == Blocks.HOPPER) {
            Vec3d vec3d = new Vec3d((Vec3i)this.currentHopper);
            if (this.player.func_174824_e(0.0f).distanceTo(vec3d.addVector(0.5, 0.5, 0.5)) < 4.0) {
                ChatUtils.print("§aReopening hopper");
                this.hopper = null;
                this.openHopper(this.currentHopper);
                this.emptyLock.unlock();
                this.empty = false;
                return;
            }
        }
        ChatUtils.print("§aPrevious hopper unusable, removing");
        this.hopper = null;
        this.currentHopper = null;
    }

    private void openHopper(BlockPos theHopper) {
        state = State.OPENING;
        HopperAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.player, CPacketEntityAction.Action.STOP_SNEAKING));
        this.player.func_70095_a(false);
        HopperAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.player, CPacketEntityAction.Action.STOP_SNEAKING));
        this.player.func_70095_a(false);
        BlockUtils.clickOnBlock(theHopper, EnumHand.MAIN_HAND);
        guiNeedsClosing = true;
        state = State.WAITING;
    }

    private BlockPos getBestHopperPos() {
        state = State.PLACING;
        if (Notifications.getNotifications().stream().noneMatch(notification -> notification.text.equals("Trying to place new hopper"))) {
            Notifications.add(new Notifications.Notification("Trying to place new hopper", 1000));
        }
        BlockPos p = BlockUtils.getRealPos(this.player.func_174791_d());
        ArrayList<BlockPos> possible = new ArrayList<BlockPos>();
        for (int z = -3; z <= 3; ++z) {
            for (int y = -2; y <= 3; ++y) {
                for (int x = -3; x <= 3; ++x) {
                    int iz;
                    int iy;
                    int ix = p.func_177958_n() + x;
                    BlockPos pos = new BlockPos(ix, iy = p.func_177956_o() + y, iz = p.func_177952_p() + z);
                    if (!HopperAura.mc.world.func_180495_p(pos).func_185904_a().isReplaceable()) continue;
                    Vec3d vec3d = new Vec3d((Vec3i)pos);
                    if (!(this.player.func_174824_e(0.0f).distanceTo(vec3d.addVector(0.5, 0.5, 0.5)) < 3.0) || !HopperAura.mc.world.func_180495_p(pos.up()).func_185904_a().isReplaceable()) continue;
                    Vec3d vec3d2 = new Vec3d((Vec3i)pos.up());
                    if (!(this.player.func_174824_e(0.0f).distanceTo(vec3d2.addVector(0.5, 0.5, 0.5)) < 3.0) || BlockUtils.getPossibleSides(pos).size() < 1 || !HopperAura.mc.world.field_73010_i.stream().noneMatch(entityPlayer -> entityPlayer.func_174813_aQ().intersects(new AxisAlignedBB(pos)) || entityPlayer.func_174813_aQ().intersects(new AxisAlignedBB(pos.up())))) continue;
                    possible.add(pos);
                }
            }
        }
        if (possible.size() == 0) {
            ChatUtils.print("Can't find a suitable position");
            return null;
        }
        return TudSort.sort(possible.toArray(new BlockPos[0]), blockPos -> {
            List playerEntities = HopperAura.mc.world.field_73010_i;
            double d = 0.0;
            int playerEntitiesSize = playerEntities.size();
            for (int i = 0; i < playerEntitiesSize; ++i) {
                EntityPlayer aPlayer = (EntityPlayer)playerEntities.get(i);
                if (aPlayer == this.player || Arrays.stream(Utils.getAllies()).anyMatch(player -> player == aPlayer)) {
                    d -= BlockUtils.getRealPos(aPlayer.func_174791_d()).func_177951_i((Vec3i)blockPos) / 2.0;
                    continue;
                }
                d += BlockUtils.getRealPos(aPlayer.func_174791_d()).func_177951_i((Vec3i)blockPos);
            }
            return (long)(d * 100.0);
        })[0];
    }

    private boolean createHopper() {
        BlockPos found;
        state = State.PLACING;
        this.currentHopper = null;
        if (this.validHoppers.size() > 0) {
            for (int i = 0; i < this.validHoppers.size(); ++i) {
                BlockPos pos = this.validHoppers.get(i);
                if (HopperAura.mc.world.func_180495_p(pos).getBlock() == Blocks.HOPPER) {
                    Vec3d vec3d = new Vec3d((Vec3i)pos);
                    if (!(this.player.func_174824_e(0.0f).distanceTo(vec3d.addVector(0.5, 0.5, 0.5)) < 3.0)) continue;
                    ChatUtils.print("§aReusing a hopper");
                    this.placeLock.lock(1000);
                    this.hopper = null;
                    this.currentHopper = pos;
                    this.openHopper(this.currentHopper);
                    return true;
                }
                this.validHoppers.remove(i--);
            }
        }
        if ((found = this.getBestHopperPos()) != null) {
            Integer slot0 = InventoryUtils.getSlotWithItem(this.player.field_71069_bz, (Block)Blocks.HOPPER, new int[0], 1, 64);
            Integer slot1 = InventoryUtils.getSlotWithItem(this.player.field_71069_bz, Blocks.BLACK_SHULKER_BOX, new int[0], 1, 64);
            if (slot0 != null && slot1 != null) {
                this.currentHopper = found;
                this.validHoppers.add(this.currentHopper);
                if (this.hopper != null) {
                    this.player.connection.sendPacket((Packet)new CPacketCloseWindow(this.hopper.windowId));
                    this.hopper = null;
                }
                HopperAura.mc.playerController.windowClick(this.player.field_71069_bz.windowId, slot0.intValue(), InventoryUtils.getCurrentSlot(), ClickType.SWAP, (EntityPlayer)this.player);
                BlockUtils.placeBlock(found, EnumHand.MAIN_HAND, true, true);
                BlockUtils.placeBlock(found, EnumHand.MAIN_HAND, true, false);
                HopperAura.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.player, CPacketEntityAction.Action.START_SNEAKING));
                this.player.func_70095_a(true);
                HopperAura.mc.playerController.windowClick(this.player.field_71069_bz.windowId, slot1.intValue(), InventoryUtils.getCurrentSlot(), ClickType.SWAP, (EntityPlayer)this.player);
                BlockUtils.placeBlock(found.up(), EnumHand.MAIN_HAND, true, true);
                BlockUtils.placeBlock(found.up(), EnumHand.MAIN_HAND, true, false);
                HopperAura.mc.playerController.windowClick(this.player.field_71069_bz.windowId, slot1.intValue(), InventoryUtils.getCurrentSlot(), ClickType.SWAP, (EntityPlayer)this.player);
                HopperAura.mc.playerController.windowClick(this.player.field_71069_bz.windowId, slot0.intValue(), InventoryUtils.getCurrentSlot(), ClickType.SWAP, (EntityPlayer)this.player);
                ChatUtils.print("§aPlaced new hopper");
                this.placeLock.lock(1000);
                state = State.WAITING;
                return true;
            }
        }
        return false;
    }

    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> {
            this.targets.clear();
            this.targets.add(player.getGameProfile().getName());
        }, "Set HopperAura target"));
    }

    public void triggerSelect() {
        this.targets.clear();
        TTCp.mc.displayGuiScreen((GuiScreen)new GuiPlayerSelect((EntityPlayer[])TTCp.world.playerEntities.stream().filter(player -> !player.getName().equals(TTCp.player.func_70005_c_())).toArray(EntityPlayer[]::new), player -> {
            this.targets.remove(player.getName());
            this.targets.add(player.getName());
            return false;
        }));
    }

    public static HopperAura getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Delay: " + this.delay, it -> {
            this.delay = Keyboard.isKeyDown((int)42) ? (this.delay -= 25) : (this.delay += 25);
            if (this.delay < 50) {
                this.delay = 1000;
            }
            if (this.delay > 1000) {
                this.delay = 50;
            }
            it.text = "Delay: " + this.delay;
        }));
        this.subComponents.add(new Button("Attack " + (this.attack == 0 ? "all" : (this.attack == 1 ? "players" : "targets")), it -> {
            ++this.attack;
            if (this.attack > 2) {
                this.attack = 0;
            }
            it.text = "Attack " + (this.attack == 0 ? "all" : (this.attack == 1 ? "players" : "targets"));
        }));
        this.subComponents.add(Setting.createInt(0, 500, "RandomDelay", this, "randomDelay"));
        this.subComponents.add(Setting.createBoolean("Thread mode", this, "threadMode"));
        this.subComponents.add(Setting.createBoolean("Swing", this, "swing"));
        this.subComponents.add(Setting.createBoolean("Batches", this, "batch"));
        this.subComponents.add(Setting.createBoolean("TPSSync", this, "tpsSync"));
        this.subComponents.add(Setting.createInt(1, 10, "Iterations (i/a)", this, "iterations"));
        this.subComponents.add(Setting.createInt(0, 100, "IterationDelay", this, "iterationDelay"));
    }

    @Override
    public void onTick() {
        int i;
        if (this.threadMode) {
            this.threadLock.unlock();
        } else {
            this.threadLock.lock();
        }
        if (this.threadMode && Thread.currentThread() != this.thread) {
            return;
        }
        for (int i2 = 0; i2 < this.digging.size(); ++i2) {
            if (((Lock)this.digging.get((int)i2).t).isLocked()) continue;
            this.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, (BlockPos)this.digging.get((int)i2).o, EnumFacing.UP));
            this.digging.remove(i2--);
        }
        if (!this.placeLock.isLocked() && hopperNeedsOpening) {
            this.currentHopper = null;
            this.hopper = null;
            hopperNeedsOpening = false;
        }
        if (hopperNeedsOpening && this.currentHopper != null && HopperAura.mc.world.func_180495_p(this.currentHopper).getBlock() == Blocks.HOPPER) {
            hopperNeedsOpening = false;
            this.openHopper(this.currentHopper);
            state = State.WAITING;
        }
        if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer && guiNeedsClosing && this.currentHopper != null || this.closeLock.isLocked()) {
            if (!this.closeLock.isLocked()) {
                this.closeLock.lock(500);
            }
            this.hopper = ((GuiContainer)Minecraft.getMinecraft().currentScreen).inventorySlots;
            Minecraft.getMinecraft().displayGuiScreen(null);
            guiNeedsClosing = false;
        }
        if (!this.toAttack.hasNext()) {
            EntityPlayer[] players = TTCp.world.playerEntities.toArray(new EntityPlayer[0]);
            for (i = 0; i < players.length; ++i) {
                if (!(players[i].func_70032_d((Entity)TTCp.player) < 6.0f) || Team.getInstance().names.contains(players[i].getGameProfile().getName()) || Friend.getInstance().names.contains(players[i].getGameProfile().getName()) || players[i].getGameProfile().getName().equals(TTCp.mc.getSession().getProfile().getName()) || AltControl.getInstance().isAlt(players[i])) continue;
                if (!this.targets.isEmpty() || this.attack == 2) {
                    if (!this.targets.contains(players[i].getGameProfile().getName())) continue;
                    this.toAttack.add((EntityLivingBase)players[i]);
                    continue;
                }
                this.toAttack.add((EntityLivingBase)players[i]);
            }
        }
        if (!this.toAttack.hasNext() && this.attack == 0) {
            EntityLivingBase[] entities = Utils.getEntities(EntityLivingBase.class, EntityLivingBase::isEntityAlive);
            for (i = 0; i < entities.length; ++i) {
                if (!(entities[i].func_70032_d((Entity)TTCp.player) < 6.0f) || entities[i] instanceof EntityPlayer) continue;
                this.toAttack.add(entities[i]);
            }
        }
        if (!this.switchTimer.isLocked()) {
            this.switchTimer.lock();
        }
        if (!this.timer.isLocked()) {
            int e = this.extraDelay();
            this.switchTimer.lock(this.delay(e) / 3);
            this.timer.lock(this.delay(e));
            this.cBatch = !this.cBatch && this.batch;
            if (this.cBatch) {
                this.timer.lock(this.delay(e) * 2);
                this.switchTimer.lock(this.delay(e) * 2 / 3);
            }
            if (this.toAttack.hasNext()) {
                this.attackNext();
            } else {
                state = State.IDLE;
            }
        }
    }

    private int delay(int e) {
        return (int)((float)this.delay + (float)e * (this.tpsSync ? Utils.tpsMultiplier() : 1.0f));
    }

    private int extraDelay() {
        return (int)((double)this.randomDelay * Math.random());
    }

    private boolean getWeapon() {
        this.reloadHopper();
        if (this.currentHopper == null) {
            return false;
        }
        if (this.hopper == null) {
            return false;
        }
        if (this.player.func_184614_ca().getCount() >= 1) {
            return true;
        }
        state = State.REPLACING;
        List inventorySlots = this.hopper.inventorySlots;
        boolean d = false;
        for (int i = 0; i < 5; ++i) {
            Slot slot = (Slot)inventorySlots.get(i);
            if (!slot.getHasStack() || slot.getStack().getCount() < 1) continue;
            HopperAura.mc.playerController.windowClick(this.hopper.windowId, i, 0, ClickType.PICKUP, (EntityPlayer)this.player);
            HopperAura.mc.playerController.windowClick(this.hopper.windowId, 32 + InventoryUtils.getCurrentSlot(), 1, ClickType.PICKUP, (EntityPlayer)this.player);
            HopperAura.mc.playerController.windowClick(this.hopper.windowId, i, 0, ClickType.PICKUP, (EntityPlayer)this.player);
            HopperAura.mc.playerController.windowClick(this.hopper.windowId, -999, 0, ClickType.PICKUP, (EntityPlayer)this.player);
            d = true;
            break;
        }
        if (!d) {
            if (!this.empty) {
                this.empty = true;
                this.emptyLock.lock(1000);
            }
            if (this.empty && !this.emptyLock.isLocked()) {
                this.currentHopper = null;
                this.hopper = null;
                this.validHoppers.remove(this.currentHopper);
                this.emptyLock.unlock();
                this.empty = false;
            }
        } else {
            this.emptyLock.unlock();
            this.empty = false;
        }
        return d;
    }

    public void attackNext() {
        EntityLivingBase entity = this.toAttack.next();
        state = State.ATTACKING;
        if (!this.superAttack || entity.hurtTime <= 0) {
            for (int i = 0; i < this.iterations; ++i) {
                if (!this.getWeapon()) {
                    state = State.WAITING;
                    continue;
                }
                BlockUtils.lookAt(entity.func_174791_d().addVector(0.0, (entity.func_174813_aQ().maxY - entity.func_174813_aQ().minY) / 2.0, 0.0));
                TTCp.mc.playerController.attackEntity((EntityPlayer)TTCp.player, (Entity)entity);
                if (this.swing) {
                    TTCp.player.swingArm(EnumHand.MAIN_HAND);
                }
                if (this.iterations <= 1) continue;
                try {
                    Thread.sleep(this.iterationDelay);
                    continue;
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 4;
    }

    public static enum SwitchType {
        HOTBAR,
        SWAP;

    }

    public static enum State {
        PLACING,
        ATTACKING,
        REPLACING,
        OPENING,
        WAITING,
        IDLE;

    }
}
