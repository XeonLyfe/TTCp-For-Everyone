package tudbut.mod.client.ttcp.utils;

import de.tudbut.io.StreamWriter;
import de.tudbut.tools.Tools;
import java.io.FileOutputStream;
import tudbut.debug.DebugProfiler;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.JSModule;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.ConfigSaverTCN;

public class ConfigUtils {
    public static String make(TTCp ttcp) {
        return Tools.mapToString(ConfigUtils.makeTCN(ttcp).toMap());
    }

    public static TCN makeTCN(TTCp ttcp) {
        TCN tcn = TCN.getEmpty();
        tcn.set("init", "true");
        ConfigUtils.makeClient(ttcp, tcn);
        ConfigUtils.makeModules(tcn);
        return tcn;
    }

    private static void makeClient(TTCp ttcp, TCN tcn) {
        try {
            TCN cfg = ConfigSaverTCN.saveConfig(ttcp);
            tcn.set("client", cfg);
        }
        catch (Exception e) {
            System.err.println("Couldn't save config of client");
            e.printStackTrace();
            tcn.set("init", null);
        }
    }

    private static void makeModules(TCN tcn) {
        TCN cfg = TCN.getEmpty();
        for (int i = 0; i < TTCp.modules.length; ++i) {
            Module module = TTCp.modules[i];
            try {
                module.onConfigSave();
                if (module instanceof JSModule) {
                    StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/config/" + ((JSModule)module).id + ".jsmodulecfg.json"));
                    writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(module)).toCharArray());
                    continue;
                }
                TCN moduleTCN = ConfigSaverTCN.saveConfig(module);
                cfg.set(module.toString(), moduleTCN);
                continue;
            }
            catch (Exception e) {
                System.err.println("Couldn't save config of module " + module.toString());
                e.printStackTrace();
                tcn.set("init", null);
            }
        }
        tcn.set("modules", cfg);
    }

    public static void load(TTCp ttcp, String config) {
        try {
            System.out.println("Reading as TCNMap...");
            TCN tcn = TCN.readMap(Tools.stringToMap(config));
            if (!tcn.getBoolean("init").booleanValue()) {
                throw new Exception();
            }
            System.out.println("Done");
            ConfigUtils.loadTCN(ttcp, tcn);
        }
        catch (Exception e0) {
            System.err.println("Couldn't load config as TCNMap");
            try {
                System.out.println("Reading as TCN...");
                TCN tcn = TCN.read(config);
                System.out.println("Done");
                ConfigUtils.loadTCN(ttcp, tcn);
            }
            catch (Exception e1) {
                System.err.println("Couldn't load config");
            }
        }
    }

    public static void loadTCN(TTCp ttcp, TCN tcn) {
        ConfigUtils.loadClient(ttcp, tcn);
        ConfigUtils.loadModules(tcn);
    }

    private static void loadClient(TTCp ttcp, TCN tcn) {
        try {
            ConfigSaverTCN.loadConfig(ttcp, tcn.getSub("client"));
        }
        catch (Exception e) {
            System.err.println("Couldn't load config of client");
            e.printStackTrace();
        }
    }

    private static void loadModules(TCN tcn) {
        tcn = tcn.getSub("modules");
        DebugProfiler profiler = new DebugProfiler("ConfigLoadProfiler", "init");
        for (int i = 0; i < TTCp.modules.length; ++i) {
            Module module = TTCp.modules[i];
            profiler.next(module.toString());
            if (module instanceof JSModule) continue;
            if (module.enabled) {
                module.enabled = false;
                module.onDisable();
            }
            try {
                ConfigSaverTCN.loadConfig(module, tcn.getSub(module.toString()));
                try {
                    if (module.enabled) {
                        module.onEnable();
                    }
                }
                catch (NullPointerException nullPointerException) {
                    // empty catch block
                }
                module.onConfigLoad();
                module.updateBindsFull();
                continue;
            }
            catch (Exception e) {
                module.enabled = module.defaultEnabled();
                System.err.println("Couldn't load config of module " + module.toString());
                e.printStackTrace();
            }
        }
        profiler.endAll();
        System.out.println(profiler.getResults());
        profiler.delete();
    }
}
