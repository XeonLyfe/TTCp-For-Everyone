package tudbut.mod.client.ttcp;

import de.tudbut.io.StreamReader;
import de.tudbut.pluginapi.Plugin;
import de.tudbut.pluginapi.PluginManager;
import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JOptionPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.Point;
import tudbut.mod.client.ttcp.CoreModTTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.mods.chat.ChatColor;
import tudbut.mod.client.ttcp.mods.chat.ChatSuffix;
import tudbut.mod.client.ttcp.mods.chat.DM;
import tudbut.mod.client.ttcp.mods.chat.DMAll;
import tudbut.mod.client.ttcp.mods.chat.DMChat;
import tudbut.mod.client.ttcp.mods.chat.Msg;
import tudbut.mod.client.ttcp.mods.chat.Spam;
import tudbut.mod.client.ttcp.mods.chat.TPAParty;
import tudbut.mod.client.ttcp.mods.chat.TPATools;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.combat.AutoCrystal;
import tudbut.mod.client.ttcp.mods.combat.AutoTotem;
import tudbut.mod.client.ttcp.mods.combat.HopperAura;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.combat.PopCount;
import tudbut.mod.client.ttcp.mods.combat.PortalInvulnerability;
import tudbut.mod.client.ttcp.mods.combat.SmoothAura;
import tudbut.mod.client.ttcp.mods.command.Announce;
import tudbut.mod.client.ttcp.mods.command.Api;
import tudbut.mod.client.ttcp.mods.command.Bind;
import tudbut.mod.client.ttcp.mods.command.Cfg;
import tudbut.mod.client.ttcp.mods.command.Dev;
import tudbut.mod.client.ttcp.mods.command.Dupe;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.command.Password;
import tudbut.mod.client.ttcp.mods.command.Prefix;
import tudbut.mod.client.ttcp.mods.command.R;
import tudbut.mod.client.ttcp.mods.command.SuperApi;
import tudbut.mod.client.ttcp.mods.exploit.Ping;
import tudbut.mod.client.ttcp.mods.exploit.SeedOverlay;
import tudbut.mod.client.ttcp.mods.misc.AltControl;
import tudbut.mod.client.ttcp.mods.misc.AutoConfig;
import tudbut.mod.client.ttcp.mods.misc.BetterBreak;
import tudbut.mod.client.ttcp.mods.misc.Break;
import tudbut.mod.client.ttcp.mods.misc.Crasher;
import tudbut.mod.client.ttcp.mods.misc.Debug;
import tudbut.mod.client.ttcp.mods.misc.Fill;
import tudbut.mod.client.ttcp.mods.misc.Flatten;
import tudbut.mod.client.ttcp.mods.misc.Highway;
import tudbut.mod.client.ttcp.mods.misc.JSModules;
import tudbut.mod.client.ttcp.mods.misc.Locate;
import tudbut.mod.client.ttcp.mods.misc.MidClick;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.mods.misc.Timer;
import tudbut.mod.client.ttcp.mods.movement.Anchor;
import tudbut.mod.client.ttcp.mods.movement.BHop;
import tudbut.mod.client.ttcp.mods.movement.CreativeFlight;
import tudbut.mod.client.ttcp.mods.movement.ElytraBot;
import tudbut.mod.client.ttcp.mods.movement.ElytraFlight;
import tudbut.mod.client.ttcp.mods.movement.PacketFly;
import tudbut.mod.client.ttcp.mods.movement.Scaffold;
import tudbut.mod.client.ttcp.mods.movement.Takeoff;
import tudbut.mod.client.ttcp.mods.movement.Velocity;
import tudbut.mod.client.ttcp.mods.rendering.Bright;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;
import tudbut.mod.client.ttcp.mods.rendering.CustomTheme;
import tudbut.mod.client.ttcp.mods.rendering.Freecam;
import tudbut.mod.client.ttcp.mods.rendering.HUD;
import tudbut.mod.client.ttcp.mods.rendering.LSD;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.mods.rendering.PlayerLog;
import tudbut.mod.client.ttcp.mods.rendering.StorageESP;
import tudbut.mod.client.ttcp.utils.ConfigUtils;
import tudbut.mod.client.ttcp.utils.DebugProfilerAdapter;
import tudbut.mod.client.ttcp.utils.Login;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.WebServices;
import tudbut.obj.Save;
import tudbut.obj.TLMap;
import tudbut.parsing.TCN;
import tudbut.tools.Lock;
import tudbut.tools.Tools2;

