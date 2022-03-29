package tudbut.mod.client.ttcp.gui.lib.component;

import java.lang.reflect.Field;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.utils.Module;

public class ToggleButton
extends Component {
    String field;
    Module module;

    public ToggleButton(String s, Module module, String field) {
        this.text = s;
        this.module = module;
        this.field = field;
        this.update();
    }

    @Override
    public synchronized void update() {
        this.green = ToggleButton.field(this.module, this.field);
    }

    @Override
    public synchronized void click(int x, int y, int mouseButton) {
        super.click(x, y, mouseButton);
        ToggleButton.field(this.module, this.field, this.green);
    }

    private static Boolean field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return (Boolean)f.get(m);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void field(Module m, String s, Object o) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            f.set(m, o);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static interface ClickEvent {
        public void click(Button var1);
    }
}
