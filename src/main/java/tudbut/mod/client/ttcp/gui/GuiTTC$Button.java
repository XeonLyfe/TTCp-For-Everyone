package tudbut.mod.client.ttcp.gui;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.Gui;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;
import tudbut.mod.client.ttcp.utils.Module;

public class GuiTTC$Button {
    public int x;
    public int y;
    public AtomicReference<String> text;
    public int color = -2147418368;
    public Module module;
    GuiTTC$ButtonClickEvent event;
    private boolean mouseDown = false;
    private int mouseDownButton = 0;
    private GuiTTC$Button[] subButtons;
    private boolean display = true;

    public GuiTTC$Button(String text, GuiTTC$ButtonClickEvent event) {
        this(0, 0, text, event, null);
    }

    public GuiTTC$Button(int x, int y, String text, GuiTTC$ButtonClickEvent event, Module module) {
        if (module != null) {
            if (module.clickGuiX != null && module.clickGuiY != null) {
                x = module.clickGuiX;
                y = module.clickGuiY;
            }
            this.subButtons = module.subButtons.toArray(new GuiTTC$Button[0]);
            this.display = module.displayOnClickGUI();
        }
        this.x = x;
        this.y = y;
        this.text = new AtomicReference<String>(text);
        this.event = event;
        this.module = module;
        if (ClickGUI.getInstance() != null) {
            this.color = ClickGUI.getInstance().getTheme().getButtonColor();
        }
    }

    public void draw(GuiTTC gui) {
        if (!this.display) {
            return;
        }
        int color = this.color;
        if (gui.cx >= this.x && gui.cy >= this.y && gui.cx <= this.x + 150 && gui.cy <= this.y + this.ySize()) {
            Color c = new Color(color, true);
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            int a = c.getAlpha();
            color = new Color(Math.min(r += 32, 255), Math.min(g += 32, 255), Math.min(b += 32, 255), Math.min(a += 32, 255)).getRGB();
        }
        Gui.drawRect((int)this.x, (int)this.y, (int)(this.x + 150), (int)(this.y + this.ySize()), (int)color);
        gui.fontRenderer.drawString(this.text.get(), (float)(this.x + 6), (float)this.y + (float)this.ySize() / 2.0f - 4.0f, ClickGUI.getInstance().getTheme().getTextColor(), ClickGUI.getInstance().getTheme().hasShadow());
        if (this.module != null && this.module.enabled ^ this.module.clickGuiShow) {
            for (int i = 0; i < this.subButtons.length; ++i) {
                GuiTTC$Button b = this.subButtons[i];
                if (b == null) continue;
                b.x = this.x;
                b.y = this.y + ((i + 1) * 15 + 5);
                b.color = ClickGUI.getInstance().getTheme().getSubButtonColor();
                b.draw(gui);
            }
        }
    }

    public int ySize() {
        return this.module == null ? 15 : 20;
    }

    public boolean mouseClicked(int clickX, int clickY, int button) {
        if (clickX >= this.x && clickY >= this.y && clickX < this.x + 150 && clickY < this.y + this.ySize()) {
            this.mouseDown = true;
            if (ClickGUI.getInstance().flipButtons) {
                button = button == 0 ? 1 : (button == 1 ? 0 : button);
            }
            this.mouseDownButton = button;
            this.click(button);
            return true;
        }
        if (this.module != null && this.module.enabled ^ this.module.clickGuiShow) {
            for (int i = 0; i < this.subButtons.length; ++i) {
                GuiTTC$Button b = this.subButtons[i];
                if (b == null) continue;
                b.x = this.x;
                b.y = this.y + ((i + 1) * 15 + 5);
                b.color = ClickGUI.getInstance().getTheme().getSubButtonColor();
                if (!b.mouseClicked(clickX, clickY, button)) continue;
                return true;
            }
        }
        return false;
    }

    public void mouseReleased() {
        this.mouseDown = false;
        if (this.module != null && this.module.enabled ^ this.module.clickGuiShow) {
            this.subButtons = this.module.subButtons.toArray(new GuiTTC$Button[0]);
            for (int i = 0; i < this.subButtons.length; ++i) {
                this.subButtons[i].mouseReleased();
            }
        }
    }

    protected void click(int button) {
        if (button == 0) {
            this.event.run(this.text);
        }
        if (button == 2 && this.module != null) {
            this.module.clickGuiShow = !this.module.clickGuiShow;
        }
    }

    protected void onTick(GuiTTC gui) {
        this.color = ClickGUI.getInstance().getTheme().getButtonColor();
        if (this.module != null) {
            if (this.mouseDown && this.mouseDownButton == 1) {
                this.x = gui.cx - 75;
                this.y = gui.cy - 10;
                this.x = this.x / 5 * 5;
                this.y = this.y / 5 * 5;
            }
            this.module.clickGuiX = this.x;
            this.module.clickGuiY = this.y;
        }
    }
}
