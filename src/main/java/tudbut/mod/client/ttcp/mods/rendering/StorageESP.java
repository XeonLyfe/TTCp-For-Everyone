package tudbut.mod.client.ttcp.mods.rendering;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.Tesselator;
import tudbut.mod.client.ttcp.utils.category.Render;
import tudbut.obj.Save;

@Render
public class StorageESP
extends Module {
    @Save
    public boolean chest = true;
    @Save
    public boolean shulkerBox = true;
    @Save
    public boolean enderChest = true;
    @Save
    public boolean furnace = true;
    @Save
    public boolean dispenserAndDropper = true;
    @Save
    public boolean storageMinecart = true;
    @Save
    public boolean hopper = true;
    Vec3d drawPos = new Vec3d(0.0, 0.0, 0.0);

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        boolean b = this.enabled;
        this.enabled = true;
        this.subComponents.add(Setting.createBoolean("Chest", this, "chest"));
        this.subComponents.add(Setting.createBoolean("Shulker Box", this, "shulkerBox"));
        this.subComponents.add(Setting.createBoolean("Ender Chest", this, "enderChest"));
        this.subComponents.add(Setting.createBoolean("Storage Minecarts", this, "storageMinecart"));
        this.subComponents.add(Setting.createBoolean("Hopper", this, "hopper"));
        this.subComponents.add(Setting.createBoolean("Droppers", this, "dispenserAndDropper"));
        this.subComponents.add(Setting.createBoolean("Furnace", this, "furnace"));
        this.enabled = b;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (this.enabled) {
            Entity e = TTCp.mc.getRenderViewEntity();
            this.drawPos = e.getPositionEyes(event.getPartialTicks()).addVector(0.0, (double)(-e.getEyeHeight()), 0.0);
            for (TileEntity tileEntity : TTCp.mc.world.field_147482_g) {
                if (this.isESP(tileEntity)) {
                    Tesselator.drawAroundBlock(tileEntity.getPos(), -2130771968, this.drawPos);
                }
                for (Entity entity : TTCp.world.loadedEntityList) {
                    if (!(entity instanceof EntityMinecartContainer) || !this.storageMinecart) continue;
                    Tesselator.drawAroundBlock(entity.getPosition(), -2130771968, this.drawPos);
                }
            }
        }
    }

    public boolean isESP(TileEntity e) {
        return e instanceof TileEntityChest && this.chest || e instanceof TileEntityEnderChest && this.enderChest || e instanceof TileEntityShulkerBox && this.shulkerBox || e instanceof TileEntityFurnace && this.furnace || e instanceof TileEntityDropper && this.dispenserAndDropper || e instanceof TileEntityDispenser && this.dispenserAndDropper || e instanceof TileEntityHopper && this.hopper;
    }

    @Override
    public void onTick() {
    }
}
