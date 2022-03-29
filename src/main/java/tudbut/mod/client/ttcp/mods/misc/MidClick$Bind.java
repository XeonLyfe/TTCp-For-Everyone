package tudbut.mod.client.ttcp.mods.misc;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface MidClick$Bind {
    public Type getType();

    public String getName();

    public void call(Data var1);

    public static interface Data {
        public BlockPos block();

        public Entity entity();
    }

    public static enum Type {
        BLOCK,
        ENTITY,
        PLAYER;

    }
}
