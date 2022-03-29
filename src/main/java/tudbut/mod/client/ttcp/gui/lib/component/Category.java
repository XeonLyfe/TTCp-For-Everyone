package tudbut.mod.client.ttcp.gui.lib.component;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.Gui;
import org.lwjgl.util.Point;
import org.lwjgl.util.Rectangle;
import tudbut.mod.client.ttcp.gui.lib.GUIManager;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.obj.Transient;

public class Category
extends Component {
    public Point location;
    @Transient
    int clickX;
    @Transient
    int clickY;

    public Category() {
        this.green = true;
        this.clickX = 0;
        this.clickY = 0;
    }

    public void render() {
        this.render(this.location.getX(), new AtomicInteger(this.location.getY()), -1, false, 0);
    }

    @Override
    public void render(int x, AtomicInteger y, int sub, boolean isLastInList, int yLineSize) {
        this.loc = new Point(x + 8 + sub * 8, y.get());
        GUIManager.renderedComponents.set(new Rectangle(x + sub * 8, y.get(), x + (200 - sub * 8), y.get() + this.size()), this);
        int width = this.fontRenderer.getStringWidth(this.text);
        Gui.drawRect((int)(x + 2), (int)(y.get() + 4), (int)(x + 200), (int)(y.get() + this.subSizes() + this.size()), (int)GUIManager.frameBackground);
        Gui.drawRect((int)(x + 200), (int)(y.get() + 4), (int)(x + 200 - 1), (int)(y.get() + this.subSizes() + this.size()), (int)GUIManager.frameColor);
        Gui.drawRect((int)(x + width), (int)(y.get() + 4), (int)(x + 200), (int)(y.get() + 4 + 1), (int)GUIManager.frameColor);
        this.fontRenderer.drawString(this.text, x, y.get(), this.green ? GUIManager.fontColorOn : GUIManager.fontColorOff);
        y.addAndGet(this.size());
        if (this.subComponentsShown) {
            for (int i = 0; i < this.subComponents.size(); ++i) {
                Component component = (Component)this.subComponents.get(i);
                component.render(x, y, 0, false, component.size());
            }
        }
        Gui.drawRect((int)(x + 2), (int)y.get(), (int)(x + 200), (int)(y.get() - 1), (int)GUIManager.frameColor);
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        if (mouseButton == 0) {
            this.subComponentsShown = !this.subComponentsShown;
        }
        this.clickX = x;
        this.clickY = y;
    }

    @Override
    public void move(int x, int y, int mouseButton) {
        if (mouseButton == 1) {
            this.location.setX(this.location.getX() + x - this.clickX);
            this.location.setY(this.location.getY() + y - this.clickY);
            this.loc = this.location;
        }
    }
}
