package tudbut.mod.client.ttcp.gui.lib;

import java.util.ArrayList;
import org.lwjgl.util.Rectangle;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.obj.TLMap;

public class GUIManager {
    public static int fontColorOn = -16711936;
    public static int fontColorOff = -65536;
    public static int frameColor = -1;
    public static int frameBackground = -1610612736;
    public static int sliderBackground = -8355712;
    public static int sliderColor = -1;
    public static TLMap<Rectangle, Component> renderedComponents = new TLMap();
    static Component dragging = null;

    public static synchronized void click(int mouseX, int mouseY, int mouseButton) {
        dragging = null;
        ArrayList<TLMap.Entry<Rectangle, Component>> entries = renderedComponents.entries();
        int entriesSize = entries.size();
        for (int i = 0; i < entriesSize; ++i) {
            TLMap.Entry<Rectangle, Component> entry = entries.get(i);
            if (mouseX < ((Rectangle)entry.key).getX() || mouseY < ((Rectangle)entry.key).getY() || mouseX > ((Rectangle)entry.key).getWidth() || mouseY > ((Rectangle)entry.key).getHeight()) continue;
            ((Component)entry.val).click(mouseX - ((Component)entry.val).loc.getX(), mouseY - ((Component)entry.val).loc.getY(), mouseButton);
            return;
        }
    }

    public static synchronized void move(int mouseX, int mouseY, int mouseButton) {
        if (dragging == null) {
            ArrayList<TLMap.Entry<Rectangle, Component>> entries = renderedComponents.entries();
            int entriesSize = entries.size();
            for (int i = 0; i < entriesSize; ++i) {
                TLMap.Entry<Rectangle, Component> entry = entries.get(i);
                if (mouseX < ((Rectangle)entry.key).getX() || mouseY < ((Rectangle)entry.key).getY() || mouseX > ((Rectangle)entry.key).getWidth() || mouseY > ((Rectangle)entry.key).getHeight()) continue;
                dragging = (Component)entry.val;
                break;
            }
        }
        if (dragging != null) {
            dragging.move(mouseX - GUIManager.dragging.loc.getX(), mouseY - GUIManager.dragging.loc.getY(), mouseButton);
        }
    }
}