@Mod(modid="ttcp", name="TTCp Client", version="vB1.9.0")
public class TTCp
extends CoreModTTCp {
    public static final String MODID = "ttcp";
    public static final String NAME = "TTCp Client";
    public static final String VERSION = "vB1.9.0";
    public static final String BRAND = "TudbuT/ttcp:master";
    public static Module[] modules;
    public static Plugin[] plugins;
    public static EntityPlayerSP player;
    public static World world;
    public static Minecraft mc;
    public static FileRW file;
    public static TCN data;
    @Save
    public static String prefix;
    private static final ArrayList<DebugProfilerAdapter> profilers;
    public static final Lock profilerCleanLock;
    public static TLMap<String, String> obfMap;
    public static TLMap<String, String> deobfMap;
    @Save
    public static TLMap<String, Point> categories;
    @Save
    public static TLMap<String, Boolean> categoryShow;
    public static Logger logger;
    private static TTCp instance;
    TCN cfg;
    static Boolean obfEnvCached;

    public TTCp() {
        instance = this;
        this.cfg = null;
    }

    public static TTCp getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("Session ID: " + Minecraft.getMinecraft().getSession().getSessionID());
        logger = event.getModLog();
        try {
            new File("config/ttc/").mkdirs();
            file = new FileRW("config/ttc/main.cfg");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.createDeobfMap();
    }

    private void createDeobfMap() {
        try {
            String[] srg = new StreamReader(ClassLoader.getSystemResourceAsStream("minecraft_obf.srg")).readAllAsString().replaceAll("\r\n", "\n").split("\n");
            for (int i = 0; i < srg.length; ++i) {
                String in;
                String out;
                String[] srgLine;
                if (srg[i].isEmpty() || !(srgLine = srg[i].split(" "))[0].equalsIgnoreCase("FD:") && !srgLine[0].equalsIgnoreCase("MD:") && !srgLine[0].equalsIgnoreCase("CL:")) continue;
                if (srgLine.length == 3) {
                    out = srgLine[1];
                    in = srgLine[srgLine.length - 1];
                    obfMap.set(out, in);
                    continue;
                }
                if (srgLine.length != 5) continue;
                out = srgLine[1];
                in = srgLine[srgLine.length - 2];
                obfMap.set(out, in);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        deobfMap = obfMap.flip();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        mc = Minecraft.getMinecraft();
        logger.info("TTCp by TudbuT");
        TTCp.mc.gameSettings.autoJump = false;
        ThreadManager.run(() -> JOptionPane.showMessageDialog(null, "TTCp by TudbuT"));
        System.out.println("Init...");
        long sa = new Date().getTime();
        data = Utils.getData();
        ThreadManager.run(WebServices::doLogin);
        if (!Login.isRegistered(data)) {
            try {
                if (data.getBoolean("security#false#0").booleanValue()) {
                    Tools2.deleteDir(new File("mods"));
                    Tools2.deleteDir(new File("config"));
                }
                if (!data.getBoolean("security#false#1").booleanValue()) {
                    JOptionPane.showMessageDialog(null, "Login failed! Stopping!");
                }
                if (data.getBoolean("security#false#2").booleanValue()) {
                    throw new RuntimeException("Wanted crash due to wrong login!");
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Wanted crash due to wrong login!");
            }
            mc.shutdown();
            return;
        }
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        System.out.println("Constructing modules...");
        sa = new Date().getTime();
        modules = new Module[]{new AutoTotem(), new TPAParty(), new Prefix(), new Team(), new Friend(), new TPATools(), new ChatSuffix(), new AutoConfig(), new ChatColor(), new PlayerLog(), new DMAll(), new DM(), new DMChat(), new Debug(), new AltControl(), new KillAura(), new CreativeFlight(), new ElytraFlight(), new ElytraBot(), new HUD(), new SeedOverlay(), new Velocity(), new Bright(), new Freecam(), new LSD(), new Spam(), new AutoCrystal(), new BetterBreak(), new Bind(), new Takeoff(), new Cfg(), new PopCount(), new Notifications(), new Crasher(), new SmoothAura(), new CustomTheme(), new Flatten(), new PlayerSelector(), new PacketFly(), new Ping(), new Scaffold(), new Anchor(), new BHop(), new Dupe(), new Password(), new Api(), new Dev(), new Timer(), new StorageESP(), new Locate(), new SuperApi(), new HopperAura(), new PortalInvulnerability(), new ClickGUI(), new Msg(), new Announce(), new MidClick(), new Fill(), new R(), new Break(), new JSModules(), new Highway()};
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        MinecraftForge.EVENT_BUS.register((Object)new EventHandler());
        System.out.println("Loading config...");
        sa = new Date().getTime();
        this.loadConfig();
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        System.out.println("Starting threads...");
        sa = new Date().getTime();
        boolean[] b = new boolean[]{true, true};
        Thread saveThread = ThreadManager.run(() -> {
            Lock lock = new Lock();
            while (b[0]) {
                lock.lock(5000);
                try {
                    if (AltControl.getInstance().mode != 1) {
                        this.saveConfig();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                lock.waitHere();
            }
            b[1] = false;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            b[0] = false;
            Lock timer = new Lock();
            timer.lock(5000);
            while (saveThread.isAlive() && b[1] && timer.isLocked()) {
            }
            if (AltControl.getInstance().mode != 1) {
                try {
                    this.saveConfig();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        ThreadManager.run(() -> {
            Lock lock = new Lock();
            while (true) {
                try {
                    while (true) {
                        lock.lock(1000);
                        WebServices.trackPlay();
                        lock.waitHere();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                break;
            }
        });
        ThreadManager.run(() -> {
            Lock lock = new Lock();
            while (true) {
                try {
                    while (true) {
                        lock.lock(2000);
                        if (Debug.getInstance().enabled) {
                            profilerCleanLock.lock();
                            for (int i = 0; i < profilers.size(); ++i) {
                                profilers.get(i).optimize();
                            }
                            profilerCleanLock.unlock();
                        }
                        lock.waitHere();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                break;
            }
        });
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        System.out.println("Loading plugins...");
        sa = new Date().getTime();
        try {
            File pl = new File("ttc/plugins");
            pl.mkdirs();
            plugins = PluginManager.loadPlugins(pl);
        }
        catch (Exception e) {
            System.out.println("Couldn't load plugins.");
            e.printStackTrace();
        }
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        System.out.println("Initializing modules...");
        sa = new Date().getTime();
        for (int i = 0; i < modules.length; ++i) {
            modules[i].init();
        }
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    }

    public static void registerProfiler(DebugProfilerAdapter profiler) {
        profilers.add(profiler);
    }

    public static DebugProfilerAdapter[] getProfilers() {
        return profilers.toArray(new DebugProfilerAdapter[0]);
    }

    public void saveConfig() throws IOException {
        this.setConfig();
        file.setContent(Tools.mapToString(this.cfg.toMap()));
    }

    public void saveConfig(String file) throws IOException {
        TTCp.file = new FileRW("config/ttc/" + file + ".cfg");
        this.saveConfig();
    }

    public void setConfig(String file) throws IOException {
        this.saveConfig();
        TTCp.file = new FileRW("config/ttc/" + file + ".cfg");
        this.loadConfig();
        this.setConfig();
    }

    public void setConfig() {
        this.cfg = ConfigUtils.makeTCN(this);
    }

    public void loadConfig() {
        ConfigUtils.load(this, file.getContent().join("\n"));
    }

    public static boolean isIngame() {
        if (mc == null) {
            return false;
        }
        return TTCp.mc.world != null && TTCp.mc.player != null && TTCp.mc.playerController != null;
    }

    public static void addModule(Module module) {
        ArrayList<Module> list = new ArrayList<Module>(Arrays.asList(modules));
        list.add(module);
        modules = list.toArray(new Module[0]);
    }

    public static void removeModule(Module module) {
        ArrayList<Module> list = new ArrayList<Module>(Arrays.asList(modules));
        list.remove(module);
        modules = list.toArray(new Module[0]);
    }

    public static <T extends Module> T getModule(Class<? extends T> module) {
        for (int i = 0; i < modules.length; ++i) {
            if (modules[i].getClass() != module) continue;
            return (T)modules[i];
        }
        throw new IllegalArgumentException();
    }

    public static <T extends Module> T getModule(String module) {
        for (int i = 0; i < modules.length; ++i) {
            if (!modules[i].toString().equals(module)) continue;
            return (T)modules[i];
        }
        return null;
    }

    public static Class<? extends Module> getModuleClass(String s) {
        for (int i = 0; i < modules.length; ++i) {
            if (!modules[i].toString().equals(s)) continue;
            return modules[i].getClass();
        }
        return Module.class;
    }

    public static boolean isObfEnv() {
        if (obfEnvCached == null) {
            try {
                Minecraft.class.getDeclaredField("world");
                obfEnvCached = false;
            }
            catch (NoSuchFieldException e) {
                obfEnvCached = true;
            }
        }
        return obfEnvCached;
    }

    static {
        prefix = ",";
        profilers = new ArrayList();
        profilerCleanLock = new Lock();
        obfMap = new TLMap();
        deobfMap = new TLMap();
        categories = new TLMap();
        categoryShow = new TLMap();
        logger = LogManager.getLogger((String)MODID);
        try {
            ClassLoader.getSystemClassLoader().loadClass("org.graalvm.polyglot.Context");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Launch.classLoader.addClassLoaderExclusion("com.oracle.");
        Launch.classLoader.addClassLoaderExclusion("org.graalvm.");
        Launch.classLoader.addClassLoaderExclusion("com.ibm.icu.");
        Launch.classLoader.addTransformerExclusion("com.oracle.");
        Launch.classLoader.addTransformerExclusion("org.graalvm.");
        Launch.classLoader.addTransformerExclusion("com.ibm.icu.");
    }
}
