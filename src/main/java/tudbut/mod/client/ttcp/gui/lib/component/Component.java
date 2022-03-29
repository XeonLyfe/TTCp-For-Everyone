package tudbut.mod.client.ttcp.gui.lib.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;
import tudbut.mod.client.ttcp.gui.lib.GUIManager;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.obj.Save;

public abstract class Component {
    public Point loc;
    FontRenderer fontRenderer;
    public ArrayList<Component> subComponents;
    public String text;
    public boolean green;
    @Save
    public boolean subComponentsShown;
    public boolean displayConfirmation;
    private final Button[] confirmationButtons;

    public Component() {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.subComponents = new ArrayList();
        this.text = "";
        this.green = false;
        this.subComponentsShown = false;
        this.displayConfirmation = false;
        this.confirmationButtons = new Button[3];
        if (!(this instanceof Button)) {
            this.confirmationButtons[0] = new Button("Are you sure?", it -> {});
            this.confirmationButtons[1] = new Button("Yes", it -> {
                this.displayConfirmation = false;
                this.onConfirm(true);
            });
            this.confirmationButtons[2] = new Button("No", it -> {
                this.displayConfirmation = false;
                this.onConfirm(false);
            });
        }
    }

    public void render(int x, AtomicInteger y, int sub, boolean isLastInList, int yLineSize) {
        this.loc = new Point(x + 8 + sub * 8, y.get());
        GUIManager.renderedComponents.set(new Rectangle(x + sub * 8, y.get(), x + (200 - sub * 8), y.get() + this.size()), this);
        if (isLastInList) {
            Gui.drawRect((int)(x + 2 + sub * 8), (int)y.get(), (int)(x + 2 + sub * 8 + 1), (int)(y.get() + 4), (int)GUIManager.frameColor);
        } else {
            Gui.drawRect((int)(x + 2 + sub * 8), (int)y.get(), (int)(x + 2 + sub * 8 + 1), (int)(y.get() + yLineSize), (int)GUIManager.frameColor);
        }
        Gui.drawRect((int)(x + 2 + sub * 8), (int)y.get(), (int)(x + 2 + sub * 8 + 1), (int)(y.get() + this.subSizes() + (isLastInList ? 5 : this.size())), (int)GUIManager.frameColor);
        Gui.drawRect((int)(x + 2 + sub * 8), (int)(y.get() + 4), (int)(x + 5 + sub * 8 + 1), (int)(y.get() + 4 + 1), (int)GUIManager.frameColor);
        this.fontRenderer.drawString(this.text, x + 8 + sub * 8, y.get(), this.green ? GUIManager.fontColorOn : GUIManager.fontColorOff);
        this.draw(x + 8 + sub * 8, y.get());
        y.addAndGet(this.size());
        if (this.subComponentsShown) {
            List<Component> subComponents = this.subComponents;
            if (this.displayConfirmation) {
                subComponents = Arrays.asList(this.confirmationButtons);
            }
            for (int i = 0; i < subComponents.size(); ++i) {
                Component component = subComponents.get(i);
                component.render(x, y, sub + 1, i == subComponents.size() - 1, i == subComponents.size() - 1 && isLastInList && component.subComponents.size() == 0 ? 4 : component.size());
            }
        }
    }

    public void draw(int x, int y) {
    }

    protected int subSizes() {
        int size = 0;
        if (this.subComponentsShown) {
            if (this.displayConfirmation) {
                return 30;
            }
            for (int i = 0; i < this.subComponents.size(); ++i) {
                size += this.subComponents.get(i).size() + this.subComponents.get(i).subSizes();
            }
        }
        return size;
    }

    protected int size() {
        return 10;
    }

    public void update() {
    }

    public void click(int x, int y, int mouseButton) {
        if (mouseButton == 0) {
            boolean bl = this.green = !this.green;
        }
        if (mouseButton == 2) {
            this.subComponentsShown = !this.subComponentsShown;
        }
    }

    public void move(int x, int y, int mouseButton) {
    }

    public void onConfirm(boolean result) {
    }
}
