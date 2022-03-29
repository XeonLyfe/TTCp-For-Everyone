package tudbut.mod.client.ttcp.utils.ttcic.task;

import net.minecraft.entity.player.EntityPlayer;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.utils.ttcic.task.TaskFollowPlayer;

public class TaskKillPlayer
extends TaskFollowPlayer {
    @Override
    public void onTick() {
        super.onTick();
        KillAura aura = TTCp.getModule(KillAura.class);
        if (!aura.enabled) {
            aura.toggle();
        }
        if (this.getEntity() instanceof EntityPlayer) {
            aura.targets.add(((EntityPlayer)this.getEntity()).getGameProfile().getName());
        } else {
            aura.targets.clear();
            aura.attack = 0;
        }
    }
}
