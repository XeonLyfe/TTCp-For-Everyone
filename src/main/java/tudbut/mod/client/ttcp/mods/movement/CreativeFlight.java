package tudbut.mod.client.ttcp.mods.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.PlayerCapabilities;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Movement;

@Movement
public class CreativeFlight
extends Module {
    boolean init;

    @Override
    public void onSubTick() {
        if (TTCp.mc.world == null) {
            this.init = false;
            return;
        }
        EntityPlayerSP player = TTCp.player;
        PlayerCapabilities capabilities = player.field_71075_bZ;
        if (this.init) {
            capabilities.isFlying = true;
        } else if (player.func_184613_cA()) {
            player.field_70159_w = 0.0;
            player.field_70181_x = 0.5;
            player.field_70179_y = 0.0;
            this.init = true;
        }
        if (Keyboard.isKeyDown((int)44) && TTCp.mc.currentScreen == null || player.field_70122_E) {
            this.onDisable();
        }
    }

    @Override
    public void onDisable() {
        EntityPlayerSP player = TTCp.player;
        PlayerCapabilities capabilities = player.field_71075_bZ;
        capabilities.isFlying = false;
        this.init = false;
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public int danger() {
        return 2;
    }
}
