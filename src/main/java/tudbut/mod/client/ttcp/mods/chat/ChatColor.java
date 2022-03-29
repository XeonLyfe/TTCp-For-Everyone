package tudbut.mod.client.ttcp.mods.chat;

import tudbut.mod.client.ttcp.gui.lib.component.ToggleButton;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;

@Chat
public class ChatColor
extends Module {
    static ChatColor instance;
    @Save
    private boolean useSpace = false;
    @Save
    public Prefix prefix = Prefix.Green;
    @Save
    public static boolean hide;
    @Save
    public static boolean bold;
    @Save
    public static boolean italic;
    @Save
    public static boolean underline;

    public ChatColor() {
        this.updateBinds();
        instance = this;
    }

    public static ChatColor getInstance() {
        return instance;
    }

    public String get() {
        return this.enabled ? (this.useSpace ? this.prefix.prefix + " " : this.prefix.prefix) : "";
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new ToggleButton("Add space", this, "useSpace"));
        this.subComponents.add(new ToggleButton("Try to hide code", this, "hide"));
        this.subComponents.add(Setting.createEnum(Prefix.class, "Color", this, "prefix"));
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 1;
    }

    static {
        hide = false;
        bold = false;
        italic = false;
        underline = false;
    }

    public static enum Prefix {
        Green(">"),
        Blue("'"),
        Black("#"),
        Gold("$"),
        Red("£"),
        Aqua("^"),
        Yellow("&"),
        DarkBlue("\\"),
        DarkRed("%"),
        Gray(".");

        public final String prefix;

        private Prefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
