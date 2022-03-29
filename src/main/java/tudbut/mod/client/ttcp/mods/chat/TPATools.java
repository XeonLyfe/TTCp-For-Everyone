package tudbut.mod.client.ttcp.mods.chat;

import java.util.Objects;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;

@Chat
public class TPATools
extends Module {
    static TPATools instance;
    @Save
    public int delay = 1000;
    private boolean stop = false;

    public TPATools() {
        instance = this;
    }

    public static TPATools getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Send /tpa to everyone", text -> this.onChat("tpa", null)));
        this.subComponents.add(new Button("Send /tpahere to everyone", text -> this.onChat("tpahere", null)));
        this.subComponents.add(new Button("Delay: " + this.delay, it -> {
            this.delay = Keyboard.isKeyDown((int)42) ? (this.delay -= 1000) : (this.delay += 1000);
            if (this.delay > 5000) {
                this.delay = 1000;
            }
            if (this.delay < 1000) {
                this.delay = 5000;
            }
            it.text = "Delay: " + this.delay;
        }));
        this.subComponents.add(new Button("Stop", it -> {
            this.stop = true;
            TTCp.player.sendChatMessage("/tpacancel");
            ThreadManager.run(() -> {
                it.text = "Done";
                try {
                    Thread.sleep(2000 + this.delay);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.stop = false;
                it.text = "Stop";
            });
        }));
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onEverySubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
        if (s.equalsIgnoreCase("delay")) {
            this.delay = Integer.parseInt(args[1]);
            ChatUtils.print("Set!");
        }
        if (s.equalsIgnoreCase("tpa")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                    if (this.stop) {
                        return;
                    }
                    try {
                        TTCp.mc.player.sendChatMessage("/tpa " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                        Thread.sleep(TPATools.getInstance().delay);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                ChatUtils.print("Done!");
            });
        }
        if (s.equalsIgnoreCase("tpahere")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                    if (this.stop) {
                        return;
                    }
                    try {
                        TTCp.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                        Thread.sleep(TPATools.getInstance().delay);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                ChatUtils.print("Done!");
            });
        }
        this.updateBinds();
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }
}
