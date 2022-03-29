package tudbut.mod.client.ttcp.gui;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.Gui;
import tudbut.mod.client.ttcp.gui.GuiPlayerSelect;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;

public class GuiPlayerSelect$Button {
    public int x;
    public int y;
    public AtomicReference<String> text;
    public int color = -2147418368;
    GuiTTC.ButtonClickEvent event;

    public GuiPlayerSelect$Button(int x, int y, String text, GuiTTC.ButtonClickEvent event) {
        this.x = x;
        this.y = y;
        this.text = new AtomicReference<String>(text);
        this.event = event;
        if (ClickGUI.getInstance() != null) {
            this.color = ClickGUI.getInstance().getTheme().getButtonColor();
        }
    }

    public void draw(GuiPlayerSelect gui) {
        int color = this.color;
        if (gui.cx >= this.x && gui.cy >= this.y && gui.cx <= this.x + 150 && gui.cy <= this.y + 20) {
            Color c = new Color(color, true);
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            int a = c.getAlpha();
            color = new Color(Math.min(r += 32, 255), Math.min(g += 32, 255), Math.min(b += 32, 255), Math.min(a += 32, 255)).getRGB();
        }
        Gui.drawRect((int)this.x, (int)this.y, (int)(this.x + 150), (int)(this.y + 20), (int)color);
        gui.fontRenderer.drawString(this.text.get(), (float)(this.x + 6), (float)(this.y + 6), ClickGUI.getInstance().getTheme().getTextColor(), ClickGUI.getInstance().getTheme().hasShadow());
    }

    public boolean mouseClicked(int clickX, int clickY, int button) {
        if (clickX >= this.x && clickY >= this.y && clickX <= this.x + 150 && clickY <= this.y + 20) {
            this.click(button);
            return true;
        }
        return false;
    }

    protected void click(int button) {
        if (button == 0) {
            this.event.run(this.text);
        }
    }

    protected void onTick() {
        this.color = ClickGUI.getInstance().getTheme().getButtonColor();
    }
}
