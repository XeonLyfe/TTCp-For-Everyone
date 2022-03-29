package tudbut.mod.client.ttcp.mods.misc;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.combat.SmoothAura;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.misc.MidClick;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;

public enum MidClick$CustomBindsPlayer implements MidClick.PartialBind
{
    NONE(data -> {}),
    Friend(data -> {
        ArrayList<String> names = tudbut.mod.client.ttcp.mods.command.Friend.getInstance().names;
        if (names.contains(data.entity().getName())) {
            names.remove(data.entity().getName());
            Notifications.add(new Notifications.Notification(data.entity().getName() + " removed from your friends."));
        } else {
            names.add(data.entity().getName());
            Notifications.add(new Notifications.Notification(data.entity().getName() + " added to your friends."));
        }
    }),
    Target(data -> {
        KillAura.getInstance().targets.clear();
        while (SmoothAura.getInstance().targets.hasNext()) {
            SmoothAura.getInstance().targets.next();
        }
        KillAura.getInstance().targets.add(data.entity().getName());
        SmoothAura.getInstance().targets.add(data.entity().getName());
    }),
    Message(data -> Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiChat(TTCp.prefix + "msg " + data.entity().getName() + " ")));

    public final MidClick.Bind toDo;

    private MidClick$CustomBindsPlayer(MidClick.PartialBind toDo) {
        this.toDo = toDo;
    }

    @Override
    public void call(MidClick.Bind.Data data) {
        this.toDo.call(data);
    }
}
