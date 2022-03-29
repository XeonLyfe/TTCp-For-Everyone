package tudbut.mod.client.ttcp.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;

public class GuiPlayerSelect
extends GuiScreen {
    private Button[] buttons;
    private final EntityPlayer[] players;
    ButtonClickEvent event;
    private int cx;
    private int cy;

    public GuiPlayerSelect(EntityPlayer[] players, ButtonClickEvent onClick) {
        this.mc = TTCp.mc;
        this.event = onClick;
        this.players = players;
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void initGui() {
        this.mc.mouseHelper.ungrabMouseCursor();
        while (Mouse.isGrabbed()) {
            this.mc.mouseHelper.ungrabMouseCursor();
        }
        this.buttons = new Button[256];
        this.resetButtons();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
    }

    public void updateScreen() {
        while (this.buttons == null) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.buttons != null) continue;
            this.resetButtons();
        }
        for (int i = 0; i < this.buttons.length; ++i) {
            if (this.buttons[i] == null) continue;
            this.buttons[i].onTick();
        }
    }

    private void resetButtons() {
        System.out.println("Resetting buttons on PlayerSelectGUI");
        int j = 0;
        for (int i = 0; i < this.players.length; ++i) {
            Button b;
            int x = j / 8;
            int y = j - x * 8;
            int r = i;
            this.buttons[i] = b = new Button(10 + 160 * x, 10 + y * 30, this.players[r].getName(), text -> {
                EntityPlayer player = this.players[r];
                if (this.event.run(player)) {
                    this.close();
                }
            });
            ++j;
        }
    }

    private void updateButtons() {
        while (this.buttons == null) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.buttons != null) continue;
            this.resetButtons();
        }
        for (int i = 0; i < this.buttons.length; ++i) {
            if (this.buttons[i] == null) continue;
            this.buttons[i].text.set(this.players[i].getName());
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.cx = mouseX;
        this.cy = mouseY;
        for (Button button : this.buttons) {
            if (button == null || !button.mouseClicked(mouseX, mouseY, mouseButton)) continue;
            return;
        }
    }

    public void close() {
        this.onGuiClosed();
        TTCp.mc.displayGuiScreen(null);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.cx = mouseX;
        this.cy = mouseY;
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.cx = mouseX;
        this.cy = mouseY;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.updateButtons();
        this.drawDefaultBackground();
        this.cx = mouseX;
        this.cy = mouseY;
        for (int i = 0; i < this.buttons.length; ++i) {
            if (this.buttons[i] == null) continue;
            this.buttons[i].draw(this);
        }
        if (ClickGUI.getInstance().mouseFix) {
            GuiPlayerSelect.func_73734_a((int)(mouseX - 2), (int)(mouseY - 2), (int)(mouseX + 2), (int)(mouseY + 2), (int)-1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static interface ButtonClickEvent {
        public boolean run(EntityPlayer var1);
    }

    public static class Button {
        public int x;
        public int y;
        public AtomicReference<String> text;
        public int color = -2147418368;
        GuiTTC.ButtonClickEvent event;

        public Button(int x, int y, String text, GuiTTC.ButtonClickEvent event) {
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
}
