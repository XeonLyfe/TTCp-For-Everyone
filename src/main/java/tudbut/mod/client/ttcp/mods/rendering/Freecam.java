package tudbut.mod.client.ttcp.mods.rendering;

import java.util.Objects;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.ModuleEventRegistry;
import tudbut.mod.client.ttcp.mods.rendering.LSD;
import tudbut.mod.client.ttcp.utils.FreecamPlayer;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.category.Render;

@Render
public class Freecam
extends Module {
    GameType type;

    public static Freecam getInstance() {
        return TTCp.getModule(Freecam.class);
    }

    @Override
    public boolean displayOnClickGUI() {
        return true;
    }

    @Override
    public boolean doStoreEnabled() {
        return false;
    }

    @Override
    public void onEnable() {
        if (TTCp.isIngame() && !LSD.getInstance().enabled && TTCp.mc.getRenderViewEntity() == TTCp.player) {
            FreecamPlayer player = new FreecamPlayer(TTCp.player, TTCp.world);
            TTCp.world.spawnEntity((Entity)player);
            this.type = TTCp.mc.playerController.getCurrentGameType();
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
        }
        TTCp.mc.gameSettings.thirdPersonView = 0;
        TTCp.mc.renderChunksMany = true;
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (TTCp.isIngame() && this.enabled) {
            EntityPlayerSP main = TTCp.player;
            Entity e = TTCp.mc.getRenderViewEntity();
            assert (e != null);
            Vec3d p = e.getPositionEyes(event.getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
            Vec3d pos = main.getPositionVector();
            float entityHalfed = main.width / 2.0f + 0.01f;
            float entityHeight = main.height + 0.01f;
            Tesselator.ready();
            Tesselator.translate(-p.x, -p.y, -p.z);
            Tesselator.color(-2130771968);
            Tesselator.depth(false);
            Tesselator.begin(7);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x - (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z - (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y + (double)entityHeight, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z + (double)entityHalfed);
            Tesselator.put(pos.x + (double)entityHalfed, pos.y - 0.01, pos.z - (double)entityHalfed);
            Tesselator.end();
        }
    }

    @Override
    public void init() {
        ModuleEventRegistry.disableOnNewPlayer.add(this);
    }
}
