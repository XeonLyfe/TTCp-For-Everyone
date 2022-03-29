package tudbut.mod.client.ttcp.mods.combat;

import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Combat;
import tudbut.obj.Save;

@Combat
public class AutoCrystalRewrite
extends Module {
    @Save
    public boolean hasOwnCrystals;

    @Override
    public void onTick() {
        if (!this.hasOwnCrystals) {
            // empty if block
        }
    }
}
