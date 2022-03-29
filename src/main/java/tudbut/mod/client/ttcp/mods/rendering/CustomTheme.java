package tudbut.mod.client.ttcp.mods.rendering;

import de.tudbut.tools.Mouse;
import de.tudbut.tools.Tools;
import de.tudbut.ui.windowgui.RenderableWindow;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiScreen;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Render;
import tudbut.obj.Save;
import tudbut.obj.Vector2i;
import tudbut.rendering.Maths2D;

@Render
public class CustomTheme
extends Module
implements GuiTTC.ITheme {
    RenderableWindow window = new RenderableWindow(512, 512, "Color picker", 20, false);
    BufferedImage image0;
    BufferedImage image1;
    BufferedImage image2;
    BufferedImage image3;
    BufferedImage image4;
    BufferedImage image5;
    BufferedImage[] images;
    int imageID;
    BufferedImage image;
    boolean show;
    public Integer selectedColor;
    @Save
    public int textColor;
    @Save
    public int buttonColor;
    @Save
    public int subButtonColor;
    private boolean mouseWasDown;

    public CustomTheme() {
        int y;
        int x;
        try {
            Thread.sleep(1000L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.window.getWindow().setResizable(false);
        this.window.getWindow().setVisible(false);
        this.window.getWindow().addWindowListener(new WindowListener(){

            @Override
            public void windowOpened(WindowEvent windowEvent) {
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                CustomTheme.this.show = false;
                CustomTheme.this.selectedColor = null;
                CustomTheme.this.updateBinds();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {
            }
        });
        this.image0 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image0.setRGB(x, y, new Color(x, y, Math.max(x + y - 256, 0)).getRGB());
            }
        }
        this.image1 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image1.setRGB(x, y, new Color(x, Math.max(x + y - 256, 0), y).getRGB());
            }
        }
        this.image2 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image2.setRGB(x, y, new Color(Math.max(x + y - 256, 0), x, y).getRGB());
            }
        }
        this.image3 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image3.setRGB(x, y, new Color(Math.max(x + y - 256, 0), Math.min(255, x + 128), Math.min(255, y + 128)).getRGB());
            }
        }
        this.image4 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image4.setRGB(x, y, new Color(Math.min(255, x + 128), Math.max(x + y - 256, 0), Math.min(255, y + 128)).getRGB());
            }
        }
        this.image5 = new BufferedImage(256, 256, 1);
        for (x = 0; x < 256; ++x) {
            for (y = 0; y < 256; ++y) {
                this.image5.setRGB(x, y, new Color(Math.min(255, x + 128), Math.min(255, y + 128), Math.max(x + y - 256, 0)).getRGB());
            }
        }
        Vector2i size = this.window.getSizeOnScreen();
        this.images = new BufferedImage[]{Maths2D.distortImage(this.image0, 512, 512, 1.0), Maths2D.distortImage(this.image1, 512, 512, 1.0), Maths2D.distortImage(this.image2, 512, 512, 1.0), Maths2D.distortImage(this.image3, 512, 512, 1.0), Maths2D.distortImage(this.image4, 512, 512, 1.0), Maths2D.distortImage(this.image5, 512, 512, 1.0)};
        this.imageID = 0;
        this.image = this.images[0];
        this.show = false;
        this.selectedColor = null;
        this.textColor = -1;
        this.buttonColor = -2147418368;
        this.subButtonColor = 0x4000FF00;
        this.mouseWasDown = false;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Copy theme to clipboard", it -> GuiScreen.setClipboardString((String)this.themeString())));
        this.subComponents.add(new Button("Use theme from clipboard", it -> this.themeFromString(GuiScreen.getClipboardString())));
        this.subComponents.add(new Button(this.show ? "Hide dialog" : "Show dialog", it -> {
            this.show = !this.show;
            this.window.getWindow().setVisible(this.show);
            if (!this.show) {
                this.selectedColor = null;
                this.updateBinds();
            }
            it.text = this.show ? "Hide dialog" : "Show dialog";
        }));
        if (this.selectedColor != null) {
            this.subComponents.add(new Button("Use selection as button", it -> {
                this.buttonColor = this.selectedColor;
            }));
            this.subComponents.add(new Button("Use selection as subButton", it -> {
                this.subButtonColor = this.selectedColor;
            }));
            this.subComponents.add(new Button("Use selection as text", it -> {
                this.textColor = this.selectedColor;
            }));
        }
    }

    private String themeString() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", String.valueOf(this.buttonColor));
        map.put("b", String.valueOf(this.subButtonColor));
        map.put("c", String.valueOf(this.textColor));
        return Tools.mapToString(map);
    }

    private void themeFromString(String s) {
        Map<String, String> map = Tools.stringToMap(s);
        try {
            this.buttonColor = Integer.parseInt(map.get("a"));
            this.subButtonColor = Integer.parseInt(map.get("b"));
            this.textColor = Integer.parseInt(map.get("c"));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onEnable() {
        ClickGUI.getInstance().customTheme = this;
        ClickGUI.getInstance().updateBinds();
    }

    @Override
    public void onDisable() {
        ClickGUI.getInstance().customTheme = null;
        ClickGUI.getInstance().updateBinds();
    }

    @Override
    public void onEveryTick() {
        if (this.show) {
            this.window.getWindow().setAutoRequestFocus(true);
            this.window.getWindow().setAlwaysOnTop(true);
            this.window.render((adaptedGraphics, graphics, bufferedImage) -> {
                adaptedGraphics.drawImage(0, 0, this.image);
                if (Mouse.isKeyDown(3)) {
                    if (!this.mouseWasDown) {
                        this.mouseWasDown = true;
                        ++this.imageID;
                        if (this.imageID >= this.images.length) {
                            this.imageID = 0;
                        }
                        this.image = this.images[this.imageID];
                    }
                } else {
                    this.mouseWasDown = false;
                }
                if (Mouse.isKeyDown(1)) {
                    this.updateBinds();
                    try {
                        Vector2i loc = this.window.getMousePos();
                        this.selectedColor = this.image.getRGB(loc.getX(), loc.getY());
                    }
                    catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                        // empty catch block
                    }
                }
            });
            this.window.prepareRender();
            this.window.doRender();
            this.window.swapBuffers();
        }
    }

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
        return false;
    }
}
