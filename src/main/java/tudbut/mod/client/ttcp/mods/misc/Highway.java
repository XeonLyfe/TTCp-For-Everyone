package tudbut.mod.client.ttcp.mods.misc;

import java.util.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.misc.Break;
import tudbut.mod.client.ttcp.mods.misc.Fill;
import tudbut.mod.client.ttcp.utils.BlockUtils;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.InventoryUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.mod.client.ttcp.utils.category.Misc;

@Misc
public class Highway
extends Module {
    int stage = -1;
    int y = -1;
    boolean wait = false;
    EnumFacing lastDirection;
    int i = 9;

    @Override
    public boolean doStoreEnabled() {
        return false;
    }

    @Override
    public void onEnable() {
        this.stage = -1;
        this.y = (int)Highway.mc.player.field_70163_u;
        this.nextStage();
    }

    public EnumFacing direction() {
        return Highway.mc.player.func_174811_aO();
    }

    private Fill getFill() {
        return TTCp.getModule(Fill.class);
    }

    private Break getBreak() {
        return TTCp.getModule(Break.class);
    }

    public void selectObby() {
        Integer obbySlot = InventoryUtils.getSlotWithItem(TTCp.player.field_71069_bz, Blocks.OBSIDIAN, Utils.range(0, 8), 1, 64);
        if (obbySlot == null) {
            InventoryUtils.setCurrentSlot(8);
            BlockPos pos = BlockUtils.getRealPos(Highway.mc.player.func_174791_d());
            this.getFill().placeBlockIfPossible(pos.func_177958_n(), pos.func_177956_o() + 2, pos.func_177952_p());
            pos = pos.add(0, 2, 0);
            BlockUtils.clickOnBlock(pos, EnumHand.MAIN_HAND);
            this.wait = true;
        }
        ResourceLocation slotType = TTCp.player.field_71069_bz.getSlot(43).getStack().getItem().getRegistryName();
        if (!(obbySlot == null || slotType != null && slotType.toString().equals(Objects.requireNonNull(Blocks.OBSIDIAN.getRegistryName()).toString()))) {
            InventoryUtils.inventorySwap(obbySlot, 43, 0L, 0L, 0L);
        }
    }

    @Override
    public void onTick() {
        EnumFacing direction = this.direction();
        if (Highway.mc.player.field_70125_A < -60.0f) {
            direction = this.lastDirection;
            Highway.mc.player.field_70125_A = 20.0f;
        }
        Highway.mc.player.field_70177_z = direction.getHorizontalAngle();
        if (this.wait) {
            this.getObby();
            return;
        }
        if (InventoryUtils.getCurrentSlot() == 7) {
            this.selectObby();
        }
        if ((int)Highway.mc.player.field_70163_u < this.y) {
            this.player.func_191986_a(0.0f, 1.0f, -1.0f);
        }
        if (this.stage == 1) {
            this.removeLava();
        } else if (this.stageDone()) {
            this.nextStage();
        }
        this.lastDirection = direction;
    }

    private void getObby() {
        if (Highway.mc.currentScreen instanceof GuiContainer) {
            Item item = this.player.field_71071_by.getStackInSlot(this.i).getItem();
            if (item.getRegistryName().equals((Object)Blocks.NETHERRACK.getRegistryName()) || item.getRegistryName().equals((Object)Blocks.COBBLESTONE.getRegistryName()) || item.getRegistryName().equals((Object)Blocks.STONE.getRegistryName())) {
                InventoryUtils.drop(((GuiContainer)Highway.mc.currentScreen).inventorySlots.windowId, this.i + 18);
            }
            if (++this.i >= 45) {
                for (int i = 0; i < 27; ++i) {
                    InventoryUtils.clickSlot(((GuiContainer)Highway.mc.currentScreen).inventorySlots.windowId, i, ClickType.QUICK_MOVE, 0);
                }
                TTCp.player.closeScreen();
                this.i = 9;
                this.wait = false;
            }
        }
    }

    private void nextStage() {
        this.getFill().done = false;
        this.getBreak().done = false;
        switch (++this.stage) {
            case 16: {
                this.stage = 0;
            }
            case 0: 
            case 2: {
                this.getFill().place = false;
                this.getBreak().doBreak = true;
                this.breag();
                break;
            }
            case 1: {
                this.getFill().place = true;
                this.getBreak().doBreak = false;
                InventoryUtils.setCurrentSlot(7);
                break;
            }
            case 3: 
            case 6: {
                this.getFill().place = true;
                this.getBreak().doBreak = false;
                InventoryUtils.setCurrentSlot(7);
                this.selectObby();
                this.placeBottom();
                break;
            }
            case 4: {
                InventoryUtils.setCurrentSlot(7);
                this.placeSide(true);
                break;
            }
            case 5: {
                InventoryUtils.setCurrentSlot(7);
                this.placeSide(false);
                break;
            }
            case 7: {
                this.player.setSprinting(true);
            }
            case 8: 
            case 9: 
            case 10: 
            case 11: {
                this.player.func_191986_a(0.0f, 0.0f, 1.0f);
                break;
            }
        }
        ChatUtils.chatPrinterDebug().println("Next stage: " + this.stage);
    }

    private void placeSide(boolean first) {
        EnumFacing direction = this.direction();
        Fill placer = this.getFill();
        this.selectObby();
        BlockPos pos = BlockUtils.getRealPos(Highway.mc.player.func_174791_d());
        switch (direction) {
            case UP: 
            case DOWN: {
                pos = null;
                break;
            }
            case EAST: {
                if (first) {
                    pos = pos.add(1, 0, -2);
                    break;
                }
                pos = pos.add(1, 0, 2);
                break;
            }
            case WEST: {
                if (first) {
                    pos = pos.add(-1, 0, -2);
                    break;
                }
                pos = pos.add(-1, 0, 2);
                break;
            }
            case NORTH: {
                if (first) {
                    pos = pos.add(-2, 0, -1);
                    break;
                }
                pos = pos.add(2, 0, -1);
                break;
            }
            case SOUTH: {
                pos = first ? pos.add(-2, 0, 1) : pos.add(2, 0, 1);
            }
        }
        if (pos != null) {
            placer.start = placer.end = pos;
        }
    }

    private void placeBottom() {
        EnumFacing direction = this.direction();
        Fill placer = this.getFill();
        BlockPos pos = BlockUtils.getRealPos(Highway.mc.player.func_174791_d());
        switch (direction) {
            case UP: 
            case DOWN: {
                break;
            }
            case EAST: {
                placer.start = pos.add(1, -1, -2);
                placer.end = pos.add(1, -1, 2);
                break;
            }
            case WEST: {
                placer.start = pos.add(-1, -1, -2);
                placer.end = pos.add(-1, -1, 2);
                break;
            }
            case NORTH: {
                placer.start = pos.add(-2, -1, -1);
                placer.end = pos.add(2, -1, -1);
                break;
            }
            case SOUTH: {
                placer.start = pos.add(-2, -1, 1);
                placer.end = pos.add(2, -1, 1);
            }
        }
    }

    private void removeLava() {
        int px = (int)this.player.field_70165_t;
        int py = (int)this.player.func_174824_e((float)1.0f).y;
        int pz = (int)this.player.field_70161_v;
        for (int iy = 0; iy <= 10; ++iy) {
            for (int iz = 0; iz <= 10; ++iz) {
                for (int ix = 0; ix <= 10; ++ix) {
                    int x = px + ix - 5;
                    int y = py + iy - 5;
                    int z = pz + iz - 5;
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = Highway.mc.world.func_180495_p(pos);
                    if (state.getBlock() != Blocks.LAVA && state.getBlock() != Blocks.WATER || state.getBlock().getMetaFromState(state) != 0 || !this.getFill().placeBlockIfPossible(x, y, z)) continue;
                    return;
                }
            }
        }
        ++this.stage;
    }

    private void breag() {
        InventoryUtils.setCurrentSlot(2);
        EnumFacing direction = this.direction();
        Break breaker = this.getBreak();
        BlockPos pos = BlockUtils.getRealPos(Highway.mc.player.func_174791_d());
        switch (direction) {
            case UP: 
            case DOWN: {
                break;
            }
            case EAST: {
                breaker.start = pos.add(1, -1, -2);
                breaker.end = pos.add(1, 2, 2);
                break;
            }
            case WEST: {
                breaker.start = pos.add(-1, -1, -2);
                breaker.end = pos.add(-1, 2, 2);
                break;
            }
            case NORTH: {
                breaker.start = pos.add(-2, -1, -1);
                breaker.end = pos.add(2, 2, -1);
                break;
            }
            case SOUTH: {
                breaker.start = pos.add(-2, -1, 1);
                breaker.end = pos.add(2, 2, 1);
            }
        }
    }

    private boolean stageDone() {
        return (this.getBreak().done || this.getFill().done) && this.stage != 1;
    }
}
