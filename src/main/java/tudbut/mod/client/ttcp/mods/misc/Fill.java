package tudbut.mod.client.ttcp.mods.misc;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.misc.MidClick;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;
import tudbut.obj.TLMap;
import tudbut.tools.Lock;

@Misc
public class Fill
extends Module {
    BlockPos start = null;
    BlockPos end = null;
    @Save
    public int delay = 0;
    Lock lock = new Lock();
    public boolean place = true;
    @Save
    public boolean rotate = false;
    Item placed = null;
    TLMap<BlockPos, Lock> placedList = new TLMap();
    float altRotX = 0.0f;
    float altRotY = 0.0f;
    long lastPacket = 0L;
    @Save
    int iterations = 1;
    public boolean done = false;

    @Override
    public boolean onPacket(Packet<?> packet) {
        if (this.rotate) {
            if (packet instanceof CPacketPlayer.Rotation) {
                if (System.currentTimeMillis() - this.lastPacket < 90L) {
                    return true;
                }
                this.lastPacket = System.currentTimeMillis();
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeFloat(this.altRotX);
                buffer.writeFloat(this.altRotY);
                buffer.writeByte(((CPacketPlayer.Rotation)packet).func_149465_i() ? 1 : 0);
                try {
                    packet.readPacketData(buffer);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            if (packet instanceof CPacketPlayer.PositionRotation) {
                this.lastPacket = System.currentTimeMillis();
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_186997_a(0.0));
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_186996_b(0.0));
                buffer.writeDouble(((CPacketPlayer.PositionRotation)packet).func_187000_c(0.0));
                buffer.writeFloat(this.altRotX);
                buffer.writeFloat(this.altRotY);
                buffer.writeByte(((CPacketPlayer.PositionRotation)packet).func_149465_i() ? 1 : 0);
                try {
                    packet.readPacketData(buffer);
                    buffer.release();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        return super.onPacket(packet);
    }

    @Override
    public void updateBinds() {
        this.customKeyBinds.setIfNull("reset", new Module.KeyBind(null, this.toString() + "::onEnable", false));
        this.customKeyBinds.setIfNull("pause", new Module.KeyBind(null, this.toString() + "::togglePause", false));
        this.subComponents.clear();
        this.subComponents.add(Setting.createInt(0, 2000, "Delay", this, "delay"));
        this.subComponents.add(Setting.createBoolean("Place", this, "place"));
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
        boolean bl = this.place = !this.place;
        if (this.place) {
            Notifications.add(new Notifications.Notification("Unpaused placing."));
        }
        if (!this.place) {
            Notifications.add(new Notifications.Notification("Paused placing."));
        }
    }

    @Override
    public void onDisable() {
        if (MidClick.bindBlock != null && MidClick.bindBlock.getName().startsWith("Fill")) {
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
                return "Fill START";
            }

            @Override
            public void call(MidClick.Bind.Data data) {
                Fill.this.posCallback(data);
            }
        });
    }

    @Override
    public void onTick() {
        if (!this.lock.isLocked()) {
            this.lock.lock(this.delay);
            if (this.end != null && this.place) {
                for (int i = 0; i < this.iterations; ++i) {
                    this.run();
                }
            }
        }
    }

    private void run() {
        this.done = false;
        int px = (int)this.player.field_70165_t;
        int py = (int)this.player.func_174824_e((float)1.0f).y;
        int pz = (int)this.player.field_70161_v;
        for (int iy = 0; iy <= 10; ++iy) {
            for (int iz = 0; iz <= 10; ++iz) {
                for (int ix = 0; ix <= 10; ++ix) {
                    int x = px + ix - 5;
                    int y = py + iy - 5;
                    int z = pz + iz - 5;
                    if (x < this.start.func_177958_n() || y < this.start.func_177956_o() || z < this.start.func_177952_p() || x > this.end.func_177958_n() || y > this.end.func_177956_o() || z > this.end.func_177952_p() || !this.placeBlockIfPossible(x, y, z)) continue;
                    this.done = false;
                    return;
                }
            }
        }
        this.done = true;
    }

    boolean placeBlockIfPossible(int x, int y, int z) {
        Integer slot;
        BlockPos pos = new BlockPos(x, y, z);
        Vec3d vec3d = this.player.func_174824_e(0.0f);
        Vec3d vec3d2 = new Vec3d((double)x + (x >= 0 ? 0.5 : -0.5), (double)y + (y >= 0 ? 0.5 : -0.5), (double)z + (z >= 0 ? 0.5 : -0.5));
        if (vec3d.distanceTo(vec3d2) > (double)Fill.mc.playerController.getBlockReachDistance() - 0.25) {
            return false;
        }
        if (this.player.func_184614_ca().getCount() == 0 && this.placed != null && (slot = InventoryUtils.getSlotWithItem(this.player.field_71069_bz, this.placed, new int[0], 1, 64)) != null) {
            InventoryUtils.swap(slot, InventoryUtils.getCurrentSlot());
        }
        boolean b = false;
        if (Fill.mc.world.func_180495_p(pos).getBlock().isReplaceable((IBlockAccess)Fill.mc.world, pos) && this.player.func_184614_ca().getCount() > 0 && BlockUtils.getPossibleSides(pos).size() > 0 && this.checkBlockPing(pos)) {
            float[] floats = BlockUtils.getLegitRotations(new Vec3d((Vec3i)pos).addVector(0.5, 0.5, 0.5));
            this.altRotX = floats[0];
            this.altRotY = floats[1];
            if (this.rotate && System.currentTimeMillis() - this.lastPacket < 100L) {
                Fill.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation());
            }
            b = BlockUtils.placeBlock(pos, EnumHand.MAIN_HAND, false, true);
        }
        Item item = this.player.func_184614_ca().getItem();
        if (b && item != Items.AIR) {
            this.placed = item;
            Lock lock = new Lock();
            lock.lock(500);
            this.placedList.set(pos, lock);
        }
        return b;
    }

    private boolean checkBlockPing(BlockPos pos) {
        Lock lock = this.placedList.get(pos);
        if (lock == null) {
            return true;
        }
        if (lock.isLocked()) {
            return false;
        }
        this.placedList.set(pos, null);
        return true;
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
                    return "Fill END";
                }

                @Override
                public void call(MidClick.Bind.Data data) {
                    Fill.this.posCallback(data);
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
            Notifications.add(new Notifications.Notification("Filling!", 20000));
        }
    }
}
