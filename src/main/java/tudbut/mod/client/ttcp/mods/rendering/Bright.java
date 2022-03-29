package tudbut.mod.client.ttcp.mods.rendering;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Render;

@Render
public class Bright
extends Module {
    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onEveryTick() {
        if (this.enabled) {
            PotionEffect p = new PotionEffect(MobEffects.NIGHT_VISION, 1000, 127, true, false);
            TTCp.player.func_70690_d(p);
            p.setPotionDurationMax(true);
        } else {
            TTCp.player.removeActivePotionEffect(MobEffects.NIGHT_VISION);
        }
    }
}
