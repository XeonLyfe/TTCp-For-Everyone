package tudbut.mod.client.ttcp.gui;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Point;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.GUIManager;
import tudbut.mod.client.ttcp.gui.lib.component.Category;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.mods.rendering.ClickGUI;
import tudbut.mod.client.ttcp.utils.Module;

public class GuiRewrite
extends GuiScreen {
    private int cx;
    private int cy;
    private Category[] categories = new Category[0];

    public GuiRewrite() {
        this.mc = TTCp.mc;
        ClickGUI clickGUI = TTCp.getModule(ClickGUI.class);
        if (!clickGUI.enabled) {
            clickGUI.toggle();
        }
        this.createComponents();
    }

    public boolean doesGuiPauseGame() {
        return this.mc.player.timeInPortal != 0.0f;
    }

    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
    }

    private void createComponents() {
        int i;
        ArrayList<Category> categories = new ArrayList<Category>();
        int y = 10;
        TLMap<Class<? extends Annotation>, 1> map = new TLMap<Class<? extends Annotation>, 1>();
        for (i = 0; i < TTCp.modules.length; ++i) {
            final Module module = TTCp.modules[i];
            if (!module.displayOnClickGUI()) continue;
            Category category = (Category)map.get(module.category);
            if (category == null) {
                if (category == null) {
                    category = new Category(){
                        {
                            this.text = module.category.getSimpleName();
                        }
                    };
                    map.set(module.category, category);
                }
                if (category.location == null) {
                    category.location = new Point(10, y);
                    y += 20;
                }
                categories.add(category);
                category.subComponents.clear();
            }
            category.subComponents.add(module);
        }
        for (i = 0; i < categories.size(); ++i) {
            Category category = (Category)categories.get(i);
            Point p = TTCp.categories.get(category.text);
            Boolean b = TTCp.categoryShow.get(category.text);
            if (p == null) {
                TTCp.categories.set(category.text, category.location);
            } else {
                category.location = p;
            }
            if (b == null) {
                TTCp.categoryShow.set(category.text, new Boolean(category.subComponentsShown));
                continue;
            }
            category.subComponentsShown = b;
        }
        System.out.println(categories);
        this.categories = categories.toArray(new Category[0]);
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        System.out.println("Gui closed by ESC");
        ClickGUI.getInstance().enabled = false;
        for (Category category : this.categories) {
            TTCp.categoryShow.set(category.text, new Boolean(category.subComponentsShown));
        }
    }

    public void updateScreen() {
        for (Component value : GUIManager.renderedComponents.values()) {
            value.update();
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.cx = mouseX;
        this.cy = mouseY;
        GUIManager.click(mouseX, mouseY, mouseButton);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        this.cx = mouseX;
        this.cy = mouseY;
        GUIManager.move(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.cx = mouseX;
        this.cy = mouseY;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int m;
        this.cx = mouseX;
        this.cy = mouseY;
        GUIManager.renderedComponents = new TLMap();
        for (int i = 0; i < this.categories.length; ++i) {
            if (this.categories[i].location.getY() < -10000) {
                this.categories[i].location.setY(this.categories[i].location.getY() + 10000);
            }
            if (this.categories[i].location.getY() > 10000) {
                this.categories[i].location.setY(this.categories[i].location.getY() - 10000);
            }
            this.categories[i].render();
        }
        if (ClickGUI.getInstance().mouseFix) {
            GuiRewrite.func_73734_a((int)(mouseX - 2), (int)(mouseY - 2), (int)(mouseX + 2), (int)(mouseY + 2), (int)-1);
        }
        if ((m = Mouse.getDWheel()) != 0) {
            for (int i = 0; i < this.categories.length; ++i) {
                this.categories[i].location.setY(this.categories[i].location.getY() + m);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
