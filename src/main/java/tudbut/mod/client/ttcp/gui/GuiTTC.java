package tudbut.mod.client.ttcp.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;
import tudbut.mod.client.ttcp.utils.Module;

public class GuiTTC
extends GuiScreen {
    private Button[] buttons;
    private int cx;
    private int cy;
    private int lastScrollPos = Mouse.getEventDWheel();

    public GuiTTC() {
        this.mc = TTCp.mc;
    }

    public boolean doesGuiPauseGame() {
        return this.mc.player.timeInPortal != 0.0f;
    }

    public void initGui() {
        this.buttons = new Button[256];
        this.resetButtons();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
        this.lastScrollPos = Mouse.getEventDWheel();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUI.getInstance().enabled = false;
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
            this.buttons[i].onTick(this);
        }
    }

    public void resetButtons() {
        System.out.println("Resetting buttons on ClickGUI");
        Button[] buttons = new Button[TTCp.modules.length];
        int j = 0;
        for (int i = 0; i < TTCp.modules.length; ++i) {
            Button b;
            int x = j / 15;
            int y = j - x * 15;
            if (!TTCp.modules[i].displayOnClickGUI()) continue;
            int r = i;
            buttons[i] = b = new Button(10 + 155 * x, 10 + y * 25, TTCp.modules[r].toString() + ": " + TTCp.modules[r].enabled, text -> {
                TTCp.modules[r].enabled = !TTCp.modules[r].enabled;
                if (TTCp.modules[r].enabled) {
                    TTCp.modules[r].onEnable();
                } else {
                    TTCp.modules[r].onDisable();
                }
            }, TTCp.modules[i]);
            ++j;
        }
        this.buttons = buttons;
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
        for (int i = 0; i < TTCp.modules.length; ++i) {
            if (this.buttons[i] == null) continue;
            this.buttons[i].text.set(TTCp.modules[i].toString() + ": " + TTCp.modules[i].enabled);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.cx = mouseX;
        this.cy = mouseY;
        for (int i = 0; i < this.buttons.length; ++i) {
            Button button = this.buttons[i];
            if (button == null || !button.mouseClicked(mouseX, mouseY, mouseButton)) continue;
            return;
        }
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.cx = mouseX;
        this.cy = mouseY;
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.cx = mouseX;
        this.cy = mouseY;
        for (int i = 0; i < this.buttons.length; ++i) {
            Button button = this.buttons[i];
            if (button == null) continue;
            button.mouseReleased();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int m;
        this.updateButtons();
        this.drawDefaultBackground();
        this.cx = mouseX;
        this.cy = mouseY;
        for (int i = 0; i < this.buttons.length; ++i) {
            if (this.buttons[i] == null) continue;
            this.buttons[i].draw(this);
        }
        if (ClickGUI.getInstance().mouseFix) {
            GuiTTC.func_73734_a((int)(mouseX - 2), (int)(mouseY - 2), (int)(mouseX + 2), (int)(mouseY + 2), (int)-1);
        }
        if ((m = -Mouse.getDWheel()) != 0) {
            block5: for (int i = 0; i < this.buttons.length; ++i) {
                if (this.buttons[i] == null) continue;
                int d = (this.lastScrollPos - m) / 3;
                switch (ClickGUI.getInstance().sd) {
                    case Vertical: {
                        this.buttons[i].y += d;
                        continue block5;
                    }
                    case Horizontal: {
                        this.buttons[i].x += d;
                    }
                }
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static interface ButtonClickEvent {
        public void run(AtomicReference<String> var1);
    }

    public static class Button {
        public int x;
        public int y;
        public AtomicReference<String> text;
        public int color = -2147418368;
        public Module module;
        ButtonClickEvent event;
        private boolean mouseDown = false;
        private int mouseDownButton = 0;
        private Button[] subButtons;
        private boolean display = true;

        public Button(String text, ButtonClickEvent event) {
            this(0, 0, text, event, null);
        }

        public Button(int x, int y, String text, ButtonClickEvent event, Module module) {
            if (module != null) {
                if (module.clickGuiX != null && module.clickGuiY != null) {
                    x = module.clickGuiX;
                    y = module.clickGuiY;
                }
                this.subButtons = module.subButtons.toArray(new Button[0]);
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
                    Button b = this.subButtons[i];
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
                    Button b = this.subButtons[i];
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
                this.subButtons = this.module.subButtons.toArray(new Button[0]);
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

    public static enum Theme implements ITheme
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

        private Theme(int buttonColor, int subButtonColor) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = -1;
            this.shadow = true;
        }

        private Theme(int buttonColor, int subButtonColor, int textColor) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = textColor;
            this.shadow = true;
        }

        private Theme(int buttonColor, int subButtonColor, int textColor, boolean shadow) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = textColor;
            this.shadow = shadow;
        }
    }

    public static interface ITheme {
        public int getButtonColor();

        public int getSubButtonColor();

        public int getTextColor();

        public boolean hasShadow();
    }
}
