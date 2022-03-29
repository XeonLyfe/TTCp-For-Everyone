package tudbut.mod.client.ttcp.utils;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import tudbut.mod.client.ttcp.utils.TTCWorld;

class WorldGeneratorProvider$1
extends TTCWorld {
    WorldGeneratorProvider$1(WorldInfo info, WorldProvider wp) {
        super(info, wp);
        this.chunkProvider = this.createChunkProvider();
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return WorldGeneratorProvider.this;
    }

    public void tick() {
        super.tick();
        this.chunkProvider.tick();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return WorldGeneratorProvider.this.getLoadedChunk(x, z) != null;
    }
}
