package tudbut.mod.client.ttcp.mods.misc;

import de.tudbut.io.StreamReader;
import de.tudbut.io.StreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.JSModule;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;
import tudbut.obj.TLMap;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.ConfigSaverTCN;

@Misc
public class JSModules
extends Module {
    @Save
    ArrayList<String> jsModules = new ArrayList();
    TLMap<String, Module> modules = new TLMap();

    @Override
    public void onEnable() {
        for (String module : this.jsModules) {
            this.loadModule(module);
        }
        if (JSModules.mc.currentScreen instanceof GuiTTC) {
            ((GuiTTC)JSModules.mc.currentScreen).resetButtons();
        }
    }

    @Override
    public void onDisable() {
        for (String module : this.jsModules) {
            this.unloadModule(module);
        }
        if (JSModules.mc.currentScreen instanceof GuiTTC) {
            ((GuiTTC)JSModules.mc.currentScreen).resetButtons();
        }
    }

    public Module loadModule(String s) {
        try {
            FileInputStream stream = new FileInputStream("config/ttc/modules/" + s + ".ttcmodule.js");
            String js = new StreamReader(stream).readAllAsString();
            JSModule module = JSModule.Loader.createFromJS(js, s);
            if (module == null) {
                return null;
            }
            this.modules.set(s, module);
            try {
                try {
                    TCN tcn = JSON.read(new StreamReader(new FileInputStream("config/ttc/modules/config/" + s + ".jsmodulecfg.json")).readAllAsString());
                    ConfigSaverTCN.loadConfig(module, tcn);
                    try {
                        if (module.enabled) {
                            ((Module)module).onEnable();
                        }
                    }
                    catch (NullPointerException nullPointerException) {}
                }
                catch (Exception tcn) {
                    // empty catch block
                }
                ((Module)module).onConfigLoad();
                module.updateBindsFull();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            TTCp.addModule(module);
            return module;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unloadModule(String s) {
        if (this.modules.get(s) != null) {
            this.modules.get(s).onDisable();
            this.modules.get(s).onConfigSave();
            try {
                StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/config/" + ((JSModule)this.modules.get((String)s)).id + ".jsmodulecfg.json"));
                writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(this.modules.get(s))).toCharArray());
            }
            catch (Exception exception) {
                // empty catch block
            }
            TTCp.removeModule(this.modules.get(s));
            this.modules.set(s, null);
        }
    }

    public void reloadModule(String s) {
        this.unloadModule(s);
        if (this.loadModule(s) == null) {
            ChatUtils.print("Couldn't load module " + s);
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (new File("config/ttc/modules/config").mkdirs()) {
            ChatUtils.print("Put JSModule files in your config/ttc/modules folder!");
        }
        try {
            StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/Example.ttcmodule.js"));
            writer.writeChars("return {\n  name: 'Example',\n  onEnable: function() {\n    this.jm.printChat('Example module enabled!') // jm = The module\n    this.mc.player.swingArm(Java.type('net.minecraft.util.EnumHand').MAIN_HAND)\n  }\n};\n".toCharArray());
            writer.stream.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                this.jsModules.remove(args[1]);
                this.jsModules.add(args[1]);
                this.unloadModule(args[1]);
                if (this.loadModule(args[1]) != null) {
                    ChatUtils.print("Loaded!");
                } else {
                    ChatUtils.print("Failed to load module. It seems to be faulty!");
                }
            }
            if (args[0].equalsIgnoreCase("remove")) {
                this.jsModules.remove(args[1]);
                this.unloadModule(args[1]);
                ChatUtils.print("Unloaded!");
            }
            if (args[0].equalsIgnoreCase("reload")) {
                this.reloadModule(args[1]);
                ChatUtils.print("Reloaded!");
            }
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            for (String module : this.jsModules) {
                this.reloadModule(module);
            }
            ChatUtils.print("Reloaded!");
        }
    }
}
