package tudbut.mod.client.ttcp.utils;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.rendering.Freecam;

@SideOnly(value=Side.CLIENT)
public class FreecamPlayer
extends EntityOtherPlayerMP {
    public MovementInput movementInput;
    protected Minecraft mc;
    protected final EntityPlayerSP original;

    public FreecamPlayer(EntityPlayerSP playerSP, World world) {
        super(world, playerSP.func_146103_bH());
        this.field_71093_bK = playerSP.field_71093_bK;
        this.original = playerSP;
        this.mc = Minecraft.getMinecraft();
        this.movementInput = playerSP.movementInput;
        this.func_70065_x();
        this.field_71075_bZ.allowFlying = true;
        this.field_71075_bZ.isFlying = true;
        this.func_70080_a(playerSP.field_70165_t, playerSP.field_70163_u, playerSP.field_70161_v, playerSP.field_70177_z, playerSP.field_70125_A);
    }

    @Nonnull
    public String func_70005_c_() {
        return this.original.func_70005_c_() + "\u0000";
    }

    public boolean func_175149_v() {
        return true;
    }

    public void onLivingUpdate() {
        if (TTCp.mc.world == null) {
            Freecam.getInstance().onDisable();
            Freecam.getInstance().enabled = false;
            return;
        }
        TTCp.mc.renderChunksMany = false;
        TTCp.mc.player.func_82142_c(false);
        this.func_82142_c(true);
        this.field_71071_by.copyInventory(TTCp.player.field_71071_by);
        this.field_70126_B = this.field_70177_z;
        this.field_70127_C = this.field_70125_A;
        this.field_70758_at = this.field_70759_as;
        this.func_70101_b(this.original.field_70177_z, this.original.field_70125_A);
        this.func_70034_d(this.original.field_70177_z);
        this.original.prevRenderArmYaw = this.original.renderArmYaw;
        this.original.prevRenderArmPitch = this.original.renderArmPitch;
        this.original.renderArmPitch = (float)((double)this.original.renderArmPitch + (double)(this.original.field_70125_A - this.original.renderArmPitch) * 0.5);
        this.original.renderArmYaw = (float)((double)this.original.renderArmYaw + (double)(this.original.field_70177_z - this.original.renderArmYaw) * 0.5);
        this.func_70626_be();
        this.movementInput.updatePlayerMoveState();
        Vec2f movementVec = this.movementInput.getMoveVector();
        float f1 = MathHelper.sin((float)(this.field_70177_z * ((float)Math.PI / 180)));
        float f2 = MathHelper.cos((float)(this.field_70177_z * ((float)Math.PI / 180)));
        double x = movementVec.x * f2 - movementVec.y * f1;
        double y = (this.movementInput.jump ? 1 : 0) + (this.movementInput.sneak ? -1 : 0);
        double z = movementVec.y * f2 + movementVec.x * f1;
        float d = (float)Math.sqrt(x * x + y * y + z * z);
        this.movementInput.jump = false;
        this.movementInput.sneak = false;
        this.movementInput.forwardKeyDown = false;
        this.movementInput.backKeyDown = false;
        this.movementInput.leftKeyDown = false;
        this.movementInput.rightKeyDown = false;
        this.movementInput.moveForward = 0.0f;
        this.movementInput.moveStrafe = 0.0f;
        if (d < 1.0f) {
            d = 1.0f;
        }
        this.field_70159_w = x / (double)d;
        this.field_70181_x = y / (double)d;
        this.field_70179_y = z / (double)d;
        this.field_70145_X = true;
        this.func_70091_d(MoverType.SELF, this.field_70159_w, this.field_70181_x, this.field_70179_y);
        this.field_71107_bF = this.field_71109_bG;
        this.field_70727_aS = this.field_70726_aT;
    }
}
