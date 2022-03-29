package tudbut.mod.client.ttcp.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderer.class})
public class MixinEntityRenderer {
    @Inject(method={"hurtCameraEffect"}, at={@At(value="HEAD")}, cancellable=true)
    public void hurtCameraEffect(float partialTicks, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }

    @Inject(method={"displayItemActivation"}, at={@At(value="HEAD")}, cancellable=true)
    public void displayTotem(ItemStack stack, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
