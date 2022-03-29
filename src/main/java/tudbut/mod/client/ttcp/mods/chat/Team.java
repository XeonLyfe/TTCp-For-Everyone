package tudbut.mod.client.ttcp.mods.chat;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.chat.TPATools;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;

@Chat
public class Team
extends Module {
    static Team instance;
    @Save
    public ArrayList<String> names = new ArrayList();
    @Save
    private boolean tpa = true;
    @Save
    private boolean tpaHere = false;

    public Team() {
        instance = this;
    }

    public static Team getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Accept /tpa: " + this.tpa, it -> {
            this.tpa = !this.tpa;
            it.text = "Accept /tpa: " + this.tpa;
        }));
        this.subComponents.add(new Button("Accept /tpahere: " + this.tpaHere, it -> {
            this.tpaHere = !this.tpaHere;
            it.text = "Accept /tpahere: " + this.tpaHere;
        }));
        this.subComponents.add(new Button("Send /tpahere (/tpahere)", text -> this.onChat("", new String[]{"tpahere"})));
        this.subComponents.add(new Button("Send /tpahere (/tpa)", text -> this.onChat("", new String[]{"here"})));
        this.subComponents.add(new Button("Send DM", text -> ChatUtils.print("§c§lUse " + TTCp.prefix + "team dm <message>")));
        this.subComponents.add(new Button("Show list", text -> this.onChat("", new String[]{"list"})));
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onChat(String s, String[] args) {
        switch (args[0].toLowerCase()) {
            case "add": {
                this.names.remove(args[1]);
                this.names.add(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "remove": {
                this.names.remove(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "settpa": {
                this.tpa = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "settpahere": {
                this.tpaHere = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            }
            case "tpahere": {
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if (!this.names.contains(info.getGameProfile().getName())) continue;
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
                break;
            }
            case "here": {
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if (!this.names.contains(info.getGameProfile().getName())) continue;
                        try {
                            TTCp.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " TTCp[0]");
                            ChatUtils.print("Sent to " + info.getGameProfile().getName());
                            Thread.sleep(TPATools.getInstance().delay);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            }
            case "go": {
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if (!info.getGameProfile().getName().equals(args[1])) continue;
                        try {
                            TTCp.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " TTCp[1]");
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    ChatUtils.print("Sent!");
                });
                break;
            }
            case "dm": {
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if (!this.names.contains(info.getGameProfile().getName())) continue;
                        try {
                            TTCp.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s.substring("dm ".length()));
                            ChatUtils.print("Sent to " + info.getGameProfile().getName());
                            Thread.sleep(TPATools.getInstance().delay);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            }
            case "settings": {
                ChatUtils.print("TPA: " + (this.tpa ? "enabled" : "disabled"));
                ChatUtils.print("TPAhere: " + (this.tpaHere ? "enabled" : "disabled"));
            }
            case "list": {
                StringBuilder toPrint = new StringBuilder("Team members: ");
                for (String name : this.names) {
                    toPrint.append(name).append(", ");
                }
                if (this.names.size() >= 1) {
                    toPrint.delete(toPrint.length() - 2, toPrint.length() - 1);
                }
                ChatUtils.print(toPrint.toString());
            }
        }
        this.updateBinds();
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        if (this.tpa && s.contains("has requested to teleport to you.") && this.names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTCp.player.sendChatMessage("/tpaccept");
        }
        if (this.tpaHere && s.contains("has requested that you teleport to them.") && this.names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTCp.player.sendChatMessage("/tpaccept");
        }
        try {
            String name2 = (String)this.names.stream().filter(theName -> s.startsWith(theName + " whispers:") || s.startsWith("~" + theName + " whispers:") || s.startsWith(theName + " whispers to you:") || s.startsWith("~" + theName + " whispers to you:") || s.startsWith("From " + theName + ":") || s.startsWith("From ~" + theName + ":")).iterator().next();
            if (name2 != null) {
                String msg = s.split(": ")[1];
                if (msg.startsWith("TTCp")) {
                    if (msg.equals("TTCp[0]") && this.tpaHere) {
                        TTCp.player.sendChatMessage("/tpa " + name2);
                        ChatUtils.print("Sent TPA to " + name2 + ".");
                    }
                    if (msg.equals("TTCp[1]") && this.tpa) {
                        TTCp.player.sendChatMessage("/tpahere " + name2);
                        ChatUtils.print("Sent TPAHere to " + name2 + ".");
                    }
                    if (msg.equals("TTCp[3]")) {
                        ChatUtils.print("§c§lYou have been removed from the Team of " + name2 + "! \n§cRun ,team remove " + name2 + " to remove them as well!");
                    }
                    return true;
                }
                ChatUtils.print("§b§lDM from team member: §r<" + name2 + "> " + s.substring(s.indexOf(": ") + 2));
                return true;
            }
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                String theName2 = info.getGameProfile().getName();
                if (!s.startsWith(theName2 + " whispers:") && !s.startsWith("~" + theName2 + " whispers:") && !s.startsWith(theName2 + " whispers to you:") && !s.startsWith("~" + theName2 + " whispers to you:") && !s.startsWith("From " + theName2 + ":") && !s.startsWith("From ~" + theName2 + ":")) continue;
                try {
                    String msg = s.split(": ")[1];
                    if (!msg.startsWith("TTCp")) continue;
                    if (msg.equals("TTCp[2]")) {
                        ChatUtils.print("§c§lYou have been added to the Team of " + theName2 + "! \n§cRun ,team add " + theName2 + " to add them as well!");
                    }
                    return true;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }
}
