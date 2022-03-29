package tudbut.mod.client.ttcp.mods.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiPlayerSelect;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.misc.AltControl;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.Save;
import tudbut.tools.Lock;
import tudbut.tools.Queue;

@Combat
public class KillAura
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
    boolean superAttack = false;
    @Save
    boolean batch = false;
    boolean cBatch = false;
    @Save
    boolean switchItem = false;
    boolean noSwitch = false;
    boolean cSwitch = true;
    @Save
    boolean tpsSync = false;
    @Save
    SwitchType switchType = SwitchType.HOTBAR;
    @Save
    int iterations = 1;
    @Save
    int iterationDelay = 0;
    @Save
    int iterationRandomDelay = 0;
    Lock switchTimer = new Lock();
    @Save
    boolean rotate = false;
    @Save
    boolean swing = true;
    @Save
    boolean offhandSwing = false;
    @Save
    boolean iSwing = true;
    @Save
    boolean iOffhandSwing = false;
    @Save
    int misses = 10;
    @Save
    int iterMisses = 25;
    @Save
    float autoDest = 1.0f;
    @Save
    boolean auto = false;
    @Save
    boolean oldAuto = false;
    Map<EntityLivingBase, Float> lastHealth = new HashMap<EntityLivingBase, Float>();
    Map<EntityLivingBase, Integer> lastTotems = new HashMap<EntityLivingBase, Integer>();
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
    }, "KillAura");
    static KillAura instance;
    boolean lastAutoAttack;
    float autoAttackAvrg;
    float autoAttackBiggerAvrg;
    float delayAvrg;
    int lastAdd;
    Map<Integer, Lock> swingNotifiers;

    public KillAura() {
        this.thread.start();
        this.customKeyBinds.set("select", new Module.KeyBind(null, this.toString() + "::triggerSelect", false));
        instance = this;
        this.lastAutoAttack = false;
        this.autoAttackAvrg = -1.0f;
        this.autoAttackBiggerAvrg = -1.0f;
        this.delayAvrg = this.delay;
        this.lastAdd = -10;
        this.swingNotifiers = new HashMap<Integer, Lock>();
    }

    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> {
            this.targets.clear();
            this.targets.add(player.getGameProfile().getName());
        }, "Set KillAura target"));
    }

    public void triggerSelect() {
        this.targets.clear();
        TTCp.mc.displayGuiScreen((GuiScreen)new GuiPlayerSelect((EntityPlayer[])TTCp.world.playerEntities.stream().filter(player -> !player.getName().equals(TTCp.player.func_70005_c_())).toArray(EntityPlayer[]::new), player -> {
            this.targets.remove(player.getName());
            this.targets.add(player.getName());
            return false;
        }));
    }

    public static KillAura getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createInt(10, 1000, "Delay", this, "delay"));
        this.subComponents.add(new Button("Attack " + (this.attack == 0 ? "all" : (this.attack == 1 ? "players" : "targets")), it -> {
            ++this.attack;
            if (this.attack > 2) {
                this.attack = 0;
            }
            it.text = "Attack " + (this.attack == 0 ? "all" : (this.attack == 1 ? "players" : "targets"));
        }));
        this.subComponents.add(Setting.createInt(0, 500, "RandomDelay", this, "randomDelay"));
        this.subComponents.add(Setting.createBoolean("Thread mode", this, "threadMode"));
        this.subComponents.add(Setting.createBoolean("SuperAttack", this, "superAttack"));
        this.subComponents.add(Setting.createBoolean("Batches", this, "batch"));
        this.subComponents.add(Setting.createBoolean("Switch", this, "switchItem"));
        this.subComponents.add(Setting.createBoolean("Rotate", this, "rotate"));
        this.subComponents.add(Setting.createBoolean("TPSSync", this, "tpsSync"));
        this.subComponents.add(Setting.createEnum(SwitchType.class, "SwitchType", this, "switchType"));
        this.subComponents.add(Setting.createInt(1, 10, "Iterations (i/a)", this, "iterations"));
        this.subComponents.add(Setting.createInt(0, 100, "IterDelay", this, "iterationDelay"));
        this.subComponents.add(Setting.createInt(0, 100, "IterRandomDelay", this, "iterationRandomDelay"));
        this.subComponents.add(Setting.createBoolean("Swing", this, "swing"));
        this.subComponents.add(Setting.createBoolean("IterSwing", this, "iSwing"));
        this.subComponents.add(Setting.createBoolean("OffhandSwing", this, "offhandSwing"));
        this.subComponents.add(Setting.createBoolean("IterOffhandSwing", this, "iOffhandSwing"));
        this.subComponents.add(Setting.createInt(0, 100, "MissChance", this, "misses"));
        this.subComponents.add(Setting.createInt(0, 100, "IterMissChance", this, "iterMisses"));
        this.subComponents.add(Setting.createBoolean("AutoSetup", this, "auto"));
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
            if (this.switchItem && this.toAttack.hasNext() && !this.noSwitch) {
                switch (this.switchType) {
                    case HOTBAR: {
                        int i2 = InventoryUtils.getCurrentSlot();
                        if (i2 == 0) {
                            this.cSwitch = true;
                        }
                        if (i2 == 8) {
                            this.cSwitch = false;
                        }
                        this.cSwitch = !this.cSwitch;
                        InventoryUtils.setCurrentSlot(i2 + (this.cSwitch ? -1 : 1));
                        break;
                    }
                    case SWAP: {
                        InventoryUtils.swap(36, 1);
                    }
                }
            }
        }
        if (!this.timer.isLocked()) {
            int e = this.extraDelay();
            this.switchTimer.lock(this.delay(e) / 3);
            this.timer.lock(this.delay(e));
            this.cBatch = !this.cBatch;
            if (this.cBatch && this.batch) {
                this.timer.lock(this.delay(e) * 2);
                this.switchTimer.lock(this.delay(e) * 2 / 3);
            }
            if (this.auto) {
                try {
                    i = this.lastTotems.get(this.toAttack.peek()) - this.countTotems(this.toAttack.peek());
                    if (this.lastHealth.get(this.toAttack.peek()).floatValue() > this.toAttack.peek().getHealth() || i != 0) {
                        if (i < 0) {
                            i = 1;
                        }
                        this.autoAttack(i);
                    } else {
                        this.autoAttack(0.0f);
                    }
                    this.updateBinds();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (this.toAttack.hasNext()) {
                this.attackNext();
            }
        }
    }

    private void autoAttack(float x) {
        if (this.autoAttackAvrg == -1.0f) {
            this.autoAttackAvrg = 0.0f;
            this.autoAttackBiggerAvrg = 0.0f;
        }
        this.autoAttackAvrg = (this.autoAttackAvrg * 4.0f + x) / 5.0f;
        this.autoAttackBiggerAvrg = (this.autoAttackBiggerAvrg * 24.0f + x) / 25.0f;
        this.delayAvrg = (this.delayAvrg * 24.0f + (float)this.delay) / 25.0f;
        if (this.autoAttackAvrg / (float)Math.max(this.delay, 1) < this.autoAttackBiggerAvrg / Math.max(this.delayAvrg, 1.0f)) {
            this.lastAdd = -this.lastAdd;
            this.delay += this.lastAdd;
        } else {
            this.delay += this.lastAdd;
        }
        this.delay = Math.max(this.delay, 0);
    }

    private int countTotems(EntityLivingBase entity) {
        int t = 0;
        ItemStack itemStack = entity.getHeldItem(EnumHand.MAIN_HAND);
        if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
            t = itemStack.getCount();
        } else {
            itemStack = entity.getHeldItem(EnumHand.OFF_HAND);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                t = itemStack.getCount();
            }
        }
        return t;
    }

    private int delay(int e) {
        return (int)((float)this.delay + (float)e * (this.tpsSync ? Utils.tpsMultiplier() : 1.0f));
    }

    private int extraDelay() {
        return (int)((double)this.randomDelay * Math.random());
    }

    public void attackNext() {
        EntityLivingBase entity = this.toAttack.next();
        if (this.swing) {
            TTCp.player.swingArm(EnumHand.MAIN_HAND);
        }
        if (this.offhandSwing) {
            TTCp.player.swingArm(EnumHand.OFF_HAND);
        }
        if (Math.random() <= (double)((float)this.misses / 100.0f)) {
            return;
        }
        if (!this.superAttack || entity.hurtTime <= 0) {
            this.lastHealth.put(entity, Float.valueOf(entity.getHealth()));
            this.lastTotems.put(entity, this.countTotems(entity));
            for (int i = 0; i < this.iterations; ++i) {
                if (this.rotate) {
                    BlockUtils.lookAt(entity.func_174791_d().addVector(Math.random() * 0.3 - 0.15, (entity.func_174813_aQ().maxY - entity.func_174813_aQ().minY) / 1.5 + Math.random() * 0.3 - 0.15, Math.random() * 0.3 - 0.15));
                }
                if (Math.random() > (double)((float)this.misses / 100.0f)) {
                    TTCp.mc.playerController.attackEntity((EntityPlayer)TTCp.player, (Entity)entity);
                }
                if (i != 0 && this.iSwing) {
                    TTCp.player.swingArm(EnumHand.MAIN_HAND);
                }
                if (i != 0 && this.iOffhandSwing) {
                    TTCp.player.swingArm(EnumHand.OFF_HAND);
                }
                if (this.iterations <= 1) continue;
                try {
                    Thread.sleep((long)((double)this.iterationDelay + (double)this.iterationRandomDelay * Math.random()));
                    continue;
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        if (packet instanceof SPacketAnimation && ((SPacketAnimation)packet).getAnimationType() == 0 && this.swingNotifiers.containsKey(((SPacketAnimation)packet).getEntityID())) {
            ChatUtils.print("§8Swing by " + KillAura.mc.world.field_73010_i.stream().filter(entityPlayer -> entityPlayer.func_145782_y() == ((SPacketAnimation)packet).getEntityID()).findFirst());
            this.swingNotifiers.get(((SPacketAnimation)packet).getEntityID()).unlock();
        }
        return false;
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (args.length == 1) {
            new Thread(() -> {
                String playerName = args[0];
                Optional<EntityPlayer> player = KillAura.mc.world.field_73010_i.stream().filter(entityPlayer -> entityPlayer.getName().equals(playerName)).findFirst();
                if (player.isPresent()) {
                    ChatUtils.print("Watching...");
                    EntityPlayer detect = player.get();
                    ArrayList<Long> timings = new ArrayList<Long>();
                    Lock lock = new Lock();
                    this.swingNotifiers.put(detect.func_145782_y(), lock);
                    for (int i = 0; i < 10; ++i) {
                        while (detect.field_110158_av != 1) {
                        }
                        timings.add(System.currentTimeMillis());
                    }
                    this.swingNotifiers.put(detect.func_145782_y(), null);
                    long last = (Long)timings.get(0);
                    long l = 0L;
                    for (int i = 1; i < timings.size(); ++i) {
                        l += (Long)timings.get(i) - last;
                        last = (Long)timings.get(i);
                    }
                    ChatUtils.print("§a[TTC] KA Speed of " + detect.getName() + ": " + (float)l / ((float)timings.size() - 1.0f));
                } else {
                    ChatUtils.print("That player is not in the visual range!");
                }
            }).start();
        }
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 3;
    }

    public static enum SwitchType {
        HOTBAR,
        SWAP;

    }
}
