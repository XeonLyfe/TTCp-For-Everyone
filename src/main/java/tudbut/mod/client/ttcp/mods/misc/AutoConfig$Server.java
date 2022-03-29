package tudbut.mod.client.ttcp.mods.misc;

enum AutoConfig$Server {
    _8b8t("8b8t.xyz", true, true, true),
    _5b5t("5b5t.net", false, false, false),
    _0t0t("0b0t.org", false, false, true),
    _2b2t("2b2t.org", false, false, false),
    crystalpvp("crystalpvp.cc", false, false, false);

    String name;
    boolean stackedTots;
    boolean pvp;
    boolean tpa;

    private AutoConfig$Server(String name, boolean stackedTots, boolean pvp, boolean tpa) {
        this.name = name;
        this.stackedTots = stackedTots;
        this.pvp = pvp;
        this.tpa = tpa;
    }
}
