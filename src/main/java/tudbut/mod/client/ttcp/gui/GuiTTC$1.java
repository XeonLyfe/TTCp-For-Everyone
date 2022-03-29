package tudbut.mod.client.ttcp.gui;

import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;

class GuiTTC$1 {
    static final int[] $SwitchMap$tudbut$mod$client$ttcp$mods$rendering$ClickGUI$ScrollDirection;

    static {
        $SwitchMap$tudbut$mod$client$ttcp$mods$rendering$ClickGUI$ScrollDirection = new int[ClickGUI.ScrollDirection.values().length];
        try {
            GuiTTC$1.$SwitchMap$tudbut$mod$client$ttcp$mods$rendering$ClickGUI$ScrollDirection[ClickGUI.ScrollDirection.Vertical.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            GuiTTC$1.$SwitchMap$tudbut$mod$client$ttcp$mods$rendering$ClickGUI$ScrollDirection[ClickGUI.ScrollDirection.Horizontal.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
