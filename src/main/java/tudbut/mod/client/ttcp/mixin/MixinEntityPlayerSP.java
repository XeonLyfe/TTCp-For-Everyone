package tudbut.mod.client.ttcp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tudbut.mod.client.ttcp.utils.Utils;

@Mixin(value={EntityPlayerSP.class}, priority=934759)
public class MixinEntityPlayerSP
extends EntityPlayerSP {
    @Shadow
    private boolean serverSprintState;
    @Shadow
    private boolean serverSneakState;
    @Shadow
    private boolean prevOnGround;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    private double lastReportedPosX;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    private int positionUpdateTicks;

    public MixinEntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
        super(p_i47378_1_, p_i47378_2_, p_i47378_3_, p_i47378_4_, p_i47378_5_);
    }

    @Inject(method={"onUpdateWalkingPlayer"}, cancellable=true, at={@At(value="HEAD")})
    public void onUpdateWalkingPlayer(CallbackInfo ci) {
        Vec2f rotation = Utils.getRotation();
        if (rotation != null) {
            boolean flag1;
            ci.cancel();
            Utils.markRotationSent();
            boolean flag = this.func_70051_ag();
            if (flag != this.serverSprintState) {
                if (flag) {
                    this.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this, CPacketEntityAction.Action.START_SPRINTING));
                } else {
                    this.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this, CPacketEntityAction.Action.STOP_SPRINTING));
                }
                this.serverSprintState = flag;
            }
            if ((flag1 = this.isSneaking()) != this.serverSneakState) {
                if (flag1) {
                    this.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this, CPacketEntityAction.Action.START_SNEAKING));
                } else {
                    this.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this, CPacketEntityAction.Action.STOP_SNEAKING));
                }
                this.serverSneakState = flag1;
            }
            if (this.isCurrentViewEntity()) {
                AxisAlignedBB axisalignedbb = this.func_174813_aQ();
                double d0 = this.field_70165_t - this.lastReportedPosX;
                double d1 = axisalignedbb.minY - this.lastReportedPosY;
                double d2 = this.field_70161_v - this.lastReportedPosZ;
                double d3 = this.field_70177_z - this.lastReportedYaw;
                double d4 = this.field_70125_A - this.lastReportedPitch;
                ++this.positionUpdateTicks;
                boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4 || this.positionUpdateTicks >= 20;
                boolean flag3 = true;
                if (this.func_184218_aH()) {
                    this.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(this.field_70159_w, -999.0, this.field_70179_y, rotation.x, rotation.y, this.field_70122_E));
                    flag2 = false;
                } else if (flag2 && flag3) {
                    this.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(this.field_70165_t, axisalignedbb.minY, this.field_70161_v, rotation.x, rotation.y, this.field_70122_E));
                } else if (flag2) {
                    this.connection.sendPacket((Packet)new CPacketPlayer.Position(this.field_70165_t, axisalignedbb.minY, this.field_70161_v, this.field_70122_E));
                } else if (flag3) {
                    this.connection.sendPacket((Packet)new CPacketPlayer.Rotation(rotation.x, rotation.y, this.field_70122_E));
                } else if (this.prevOnGround != this.field_70122_E) {
                    this.connection.sendPacket((Packet)new CPacketPlayer(this.field_70122_E));
                }
                if (flag2) {
                    this.lastReportedPosX = this.field_70165_t;
                    this.lastReportedPosY = axisalignedbb.minY;
                    this.lastReportedPosZ = this.field_70161_v;
                    this.positionUpdateTicks = 0;
                }
                this.prevOnGround = this.field_70122_E;
                this.autoJumpEnabled = this.mc.gameSettings.autoJump;
            }
        }
    }
}
