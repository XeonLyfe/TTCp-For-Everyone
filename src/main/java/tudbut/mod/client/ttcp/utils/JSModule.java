package tudbut.mod.client.ttcp.utils;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.JSFieldMapper;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.JavaScript;
import tudbut.obj.Save;

@JavaScript
public class JSModule
extends Module {
    private final Context context;
    private final Value jsModule;
    public final String id;
    public ArrayList<GuiTTC.Button> sb = new ArrayList();
    @Save
    String cfgStr = "{}";

    @Override
    public String toString() {
        if (this.jsModule == null) {
            return "JSModule (ERROR)";
        }
        String s = "JSModule (ERROR)";
        try {
            s = this.jsModule.getMember("name").asString();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        return s;
    }

    public JSModule(Context context, Value jsModuleInput, String id) {
        this.context = context;
        this.jsModule = jsModuleInput;
        try {
            this.jsModule.putMember("jm", this);
            context.eval("js", "jsModuleObj.cfg = {}");
            this.jsModule.putMember("mc", Minecraft.getMinecraft());
            System.out.println("JSModule has: " + this.jsModule.getMemberKeys());
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        this.id = id;
        this.key = new Module.KeyBind(null, this.toString() + "::toggle", true);
        this.updateBinds();
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        try {
            if (this.jsModule.hasMember("onPacket")) {
                return this.jsModule.getMember("onPacket").execute(packet).asBoolean();
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateBinds() {
        if (this.jsModule == null) {
            return;
        }
        try {
            if (this.jsModule.hasMember("updateBinds")) {
                this.jsModule.getMember("updateBinds").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        this.subButtons = this.sb;
    }

    @Override
    public void onEnable() {
        try {
            if (this.jsModule.hasMember("onEnable")) {
                this.jsModule.getMember("onEnable").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (this.jsModule.hasMember("onDisable")) {
                this.jsModule.getMember("onDisable").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick() {
        try {
            if (this.jsModule.hasMember("onTick")) {
                this.jsModule.getMember("onTick").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryTick() {
        try {
            if (this.jsModule.hasMember("onEveryTick")) {
                this.jsModule.getMember("onEveryTick").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubTick() {
        try {
            if (this.jsModule.hasMember("onSubTick")) {
                this.jsModule.getMember("onSubTick").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEverySubTick() {
        try {
            if (this.jsModule.hasMember("onEverySubTick")) {
                this.jsModule.getMember("onEverySubTick").execute(new Object[0]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        try {
            if (this.jsModule.hasMember("onServerChat")) {
                return this.jsModule.getMember("onServerChat").execute(s, formatted).asBoolean();
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onChat(String s, String[] args) {
        try {
            if (this.jsModule.hasMember("onChat")) {
                this.jsModule.getMember("onChat").execute(s, args);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            if (this.jsModule.hasMember("onEveryChat")) {
                this.jsModule.getMember("onEveryChat").execute(s, args);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void printChat(String toPrint) {
        ChatUtils.print(toPrint);
    }

    @Override
    public void onConfigSave() {
        this.cfgStr = this.context.eval("js", "JSON.stringify(jsModuleObj.cfg)").asString();
    }

    @Override
    public void onConfigLoad() {
        this.context.eval("js", "jsModuleObj.cfg = " + this.cfgStr);
    }

    public static class Loader {
        public static JSModule createFromJS(String js, String id) {
            try {
                Context context = JSFieldMapper.createMapperContext();
                return new JSModule(context, context.eval("js", "const jsModuleObj = (function(){" + js + "})(); jsModuleObj"), id);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
