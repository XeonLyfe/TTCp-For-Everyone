package tudbut.mod.client.ttcp.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeModContainer;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.TTCWorld;
import tudbut.obj.Vector2i;

public class WorldGeneratorProvider
extends WorldProvider
implements IChunkProvider {
    int dim;
    IChunkGenerator generator;
    long seed;
    World w;
    Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<Integer, Map<Integer, Chunk>>();

    public WorldGeneratorProvider(WorldInfo info, long seed, int dim) {
        this.seed = seed;
        this.dim = dim;
        NBTTagCompound nbt = info.cloneNBTCompound(null);
        nbt.setLong("RandomSeed", seed);
        info = new WorldInfo(nbt);
        this.biomeProvider = new BiomeProvider(info);
        TTCWorld[] w = new TTCWorld[1];
        WorldInfo finalInfo = info;
        ForgeModContainer.fixVanillaCascading = false;
        w[0] = new TTCWorld(finalInfo, this){
            {
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
        };
        this.w = w[0];
        this.setWorld(w[0]);
        String gs = info.getGeneratorOptions();
        ChunkGeneratorSettings.Factory cgs = new ChunkGeneratorSettings.Factory();
        this.generator = new WorldType("").getChunkGenerator((World)w[0], gs);
    }

    public long getSeed() {
        return this.seed;
    }

    public World getWorld() {
        return this.w;
    }

    public DimensionType getDimensionType() {
        return DimensionType.getById((int)this.dim);
    }

    public BiomeProvider getBiomeProvider() {
        return this.biomeProvider;
    }

    public Biome getBiomeForCoords(BlockPos pos) {
        return this.w.getBiomeForCoordsBody(pos);
    }

    @Nullable
    public Chunk getLoadedChunk(int x, int z) {
        return this.chunks.containsKey(x) ? this.chunks.get(x).get(z) : null;
    }

    public Chunk provideChunk(int x, int z) {
        return this.getLoadedChunk(x, z) != null ? this.getLoadedChunk(x, z) : this.gen(x, z);
    }

    public Chunk gen(int x, int z) {
        ChatUtils.chatPrinterDebug().println("Generating SeedOverlay chunk at " + x + " " + z);
        Chunk chunk = this.generator.generateChunk(x, z);
        if (!this.chunks.containsKey(x)) {
            this.chunks.put(x, new HashMap());
        }
        this.chunks.get(x).put(z, chunk);
        chunk.onLoad();
        chunk.populate((IChunkProvider)this, this.generator);
        chunk.onTick(true);
        return chunk;
    }

    public boolean tick() {
        Integer[] keys0 = this.chunks.keySet().toArray(new Integer[0]);
        for (int i = 0; i < keys0.length; ++i) {
            Integer[] keys1 = this.chunks.get(keys0[i]).keySet().toArray(new Integer[0]);
            for (int j = 0; j < keys1.length; ++j) {
                Vector2i coord = new Vector2i(keys0[i], keys1[j]);
                Vector2i block = new Vector2i(coord.getX() * 16, coord.getY() * 16);
                this.chunks.get(coord.getX()).get(coord.getY()).onTick(true);
                if (!(TTCp.player.func_70011_f((double)block.getX(), TTCp.player.field_70163_u, (double)block.getY()) > 128.0)) continue;
                this.chunks.get(coord.getX()).get(coord.getY()).onUnload();
                this.chunks.get(coord.getX()).remove(coord.getY());
                if (!this.chunks.get(coord.getX()).isEmpty()) continue;
                this.chunks.remove(coord.getX());
            }
        }
        return false;
    }

    public String makeString() {
        return "";
    }

    public boolean isChunkGeneratedAt(int x, int z) {
        return this.getLoadedChunk(x, z) != null;
    }
}
