package tudbut.mod.client.ttcp.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.rendering.LSD;
import tudbut.mod.client.ttcp.utils.FreecamPlayer;

public class LSDRenderer
extends FreecamPlayer {
    public static final int MODE_EPILEPSY = 0;
    public static final int MODE_UPSIDE_DOWN = 1;
    public static final int MODE_HAND0 = 2;
    public static final int MODE_HAND1 = 3;
    public static final int MODE_CAMERA = 4;
    public static final int MODE_ROTATION0 = 5;
    public static final int MODE_ROTATION1 = 6;
    public static final int MODE_ROTATION2 = 7;
    public static final int MODE_ROTATION3 = 8;
    public static final int MODE_EXC = 9;
    public static final int MODE_ALL = 10;
    public static int mode = 0;

    public LSDRenderer(EntityPlayerSP playerSP, World world) {
        super(playerSP, world);
    }

    @Override
    public void onLivingUpdate() {
        if (!TTCp.isIngame()) {
            LSD.getInstance().onDisable();
            return;
        }
        this.field_71071_by.copyInventory(TTCp.player.field_71071_by);
        this.original.renderArmYaw = this.original.field_70177_z;
        this.original.renderArmPitch = this.original.field_70125_A;
        this.original.prevRenderArmYaw = this.field_70126_B;
        this.original.prevRenderArmPitch = this.field_70127_C;
        this.func_70101_b(this.original.field_70177_z, this.original.field_70125_A);
        this.field_71109_bG = 0.0f;
        this.field_70726_aT = 0.0f;
        this.field_71107_bF = 0.0f;
        this.field_70727_aS = 0.0f;
        switch (mode) {
            case 10: {
                this.exc();
                this.hand1();
                this.rotation3();
                this.epilepsy();
                break;
            }
            case 0: {
                this.epilepsy();
                break;
            }
            case 2: {
                this.hand0();
                break;
            }
            case 3: {
                this.hand1();
                break;
            }
            case 4: {
                this.camera();
                break;
            }
            case 5: {
                this.rotation0();
                break;
            }
            case 6: {
                this.rotation1();
                break;
            }
            case 1: 
            case 7: {
                this.rotation2();
                break;
            }
            case 8: {
                this.rotation3();
                break;
            }
            case 9: {
                this.exc();
            }
        }
        this.func_70034_d(-this.original.field_70177_z);
        this.func_70626_be();
        this.field_70145_X = true;
    }

    public void epilepsy() {
        this.field_70125_A = 0.0f;
        this.field_71109_bG = (float)(5.0 - Math.random() * 10.0);
        this.field_70726_aT = (float)(5.0 - Math.random() * 10.0);
        this.field_71107_bF = (float)(5.0 - Math.random() * 10.0);
        this.field_70727_aS = (float)(5.0 - Math.random() * 10.0);
    }

    public void hand0() {
        this.original.renderArmYaw = 0.0f;
        this.original.renderArmPitch = 0.0f;
        this.original.prevRenderArmYaw = 0.0f;
        this.original.prevRenderArmPitch = 0.0f;
    }

    public void hand1() {
        this.original.renderArmYaw = this.original.field_70177_z;
        this.original.renderArmPitch = this.original.field_70125_A;
        this.original.prevRenderArmYaw = 0.0f;
        this.original.prevRenderArmPitch = 0.0f;
    }

    public void camera() {
        this.field_71109_bG = (float)((double)(this.field_70177_z / 180.0f) * Math.PI);
        this.field_70726_aT = (float)((double)(this.field_70125_A / 180.0f) * Math.PI);
        this.field_71107_bF = this.field_71109_bG;
        this.field_70727_aS = this.field_70726_aT;
    }

    public void exc() {
        this.field_70177_z -= 90.0f;
        this.field_70125_A -= 90.0f;
    }

    public void rotation0() {
        this.field_70177_z -= 90.0f;
        this.field_70125_A -= 90.0f;
    }

    public void rotation1() {
        this.field_70177_z = this.original.field_70177_z + 90.0f;
        this.field_70125_A = this.original.field_70125_A + 90.0f;
    }

    public void rotation2() {
        this.field_70177_z = this.original.field_70177_z + 180.0f;
        this.field_70125_A = this.original.field_70125_A + 180.0f;
    }

    public void rotation3() {
        this.field_70177_z = this.original.field_70177_z - 180.0f;
        this.field_70125_A = this.original.field_70125_A - 180.0f;
    }
}
