package tudbut.mod.client.ttcp.mods.combat;

import java.util.Date;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.ChatUtils;

public class PopCount$Counter {
    private EntityPlayer player;
    private final String name;
    private int totCountLast = -1;
    private int switches = 0;
    private int pops = 0;
    private boolean autoToxic;
    private long lastPop = 0L;
    private float popDelay = 2000.0f;

    public PopCount$Counter(EntityPlayer player) {
        this.player = player;
        this.name = player.getName();
    }

    public void reload(boolean autoToxic, boolean enabled, boolean countOwn) {
        this.autoToxic = autoToxic;
        if (this.player.func_184592_cb().getItem() == Items.TOTEM_OF_UNDYING || this.player.func_184592_cb().getItem() == Items.AIR) {
            if (this.totCountLast == -1) {
                this.totCountLast = this.player.func_184592_cb().getCount();
                this.lastPop = new Date().getTime();
            }
            this.reload0(enabled, countOwn);
        }
    }

    private void reload0(boolean enabled, boolean countOwn) {
        int totCount = this.player.func_184592_cb().getCount();
        if (totCount > this.totCountLast) {
            ++this.switches;
            if (totCount != 1 && enabled && (countOwn || this.player.func_145782_y() != TTCp.player.func_145782_y())) {
                ChatUtils.printChatAndHotbar("§a§l" + this.player.getName() + " switched (now " + this.switches + " switches)");
                Notifications.add(new Notifications.Notification(this.player.getName() + " switched (now " + this.switches + " switches)"));
            }
        }
        if (totCount < this.totCountLast) {
            float timeSinceLastPop;
            this.pops += this.totCountLast - totCount;
            if (enabled && (countOwn || this.player.func_145782_y() != TTCp.player.func_145782_y())) {
                ChatUtils.printChatAndHotbar("§a§l" + this.player.getName() + " popped " + (this.totCountLast - totCount) + " (now " + this.pops + " pops)");
                Notifications.add(new Notifications.Notification(this.player.getName() + " popped " + (this.totCountLast - totCount) + " (now " + this.pops + " pops)"));
                if (this.autoToxic && this.player != TTCp.player && this.player.func_70032_d((Entity)TTCp.player) < 10.0f) {
                    ChatUtils.simulateSend("EZ pop " + this.player.getName() + "! TTC on top!", false);
                }
            }
            if ((timeSinceLastPop = (float)(new Date().getTime() - this.lastPop)) < 8000.0f) {
                this.popDelay = (this.popDelay * 4.0f + timeSinceLastPop) / 5.0f;
            }
            this.lastPop = new Date().getTime();
        }
        this.totCountLast = totCount;
    }

    public int getSwitches() {
        return this.switches;
    }

    public int getPops() {
        return this.pops;
    }

    public float getPopDelay() {
        return this.popDelay;
    }

    public float popsPerSecond() {
        return 1000.0f / this.popDelay;
    }

    public long predictNextPop() {
        return (long)((float)this.lastPop + this.popDelay);
    }

    public long predictNextPopDelay() {
        return Math.max(0L, (long)((float)this.lastPop + this.popDelay) - new Date().getTime());
    }

    public float predictPopProgress() {
        long l = new Date().getTime() - this.lastPop;
        return Math.min(1.0f, (float)l * 1.0f / this.popDelay * 1.0f);
    }

    public boolean isPopping() {
        return new Date().getTime() - this.lastPop < 8000L;
    }

    static String access$000(PopCount$Counter x0) {
        return x0.name;
    }

    static EntityPlayer access$102(PopCount$Counter x0, EntityPlayer x1) {
        x0.player = x1;
        return x0.player;
    }
}
