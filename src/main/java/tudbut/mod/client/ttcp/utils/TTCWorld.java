package tudbut.mod.client.ttcp.utils;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class TTCWorld
extends World {
    public static TTCWorld create(final WorldInfo info, final BiomeProvider provider) {
        TTCWorld[] w;
        w = new TTCWorld[]{new TTCWorld(info, new WorldProvider(){

            public long getSeed() {
                return info.getSeed();
            }

            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }

            public BiomeProvider getBiomeProvider() {
                return provider;
            }

            public Biome getBiomeForCoords(BlockPos pos) {
                return w[0].getBiomeForCoordsBody(pos);
            }
        })};
        return w[0];
    }

    TTCWorld(WorldInfo info, WorldProvider wp) {
        super((ISaveHandler)new SaveHandlerMP(), info, wp, new Profiler(), false);
    }

    public long getSeed() {
        return this.worldInfo.getSeed();
    }

    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return this.getBiomeProvider().getBiome(pos);
    }

    protected IChunkProvider createChunkProvider() {
        return null;
    }

    public BiomeProvider getBiomeProvider() {
        return this.provider.getBiomeProvider();
    }

    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }
}
