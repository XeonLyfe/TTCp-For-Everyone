package tudbut.mod.client.ttcp.mixin;

import java.util.ConcurrentModificationException;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={SoundHandler.class})
public class MixinSoundHandler {
    @Shadow
    SoundManager field_147694_f;

    @Overwrite
    public void func_73660_a() {
        try {
            this.field_147694_f.updateAllSounds();
        }
        catch (ConcurrentModificationException ignore) {
            this.func_73660_a();
        }
    }
}
