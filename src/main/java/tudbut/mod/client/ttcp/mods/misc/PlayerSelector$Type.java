package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;

public class PlayerSelector$Type {
    public final PlayerSelector.Callback callback;
    public final String displayName;

    public PlayerSelector$Type(PlayerSelector.Callback callback, String displayName) {
        this.callback = callback;
        this.displayName = displayName;
    }
}
