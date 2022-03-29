package tudbut.mod.client.ttcp.gui.lib.component;

import java.lang.reflect.Field;
import java.util.function.Function;
import net.minecraft.client.gui.Gui;
import tudbut.mod.client.ttcp.gui.lib.GUIManager;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.utils.Module;

public class IntSlider
extends Component {
    public float f = 0.0f;
    String field;
    Module module;
    Function<Integer, String> sliderText;
    Function<Integer, Boolean> updateMethod;
    int mapper;
    int adder;

    public IntSlider(String s, Module module, String field, Function<Integer, String> text, int mapper, int adder, Function<Integer, Boolean> updateMethod) {
        this.green = true;
        this.text = s;
        this.module = module;
        this.field = field;
        this.sliderText = text;
        this.mapper = mapper;
        this.adder = adder;
        this.updateMethod = updateMethod;
        this.update();
    }

    public IntSlider(String s, Module module, String field, Function<Integer, String> text, int mapper, int adder) {
        this(s, module, field, text, mapper, adder, t -> true);
    }

    @Override
    public void draw(int x, int y) {
        Gui.drawRect((int)x, (int)(y + 13), (int)(x + 101), (int)(y + 14), (int)GUIManager.sliderBackground);
        Gui.drawRect((int)((int)Math.floor((float)x + this.f * 100.0f)), (int)(y + 11), (int)((int)Math.floor((float)x + this.f * 100.0f) + 1), (int)(y + 16), (int)GUIManager.sliderColor);
        this.fontRenderer.drawString(this.sliderText.apply(Math.round(this.f * (float)this.mapper + (float)this.adder)), x + 100 + 4, y + 10, GUIManager.sliderColor);
    }

    @Override
    public synchronized void update() {
        this.f = (float)(IntSlider.field(this.module, this.field) - this.adder) / (float)this.mapper;
    }

    @Override
    public synchronized void click(int x, int y, int mouseButton) {
        if (mouseButton == 0) {
            this.f = (float)Math.max(Math.min(x, 100), 0) / 100.0f;
        }
        IntSlider.field(this.module, this.field, Math.round(this.f * (float)this.mapper + (float)this.adder));
        if (!this.updateMethod.apply(Math.round(this.f * (float)this.mapper + (float)this.adder)).booleanValue()) {
            System.out.println("Something went wrong handling the sliders!");
            throw new RuntimeException();
        }
        this.f = (float)(IntSlider.field(this.module, this.field) - this.adder) / (float)this.mapper;
    }

    @Override
    public void move(int x, int y, int mouseButton) {
        this.click(x, y, mouseButton);
    }

    @Override
    protected int size() {
        return 20;
    }

    private static Integer field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return (Integer)f.get(m);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void field(Module m, String s, Integer o) {
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
