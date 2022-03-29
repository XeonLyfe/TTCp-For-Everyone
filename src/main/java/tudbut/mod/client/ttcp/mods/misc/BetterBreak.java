package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;

@Misc
public class BetterBreak
extends Module {
    @SubscribeEvent
    public void onBreakSpeedGet(PlayerEvent.BreakSpeed event) {
        if (!this.enabled) {
            return;
        }
        float f = event.getOriginalSpeed();
        if (event.getEntityPlayer().func_70055_a(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier((EntityLivingBase)event.getEntityPlayer())) {
            f *= 5.0f;
        }
        if (!event.getEntityPlayer().field_70122_E) {
            f *= 5.0f;
        }
        event.setNewSpeed(f);
    }

    @Override
    public void onChat(String s, String[] args) {
    }
}
