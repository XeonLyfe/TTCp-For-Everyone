package tudbut.mod.client.ttcp.mods.movement;

import de.tudbut.type.Vector2d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.FlightBot;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.mod.client.ttcp.utils.pathfinding.AStar;
import tudbut.mod.client.ttcp.utils.pathfinding.Node;
import tudbut.obj.Atomic;
import tudbut.obj.Save;
import tudbut.rendering.Maths2D;

@Movement
public class ElytraBot
extends Module {
    static ElytraBot bot;
    Atomic<Vec3d> dest;
    double orbitRotation;
    private static final double PI_TIMES_TWO = Math.PI * 2;
    private static final Vector2d zeroZero;
    private Vector2d original;
    @Save
    public boolean pathFind;
    public boolean newPath;
    Node[] nodes;
    int currentNode;
    int task;
    Atomic<Vec3d> theDest;
    Vec3d pos;
    boolean isRising;

    public ElytraBot() {
        bot = this;
        this.dest = new Atomic();
        this.orbitRotation = 0.1;
        this.original = zeroZero.clone();
        this.pathFind = false;
        this.newPath = false;
        this.nodes = null;
        this.currentNode = 0;
        this.task = -1;
        this.updateBinds();
        this.theDest = new Atomic();
        this.isRising = false;
    }

    public static ElytraBot getInstance() {
        return bot;
    }

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        if (this.task == -1 && !FlightBot.isActive()) {
            this.subComponents.add(new Button("Mode", text -> this.displayModeMenu()));
            this.subComponents.add(new Button("Pathfinding: " + this.pathFind, it -> {
                this.pathFind = !this.pathFind;
                it.text = "Pathfinding: " + this.pathFind;
            }));
        } else {
            this.subComponents.add(new Button("Stop", it -> {
                FlightBot.deactivate();
                this.task = -1;
                this.orbitRotation = 0.1;
                this.updateBinds();
            }));
        }
    }

    public void displayModeMenu() {
        this.subComponents.clear();
        this.subComponents.add(new Button("Back", it -> this.updateBinds()));
        this.subComponents.add(new Button("Orbit spawn", it -> {
            this.original = zeroZero.clone();
            this.startOrbitSpawn();
            this.updateBinds();
        }));
        this.subComponents.add(new Button("Orbit spawn from here", it -> {
            this.original = new Vector2d(Math.sqrt(TTCp.player.field_70165_t * TTCp.player.field_70165_t + TTCp.player.field_70161_v * TTCp.player.field_70161_v), 0.0);
            this.startOrbitSpawn();
            this.updateBinds();
        }));
    }

    public void startOrbitSpawn() {
        this.dest.set(new Vec3d(this.original.getX(), 260.1, this.original.getY()));
        FlightBot.deactivate();
        FlightBot.activate(this.dest);
        ChatUtils.chatPrinterDebug().println("Now flying to " + this.original);
        this.task = 0;
    }

    public void tickOrbitSpawn() {
        if (!FlightBot.isFlying() && !this.isRising) {
            Vector2d point = this.original.clone().add(this.orbitRotation * 5.0 * 16.0, 0.0);
            this.orbitRotation += 5.0 / (point.getX() * (Math.PI * 2));
            Maths2D.rotate(point, zeroZero, this.orbitRotation * (Math.PI * 2));
            Vec3d vec = this.dest.get();
            this.dest.set(new Vec3d(point.getX(), 260.1, point.getY()));
            ChatUtils.chatPrinterDebug().println("Distance traveled: " + vec.distanceTo(this.dest.get()) + "... Now flying to " + point);
        }
    }

    public synchronized void tickGoTo() {
        if ((!FlightBot.isFlying() || this.newPath) && this.pathFind && this.nodes != null) {
            this.newPath = false;
            if (this.currentNode >= this.nodes.length - 1) {
                FlightBot.deactivate();
                this.task = -1;
                this.updateBinds();
                return;
            }
            Node bp = this.nodes[this.currentNode++];
            this.theDest.set(new Vec3d((double)bp.func_177958_n() - 0.5, (double)bp.func_177956_o() + 0.2, (double)bp.func_177952_p() - 0.5));
        }
    }

    @SubscribeEvent
    public void onRenderWorld(Event event) {
        if (event instanceof RenderWorldLastEvent) {
            Node[] nodes = this.nodes;
            if (this.enabled && TTCp.isIngame() && nodes != null && this.task == 1) {
                Entity e = TTCp.mc.getRenderViewEntity();
                this.pos = e.getPositionEyes(((RenderWorldLastEvent)event).getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
                for (int i = 0; i < nodes.length; ++i) {
                    Vec3d pos = new Vec3d((Vec3i)nodes[i]).addVector(-0.5, 0.0, -0.5);
                    int color = -2130706433;
                    Tesselator.ready();
                    Tesselator.translate(-this.pos.x, -this.pos.y, -this.pos.z);
                    Tesselator.color(color);
                    Tesselator.depth(false);
                    Tesselator.begin(7);
                    Tesselator.put(pos.x - 0.5, pos.y - 0.01, pos.z + 0.5);
                    Tesselator.put(pos.x + 0.5, pos.y - 0.01, pos.z + 0.5);
                    Tesselator.put(pos.x + 0.5, pos.y - 0.01, pos.z - 0.5);
                    Tesselator.put(pos.x - 0.5, pos.y - 0.01, pos.z - 0.5);
                    Tesselator.end();
                }
            }
        }
    }

    @Override
    public void onEveryTick() {
        if (TTCp.mc.world == null) {
            return;
        }
        EntityPlayerSP player = TTCp.player;
        if (player.field_70163_u >= 260.0 && (!this.pathFind || this.task != 1)) {
            switch (this.task) {
                case -1: {
                    break;
                }
                case 0: {
                    this.tickOrbitSpawn();
                    break;
                }
                case 1: {
                    FlightBot.updateDestination(this.dest);
                    if (FlightBot.isFlying() || this.isRising) break;
                    this.task = -1;
                    FlightBot.deactivate();
                    this.updateBinds();
                }
            }
        }
        if (!this.pathFind || this.task != 1) {
            if (this.task != -1 && FlightBot.isActive()) {
                this.rise(260.0);
            }
        } else {
            this.tickGoTo();
        }
    }

    public void flyTo(BlockPos pos, boolean pathFind) {
        this.task = 1;
        this.nodes = null;
        this.currentNode = 0;
        FlightBot.deactivate();
        if (pathFind) {
            this.theDest.set(TTCp.player.func_174791_d());
            this.dest.set(new Vec3d((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p()));
            ThreadManager.run(() -> {
                Vec3d d = this.dest.get().subtract(TTCp.mc.player.func_174791_d()).normalize().scale(80.0).add(TTCp.mc.player.func_174791_d());
                if (d.subtract(TTCp.mc.player.func_174791_d()).lengthVector() > this.dest.get().subtract(TTCp.mc.player.func_174791_d()).lengthVector()) {
                    d = this.dest.get();
                }
                d = new Vec3d((double)((int)d.x), d.y, (double)((int)d.z));
                Node[][] nodes = AStar.calculate(TTCp.mc.player.getPosition(), new BlockPos(d), TTCp.world, 10000);
                if (nodes[0].length == 0) {
                    return;
                }
                this.nodes = nodes[0];
                this.newPath = true;
                FlightBot.activate(this.theDest, 1.0);
                this.currentNode = 0;
                while (this.task == 1 && FlightBot.isActive()) {
                    try {
                        for (int i = 0; i < 100; ++i) {
                            Thread.sleep(10L);
                            if (this.task == 1 && FlightBot.isActive()) continue;
                            return;
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    d = this.dest.get().subtract(TTCp.mc.player.func_174791_d()).normalize().scale(80.0).add(TTCp.mc.player.func_174791_d());
                    if (d.subtract(TTCp.mc.player.func_174791_d()).lengthVector() > this.dest.get().subtract(TTCp.mc.player.func_174791_d()).lengthVector()) {
                        d = this.dest.get();
                    }
                    if ((nodes = AStar.calculate(TTCp.mc.player.getPosition(), new BlockPos(d), TTCp.world, 1000))[0].length == 0) continue;
                    this.nodes = nodes[0];
                    float sd = Float.MAX_VALUE;
                    for (int i = 0; i < nodes[0].length; ++i) {
                        float f = (float)nodes[0][i].func_177951_i((Vec3i)TTCp.player.getPosition());
                        if (!(f < sd)) continue;
                        sd = f;
                        this.currentNode = i;
                    }
                    this.newPath = true;
                    this.tickGoTo();
                }
            });
        } else {
            FlightBot.deactivate();
            this.dest.set(new Vec3d((double)pos.func_177958_n(), 260.0, (double)pos.func_177952_p()));
            FlightBot.activate(this.dest);
        }
        this.updateBinds();
    }

    public void rise(double pos) {
        EntityPlayerSP player = TTCp.player;
        if (!FlightBot.isActive()) {
            this.isRising = false;
            return;
        }
        if (player.field_70163_u < pos) {
            FlightBot.updateDestination(new Atomic<Vec3d>(new Vec3d(player.field_70165_t, pos, player.field_70161_v)));
            this.isRising = true;
        } else if (this.isRising) {
            FlightBot.updateDestination(this.dest);
            this.isRising = false;
        }
    }

    @Override
    public void onChat(String s, String[] args) {
        if (TTCp.mc.world == null) {
            return;
        }
        if (this.task != -1 || FlightBot.isActive()) {
            ChatUtils.print("You have to stop your current task first.");
            return;
        }
        switch (args.length) {
            case 2: {
                this.flyTo(new BlockPos(Integer.parseInt(args[0]), 260, Integer.parseInt(args[1])), this.pathFind);
                ChatUtils.print("Flying...");
                break;
            }
            case 3: {
                this.flyTo(new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])), this.pathFind);
                ChatUtils.print("Flying...");
            }
        }
        this.updateBinds();
    }

    @Override
    public int danger() {
        return 2;
    }

    static {
        zeroZero = new Vector2d(0.0, 0.0);
    }
}
