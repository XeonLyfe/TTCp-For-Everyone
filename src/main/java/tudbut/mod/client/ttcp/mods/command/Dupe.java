package tudbut.mod.client.ttcp.mods.command;

import de.tudbut.timer.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;

@Command
public class Dupe
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if (args.length == 0) {
            args = new String[]{"8b8t"};
        }
        switch (args[0]) {
            case "Pkick1": {
                ChatUtils.simulateSend("§", false);
                break;
            }
            case "Pkick2": {
                TTCp.player.connection.sendPacket((Packet)new CPacketUseEntity((Entity)TTCp.player));
                break;
            }
            case "Pkick3": {
                TTCp.player.connection.sendPacket((Packet)new Packet<INetHandler>(){

                    public void readPacketData(PacketBuffer buf) throws IOException {
                    }

                    public void writePacketData(PacketBuffer buf) throws IOException {
                        buf.writeBytes(new InputStream(){

                            @Override
                            public int read() throws IOException {
                                return (int)(Math.random() * 255.0);
                            }
                        }, Integer.MAX_VALUE);
                    }

                    public void processPacket(INetHandler handler) {
                    }
                });
                break;
            }
            case "8b8t": {
                ChatUtils.print("Please wait...");
                int i = InventoryUtils.getCurrentSlot();
                Integer wood = InventoryUtils.getSlotWithItem(this.player.field_71069_bz, Blocks.PLANKS, 64);
                if (wood == null) {
                    ChatUtils.print("Error: No planks!");
                    break;
                }
                new AsyncTask<Object>(() -> {
                    InventoryUtils.clickSlot(wood, ClickType.PICKUP, 0);
                    float r = this.player.field_70125_A;
                    this.player.field_70125_A = 90.0f;
                    this.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(this.player.field_70177_z, this.player.field_70125_A, true));
                    Thread.sleep(50L);
                    InventoryUtils.clickSlot(-999, ClickType.QUICK_CRAFT, 0);
                    InventoryUtils.clickSlot(2, ClickType.QUICK_CRAFT, 1);
                    Thread.sleep(50L);
                    InventoryUtils.clickSlot(4, ClickType.QUICK_CRAFT, 1);
                    InventoryUtils.clickSlot(-999, ClickType.QUICK_CRAFT, 2);
                    Thread.sleep(50L);
                    InventoryUtils.clickSlot(i + 36, ClickType.THROW, 1);
                    Thread.sleep(50L);
                    InventoryUtils.clickSlot(2, ClickType.QUICK_MOVE, 0);
                    Thread.sleep(50L);
                    InventoryUtils.clickSlot(4, ClickType.QUICK_MOVE, 0);
                    while (this.player.field_71071_by.getCurrentItem().isEmpty()) {
                        Thread.sleep(5L);
                    }
                    Thread.sleep(50L);
                    this.player.field_70125_A = r;
                    this.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(this.player.field_70177_z, this.player.field_70125_A, true));
                    return null;
                });
            }
        }
    }
}
