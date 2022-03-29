package tudbut.mod.client.ttcp.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.ForgeEventFactory;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.misc.Debug;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;

public class ChatUtils {
    public static void print(String s) {
        if (TTCp.isIngame()) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(s));
        } else {
            TTCp.logger.info(s.replaceAll("§[a-z0-9]", ""));
        }
    }

    public static void printChatAndHotbar(String s) {
        ChatUtils.print(s);
        ChatUtils.printHotbar(s);
    }

    public static void printChatAndTitle(String s, int ms) {
        ChatUtils.print(s);
        ChatUtils.printTitle(s, "", ms);
    }

    public static void printChatAndNotification(String s, int ms) {
        ChatUtils.print(s);
        Notifications.add(new Notifications.Notification(s, ms));
    }

    public static void printTitle(String title, String subTitle, int ms) {
        Minecraft.getMinecraft().ingameGUI.displayTitle("§c" + title, null, 2, ms / 50, 2);
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, "§b" + subTitle, 2, ms / 50, 2);
    }

    public static void printHotbar(String s) {
        Minecraft.getMinecraft().ingameGUI.setOverlayMessage((ITextComponent)new TextComponentString(s), true);
    }

    public static void history(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
    }

    public static OutputStream chatOut() {
        return new OutputStream(){
            String s = "";

            @Override
            public void write(int i) {
                if ((char)i == '\n') {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage((ITextComponent)new TextComponentString(this.s));
                    this.s = "";
                } else {
                    this.s = this.s + (char)i;
                }
            }
        };
    }

    public static OutputStream chatOut(final int delay) {
        return new OutputStream(){
            String s = "";

            @Override
            public void write(int i) {
                if ((char)i == '\n') {
                    try {
                        Thread.sleep(delay);
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
        };
    }

    public static PrintStream chatPrinter() {
        return new PrintStream(ChatUtils.chatOut());
    }

    public static PrintStream chatPrinter(int delay) {
        return new PrintStream(ChatUtils.chatOut(delay));
    }

    public static OutputStream chatOutDebug() {
        return new OutputStream(){
            String s = "";

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
        };
    }

    public static PrintStream chatPrinterDebug() {
        return new PrintStream(ChatUtils.chatOutDebug());
    }

    public static void simulateSend(String msg, boolean addToHistory) {
        if ((msg = ForgeEventFactory.onClientSendMessage((String)msg)).isEmpty()) {
            return;
        }
        if (addToHistory) {
            TTCp.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
        }
        if (ClientCommandHandler.instance.func_71556_a((ICommandSender)TTCp.mc.player, msg) != 0) {
            return;
        }
        TTCp.mc.player.sendChatMessage(msg);
    }
}
