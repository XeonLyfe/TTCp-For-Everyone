package tudbut.mod.client.ttcp.mods.combat;

import java.util.Date;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.Save;
import tudbut.obj.TLMap;

@Combat
public class PopCount
extends Module {
    public TLMap<EntityPlayer, Counter> counters = new TLMap();
    @Save
    public boolean autoToxic = false;
    @Save
    public boolean countOwn = true;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Reset", text -> {
            this.counters = new TLMap();
        }));
        this.subComponents.add(Setting.createBoolean("AutoToxic", this, "autoToxic"));
        this.subComponents.add(Setting.createBoolean("CountOwn", this, "countOwn"));
    }

    @Override
    public void onEveryTick() {
        try {
            int i;
            TLMap<EntityPlayer, Counter> counters = this.counters;
            EntityPlayer[] visiblePlayers = TTCp.world.playerEntities.toArray(new EntityPlayer[0]);
            EntityPlayer[] players = counters.keys().toArray(new EntityPlayer[0]);
            for (i = 0; i < visiblePlayers.length; ++i) {
                boolean b = false;
                for (int j = 0; j < players.length; ++j) {
                    if (!counters.get(players[j]).name.equals(visiblePlayers[i].getName())) continue;
                    counters.get(players[j]).player = visiblePlayers[i];
                    if (players[j] != visiblePlayers[i]) {
                        counters.set(visiblePlayers[i], counters.get(players[j]));
                        counters.set(players[j], null);
                    }
                    b = true;
                }
                if (b) continue;
                counters.set(visiblePlayers[i], new Counter(visiblePlayers[i]));
            }
            players = counters.keys().toArray(new EntityPlayer[0]);
            for (i = 0; i < players.length; ++i) {
                Counter counter = counters.get(players[i]);
                counter.reload(this.autoToxic, this.enabled, this.countOwn);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    public static class Counter {
        private EntityPlayer player;
        private final String name;
        private int totCountLast = -1;
        private int switches = 0;
        private int pops = 0;
        private boolean autoToxic;
        private long lastPop = 0L;
        private float popDelay = 2000.0f;

        public Counter(EntityPlayer player) {
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
    }
}
