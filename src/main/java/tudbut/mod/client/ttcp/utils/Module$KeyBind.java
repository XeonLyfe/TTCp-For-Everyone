package tudbut.mod.client.ttcp.utils;

import java.lang.reflect.InvocationTargetException;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;

public class Module$KeyBind {
    public Integer key = null;
    public boolean down = false;
    public String onPress;
    public boolean alwaysOn;

    public Module$KeyBind() {
    }

    public Module$KeyBind(Integer key, String onPress, boolean alwaysOn) {
        this.key = key;
        this.onPress = onPress;
        this.alwaysOn = alwaysOn;
    }

    public void onTick() {
        if (this.key != null && TTCp.mc.currentScreen == null) {
            if (Keyboard.isKeyDown((int)this.key)) {
                if (!this.down) {
                    this.down = true;
                    if (this.onPress != null) {
                        try {
                            Object m = TTCp.getModule(this.onPress.split("::")[0]);
                            m.getClass().getMethod(this.onPress.split("::")[1], new Class[0]).invoke(m, new Object[0]);
                        }
                        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                this.down = false;
            }
        } else {
            this.down = false;
        }
    }
}
