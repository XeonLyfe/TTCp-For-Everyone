package tudbut.mod.client.ttcp.mods.misc;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.misc.MidClick;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class Break
extends Module {
    BlockPos start = null;
    BlockPos end = null;
    public boolean doBreak = true;
    @Save
    public boolean rotate = false;
    float altRotX = 0.0f;
    float altRotY = 0.0f;
    long lastPacket = 0L;
    @Save
    int iterations = 1;
    public boolean done = false;

    @Override
    public boolean onPacket(Packet<?> packet) {
        if (this.rotate) {
            PacketBuffer buffer;
            if (packet instanceof CPacketPlayer.Rotation) {
                if (System.currentTimeMillis() - this.lastPacket < 90L) {
                    return true;
                }
                this.lastPacket = System.currentTimeMillis();
                buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeFloat(this.altRotX);
                buffer.writeFloat(this.altRotY);
                buffer.writeByte(((CPacketPlayer.Rotation)packet).func_149465_i() ? 1 : 0);
                try {
                    packet.readPacketData(buffer);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (packet instanceof CPacketPlayer.PositionRotation) {
                this.lastPacket = System.currentTimeMillis();
                buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_186997_a(0.0));
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_186996_b(0.0));
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_187000_c(0.0));
                buffer.writeFloat(this.altRotX);
                buffer.writeFloat(this.altRotY);
                buffer.writeByte(((CPacketPlayer.PositionRotation)packet).func_149465_i() ? 1 : 0);
                try {
                    packet.readPacketData(buffer);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPacket(packet);
    }

    @Override
    public void updateBinds() {
        this.customKeyBinds.setIfNull("reset", new Module.KeyBind(null, this.toString() + "::onEnable", false));
        this.customKeyBinds.setIfNull("pause", new Module.KeyBind(null, this.toString() + "::togglePause", false));
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("Break", this, "doBreak"));
        this.subComponents.add(Setting.createBoolean("Rotate", this, "rotate"));
        this.subComponents.add(new Button("Reset", it -> {
            this.onEnable();
            this.updateBinds();
        }));
        this.subComponents.add(Setting.createInt(1, 5, "Iterations", this, "iterations"));
        this.subComponents.add(Setting.createKey("ResetKey", (Module.KeyBind)this.customKeyBinds.get("reset")));
        this.subComponents.add(Setting.createKey("PauseKey", (Module.KeyBind)this.customKeyBinds.get("pause")));
    }

    public void togglePause() {
        boolean bl = this.doBreak = !this.doBreak;
        if (this.doBreak) {
            Notifications.add(new Notifications.Notification("Unpaused breaking."));
        }
        if (!this.doBreak) {
            Notifications.add(new Notifications.Notification("Paused breaking."));
        }
    }

    @Override
    public void onDisable() {
        if (MidClick.bindBlock != null && MidClick.bindBlock.getName().startsWith("Break")) {
            MidClick.bindBlock = null;
        }
        MidClick.reload();
    }

    @Override
    public void onEnable() {
        this.end = null;
        this.start = null;
        Notifications.add(new Notifications.Notification("Please select the starting position with MIDCLICK!", 20000));
        MidClick.set(new MidClick.Bind(){

            @Override
            public MidClick.Bind.Type getType() {
                return MidClick.Bind.Type.BLOCK;
            }

            @Override
            public String getName() {
                return "Break START";
            }

            @Override
            public void call(MidClick.Bind.Data data) {
                Break.this.posCallback(data);
            }
        });
    }

    @Override
    public void onTick() {
        if (this.end != null && this.doBreak) {
            for (int i = 0; i < this.iterations; ++i) {
                this.run();
            }
        }
    }

    private void run() {
        int px = (int)this.player.field_70165_t;
        int py = (int)this.player.func_174824_e((float)1.0f).y;
        int pz = (int)this.player.field_70161_v;
        for (int iy = 0; iy <= 10; ++iy) {
            for (int iz = 0; iz <= 10; ++iz) {
                for (int ix = 0; ix <= 10; ++ix) {
                    int x = px + ix - 5;
                    int y = py + iy - 5;
                    int z = pz + iz - 5;
                    if (x < this.start.func_177958_n() || y < this.start.func_177956_o() || z < this.start.func_177952_p() || x > this.end.func_177958_n() || y > this.end.func_177956_o() || z > this.end.func_177952_p() || !this.breakBlockIfPossible(x, y, z)) continue;
                    this.done = false;
                    return;
                }
            }
        }
        this.done = true;
    }

    private boolean breakBlockIfPossible(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Vec3d vec3d = this.player.func_174824_e(0.0f);
        Vec3d vec3d2 = new Vec3d((double)x + (x >= 0 ? 0.5 : -0.5), (double)y, (double)z + (z >= 0 ? 0.5 : -0.5));
        if (vec3d.distanceTo(vec3d2) > (double)Break.mc.playerController.getBlockReachDistance() - 0.25) {
            return false;
        }
        if (!Break.mc.world.func_180495_p(pos).getBlock().isReplaceable((IBlockAccess)Break.mc.world, pos)) {
            float[] floats = BlockUtils.getLegitRotations(new Vec3d((Vec3i)pos).addVector(0.5, 0.5, 0.5));
            if (this.altRotX != floats[0] || this.altRotY != floats[1]) {
                this.altRotX = floats[0];
                this.altRotY = floats[1];
                if (this.rotate && System.currentTimeMillis() - this.lastPacket < 100L) {
                    Break.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation());
                }
            }
            if (Break.mc.playerController.onPlayerDamageBlock(pos, EnumFacing.DOWN)) {
                Break.mc.effectRenderer.addBlockHitEffects(pos, EnumFacing.DOWN);
                this.player.swingArm(EnumHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

    private void posCallback(MidClick.Bind.Data data) {
        if (this.start == null) {
            this.start = data.block();
            MidClick.set(new MidClick.Bind(){

                @Override
                public MidClick.Bind.Type getType() {
                    return MidClick.Bind.Type.BLOCK;
                }

                @Override
                public String getName() {
                    return "Break END";
                }

                @Override
                public void call(MidClick.Bind.Data data) {
                    Break.this.posCallback(data);
                }
            });
            Notifications.add(new Notifications.Notification("Please select the ending position with MIDCLICK!", 20000));
            return;
        }
        if (this.end == null) {
            BlockPos endSel = data.block();
            BlockPos startSel = this.start;
            this.start = new BlockPos(Math.min(startSel.func_177958_n(), endSel.func_177958_n()), Math.min(startSel.func_177956_o(), endSel.func_177956_o()), Math.min(startSel.func_177952_p(), endSel.func_177952_p()));
            this.end = new BlockPos(Math.max(startSel.func_177958_n(), endSel.func_177958_n()), Math.max(startSel.func_177956_o(), endSel.func_177956_o()), Math.max(startSel.func_177952_p(), endSel.func_177952_p()));
            MidClick.bindBlock = null;
            MidClick.reload();
            Notifications.add(new Notifications.Notification("Breaking!", 20000));
        }
    }
}
