package tudbut.mod.client.ttcp.utils;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.BlockUtils;

public class AutoCrystalUtil {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static AxisAlignedBB createBB(Vec3d crystalPos) {
        return new AxisAlignedBB(crystalPos.x - 1.0, crystalPos.y, crystalPos.z - 1.0, crystalPos.y + 2.0, crystalPos.x + 1.0, crystalPos.x + 1.0);
    }

    public static Vec2f createRotations(AxisAlignedBB box) {
        Vec3d posEyes = AutoCrystalUtil.mc.player.func_174824_e(1.0f);
        Vec3d best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (float ix = 0.0f; ix < 1.0f; ix += 0.2f) {
            for (float iy = 0.0f; iy < 1.0f; iy += 0.2f) {
                for (float iz = 0.0f; iz < 1.0f; iz += 0.2f) {
                    double f;
                    double x = box.minX + (double)ix * box.maxX;
                    double y = box.minY + (double)iy * box.maxY;
                    double z = box.minZ + (double)iz * box.maxZ;
                    Vec3d vec = new Vec3d(x, y, z);
                    RayTraceResult trace = AutoCrystalUtil.mc.world.func_72933_a(posEyes, vec);
                    if (trace == null || !((f = vec.distanceTo(posEyes)) < bestDistance)) continue;
                    bestDistance = f;
                    best = vec;
                }
            }
        }
        return BlockUtils.getLegitRotationsVector(best);
    }

    public static CPacketUseEntity createAttackPacket(int eid) {
        CPacketUseEntity packet = new CPacketUseEntity();
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(eid);
        buffer.writeEnumValue((Enum)CPacketUseEntity.Action.ATTACK);
        try {
            packet.readPacketData(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }

    public static float getExplosionCost(EntityLivingBase entity, double x, double y, double z) {
        double d9;
        double d7;
        double d5;
        double d13;
        World world = TTCp.world;
        float dmg = 0.0f;
        float f3 = 12.0f;
        Vec3d vec3d = new Vec3d(x += 0.5, y, z += 0.5);
        double d12 = entity.func_70011_f(x, y, z) / (double)f3;
        if (d12 <= 1.0 && (d13 = (double)MathHelper.sqrt((double)((d5 = entity.field_70165_t - x) * d5 + (d7 = entity.field_70163_u + (double)entity.func_70047_e() - y) * d7 + (d9 = entity.field_70161_v - z) * d9))) != 0.0) {
            double d14 = world.getBlockDensity(vec3d, entity.func_174813_aQ());
            double d10 = (1.0 - d12) * d14;
            dmg += (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0));
        }
        dmg = CombatRules.getDamageAfterAbsorb((float)dmg, (float)entity.getTotalArmorValue(), (float)((float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()));
        if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            int i = (Objects.requireNonNull(entity.getActivePotionEffect(MobEffects.RESISTANCE)).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = dmg * (float)j;
            dmg = f / 25.0f;
        }
        if (dmg <= 0.0f) {
            return 0.0f;
        }
        try {
            int k = EnchantmentHelper.getEnchantmentModifierDamage((Iterable)entity.getArmorInventoryList(), (DamageSource)DamageSource.GENERIC);
            if (k > 0) {
                dmg = CombatRules.getDamageAfterMagicAbsorb((float)dmg, (float)k);
            }
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
        return Math.max(dmg, 0.0f);
    }

    public static boolean canPlace(BlockPos pos, float crystalRange) {
        EntityPlayerSP player = TTCp.player;
        Vec3d vec3d = new Vec3d((Vec3i)pos);
        if (player.getPositionEyes(1.0f).distanceTo(vec3d) > (double)crystalRange) {
            return false;
        }
        World world = TTCp.world;
        IBlockState iblockstate = world.getBlockState(pos);
        if (iblockstate.getBlock() != Blocks.OBSIDIAN && iblockstate.getBlock() != Blocks.BEDROCK) {
            return false;
        }
        BlockPos blockpos = pos.up();
        BlockPos blockpos1 = blockpos.up();
        boolean flag = !world.isAirBlock(blockpos) && !world.getBlockState(blockpos).getBlock().isReplaceable((IBlockAccess)world, blockpos);
        if (flag |= !world.isAirBlock(blockpos1) && !world.getBlockState(blockpos1).getBlock().isReplaceable((IBlockAccess)world, blockpos1)) {
            return false;
        }
        double d0 = blockpos.func_177958_n();
        double d1 = blockpos.func_177956_o();
        double d2 = blockpos.func_177952_p();
        AxisAlignedBB thisHitbox = new AxisAlignedBB(d0, d1, d2, d0 + 1.0, d1 + 2.0, d2 + 1.0);
        List list = world.getEntitiesWithinAABBExcludingEntity(null, thisHitbox);
        return list.isEmpty();
    }
}
