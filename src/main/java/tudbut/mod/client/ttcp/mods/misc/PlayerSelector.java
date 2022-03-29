package tudbut.mod.client.ttcp.mods.misc;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.tools.ArrayTools;

@Misc
public class PlayerSelector
extends Module {
    static Minecraft mc = Minecraft.getMinecraft();
    static int selected = 0;
    static boolean downDown = false;
    static boolean upDown = false;
    static boolean rightDown = false;
    static boolean leftDown = false;
    static boolean enterDown = false;
    static int selectedType = -1;
    public static boolean displayInRangeOnly = true;
    static NetworkPlayerInfo[] playersLastTick = new NetworkPlayerInfo[0];
    public static ArrayList<Type> types = new ArrayList();

    public static void render() {
        int j;
        boolean b;
        NetworkPlayerInfo player2;
        int i;
        NetworkPlayerInfo[] players;
        ScaledResolution resolution = new ScaledResolution(mc);
        if (displayInRangeOnly) {
            try {
                players = ArrayTools.getFromArray(PlayerSelector.mc.world.field_73010_i.toArray(new EntityPlayer[0]), player -> PlayerSelector.mc.player.connection.getPlayerInfo(player.func_110124_au()), new NetworkPlayerInfo[0]);
            }
            catch (Throwable ignored) {
                players = new NetworkPlayerInfo[]{};
            }
        } else {
            players = PlayerSelector.mc.player.connection.getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        }
        int x = resolution.getScaledWidth() / 6;
        int y = (int)((double)resolution.getScaledHeight() / 2.5 - (double)(Math.min(players.length * 10, 10) / 2));
        for (i = 0; i < players.length; ++i) {
            player2 = players[i];
            if (player2 == null) continue;
            b = false;
            for (j = 0; j < playersLastTick.length; ++j) {
                if (playersLastTick[j] == null || !playersLastTick[j].getGameProfile().getId().equals(player2.getGameProfile().getId())) continue;
                b = true;
                break;
            }
            if (b || selected < i) continue;
            ++selected;
        }
        for (i = 0; i < playersLastTick.length; ++i) {
            player2 = playersLastTick[i];
            if (player2 == null) continue;
            b = false;
            for (j = 0; j < players.length; ++j) {
                if (players[j] == null || !players[j].getGameProfile().getId().equals(player2.getGameProfile().getId())) continue;
                b = true;
                break;
            }
            if (b) continue;
            if (selected == i) {
                selectedType = -1;
            }
            if (selected <= i) continue;
            --selected;
        }
        Type[] types = PlayerSelector.types.toArray(new Type[0]);
        if (PlayerSelector.mc.currentScreen != null) {
            selectedType = -1;
        }
        if (selected >= players.length) {
            selected = players.length - 1;
        }
        if (selected < 0) {
            selected = 0;
        }
        if (selectedType >= types.length) {
            selectedType = types.length - 1;
        }
        if (selectedType == -1) {
            for (int i2 = Math.max(0, selected - 5); i2 < Math.min(selected + 5, players.length); ++i2) {
                if (players[i2] == null) continue;
                PlayerSelector.mc.fontRenderer.drawString((selected == i2 ? "§m| §f " : "| §f ") + (players[i2].getDisplayName() != null ? Objects.requireNonNull(players[i2].getDisplayName()).getUnformattedText() : ScorePlayerTeam.formatPlayerName((Team)players[i2].getPlayerTeam(), (String)players[i2].getGameProfile().getName())) + (selected == i2 ? "§r§c > " : ""), x, y, selected == i2 ? 0xFF0000 : 65280);
                y += 10;
            }
        } else {
            for (int i3 = 0; i3 < types.length; ++i3) {
                PlayerSelector.mc.fontRenderer.drawString((selectedType == i3 ? "< §m| §f " : "< | §f ") + types[i3].displayName, x, y, selectedType == i3 ? 0xFF0000 : 65280);
                y += 10;
            }
            if (Keyboard.isKeyDown((int)28) && PlayerSelector.mc.currentScreen == null) {
                if (!enterDown) {
                    types[PlayerSelector.selectedType].callback.run(players[selected]);
                    selectedType = -1;
                }
                enterDown = true;
            } else {
                enterDown = false;
            }
        }
        if (Keyboard.isKeyDown((int)200) && PlayerSelector.mc.currentScreen == null) {
            if (!upDown) {
                if (selectedType == -1) {
                    --selected;
                } else if (selectedType != 0) {
                    --selectedType;
                }
            }
            upDown = true;
        } else {
            upDown = false;
        }
        if (Keyboard.isKeyDown((int)208) && PlayerSelector.mc.currentScreen == null) {
            if (!downDown) {
                if (selectedType == -1) {
                    ++selected;
                } else {
                    ++selectedType;
                }
            }
            downDown = true;
        } else {
            downDown = false;
        }
        if (selected >= players.length) {
            selected = players.length - 1;
        }
        if (selected < 0) {
            selected = 0;
        }
        if (selectedType >= types.length) {
            selectedType = types.length - 1;
        }
        if (Keyboard.isKeyDown((int)205) && PlayerSelector.mc.currentScreen == null) {
            if (!rightDown) {
                selectedType = 0;
            }
            rightDown = true;
        } else {
            rightDown = false;
        }
        if (Keyboard.isKeyDown((int)203) && PlayerSelector.mc.currentScreen == null) {
            if (!leftDown) {
                selectedType = -1;
            }
            leftDown = true;
        } else {
            leftDown = false;
        }
        playersLastTick = players;
    }

    public static interface Callback {
        public void run(NetworkPlayerInfo var1);
    }

    public static class Type {
        public final Callback callback;
        public final String displayName;

        public Type(Callback callback, String displayName) {
            this.callback = callback;
            this.displayName = displayName;
        }
    }
}
