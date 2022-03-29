package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.util.math.BlockPos;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class Flatten
extends Module {
    @Save
    public boolean autoSelect = false;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("AutoSelect", this, "autoSelect"));
    }

    @Override
    public void onTick() {
        BlockPos pos = BlockUtils.getRealPos(TTCp.player.func_174791_d());
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                BlockPos block = pos.add(x, -1, z);
                if (!TTCp.world.isAirBlock(block)) continue;
                BlockUtils.placeBlock(block, true);
            }
        }
    }
}
