package tudbut.mod.client.ttcp.gui;

import tudbut.mod.client.ttcp.gui.GuiTTC;

public enum GuiTTC$Theme implements GuiTTC.ITheme
{
    TTC(-2147418368, 0x4000FF00),
    BARTENDER(-13822665, -15922678),
    ETERNAL_BLUE(-16777012, -16777088),
    DARK(-14671840, -16777216),
    LIGHT(-3355444, -6710887, -16777216, false),
    HACKER(-14671840, -16777216, -16711936),
    BLOOD(-5636096, -7864320, -16711681, false),
    SKY(-16724788, -16737895, 0, false),
    KAMI_BLUE(-1154140606, -1154140606, -4473925, false),
    SCHLONGHAX(-1152043422, -1152043422, -4473925, false),
    ORANGE(-3375104, -6725632, -12566464, false),
    XV11(-12619378, -13816531, -9989793, false);

    public final int buttonColor;
    public final int subButtonColor;
    public final int textColor;
    public final boolean shadow;

    @Override
    public int getButtonColor() {
        return this.buttonColor;
    }

    @Override
    public int getSubButtonColor() {
        return this.subButtonColor;
    }

    @Override
    public int getTextColor() {
        return this.textColor;
    }

    @Override
    public boolean hasShadow() {
        return this.shadow;
    }

    private GuiTTC$Theme(int buttonColor, int subButtonColor) {
        this.buttonColor = buttonColor;
        this.subButtonColor = subButtonColor;
        this.textColor = -1;
        this.shadow = true;
    }

    private GuiTTC$Theme(int buttonColor, int subButtonColor, int textColor) {
        this.buttonColor = buttonColor;
        this.subButtonColor = subButtonColor;
        this.textColor = textColor;
        this.shadow = true;
    }

    private GuiTTC$Theme(int buttonColor, int subButtonColor, int textColor, boolean shadow) {
        this.buttonColor = buttonColor;
        this.subButtonColor = subButtonColor;
        this.textColor = textColor;
        this.shadow = shadow;
    }
}
