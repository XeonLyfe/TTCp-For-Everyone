package tudbut.mod.client.ttcp.mods.combat;

import de.tudbut.type.Vector3d;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.events.ModuleEventRegistry;
import tudbut.mod.client.ttcp.utils.AutoCrystalUtil;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.DoubleTypedObject;
import tudbut.obj.Save;

@Combat
public class AutoCrystal
extends Module {
    private static AutoCrystal instance;
    @Save
    float crystalRange = 5.0f;
    @Save
    float minDmg = 0.0f;
    @Save
    float maxDmg = 3.0f;
    @Save
    float selfDamageCostM = 2.0f;
    @Save
    boolean render = true;
    @Save
    boolean sequential = false;
    @Save
    boolean fastBreak = true;
    @Save
    boolean predict = false;
    @Save
    int predictMargin = 2;
    @Save
    int maxChain = 10;
    int chain = 0;
    float eidsPerSecond = 1.0f;
    int lastEIDUpdateEIDs = -1;
    long lastEIDUpdate = System.currentTimeMillis();
    World lastWorld;
    EnumHand main = EnumHand.MAIN_HAND;
    EntityLivingBase currentTarget;
    ArrayList<DoubleTypedObject<BlockPos, Long>> ownCrystals = new ArrayList();
    Vec3d pos;

    public AutoCrystal() {
        instance = this;
        ModuleEventRegistry.disableOnNewPlayer.add(this);
        this.pos = new Vec3d(0.0, 0.0, 0.0);
    }

    public static AutoCrystal getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createFloat(2.0f, 10.0f, "CrystalRange", this, "crystalRange"));
        this.subComponents.add(Setting.createFloat(0.0f, 20.0f, "MinDamage", this, "minDmg"));
        this.subComponents.add(Setting.createFloat(0.5f, 20.0f, "MaxDamage", this, "maxDmg"));
        this.subComponents.add(Setting.createFloat(0.0f, 10.0f, "SelfDmgCost", this, "selfDamageCostM"));
        this.subComponents.add(Setting.createBoolean("Render", this, "render"));
        this.subComponents.add(Setting.createBoolean("Sequential", this, "sequential"));
        this.subComponents.add(Setting.createBoolean("FastBreak", this, "fastBreak"));
        this.subComponents.add(Setting.createBoolean("Predict", this, "predict"));
        this.subComponents.add(Setting.createInt(0, 40, "PredictMargin", this, "predictMargin"));
        this.subComponents.add(Setting.createInt(0, 40, "MaxChainLength", this, "maxChain"));
    }

    @Override
    public void onTick() {
        this.placeCrystal();
        this.breakCrystal();
    }

    public void updateOwnCrystals() {
        for (int i = 0; i < this.ownCrystals.size(); ++i) {
            if (System.currentTimeMillis() - (Long)this.ownCrystals.get((int)i).t <= 2000L) continue;
            this.ownCrystals.remove(i--);
        }
    }

    public void eid(int eid) {
        if (this.lastWorld != AutoCrystal.mc.world) {
            this.lastEIDUpdate = System.currentTimeMillis();
            this.lastEIDUpdateEIDs = -1;
            this.eidsPerSecond = 1.0f;
            this.currentTarget = null;
        }
        long timePassed = System.currentTimeMillis() - this.lastEIDUpdate;
        if (this.lastEIDUpdateEIDs == -1) {
            this.lastEIDUpdateEIDs = eid - 1;
        }
        int addedEIDs = eid - this.lastEIDUpdateEIDs;
        this.eidsPerSecond = (this.eidsPerSecond * 8.0f + (float)addedEIDs) / ((float)timePassed / 1000.0f);
        this.lastEIDUpdate = System.currentTimeMillis();
        this.lastEIDUpdateEIDs = eid;
    }

    private void remove(BlockPos pos) {
        for (int i = 0; i < this.ownCrystals.size(); ++i) {
            if (!((BlockPos)this.ownCrystals.get((int)i).o).equals((Object)pos.down())) continue;
            this.ownCrystals.remove(i);
            break;
        }
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        if (packet instanceof SPacketSpawnObject && ((SPacketSpawnObject)packet).getType() == 51) {
            SPacketSpawnObject spawner = (SPacketSpawnObject)packet;
            System.out.println("XX");
            if (this.fastBreak) {
                Vec3d vec = new Vec3d(spawner.getX(), spawner.getY(), spawner.getZ());
                if (this.isCrystalInRange(spawner.getX(), spawner.getY(), spawner.getZ()) && this.getCostForPlacement(this.currentTarget, (int)Math.floor(spawner.getX()), (int)Math.floor(spawner.getY()), (int)Math.floor(spawner.getZ())) < 0.0f) {
                    this.hitCrystal(spawner.getEntityID(), vec);
                    if (++this.chain >= this.maxChain) {
                        return false;
                    }
                    BlockPos pos = BlockUtils.getRealPos(vec);
                    if (this.ensureCrystalsSelected()) {
                        return false;
                    }
                    this.ownCrystals.add(new DoubleTypedObject<BlockPos, Long>(pos.down(), System.currentTimeMillis()));
                    BlockUtils.clickOnBlock(pos.down(), this.main);
                    if (this.predict) {
                        int eid = Math.round((float)spawner.getEntityID() + this.eidsPerSecond * (float)EventHandler.ping[0] / 1000.0f);
                        for (int i = -this.predictMargin; i <= this.predictMargin; ++i) {
                            this.player.connection.sendPacket((Packet)AutoCrystalUtil.createAttackPacket(eid + i));
                        }
                    }
                }
            }
        }
        if (packet instanceof SPacketDestroyEntities) {
            SPacketDestroyEntities remover = (SPacketDestroyEntities)packet;
            int[] ids = remover.getEntityIDs();
            for (int i = 0; i < ids.length; ++i) {
                this.notifyEntityDeath(ids[i]);
            }
        }
        return packet instanceof SPacketExplosion;
    }

    private boolean ensureCrystalsSelected() {
        return this.player.func_184614_ca().getItem() != Items.END_CRYSTAL;
    }

    private void notifyEntityDeath(int id) {
        Entity entity = AutoCrystal.mc.world.getEntityByID(id);
        if (entity instanceof EntityEnderCrystal) {
            this.remove(BlockUtils.getRealPos(entity.getPositionVector()));
        }
    }

    public BlockPos findBestPos(EntityLivingBase toAttack) {
        BlockPos best = null;
        float bestCost = Float.POSITIVE_INFINITY;
        for (int ix = (int)Math.floor(-this.crystalRange); ix <= (int)Math.ceil(this.crystalRange); ++ix) {
            for (int iy = (int)Math.floor(-this.crystalRange); iy <= (int)Math.ceil(this.crystalRange); ++iy) {
                for (int iz = (int)Math.floor(-this.crystalRange); iz <= (int)Math.ceil(this.crystalRange); ++iz) {
                    float cost;
                    double x = (double)ix + this.player.field_70165_t;
                    double y = (double)iy + this.player.field_70163_u;
                    double z = (double)iz + this.player.field_70161_v;
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!AutoCrystalUtil.canPlace(pos, this.crystalRange) || (cost = this.getCostForPlacement(toAttack, pos.func_177958_n(), pos.up().func_177956_o(), pos.func_177952_p())) == Float.POSITIVE_INFINITY || !(cost < bestCost)) continue;
                    best = pos;
                    bestCost = cost;
                }
            }
        }
        return best;
    }

    public void findTarget() {
        if (this.currentTarget == null || this.currentTarget.getHealth() == 0.0f) {
            EntityLivingBase best = null;
            float bestStat = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < AutoCrystal.mc.world.field_72996_f.size(); ++i) {
                Entity e = (Entity)AutoCrystal.mc.world.field_72996_f.get(i);
                if (!(e instanceof EntityLivingBase)) continue;
                EntityLivingBase entity = (EntityLivingBase)e;
                float stat = entity.getHealth() + entity.getAbsorptionAmount() + (float)entity.getTotalArmorValue() - entity.func_70032_d((Entity)this.player) * 20.0f;
                if (entity instanceof EntityPlayer) {
                    stat *= 100.0f;
                }
                if (!Arrays.stream(Utils.getAllies()).noneMatch(ally -> ally.equals((Object)entity)) || !(stat > bestStat)) continue;
                best = entity;
                bestStat = stat;
            }
            this.currentTarget = best;
            if (best != null) {
                ChatUtils.print("New target: " + best + " at " + bestStat);
            }
        }
    }

    public void placeCrystal() {
        if (this.lastWorld != AutoCrystal.mc.world) {
            this.lastEIDUpdate = System.currentTimeMillis();
            this.lastEIDUpdateEIDs = -1;
            this.eidsPerSecond = 1.0f;
            this.currentTarget = null;
            this.lastWorld = AutoCrystal.mc.world;
        }
        this.findTarget();
        if (this.currentTarget == null) {
            return;
        }
        if (this.ensureCrystalsSelected()) {
            return;
        }
        BlockPos pos = this.findBestPos(this.currentTarget);
        this.updateOwnCrystals();
        if (this.sequential && this.ownCrystals.size() != 0) {
            return;
        }
        if (pos != null) {
            this.ownCrystals.add(new DoubleTypedObject<BlockPos, Long>(pos, System.currentTimeMillis()));
            BlockUtils.clickOnBlock(pos, this.main);
            this.chain = 0;
        }
    }

    public void breakCrystal() {
        for (int i = 0; i < AutoCrystal.mc.world.field_72996_f.size(); ++i) {
            Entity e = (Entity)AutoCrystal.mc.world.field_72996_f.get(i);
            if (!(e instanceof EntityEnderCrystal) || !this.ownCrystals.stream().anyMatch(c -> ((BlockPos)c.o).equals((Object)BlockUtils.getRealPos(e.getPositionVector()).down()))) continue;
            this.hitCrystal(e.getEntityId(), e.getPositionVector());
            this.remove(BlockUtils.getRealPos(e.getPositionVector()));
        }
    }

    private void hitCrystal(int entityID, Vec3d pos) {
        Utils.setRotation(AutoCrystalUtil.createRotations(AutoCrystalUtil.createBB(pos)));
        this.player.connection.sendPacket((Packet)AutoCrystalUtil.createAttackPacket(entityID));
        this.player.swingArm(this.main);
    }

    private boolean isCrystalInRange(double x, double y, double z) {
        return this.player.func_174824_e(1.0f).distanceTo(new Vec3d(x, y, z)) <= (double)this.crystalRange;
    }

    public float getCostForPlacement(EntityLivingBase entityOther, int x, int y, int z) {
        float g = AutoCrystalUtil.getExplosionCost((EntityLivingBase)this.player, x, y, z);
        float h = AutoCrystalUtil.getExplosionCost(entityOther, x, y, z);
        System.out.println(g);
        if (h < this.minDmg || g > this.maxDmg && this.maxDmg != -1.0f) {
            return Float.POSITIVE_INFINITY;
        }
        return g * this.selfDamageCostM - h;
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public int danger() {
        return 3;
    }

    public void selectObby() {
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        try {
            if (this.enabled && TTCp.isIngame() && this.render) {
                int i;
                Entity e = TTCp.mc.getRenderViewEntity();
                assert (e != null);
                this.pos = e.getPositionEyes(event.getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
                EntityEnderCrystal[] crystals = TTCp.world.getEntities(EntityEnderCrystal.class, ent -> ent.func_70032_d((Entity)TTCp.player) < this.crystalRange * 5.0f).toArray(new EntityEnderCrystal[0]);
                for (i = 0; i < crystals.length; ++i) {
                    BlockPos bp = BlockUtils.getRealPos(crystals[i].func_174791_d());
                    float dmg = AutoCrystalUtil.getExplosionCost((EntityLivingBase)TTCp.player, bp.func_177958_n(), bp.func_177956_o(), bp.func_177952_p());
                    float f1 = Color.RGBtoHSB(255, 0, 255, null)[0] - 1.0f;
                    float f2 = Color.RGBtoHSB(0, 255, 0, null)[0];
                    float hue = f2 + (f1 - f2) * Math.min(dmg / 20.0f, 1.0f);
                    int color = 0xFF000000 | Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    this.drawAroundBlock(new Vector3d((double)bp.func_177958_n() + 0.5, bp.func_177956_o(), (double)bp.func_177952_p() + 0.5), color);
                    Tesselator.depth(false);
                    EntityRenderer.drawNameplate((FontRenderer)AutoCrystal.mc.fontRenderer, (String)(dmg + ""), (float)((float)(-this.pos.x + (double)bp.func_177958_n() + 0.5)), (float)((float)(-this.pos.y + (double)bp.func_177956_o() + 0.66)), (float)((float)(-this.pos.z + (double)bp.func_177952_p() + 0.5)), (int)0, (float)this.player.field_70177_z, (float)this.player.field_70125_A, (boolean)false, (boolean)false);
                    Tesselator.depth(true);
                }
                for (i = 0; i < this.ownCrystals.size(); ++i) {
                    BlockPos pos = (BlockPos)this.ownCrystals.get((int)i).o;
                    this.drawAroundBlock(new Vector3d((double)pos.func_177958_n() + 0.5, pos.func_177956_o(), (double)pos.func_177952_p() + 0.5), -2139062017);
                }
            }
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    public void drawAroundBlock(Vector3d pos, int color) {
        try {
            Tesselator.ready();
            Tesselator.translate(-this.pos.x, -this.pos.y, -this.pos.z);
            Tesselator.color(color);
            Tesselator.depth(false);
            Tesselator.begin(7);
            Tesselator.put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            Tesselator.put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            Tesselator.put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            Tesselator.put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            Tesselator.next();
            Tesselator.end();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static enum State {
        IDLE,
        ATTACK,
        BREAK,
        FACEPLACE;

    }
}
