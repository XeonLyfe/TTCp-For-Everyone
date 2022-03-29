package tudbut.mod.client.ttcp.mods.chat;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;

@Chat
public class ChatSuffix
extends Module {
    @Save
    String suffix = "";
    @Save
    int mode = 0;
    @Save
    public int chance = 100;
    private static ChatSuffix instance;

    public ChatSuffix() {
        instance = this;
    }

    public static ChatSuffix getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        boolean b = this.enabled;
        this.enabled = true;
        this.subComponents.add(new Button("Chance: " + this.chance + "%", it -> {
            this.chance = Keyboard.isKeyDown((int)42) ? (this.chance -= 5) : (this.chance += 5);
            if (this.chance > 100) {
                this.chance = 0;
            }
            if (this.chance < 0) {
                this.chance = 100;
            }
            it.text = "Chance: " + this.chance + "%";
        }));
        this.subComponents.add(new Button("Mode:" + (this.mode == -1 ? " CUSTOM" : this.get(100)), it -> {
            this.mode = Keyboard.isKeyDown((int)42) ? --this.mode : ++this.mode;
            if (this.mode > 9) {
                this.mode = 0;
            }
            if (this.mode < 0) {
                this.mode = 9;
            }
            it.text = "Mode:" + this.get(100);
        }));
        this.enabled = b;
    }

    public String get(int chance) {
        if (!this.enabled) {
            return "";
        }
        if (Math.random() < (double)chance / 100.0) {
            if (this.mode == -1) {
                return " " + this.suffix;
            }
            switch (this.mode) {
                case 0: {
                    return " ›TTCp‹";
                }
                case 1: {
                    return " »TTCp«";
                }
                case 2: {
                    return " ‹TTCp›";
                }
                case 3: {
                    return " «TTCp»";
                }
                case 4: {
                    return " | TTCp";
                }
                case 5: {
                    return " → TTCp";
                }
                case 6: {
                    return " ᴛᴛᴄ";
                }
                case 7: {
                    return " ᴛᴛᴄᴘ";
                }
                case 8: {
                    return " ᴛᴛᴄ ᴏɴ ᴛᴏᴘ";
                }
                case 9: {
                    return " ᴛᴛᴄᴘ ᴏɴ ᴛᴏᴘ";
                }
            }
        }
        return "";
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
        this.suffix = s;
        this.mode = -1;
        ChatUtils.print("Done!");
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 2;
    }
}
