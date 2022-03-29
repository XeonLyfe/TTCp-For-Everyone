package tudbut.mod.client.ttcp.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.WorldInfo;
import tudbut.mod.client.ttcp.utils.TTCWorld;

final class TTCWorld$1
extends WorldProvider {
    final WorldInfo val$info;
    final BiomeProvider val$provider;
    final TTCWorld[] val$w;

    TTCWorld$1(WorldInfo worldInfo, BiomeProvider biomeProvider, TTCWorld[] tTCWorldArray) {
        this.val$info = worldInfo;
        this.val$provider = biomeProvider;
        this.val$w = tTCWorldArray;
    }

    public long getSeed() {
        return this.val$info.getSeed();
    }

    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

    public BiomeProvider getBiomeProvider() {
        return this.val$provider;
    }

    public Biome getBiomeForCoords(BlockPos pos) {
        return this.val$w[0].getBiomeForCoordsBody(pos);
    }
}
