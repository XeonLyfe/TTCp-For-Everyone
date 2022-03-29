package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import tudbut.mod.client.ttcp.mods.misc.MidClick;

class MidClick$1
implements MidClick.Bind.Data {
    final RayTraceResult val$hover;

    MidClick$1(RayTraceResult rayTraceResult) {
        this.val$hover = rayTraceResult;
    }

    @Override
    public BlockPos block() {
        return this.val$hover.getBlockPos();
    }

    @Override
    public Entity entity() {
        return this.val$hover.entityHit;
    }
}
