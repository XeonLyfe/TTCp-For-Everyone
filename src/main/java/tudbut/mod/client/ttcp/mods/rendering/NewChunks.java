package tudbut.mod.client.ttcp.mods.rendering;

import de.tudbut.type.Vector3d;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Render;

@Render
public class NewChunks
extends Module {
    ArrayList<ChunkPos> chunks = new ArrayList();
    Vec3d pos = new Vec3d(0.0, 0.0, 0.0);

    @SubscribeEvent
    public void onChunkData(ChunkEvent.Load event) {
        if (TTCp.isIngame() && Utils.isCallingFrom(Chunk.class)) {
            this.chunks.add(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onRenderWorld(Event event) {
        if (event instanceof RenderWorldLastEvent && this.enabled && TTCp.isIngame()) {
            Entity e = TTCp.mc.getRenderViewEntity();
            assert (e != null);
            this.pos = e.getPositionEyes(((RenderWorldLastEvent)event).getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
            for (int i = 0; i < this.chunks.size(); ++i) {
                this.drawAroundChunk(new Vector3d(this.chunks.get((int)i).x * 16 + 8, 64.0, this.chunks.get((int)i).z * 16 + 8), -2130771968);
            }
        }
    }

    public void drawAroundChunk(Vector3d pos, int color) {
        try {
            Tesselator.ready();
            Tesselator.translate(-this.pos.x, -this.pos.y, -this.pos.z);
            Tesselator.color(color);
            Tesselator.depth(false);
            Tesselator.begin(1);
            Tesselator.put(pos.getX() - 8.0, pos.getY() - 0.01, pos.getZ() + 8.0);
            Tesselator.put(pos.getX() + 8.0, pos.getY() - 0.01, pos.getZ() + 8.0);
            Tesselator.put(pos.getX() + 8.0, pos.getY() - 0.01, pos.getZ() + 8.0);
            Tesselator.put(pos.getX() + 8.0, pos.getY() - 0.01, pos.getZ() - 8.0);
            Tesselator.put(pos.getX() + 8.0, pos.getY() - 0.01, pos.getZ() - 8.0);
            Tesselator.put(pos.getX() - 8.0, pos.getY() - 0.01, pos.getZ() - 8.0);
            Tesselator.put(pos.getX() - 8.0, pos.getY() - 0.01, pos.getZ() - 8.0);
            Tesselator.put(pos.getX() - 8.0, pos.getY() - 0.01, pos.getZ() + 8.0);
            Tesselator.end();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick() {
    }
}
