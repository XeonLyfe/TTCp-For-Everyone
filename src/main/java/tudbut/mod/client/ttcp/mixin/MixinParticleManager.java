package tudbut.mod.client.ttcp.mixin;

import com.google.common.collect.Lists;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ParticleManager.class})
public class MixinParticleManager {
    @Shadow
    private Queue<ParticleEmitter> field_178933_d;
    @Shadow
    private Queue<Particle> field_187241_h;
    @Shadow
    private ArrayDeque<Particle>[][] field_78876_b;

    @Overwrite
    public void func_78868_a() {
        try {
            for (int i = 0; i < 4; ++i) {
                this.func_178922_a(i);
            }
            if (!this.field_178933_d.isEmpty()) {
                ParticleEmitter[] particleEmitters;
                ArrayList<ParticleEmitter> list = Lists.newArrayList();
                for (ParticleEmitter particleemitter : particleEmitters = this.field_178933_d.toArray(new ParticleEmitter[0])) {
                    particleemitter.onUpdate();
                    if (particleemitter.func_187113_k()) continue;
                    list.add(particleemitter);
                }
                this.field_178933_d.removeAll(list);
            }
            if (!this.field_187241_h.isEmpty()) {
                Particle particle = this.field_187241_h.poll();
                while (particle != null) {
                    int k;
                    int j = particle.getFXLayer();
                    int n = k = particle.shouldDisableDepth() ? 0 : 1;
                    if (this.field_78876_b[j][k].size() >= 16384) {
                        this.field_78876_b[j][k].removeFirst();
                    }
                    this.field_78876_b[j][k].add(particle);
                    particle = this.field_187241_h.poll();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Shadow
    private void func_178922_a(int i) {
    }
}
