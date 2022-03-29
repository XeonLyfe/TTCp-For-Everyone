package tudbut.mod.client.ttcp.mods.combat;

import de.tudbut.type.Vector2d;
import java.util.Date;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.GuiPlayerSelect;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.chat.Team;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.misc.AltControl;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.Save;
import tudbut.tools.Queue;

@Combat
public class SmoothAura
extends Module {
    @Save
    int delay = 200;
    long last = 0L;
    @Save
    int attack = 0;
    public Queue<Entity> toAttack = new Queue();
    public Queue<String> targets = new Queue();
    public String target = null;
    static SmoothAura instance;

    public SmoothAura() {
        this.customKeyBinds.set("select", new Module.KeyBind(null, this.toString() + "::triggerSelect", false));
        instance = this;
    }

    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> {
            while (this.targets.hasNext()) {
                this.targets.next();
            }
            this.targets.add(player.getGameProfile().getName());
        }, "Set SmoothAura target"));
    }

    public void triggerSelect() {
        while (this.targets.hasNext()) {
            this.targets.next();
        }
        this.target = null;
        TTCp.mc.displayGuiScreen((GuiScreen)new GuiPlayerSelect((EntityPlayer[])TTCp.world.playerEntities.stream().filter(player -> !player.getName().equals(TTCp.player.func_70005_c_())).toArray(EntityPlayer[]::new), player -> {
            if (!this.targets.toList().contains(player.getName())) {
                this.targets.add(player.getName());
            }
            return false;
        }));
    }

    public static SmoothAura getInstance() {
        return instance;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Delay: " + this.delay, it -> {
            this.delay = Keyboard.isKeyDown((int)42) ? (this.delay -= 25) : (this.delay += 25);
            if (this.delay < 50) {
                this.delay = 1000;
            }
            if (this.delay > 1000) {
                this.delay = 50;
            }
            it.text = "Delay: " + this.delay;
        }));
    }

    @Override
    public void onTick() {
        if (TTCp.world != null) {
            boolean shouldNext = true;
            if (!this.toAttack.hasNext()) {
                EntityPlayer[] players = TTCp.world.playerEntities.toArray(new EntityPlayer[0]);
                for (int i = 0; i < players.length; ++i) {
                    if (!(players[i].func_70032_d((Entity)TTCp.player) < 8.0f) || Team.getInstance().names.contains(players[i].getGameProfile().getName()) || Friend.getInstance().names.contains(players[i].getGameProfile().getName()) || players[i].getGameProfile().getName().equals(TTCp.mc.getSession().getProfile().getName()) || AltControl.getInstance().isAlt(players[i]) || players[i].func_110143_aJ() == 0.0f || !players[i].getName().equals(this.target)) continue;
                    this.toAttack.add((Entity)players[i]);
                    shouldNext = false;
                }
            }
            if (shouldNext && this.targets.hasNext()) {
                this.target = this.targets.next();
            }
            if (this.toAttack.hasNext()) {
                this.attackNext();
            }
        }
    }

    public void attackNext() {
        Entity entity = this.toAttack.next();
        Vector2d rot = new Vector2d(TTCp.player.field_70177_z, TTCp.player.field_70125_A);
        BlockUtils.lookCloserTo(entity.getPositionVector().addVector(0.0, (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2.0, 0.0), (float)(Math.random() * 20.0));
        if (new Date().getTime() >= this.last + (long)this.delay) {
            this.last = new Date().getTime();
            if (TTCp.mc.objectMouseOver != null && TTCp.mc.objectMouseOver.entityHit != null) {
                TTCp.mc.playerController.attackEntity((EntityPlayer)TTCp.player, TTCp.mc.objectMouseOver.entityHit);
                TTCp.player.setSprinting(false);
                TTCp.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)TTCp.player, CPacketEntityAction.Action.STOP_SPRINTING));
                TTCp.player.setSprinting(true);
                TTCp.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)TTCp.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            TTCp.player.swingArm(EnumHand.MAIN_HAND);
            TTCp.player.func_184821_cY();
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }

    @Override
    public void onConfigLoad() {
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 3;
    }
}
