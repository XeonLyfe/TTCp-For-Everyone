package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.mod.client.ttcp.utils.ttcic.ControlCenter;
import tudbut.mod.client.ttcp.utils.ttcic.packet.ds.PacketPlayer;

@Misc
public class AltControlRewrite
extends Module {
    @Override
    public void updateBinds() {
        this.subComponents.clear();
        if (!ControlCenter.isRunning()) {
            this.subComponents.add(new Button("Start Server", it -> {
                this.subComponents.clear();
                this.subComponents.add(new Button("Starting...", a -> {}));
                ControlCenter.server();
                this.updateBinds();
            }));
            this.subComponents.add(new Button("Start Client", it -> {
                this.subComponents.clear();
                this.subComponents.add(new Button("Starting...", a -> {}));
                ControlCenter.client();
                this.updateBinds();
            }));
        } else {
            this.subComponents.add(new Button("Stop", it -> {
                this.subComponents.clear();
                ControlCenter.stop = true;
                new Thread(() -> {
                    this.subComponents.add(new Button("Stopped.", a -> {}));
                    while (ControlCenter.isRunning()) {
                    }
                    this.updateBinds();
                }).start();
            }));
            this.subComponents.add(new Button("Group", it -> {
                for (PacketPlayer packetPlayer : ControlCenter.getGroup()) {
                    ChatUtils.print(packetPlayer.name);
                }
            }));
        }
    }

    @Override
    public void onTick() {
        ControlCenter.onTick();
    }
}
