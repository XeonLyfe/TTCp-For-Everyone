package tudbut.mod.client.ttcp.events;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;

public interface ParticleLoop$Particle {
    public boolean summon();

    public EnumParticleTypes getType();

    public Vec3d getPosition();
}
