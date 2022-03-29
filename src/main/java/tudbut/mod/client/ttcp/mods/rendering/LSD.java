package tudbut.mod.client.ttcp.mods.rendering;

import java.lang.reflect.Field;
import java.util.Objects;
import net.minecraft.entity.Entity;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.ModuleEventRegistry;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.mods.rendering.Freecam;
import tudbut.mod.client.ttcp.utils.LSDRenderer;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Render;

@Render
public class LSD
extends Module {
    int mode = 0;

    public LSD() {
        try {
            this.subButtons.add(new GuiTTC.Button("Mode: " + this.getMode(this.mode), text -> {
                ++this.mode;
                if (this.mode > 10) {
                    this.mode = 0;
                }
                try {
                    text.set("Mode: " + this.getMode(this.mode));
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }));
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static LSD getInstance() {
        return TTCp.getModule(LSD.class);
    }

    private String getMode(int mode) throws IllegalAccessException {
        Class<LSDRenderer> clazz = LSDRenderer.class;
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            if (fields[i].getInt(null) != mode || fields[i].getName().equals("mode")) continue;
            return fields[i].getName();
        }
        return null;
    }

    @Override
    public boolean displayOnClickGUI() {
        return true;
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public boolean doStoreEnabled() {
        return false;
    }

    @Override
    public void onTick() {
        LSDRenderer.mode = this.mode;
    }

    @Override
    public void onEnable() {
        if (TTCp.isIngame() && !Freecam.getInstance().enabled) {
            LSDRenderer player = new LSDRenderer(TTCp.player, TTCp.world);
            TTCp.world.spawnEntity((Entity)player);
            TTCp.mc.renderChunksMany = true;
            TTCp.mc.setRenderViewEntity((Entity)player);
        } else {
            this.enabled = false;
        }
    }

    @Override
    public int danger() {
        return 1;
    }

    @Override
    public void onDisable() {
        if (TTCp.isIngame()) {
            TTCp.world.removeEntity(Objects.requireNonNull(TTCp.mc.getRenderViewEntity()));
            TTCp.mc.setRenderViewEntity((Entity)TTCp.mc.player);
            TTCp.mc.renderChunksMany = true;
        }
    }

    @Override
    public void init() {
        ModuleEventRegistry.disableOnNewPlayer.add(this);
    }
}
