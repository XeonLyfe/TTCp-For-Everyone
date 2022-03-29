package tudbut.mod.client.ttcp.gui.lib.component;

import java.lang.reflect.Field;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.utils.Module;

public class EnumButton
extends Component {
    String field;
    Module module;
    Class<? extends Enum<?>> enumType;
    Enum<?>[] enums;

    public EnumButton(Class<? extends Enum<?>> enumType, String s, Module module, String field) {
        this.green = true;
        this.enumType = enumType;
        this.enums = enumType.getEnumConstants();
        this.text = s;
        this.module = module;
        this.field = field;
        for (int i = 0; i < this.enums.length; ++i) {
            int finalI = i;
            Button button = new Button(this.enums[i].toString(), it -> {
                this.field(module, field, finalI);
                for (Component component : this.subComponents) {
                    component.green = false;
                }
                it.green = true;
            });
            this.subComponents.add(button);
            button.green = this.field(module, field) == this.enums[i].ordinal();
        }
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        super.click(x, y, mouseButton);
        this.green = true;
    }

    private int field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return ((Enum)f.get(m)).ordinal();
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void field(Module m, String s, int o) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            f.set(m, this.enumType.getEnumConstants()[o]);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
