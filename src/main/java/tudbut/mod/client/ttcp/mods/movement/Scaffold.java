package tudbut.mod.client.ttcp.mods.movement;

import java.util.Date;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.mods.combat.AutoTotem;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Movement;
import tudbut.obj.Save;
import tudbut.tools.Lock;
import tudbut.tools.ThreadPool;

@Movement
public class Scaffold
extends Module {
    BlockPos last = null;
    long lastJump = 0L;
    Lock swapLock = new Lock();
    ThreadPool swapThread = new ThreadPool(1, "Swap thread", true);
    @Save
    boolean tower = false;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        this.subComponents.add(Setting.createBoolean("Tower", this, "tower"));
    }

    @Override
    public void onSubTick() {
        BlockPos pos;
        EntityPlayerSP player = TTCp.player;
        World world = TTCp.world;
        if (player.field_70163_u == (double)((int)player.field_70163_u)) {
            this.lastJump = 0L;
        }
        if (!((Boolean)Utils.getPrivateField(EntityLivingBase.class, player, Utils.getFieldsForType(EntityLivingBase.class, Boolean.TYPE)[2])).booleanValue()) {
            if (new Date().getTime() - this.lastJump > 500L) {
                player.field_70181_x = 0.0;
                player.field_70122_E = true;
            }
        } else {
            this.lastJump = new Date().getTime();
        }
        Vec3d vec = player.func_174791_d();
        if (this.tower && player.movementInput.jump && player.field_70159_w == 0.0 && player.field_70179_y == 0.0) {
            this.lastJump = new Date().getTime();
            player.field_70181_x = 0.42f;
            player.field_70122_E = false;
            pos = BlockUtils.getRealPos(vec.addVector(0.0, -0.2, 0.0)).down();
        } else {
            pos = BlockUtils.getRealPos(vec).down();
        }
        if (world.getBlockState(pos).getBlock().isReplaceable((IBlockAccess)world, pos)) {
            if (player.func_184614_ca().getCount() < 5 && player.func_184614_ca().getCount() != 0) {
                Integer slot = InventoryUtils.getSlotWithItem(player.field_71069_bz, player.func_184614_ca().getItem(), new int[]{45}, 5, 64);
                if (slot != null && !this.swapLock.isLocked()) {
                    this.swapLock.lock(1500);
                    this.swapThread.run(() -> {
                        InventoryUtils.inventorySwap(slot, player.field_71071_by.currentItem + 36, AutoTotem.getInstance().sdelay, AutoTotem.getInstance().pdelay, AutoTotem.getInstance().cdelay);
                        if (EventHandler.ping[0] > 0L) {
                            this.swapLock.lock((int)EventHandler.ping[0]);
                        }
                    });
                }
            } else if (player.func_184614_ca().getCount() == 0) {
                this.toggle();
            }
            if (player.movementInput.jump && (player.field_70163_u >= 0.4 || Math.abs(Math.abs(player.field_70163_u) - (double)Math.abs((long)player.field_70163_u)) < 0.05 || Math.abs(Math.abs(player.field_70163_u) - (double)Math.abs((long)player.field_70163_u)) > 0.25)) {
                return;
            }
            if (BlockUtils.placeBlock(pos, EnumHand.MAIN_HAND, true, false)) {
                if (player.field_70122_E || this.tower) {
                    player.field_70181_x = 0.0;
                }
                this.last = pos;
            } else {
                int n;
                int dx = pos.func_177958_n() - this.last.func_177958_n();
                int dy = pos.func_177956_o() - this.last.func_177956_o();
                int dz = pos.func_177952_p() - this.last.func_177952_p();
                boolean b = false;
                for (int x = 1; x <= Math.abs(dx); ++x) {
                    int n2 = n = dx < 0 ? -1 : 1;
                    if (BlockUtils.placeBlock(this.last.add(n * x, 0, 0), EnumHand.MAIN_HAND, true, false)) continue;
                    b = true;
                }
                for (int y = 1; y <= Math.abs(dy); ++y) {
                    int n3 = n = dy < 0 ? -1 : 1;
                    if (BlockUtils.placeBlock(this.last.add(dx, n * y, 0), EnumHand.MAIN_HAND, true, false)) continue;
                    b = true;
                }
                for (int z = 1; z <= Math.abs(dz); ++z) {
                    int n4 = n = dz < 0 ? -1 : 1;
                    if (BlockUtils.placeBlock(this.last.add(dx, dy, n * z), EnumHand.MAIN_HAND, true, false)) continue;
                    b = true;
                }
                if (!b) {
                    this.last = pos;
                }
            }
        }
    }
}
