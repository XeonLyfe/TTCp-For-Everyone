package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class Timer
extends Module {
    @Save
    boolean fasten = false;
    @Save
    boolean slowdown = true;
    @Save
    boolean fullSync = false;
    long lastTick = -1L;
    @Save
    float m = 0.5f;
    float tps = 20.0f;
    long lastDiff = 0L;
    boolean hasSynched = false;
    boolean isSynching = false;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createFloat(1.0f, 50.0f, "Multiplier", this, "m"));
        this.subComponents.add(Setting.createBoolean("TPSFasten", this, "fasten"));
        this.subComponents.add(Setting.createBoolean("TPSSlowdown", this, "slowdown"));
        this.subComponents.add(Setting.createBoolean("FullSync", this, "fullSync"));
    }

    @Override
    public void onDisable() {
        Timer.setGameTimer(20.0f);
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        if (packet instanceof SPacketTimeUpdate) {
            long time = System.currentTimeMillis();
            if (this.lastTick != -1L) {
                long diff = time - this.lastTick;
                this.time(diff);
                this.lastDiff = diff;
            }
            this.lastTick = time;
            this.hasSynched = false;
        }
        return false;
    }

    @Override
    public void onSubTick() {
        if (!this.fullSync) {
            long diff;
            long time = System.currentTimeMillis();
            if (this.lastTick != -1L && (diff = time - this.lastTick) > 3000L && diff > this.lastDiff) {
                this.time(diff);
            }
            Timer.setGameTimer(this.tps * this.m);
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (this.enabled && this.fullSync && !this.hasSynched && TTCp.isIngame()) {
            this.hasSynched = true;
            this.isSynching = true;
            Timer.setGameTimer(100.0f);
        }
    }

    @Override
    public void onTick() {
        if (this.isSynching) {
            this.isSynching = false;
            Timer.setGameTimer(this.tps);
        }
    }

    public void time(long diff) {
        if (this.lastTick != -1L && diff > 50L) {
            this.tps = 1000.0f / (float)diff * 20.0f;
        }
        if (!this.fasten && this.tps > 20.0f) {
            this.tps = 20.0f;
        } else if (!this.slowdown && this.tps < 20.0f) {
            this.tps = 20.0f;
        }
    }

    public static void setGameTimer(float tps) {
        Utils.setPrivateField(net.minecraft.util.Timer.class, Utils.getPrivateField(Minecraft.class, Minecraft.getMinecraft(), Utils.getFieldsForType(Minecraft.class, net.minecraft.util.Timer.class)[0]), Utils.getFieldsForType(net.minecraft.util.Timer.class, Float.TYPE)[2], Float.valueOf(1000.0f / tps));
    }
}
