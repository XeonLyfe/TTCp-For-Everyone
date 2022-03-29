package tudbut.mod.client.ttcp.mods.rendering;

import tudbut.mod.client.ttcp.gui.GuiTTCIngame;
import tudbut.mod.client.ttcp.mods.combat.AutoTotem;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Render;
import tudbut.obj.Save;

@Render
public class HUD
extends Module {
    static HUD instance;
    @Save
    public boolean showPopPredict = false;

    public HUD() {
        instance = this;
    }

    public static HUD getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("Show PopPredict", this, "showPopPredict"));
    }

    public void renderHUD() {
        if (this.enabled) {
            GuiTTCIngame.draw();
            this.renderTotems();
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    public void renderTotems() {
        if (this.enabled) {
            AutoTotem.instance.renderTotems();
        }
    }
}
