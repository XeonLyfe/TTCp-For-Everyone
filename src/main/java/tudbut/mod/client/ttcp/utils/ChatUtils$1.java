package tudbut.mod.client.ttcp.utils;

import java.io.OutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

final class ChatUtils$1
extends OutputStream {
    String s = "";

    ChatUtils$1() {
    }

    @Override
    public void write(int i) {
        if ((char)i == '\n') {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(this.s));
            this.s = "";
        } else {
            this.s = this.s + (char)i;
        }
    }
}
