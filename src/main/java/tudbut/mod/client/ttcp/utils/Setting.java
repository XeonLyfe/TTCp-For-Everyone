package tudbut.mod.client.ttcp.utils;

import java.lang.reflect.Field;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.gui.lib.component.EnumButton;
import tudbut.mod.client.ttcp.gui.lib.component.IntSlider;
import tudbut.mod.client.ttcp.gui.lib.component.Slider;
import tudbut.mod.client.ttcp.gui.lib.component.ToggleButton;
import tudbut.mod.client.ttcp.utils.Module;

public class Setting {
    public static Component createInt(int min, int max, String string, Module module, String field, Runnable onClick) {
        return new IntSlider(string, module, field, String::valueOf, max - min, min, it -> {
            onClick.run();
            return true;
        });
    }

    public static Component createEnum(Class<? extends Enum<?>> theEnum, String string, Module module, String field, Runnable onClick) {
        return new EnumButton(theEnum, string, module, field);
    }

    public static Component createInt(int min, int max, String string, Module module, String field) {
        return Setting.createInt(min, max, string, module, field, () -> {});
    }

    public static Component createEnum(Class<? extends Enum<?>> theEnum, String string, Module module, String field) {
        return Setting.createEnum(theEnum, string, module, field, () -> {});
    }

    public static Component createFloat(float min, float max, String string, Module module, String field) {
        return new Slider(string, module, field, it -> String.valueOf((float)Math.round(it.floatValue() * 100.0f) / 100.0f), max - min, min, it -> true);
    }

    public static Component createBoolean(String string, Module module, String field) {
        return new ToggleButton(string, module, field);
    }

    public static Component createKey(String string, Module.KeyBind keyBind) {
        return new Button(string + ": " + (keyBind.key == null ? "NONE" : Keyboard.getKeyName((int)keyBind.key)), it -> {
            int i = Setting.getKeyPress();
            if (i != -1) {
                keyBind.key = i;
                it.text = string + ": " + Keyboard.getKeyName((int)keyBind.key);
            } else {
                keyBind.key = null;
                it.text = string + ": " + "NONE (Hold)";
                new Thread(() -> {
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    it.text = string + ": " + "NONE";
                }).start();
            }
        });
    }

    private static int getKeyPress() {
        for (int i = 0; i < 256; ++i) {
            if (!Keyboard.isKeyDown((int)i)) continue;
            return i;
        }
        return -1;
    }

    private static Object field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return f.get(m);
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
}
