package tudbut.mod.client.ttcp.mods.chat;

public enum ChatColor$Prefix {
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

    private ChatColor$Prefix(String prefix) {
        this.prefix = prefix;
    }
}
