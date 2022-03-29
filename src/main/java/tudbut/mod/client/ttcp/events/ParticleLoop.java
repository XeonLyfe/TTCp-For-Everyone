package tudbut.mod.client.ttcp.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttcp.TTCp;

public class ParticleLoop {
    static List<Particle> particleMap = new ArrayList<Particle>();

    public static void register(Particle particle) {
        particleMap.add(particle);
    }

    public static void run() {
        Particle[] particles = particleMap.toArray(new Particle[0]);
        for (int i = 0; i < particles.length; ++i) {
            if (particles[i].summon()) {
                Vec3d pos = particles[i].getPosition();
                if (TTCp.mc.world == null) continue;
                TTCp.mc.world.func_175682_a(particles[i].getType(), true, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]);
                continue;
            }
            particleMap.remove(particles[i]);
        }
    }

    public static interface Particle {
        public boolean summon();

        public EnumParticleTypes getType();

        public Vec3d getPosition();
    }
}
