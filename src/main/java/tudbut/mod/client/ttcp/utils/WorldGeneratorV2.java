package tudbut.mod.client.ttcp.utils;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;
import java.io.File;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.CryptManager;
import net.minecraft.util.Util;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerDemo;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.tools.Tools2;

@SideOnly(value=Side.CLIENT)
public class WorldGeneratorV2
extends MinecraftServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final WorldSettings worldSettings;
    public boolean done = false;
    private boolean isGamePaused;

    public static WorldGeneratorV2 create(WorldSettings settings) {
        YggdrasilAuthenticationService as = new YggdrasilAuthenticationService(TTCp.mc.getProxy(), UUID.randomUUID().toString());
        YggdrasilGameProfileRepository gpr = new YggdrasilGameProfileRepository(as);
        return new WorldGeneratorV2(TTCp.mc, settings, as, as.createMinecraftSessionService(), (GameProfileRepository)gpr, new PlayerProfileCache((GameProfileRepository)gpr, new File("ttc/usercache.json")));
    }

    public WorldGeneratorV2(Minecraft clientIn, WorldSettings worldSettingsIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
        super(new File("ttc/saves"), clientIn.getProxy(), clientIn.getDataFixer(), authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
        Tools2.deleteDir(new File("ttc/saves/main"));
        new File("ttc/saves/main").mkdirs();
        this.setServerOwner("TTCp");
        this.setFolderName("main");
        this.setWorldName("main");
        this.setDemo(clientIn.isDemo());
        this.canCreateBonusChest(worldSettingsIn.isBonusChestEnabled());
        this.setBuildLimit(256);
        this.setPlayerList(new PlayerList(this){});
        this.worldSettings = this.isDemo() ? WorldServerDemo.DEMO_WORLD_SETTINGS : worldSettingsIn;
    }

    public ServerCommandManager createCommandManager() {
        return new ServerCommandManager((MinecraftServer)this);
    }

    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        if (worldinfo == null) {
            worldinfo = new WorldInfo(this.worldSettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
        }
        WorldServer overWorld = this.isDemo() ? (WorldServer)new WorldServerDemo((MinecraftServer)this, isavehandler, worldinfo, 0, this.profiler).func_175643_b() : (WorldServer)new WorldServer((MinecraftServer)this, isavehandler, worldinfo, 0, this.profiler).init();
        overWorld.initialize(this.worldSettings);
        Integer[] integerArray = DimensionManager.getStaticDimensionIDs();
        int n = integerArray.length;
        for (int i = 0; i < n; ++i) {
            int dim = integerArray[i];
            WorldServer world = dim == 0 ? overWorld : (WorldServer)new WorldServerMulti((MinecraftServer)this, isavehandler, dim, overWorld, this.profiler).init();
            world.func_72954_a((IWorldEventListener)new ServerWorldEventHandler((MinecraftServer)this, world));
            if (!this.isSinglePlayer()) {
                world.func_72912_H().setGameType(this.getGameType());
            }
            MinecraftForge.EVENT_BUS.post((Event)new WorldEvent.Load((World)world));
        }
        this.getPlayerList().setPlayerManager(new WorldServer[]{overWorld});
        this.initialWorldChunkLoad();
        this.done = true;
    }

    public boolean init() {
        LOGGER.info("Starting integrated minecraft server version 1.12.2");
        this.setOnlineMode(true);
        this.setCanSpawnAnimals(true);
        this.setCanSpawnNPCs(true);
        this.setAllowPvp(true);
        this.setAllowFlight(true);
        LOGGER.info("Generating keypair");
        this.setKeyPair(CryptManager.generateKeyPair());
        if (!FMLCommonHandler.instance().handleServerAboutToStart((MinecraftServer)this)) {
            return false;
        }
        this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.worldSettings.getSeed(), this.worldSettings.getTerrainType(), this.worldSettings.getGeneratorOptions());
        this.setMOTD(this.getServerOwner() + " - " + this.worlds[0].func_72912_H().getWorldName());
        return FMLCommonHandler.instance().handleServerStarting((MinecraftServer)this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        boolean flag = this.isGamePaused;
        boolean bl = this.isGamePaused = Minecraft.getMinecraft().getConnection() != null && Minecraft.getMinecraft().isGamePaused();
        if (!flag && this.isGamePaused) {
            LOGGER.info("Saving and pausing game...");
            this.getPlayerList().saveAllPlayerData();
            this.saveAllWorlds(false);
        }
        if (this.isGamePaused) {
            Queue queue = this.futureTaskQueue;
            synchronized (queue) {
                while (!this.futureTaskQueue.isEmpty()) {
                    Util.runTask((FutureTask)((FutureTask)this.futureTaskQueue.poll()), (Logger)LOGGER);
                }
            }
        } else {
            super.tick();
            if (TTCp.mc.gameSettings.renderDistanceChunks != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", (Object)TTCp.mc.gameSettings.renderDistanceChunks, (Object)this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(TTCp.mc.gameSettings.renderDistanceChunks);
            }
            WorldInfo worldinfo1 = this.worlds[0].func_72912_H();
            WorldInfo worldinfo = TTCp.mc.world.func_72912_H();
            if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
                LOGGER.info("Changing difficulty to {}, from {}", (Object)worldinfo.getDifficulty(), (Object)worldinfo1.getDifficulty());
                this.setDifficultyForAllWorlds(worldinfo.getDifficulty());
            } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
                LOGGER.info("Locking difficulty to {}", (Object)worldinfo.getDifficulty());
                for (WorldServer worldserver : this.worlds) {
                    if (worldserver == null) continue;
                    worldserver.func_72912_H().setDifficultyLocked(true);
                }
            }
        }
    }

    public boolean canStructuresSpawn() {
        return false;
    }

    public GameType getGameType() {
        return this.worldSettings.getGameType();
    }

    public EnumDifficulty getDifficulty() {
        return TTCp.mc.world.func_72912_H().getDifficulty();
    }

    public boolean isHardcore() {
        return this.worldSettings.getHardcoreEnabled();
    }

    public boolean shouldBroadcastRconToOps() {
        return true;
    }

    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    public void saveAllWorlds(boolean isSilent) {
        super.saveAllWorlds(isSilent);
    }

    public File getDataDirectory() {
        return new File("ttcSaves");
    }

    public boolean isDedicatedServer() {
        return false;
    }

    public boolean shouldUseNativeTransport() {
        return false;
    }

    public void finalTick(CrashReport report) {
    }

    public CrashReport addServerInfoToCrashReport(CrashReport report) {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addDetail("Type", (ICrashReportDetail)new ICrashReportDetail<String>(){

            public String call() throws Exception {
                return "Integrated Server (map_client.txt)";
            }
        });
        report.getCategory().addDetail("Is Modded", (ICrashReportDetail)new ICrashReportDetail<String>(){

            public String call() throws Exception {
                String s = ClientBrandRetriever.getClientModName();
                if (!s.equals("vanilla")) {
                    return "Definitely; Client brand changed to '" + s + "'";
                }
                s = WorldGeneratorV2.this.getServerModName();
                if (!"vanilla".equals(s)) {
                    return "Definitely; Server brand changed to '" + s + "'";
                }
                return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.";
            }
        });
        return report;
    }

    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        super.setDifficultyForAllWorlds(difficulty);
    }

    public void addServerStatsToSnooper(Snooper playerSnooper) {
    }

    public boolean isSnooperEnabled() {
        return Minecraft.getMinecraft().isSnooperEnabled();
    }

    public String shareToLAN(GameType type, boolean allowCheats) {
        return "";
    }

    public void stopServer() {
        this.initiateShutdown();
        super.stopServer();
    }

    public void initiateShutdown() {
        super.initiateShutdown();
    }

    public void setGameType(GameType gameMode) {
        super.setGameType(gameMode);
        this.getPlayerList().setGameType(gameMode);
    }

    public boolean isCommandBlockEnabled() {
        return true;
    }

    public int getOpPermissionLevel() {
        return 4;
    }
}
