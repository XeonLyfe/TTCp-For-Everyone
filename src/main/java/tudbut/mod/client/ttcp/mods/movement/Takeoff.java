package tudbut.mod.client.ttcp.mods.movement;

import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Atomic;

@Movement
public class Takeoff
extends Module {
    boolean isTakingOff = false;

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEnable() {
        ChatUtils.print("Starting elytra...");
        this.isTakingOff = true;
        FlightBot.activate(new Atomic<Vec3d>(TTCp.mc.player.func_174791_d().addVector(0.0, 4.0, 0.0)));
        ChatUtils.print("Bot started.");
    }

    @Override
    public void onDisable() {
        this.isTakingOff = false;
        this.enabled = false;
        FlightBot.deactivate();
    }

    @Override
    public void onTick() {
        if (!FlightBot.isFlying() && this.isTakingOff && TTCp.player.func_184613_cA()) {
            FlightBot.deactivate();
            this.isTakingOff = false;
            this.enabled = false;
            this.onDisable();
            ChatUtils.print("Elytra started.");
        }
    }

    @Override
    public void onChat(String s, String[] args) {
    }
}
