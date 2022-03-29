package tudbut.mod.client.ttcp;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

@IFMLLoadingPlugin.Name(value="CoreModTTCp")
@IFMLLoadingPlugin.MCVersion(value="1.12.2")
public class CoreModTTCp
implements IFMLLoadingPlugin {
    public CoreModTTCp() {
        MixinBootstrap.init();
        Mixins.addConfigurations("mixins.ttcp.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
    }

    public String getAccessTransformerClass() {
        return null;
    }
}
