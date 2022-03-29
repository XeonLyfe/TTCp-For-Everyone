package tudbut.mod.client.ttcp.utils;

import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.mods.combat.HopperAura;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.tools.Lock;

public class InventoryUtils {
    public static final int OFFHAND_SLOT = 45;
    private static final Lock swapLock = new Lock();

    public static Integer getSlotWithItem(Container inv, Item item, int amount) {
        return InventoryUtils.getSlotWithItem(inv, item, Utils.range(0, 8), amount, amount);
    }

    public static Integer getSlotWithItem(Container inv, Item item, int[] not, int amountMin, int amountMax) {
        block0: for (int i = 0; i < inv.getInventory().size(); ++i) {
            for (int j = 0; j < not.length; ++j) {
                if (i == not[j]) continue block0;
            }
            ItemStack stack = inv.getSlot(i).getStack();
            if (!stack.getItem().equals(item) || stack.getCount() < amountMin || stack.getCount() > amountMax) continue;
            return i;
        }
        return null;
    }

    public static Integer getSlotWithItem(Container inv, Block item, int amount) {
        return InventoryUtils.getSlotWithItem(inv, item, Utils.range(0, 8), amount, amount);
    }

    public static Integer getSlotWithItem(Container inv, Block item, int[] not, int amountMin, int amountMax) {
        block0: for (int i = 0; i < inv.getInventory().size(); ++i) {
            for (int j = 0; j < not.length; ++j) {
                if (i == not[j]) continue block0;
            }
            ItemStack stack = inv.getSlot(i).getStack();
            if (stack.getItem().getRegistryName() == null || !stack.getItem().getRegistryName().toString().equals(Objects.requireNonNull(item.getRegistryName()).toString()) || stack.getCount() < amountMin || stack.getCount() > amountMax) continue;
            return i;
        }
        return null;
    }

    public static int getItemAmount(Container inv, Item item) {
        int c = 0;
        for (int i = 0; i < inv.getInventory().size(); ++i) {
            ItemStack stack = inv.getSlot(i).getStack();
            if (!stack.getItem().equals(item)) continue;
            c += stack.getCount();
        }
        return c;
    }

    public static void setCurrentSlot(int id) {
        if (TTCp.player.field_71071_by.currentItem != id) {
            TTCp.player.field_71071_by.currentItem = id;
            TTCp.player.connection.sendPacket((Packet)new CPacketHeldItemChange(id));
        }
    }

    public static int getCurrentSlot() {
        return TTCp.player.field_71071_by.currentItem;
    }

    public static void drop(int slot) {
        InventoryUtils.clickSlot(slot, ClickType.THROW, 1);
    }

    public static void drop(int windowId, int slot) {
        InventoryUtils.clickSlot(windowId, slot, ClickType.THROW, 1);
    }

    public static void clickSlot(int slot, ClickType type, int key) {
        TTCp.mc.playerController.windowClick(TTCp.mc.player.field_71069_bz.windowId, slot, key, type, (EntityPlayer)TTCp.mc.player);
    }

    public static void clickSlot(int windowId, int slot, ClickType type, int key) {
        TTCp.mc.playerController.windowClick(windowId, slot, key, type, (EntityPlayer)TTCp.mc.player);
    }

    public static void swap(int slot, int hotbarSlot) {
        InventoryUtils.clickSlot(slot, ClickType.SWAP, hotbarSlot);
    }

    public static void inventorySwap(int slot0, int slot1, long mainDelay, long postDelay, long cooldownDelay) {
        HopperAura.pause();
        swapLock.waitHere();
        swapLock.lock();
        if (slot1 == 44) {
            int i = slot0;
            slot0 = slot1;
            slot1 = i;
        }
        try {
            GuiScreen screen = TTCp.mc.currentScreen;
            boolean doResetScreen = false;
            if (screen instanceof GuiContainer && !(screen instanceof GuiInventory)) {
                TTCp.player.closeScreen();
                Thread.sleep(500L);
                doResetScreen = true;
            }
            InventoryUtils.swap(slot0, 8);
            Thread.sleep(mainDelay);
            InventoryUtils.swap(slot1, 8);
            Thread.sleep(postDelay);
            InventoryUtils.swap(slot0, 8);
            Thread.sleep(cooldownDelay);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        swapLock.unlock();
        HopperAura.resume();
    }
}
