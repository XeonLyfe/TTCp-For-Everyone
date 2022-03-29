package tudbut.mod.client.ttcp.utils;

import java.io.OutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

final class ChatUtils$2
extends OutputStream {
    String s = "";
    final int val$delay;

    ChatUtils$2(int n) {
        this.val$delay = n;
    }

    @Override
    public void write(int i) {
        if ((char)i == '\n') {
            try {
                Thread.sleep(this.val$delay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(this.s));
            this.s = "";
        } else {
            this.s = this.s + (char)i;
        }
    }
}
