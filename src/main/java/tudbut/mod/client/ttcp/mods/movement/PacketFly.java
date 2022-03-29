package tudbut.mod.client.ttcp.mods.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Save;

@Movement
public class PacketFly
extends Module {
    @Save
    Mode mode = Mode.CREATIVE;
    @Save
    int speed = 2;
    @Save
    boolean glide = true;
    @Save
    boolean useUWP = false;
    @Save
    int forceOffset = -20;
    @Save
    boolean forceP0 = false;
    @Save
    boolean forceP1 = true;
    @Save
    boolean forceGround = true;
    @Save
    boolean confirmPacket = true;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createEnum(Mode.class, "Mode", this, "mode"));
        this.subComponents.add(Setting.createBoolean("UWP", this, "useUWP"));
        this.subComponents.add(Setting.createInt(1, 40, "Speed (b/s)", this, "speed"));
        this.subComponents.add(Setting.createInt(-1000, 1000, "Force offset", this, "forceOffset"));
        this.subComponents.add(Setting.createBoolean("MainPacket type", this, "forceP0"));
        this.subComponents.add(Setting.createBoolean("ForcePacket type", this, "forceP1"));
        this.subComponents.add(Setting.createBoolean("Force onGround", this, "forceGround"));
        this.subComponents.add(Setting.createBoolean("Constant glide", this, "glide"));
        this.subComponents.add(Setting.createBoolean("ConfirmPacket", this, "confirmPacket"));
    }

    @Override
    public void onSubTick() {
        double posZ;
        double posY;
        double posX;
        float d;
        double z;
        double y;
        double x;
        float f2;
        float f1;
        Vec2f movementVec;
        EntityPlayerSP player = TTCp.player;
        PlayerCapabilities capabilities = player.field_71075_bZ;
        capabilities.isFlying = this.mode == Mode.CREATIVE;
        float speed = (float)this.speed / 20.0f;
        player.field_70181_x = this.glide ? (player.field_70181_x -= 0.05 * (double)speed) : 0.0;
        if (this.forceGround) {
            player.field_70122_E = true;
        }
        if (this.mode == Mode.CONTROL) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            player.field_70159_w = x / (double)d * (double)speed;
            player.field_70181_x = y / (double)d * (double)speed;
            player.field_70179_y = z / (double)d * (double)speed;
        }
        if (this.mode == Mode.CONTROL_PACKET) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            posX = player.field_70165_t + x / (double)d * (double)speed;
            posY = player.field_70163_u + y / (double)d * (double)speed;
            posZ = player.field_70161_v + z / (double)d * (double)speed;
            player.field_70165_t = posX;
            player.field_70163_u = posY;
            player.field_70161_v = posZ;
            player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70177_z, player.field_70125_A, this.forceP0));
        }
        if (this.mode == Mode.BOOST) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                player.field_70181_x -= 1.0;
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            player.field_70159_w = x / (double)d * (double)speed;
            player.field_70181_x = y * 2.0 * (double)speed;
            player.field_70179_y = z / (double)d * (double)speed;
        }
        if (this.mode == Mode.FORCE) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            posX = player.field_70165_t + x / (double)d * (double)speed;
            posY = player.field_70163_u + y / (double)d * (double)speed;
            posZ = player.field_70161_v + z / (double)d * (double)speed;
            player.field_70165_t = posX;
            player.field_70163_u = posY;
            player.field_70161_v = posZ;
            player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70177_z, player.field_70125_A, this.forceP0));
            player.connection.sendPacket((Packet)new CPacketPlayer.Position(posX, posY + (double)this.forceOffset, posZ, this.forceP1));
        }
        if (this.mode == Mode.ELYTRAFORCE) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            posX = player.field_70165_t + x / (double)d * (double)speed;
            posY = player.field_70163_u + y / (double)d * (double)speed;
            posZ = player.field_70161_v + z / (double)d * (double)speed;
            player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)player, CPacketEntityAction.Action.START_FALL_FLYING));
            player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(posX, posY, posZ, player.field_70177_z, player.field_70125_A, this.forceP0));
            player.connection.sendPacket((Packet)new CPacketPlayer.Position(posX, posY + (double)this.forceOffset, posZ, this.forceP1));
        }
        if (this.mode == Mode.ELYTRA) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            posX = player.field_70165_t + x / (double)d * (double)speed;
            posY = player.field_70163_u + y / (double)d * (double)speed;
            posZ = player.field_70161_v + z / (double)d * (double)speed;
            player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)player, CPacketEntityAction.Action.START_FALL_FLYING));
            player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(posX, posY, posZ, player.field_70177_z, player.field_70125_A, this.forceP0));
        }
        if (this.mode == Mode.DAMAGE) {
            movementVec = player.movementInput.getMoveVector();
            f1 = MathHelper.sin((float)(player.field_70177_z * ((float)Math.PI / 180)));
            f2 = MathHelper.cos((float)(player.field_70177_z * ((float)Math.PI / 180)));
            x = movementVec.x * f2 - movementVec.y * f1;
            y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            z = movementVec.y * f2 + movementVec.x * f1;
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                return;
            }
            d = (float)Math.sqrt(x * x + y * y + z * z);
            if (d < 1.0f) {
                d = 1.0f;
            }
            posX = player.field_70165_t + x / (double)d * (double)speed;
            posY = player.field_70163_u + y / (double)d * (double)speed;
            posZ = player.field_70161_v + z / (double)d * (double)speed;
            player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(posX, posY, posZ, player.field_70177_z, player.field_70125_A, this.forceP0));
            player.connection.sendPacket((Packet)new CPacketPlayer.Position(posX, posY + (double)this.forceOffset, posZ, this.forceP1));
        }
        if (this.glide) {
            player.field_70181_x -= 0.05 * (double)speed;
        }
        if (this.useUWP) {
            player.connection.sendPacket((Packet)new CPacketInput((float)player.field_70159_w / (float)this.speed, (float)player.field_70179_y / (float)this.speed, player.field_70181_x > 0.0, player.field_70181_x < 0.0 || player.isSneaking()));
        }
    }

    @Override
    public void onDisable() {
        TTCp.player.field_71075_bZ.isFlying = false;
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        return packet instanceof CPacketInput && !this.useUWP || packet instanceof CPacketPlayerAbilities || packet instanceof CPacketConfirmTeleport && !this.confirmPacket;
    }

    static enum Mode {
        CREATIVE,
        CONTROL,
        CONTROL_PACKET,
        FORCE,
        BOOST,
        ELYTRA,
        DAMAGE,
        ELYTRAFORCE;

    }
}
