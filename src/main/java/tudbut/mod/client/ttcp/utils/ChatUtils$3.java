package tudbut.mod.client.ttcp.utils;

import java.io.OutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import tudbut.mod.client.ttcp.mods.misc.Debug;

final class ChatUtils$3
extends OutputStream {
    String s = "";

    ChatUtils$3() {
    }

    @Override
    public void write(int i) {
        if ((char)i == '\n') {
            if (Debug.getInstance().enabled) {
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(this.s));
            }
            System.out.println(this.s);
            this.s = "";
        } else {
            this.s = this.s + (char)i;
        }
    }
}
