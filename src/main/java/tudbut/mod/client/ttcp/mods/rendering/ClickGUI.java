package tudbut.mod.client.ttcp.mods.rendering;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiRewrite;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Render;
import tudbut.obj.Save;
import tudbut.obj.TLMap;

@Render
public class ClickGUI
extends Module {
    static ClickGUI instance;
    @Save
    public boolean mouseFix = false;
    @Save
    public boolean flipButtons = false;
    @Save
    public int themeID = 0;
    public GuiTTC.ITheme customTheme = null;
    private int confirmInstance = 0;
    @Save
    public ScrollDirection sd = ScrollDirection.Vertical;

    public GuiTTC.ITheme getTheme() {
        if (this.customTheme != null) {
            return this.customTheme;
        }
        return GuiTTC.Theme.values()[this.themeID];
    }

    public ClickGUI() {
        instance = this;
        this.clickGuiShow = true;
    }

    public static ClickGUI getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Flip buttons: " + this.flipButtons, it -> {
            this.flipButtons = !this.flipButtons;
            it.text = "Flip buttons: " + this.flipButtons;
        }));
        this.subComponents.add(new Button("Theme: " + this.getTheme(), it -> {
            if (this.customTheme == null) {
                this.themeID = Keyboard.isKeyDown((int)42) ? --this.themeID : ++this.themeID;
                if (this.themeID < 0) {
                    this.themeID = GuiTTC.Theme.values().length - 1;
                }
                if (this.themeID > GuiTTC.Theme.values().length - 1) {
                    this.themeID = 0;
                }
                it.text = "Theme: " + this.getTheme();
            }
        }));
        this.subComponents.add(Setting.createEnum(ScrollDirection.class, "Scroll", this, "sd"));
        this.subComponents.add(new Button("Reset layout", it -> {
            this.displayConfirmation = true;
            this.confirmInstance = 0;
        }));
        this.subComponents.add(new Button("Mouse fix: " + this.mouseFix, it -> {
            this.mouseFix = !this.mouseFix;
            it.text = "Mouse fix: " + this.mouseFix;
        }));
        this.subComponents.add(new Button("Reset client", it -> {
            this.displayConfirmation = true;
            this.confirmInstance = 1;
        }));
    }

    @Override
    public void onEnable() {
        try {
            ChatUtils.print("Showing ClickGUI");
            TTCp.mc.displayGuiScreen((GuiScreen)new GuiRewrite());
        }
        catch (Exception e) {
            e.printStackTrace();
            this.enabled = false;
        }
    }

    @Override
    public void onConfirm(boolean result) {
        if (result) {
            switch (this.confirmInstance) {
                case 0: {
                    this.enabled = false;
                    this.onDisable();
                    TTCp.categories = new TLMap();
                    this.enabled = true;
                    this.onEnable();
                    break;
                }
                case 1: {
                    this.displayConfirmation = true;
                    this.confirmInstance = 2;
                    break;
                }
                case 2: {
                    this.enabled = false;
                    this.onDisable();
                    try {
                        TTCp.file.setContent("");
                        TTCp.file = null;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    Minecraft.getMinecraft().shutdown();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (TTCp.mc.currentScreen != null && TTCp.mc.currentScreen.getClass() == GuiRewrite.class) {
            TTCp.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onEveryTick() {
        if (this.key.key == null) {
            this.key.key = 51;
            this.updateBindsFull();
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    public static enum ScrollDirection {
        Vertical,
        Horizontal;

    }
}
