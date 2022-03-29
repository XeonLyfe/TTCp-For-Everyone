package tudbut.mod.client.ttcp.mods.rendering;

import java.util.ArrayList;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.category.Render;
import tudbut.obj.Save;

@Render
public class PlayerLog
extends Module {
    NetworkPlayerInfo[] playersLastTick;
    EntityPlayer[] visiblePlayersLastTick;
    ArrayList<AxisAlignedBB> logouts = new ArrayList();
    @Save
    public boolean spots = true;
    @Save
    public boolean messages = false;
    Vec3d pos = new Vec3d(0.0, 0.0, 0.0);

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Reset logout spots", it -> this.logouts.clear()));
        this.subComponents.add(Setting.createBoolean("LogOutSpots", this, "spots"));
        this.subComponents.add(Setting.createBoolean("Messages", this, "messages"));
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public void onSubTick() {
        if (TTCp.mc.getConnection() == null) {
            return;
        }
        if (this.playersLastTick == null) {
            this.playersLastTick = TTCp.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        }
        EntityPlayer[] visiblePlayersThisTick = TTCp.mc.world.field_73010_i.toArray(new EntityPlayer[0]);
        NetworkPlayerInfo[] playersThisTick = TTCp.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        try {
            int j;
            String name;
            int i;
            if (playersThisTick.length < this.playersLastTick.length) {
                for (i = 0; i < this.playersLastTick.length; ++i) {
                    try {
                        boolean b = true;
                        name = this.playersLastTick[i].getGameProfile().getName();
                        for (j = 0; j < playersThisTick.length; ++j) {
                            if (!playersThisTick[j].getGameProfile().getName().equals(name)) continue;
                            b = false;
                        }
                        if (!b) continue;
                        if (this.messages) {
                            ChatUtils.print(name + " left!");
                        }
                        for (j = 0; j < this.visiblePlayersLastTick.length; ++j) {
                            if (!this.visiblePlayersLastTick[j].getGameProfile().getName().equals(name)) continue;
                            Vec3d vec = this.visiblePlayersLastTick[j].func_174791_d();
                            if (this.messages) {
                                ChatUtils.print("§c§l§c§lThe player §r" + this.visiblePlayersLastTick[j].getName() + "§c§l left at " + (double)Math.round(vec.x * 100.0) / 100.0 + " " + (double)Math.round(vec.y * 100.0) / 100.0 + " " + (double)Math.round(vec.z * 100.0) / 100.0 + " !");
                            }
                            if (!this.spots) continue;
                            this.logouts.add(this.visiblePlayersLastTick[j].func_174813_aQ().offset(0.0, 0.0, 0.0));
                        }
                        continue;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (playersThisTick.length > this.playersLastTick.length) {
                for (i = 0; i < playersThisTick.length; ++i) {
                    try {
                        boolean b = true;
                        name = playersThisTick[i].getGameProfile().getName();
                        for (j = 0; j < this.playersLastTick.length; ++j) {
                            if (!this.playersLastTick[j].getGameProfile().getName().equals(name)) continue;
                            b = false;
                        }
                        if (!b || !this.messages) continue;
                        ChatUtils.print(name + " joined!");
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.playersLastTick = playersThisTick;
        this.visiblePlayersLastTick = visiblePlayersThisTick;
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @SubscribeEvent
    public void onRenderWorld(Event event) {
        if (event instanceof RenderWorldLastEvent && this.enabled && TTCp.isIngame()) {
            Entity e = TTCp.mc.getRenderViewEntity();
            assert (e != null);
            this.pos = e.getPositionEyes(((RenderWorldLastEvent)event).getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
            for (int i = 0; i < this.logouts.size(); ++i) {
                this.drawAroundBox(this.logouts.get(i), -2147418368);
            }
        }
    }

    public void drawAroundBox(AxisAlignedBB box, int color) {
        try {
            Tesselator.ready();
            Tesselator.translate(-this.pos.x, -this.pos.y, -this.pos.z);
            Tesselator.color(color);
            Tesselator.depth(false);
            Tesselator.begin(7);
            double entityHalfed = (box.maxX - box.minX) / 2.0;
            double entityHeight = box.maxY - box.minY;
            Vec3d pos = new Vec3d(box.maxX - entityHalfed, box.minY, box.maxZ - entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.next();
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            Tesselator.put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            Tesselator.end();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
