package tudbut.mod.client.ttcp.mods.movement;

import de.tudbut.timer.AsyncTask;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Save;
import tudbut.tools.Lock;

@Movement
public class ElytraFlight
extends Module {
    @Save
    float speed = 1.0f;
    @Save
    boolean autoTakeoff = false;
    @Save
    boolean tpsSync = false;
    @Save
    float upDiv = 1.0f;
    @Save
    float boostMod = 2.0f;
    @Save
    int takeoffTicks = 1;
    int lastTakeoffTry = 0;
    @Save
    int restartDelay = 500;
    Lock takeoff = new Lock();
    boolean restarting = false;
    boolean init;

    @Override
    public void updateBinds() {
        this.customKeyBinds.setIfNull("faster", new Module.KeyBind(null, this + "::faster", false));
        this.customKeyBinds.setIfNull("slower", new Module.KeyBind(null, this + "::slower", false));
        this.customKeyBinds.setIfNull("boost", new Module.KeyBind(null, this + "::boost", true));
        this.customKeyBinds.setIfNull("restart", new Module.KeyBind(null, this + "::restart", true));
        this.subComponents.clear();
        this.subComponents.add(Setting.createFloat(1.0f, 10.0f, "Speed", this, "speed"));
        this.subComponents.add(Setting.createBoolean("AutoTakeoff", this, "autoTakeoff"));
        this.subComponents.add(Setting.createInt(0, 40, "TakeoffTicks", this, "takeoffTicks"));
        this.subComponents.add(Setting.createFloat(1.0f, 50.0f, "Boost", this, "boostMod"));
        this.subComponents.add(Setting.createFloat(1.0f, 1000.0f, "UpDiv", this, "upDiv"));
        this.subComponents.add(Setting.createInt(0, 1000, "RestartDelay", this, "restartDelay"));
        this.subComponents.add(Setting.createBoolean("TPS Sync", this, "tpsSync"));
        this.subComponents.add(Setting.createKey("Faster", (Module.KeyBind)this.customKeyBinds.get("faster")));
        this.subComponents.add(Setting.createKey("Slower", (Module.KeyBind)this.customKeyBinds.get("slower")));
        this.subComponents.add(Setting.createKey("Boost", (Module.KeyBind)this.customKeyBinds.get("boost")));
        this.subComponents.add(Setting.createKey("Restart", (Module.KeyBind)this.customKeyBinds.get("restart")));
    }

    public void restart() {
        InventoryUtils.clickSlot(6, ClickType.PICKUP, 0);
        new AsyncTask<Object>(() -> {
            Thread.sleep(this.restartDelay);
            InventoryUtils.clickSlot(6, ClickType.PICKUP, 0);
            this.takeoff.lock();
            this.restarting = true;
            return null;
        });
    }

    public void boost() {
        this.player.field_70159_w *= (double)this.boostMod;
        this.player.field_70181_x *= (double)this.boostMod;
        this.player.field_70179_y *= (double)this.boostMod;
    }

    public void faster() {
        this.speed = (float)((double)this.speed + 0.1);
        if (this.speed > 5.0f) {
            this.speed = 5.0f;
        }
        this.updateBinds();
    }

    public void slower() {
        this.speed = (float)((double)this.speed - 0.1);
        if (this.speed < 0.1f) {
            this.speed = 0.1f;
        }
        this.updateBinds();
    }

    @Override
    public void onEveryTick() {
        if (TTCp.mc.world == null) {
            this.init = false;
            return;
        }
        EntityPlayerSP player = TTCp.player;
        if (this.restarting) {
            if (player.func_184613_cA()) {
                player.field_70159_w = 0.0;
                player.field_70181_x = 0.0;
                player.field_70179_y = 0.0;
                this.init = true;
                this.takeoff.unlock();
                this.restarting = false;
                this.negateElytraFallMomentum((EntityPlayer)player);
            } else if (this.autoTakeoff && player.field_70181_x < 0.0 && this.lastTakeoffTry >= this.takeoffTicks) {
                player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)player, CPacketEntityAction.Action.START_FALL_FLYING));
                this.lastTakeoffTry = 0;
            }
        }
        ++this.lastTakeoffTry;
    }

    @Override
    public void onTick() {
        if (TTCp.mc.world == null) {
            this.init = false;
            return;
        }
        EntityPlayerSP player = TTCp.player;
        FlightBot.setSpeed(this.speed());
        boolean blockMovement = FlightBot.tickBot();
        if (this.init) {
            if (TTCp.player == TTCp.mc.getRenderViewEntity()) {
                if (!blockMovement) {
                    double z;
                    double y;
                    Vec2f movementVec = player.movementInput.getMoveVector();
                    float f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
                    float f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
                    double x = movementVec.x * f2 - movementVec.y * f1;
                    float d = (float)Math.sqrt(x * x + (y = (double)((player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0))) * y + (z = (double)(movementVec.y * f2 + movementVec.x * f1)) * z);
                    if (d < 1.0f) {
                        d = 1.0f;
                    }
                    player.field_70159_w = x / (double)d * (double)this.speed();
                    player.field_70181_x = y / (double)d * (double)this.speed() / (double)this.upDiv;
                    player.field_70179_y = z / (double)d * (double)this.speed();
                }
            } else if (!FlightBot.isFlying()) {
                player.field_70159_w = 0.0;
                player.field_70181_x = 0.0;
                player.field_70179_y = 0.0;
            }
            this.negateElytraFallMomentum((EntityPlayer)player);
        } else if (player.func_184613_cA()) {
            player.field_70159_w = 0.0;
            player.field_70181_x = 1.0;
            player.field_70179_y = 0.0;
            this.init = true;
            this.takeoff.unlock();
            this.negateElytraFallMomentum((EntityPlayer)player);
        } else if (this.autoTakeoff && player.field_70181_x < 0.0 && !player.field_70122_E) {
            this.takeoff.lock();
            if (this.lastTakeoffTry >= this.takeoffTicks) {
                player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)player, CPacketEntityAction.Action.START_FALL_FLYING));
                this.lastTakeoffTry = 0;
            }
        }
        if (!player.func_184613_cA()) {
            this.init = false;
        }
    }

    private float speed() {
        return this.tpsSync ? this.speed * Utils.tpsMultiplier() : this.speed;
    }

    public void negateElytraFallMomentum(EntityPlayer player) {
        if (!player.func_70090_H() && !player.func_180799_ab()) {
            Vec3d vec3d = player.func_70040_Z();
            float f = player.field_70125_A * ((float)Math.PI / 180);
            double d = vec3d.lengthVector();
            float f1 = MathHelper.cos((float)f);
            f1 = (float)((double)f1 * (double)f1 * Math.min(1.0, d / 0.4));
            player.field_70181_x -= -0.08 + (double)f1 * 0.06;
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public int danger() {
        return 2;
    }
}
