package tudbut.mod.client.ttcp.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiTTC;
import tudbut.mod.client.ttcp.gui.lib.component.Component;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Category;
import tudbut.obj.Save;
import tudbut.obj.TLMap;

public abstract class Module
extends Component {
    private static int cIndex = 0;
    public int index;
    protected static Minecraft mc = Minecraft.getMinecraft();
    public EntityPlayerSP player = null;
    @Save
    public boolean enabled = this.defaultEnabled();
    @Save
    public boolean clickGuiShow = false;
    @Save
    public Integer clickGuiX;
    @Save
    public Integer clickGuiY;
    @Save
    public KeyBind key = new KeyBind(null, this.toString() + "::toggle", true);
    public ArrayList<GuiTTC.Button> subButtons = new ArrayList();
    public Class<? extends Annotation> category;
    @Save
    public TLMap<String, KeyBind> customKeyBinds;
    private GuiTTC.Button[] confirmationButtons;
    Component keyButton;

    public Module() {
        MinecraftForge.EVENT_BUS.register((Object)this);
        this.customKeyBinds = new TLMap();
        this.confirmationButtons = new GuiTTC.Button[3];
        this.confirmationButtons[0] = new GuiTTC.Button("Are you sure?", text -> {});
        this.confirmationButtons[1] = new GuiTTC.Button("Yes", text -> {
            this.displayConfirmation = false;
            this.onConfirm(true);
        });
        this.confirmationButtons[2] = new GuiTTC.Button("No", text -> {
            this.displayConfirmation = false;
            this.onConfirm(false);
        });
        this.keyButton = Setting.createKey("KeyBind", this.key);
        this.index = cIndex++;
        this.text = this.toString();
        for (Annotation annotation : this.getClass().getDeclaredAnnotations()) {
            if (annotation.annotationType().getDeclaredAnnotation(Category.class) == null) continue;
            this.category = annotation.annotationType();
        }
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);
        if (!this.subComponents.contains(this.keyButton)) {
            this.keyButton = Setting.createKey("KeyBind", this.key);
            this.subComponents.add(this.keyButton);
        }
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        super.click(x, y, mouseButton);
        if (mouseButton == 0) {
            this.toggle();
        }
    }

    public void updateBindsFull() {
        this.green = this.enabled;
        this.updateBinds();
        this.text = this.toString();
    }

    public void updateBinds() {
    }

    public void toggle() {
        this.green = this.enabled = !this.enabled;
        if (this.enabled) {
            this.onEnable();
            ChatUtils.printChatAndNotification("§a" + this.toString() + " ON", 8000);
        } else {
            this.onDisable();
            ChatUtils.printChatAndNotification("§c" + this.toString() + " OFF", 8000);
        }
    }

    public boolean defaultEnabled() {
        return false;
    }

    public boolean doStoreEnabled() {
        return true;
    }

    public boolean displayOnClickGUI() {
        return true;
    }

    public void onSubTick() {
    }

    public void onEverySubTick() {
    }

    public void onTick() {
    }

    public void onEveryTick() {
    }

    public void onChat(String s, String[] args) {
    }

    public void onEveryChat(String s, String[] args) {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public boolean onServerChat(String s, String formatted) {
        return false;
    }

    public void onConfigLoad() {
    }

    public void onConfigSave() {
    }

    public void init() {
    }

    public int danger() {
        return 0;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    private Module get() {
        return this;
    }

    public boolean onPacket(Packet<?> packet) {
        return false;
    }

    public static class KeyBind {
        public Integer key = null;
        public boolean down = false;
        public String onPress;
        public boolean alwaysOn;

        public KeyBind() {
        }

        public KeyBind(Integer key, String onPress, boolean alwaysOn) {
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
}
