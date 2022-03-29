package tudbut.mod.client.ttcp.mods.chat;

import de.tudbut.tools.Tools;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Chat;
import tudbut.obj.Save;
import tudbut.obj.TLMap;

@Chat
public class Spam
extends Module {
    @Save
    public TLMap<String, Spammer> toSpam = new TLMap();
    public Spammer current;
    long last = 0L;

    @Override
    public void onTick() {
        NetworkPlayerInfo[] players = Objects.requireNonNull(TTCp.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        if (this.current != null && new Date().getTime() - this.last > (long)this.current.delay) {
            if (this.current.current >= this.current.toSpam.size()) {
                this.current.current = 0;
            }
            if (this.current.current >= this.current.toSpam.size()) {
                return;
            }
            String alphabet = "abcdefghijklmnopqrstuvwxyz";
            String pool = alphabet + alphabet.toUpperCase() + "0123456789     ,.-#+";
            String player = players[(int)((double)players.length * Math.random())].getGameProfile().getName();
            TTCp.player.sendChatMessage(this.current.toSpam.get(this.current.current++).replaceAll("%random10", Tools.randomString(10, pool)).replaceAll("%random20", Tools.randomString(20, pool)).replaceAll("%random30", Tools.randomString(30, pool)).replaceAll("%player", player));
            this.last = new Date().getTime();
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (args[0].equals("list")) {
            this.toSpam.set(args[1], new Spammer());
            ChatUtils.print("Done!");
        }
        if (args[0].equals("add")) {
            this.toSpam.set(args[1], new Spammer());
            ChatUtils.print("Done!");
        }
        if (args[0].equals("remove")) {
            this.toSpam.set(args[1], null);
            ChatUtils.print("Done!");
        }
        if (args[0].equals("set")) {
            this.current = this.toSpam.get(args[1]);
            ChatUtils.print("Done!");
        }
        if (args[0].equals("+")) {
            this.toSpam.get((String)args[1]).toSpam.add(s.substring(s.indexOf("+") + args[1].length() + 3));
            ChatUtils.print("Done!");
        }
        if (args[0].equals("delay")) {
            this.toSpam.get((String)args[1]).delay = (int)(Float.parseFloat(args[2]) * 1000.0f);
            ChatUtils.print("Done!");
        }
    }

    public static class Spammer {
        int delay = 5000;
        int current = 0;
        ArrayList<String> toSpam = new ArrayList();
    }
}
