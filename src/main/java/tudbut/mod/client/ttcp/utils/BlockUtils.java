package tudbut.mod.client.ttcp.utils;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ThreadManager;

public class BlockUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static ArrayList<Block> blackList = new ArrayList<Block>(Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_COMPARATOR, Blocks.POWERED_REPEATER, Blocks.UNPOWERED_REPEATER, Blocks.CAKE, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.IRON_TRAPDOOR));
    public static ArrayList<Block> shulkerList = new ArrayList<Block>(Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX));
    private static BlockPos breaking = null;
    private static Runnable done = null;

    public static void attackEntityByID(int id) {
        CPacketUseEntity packet = new CPacketUseEntity();
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(id);
        buf.writeEnumValue((Enum)CPacketUseEntity.Action.ATTACK);
        try {
            packet.readPacketData(buf);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        TTCp.player.connection.sendPacket((Packet)packet);
    }

    public static void placeBlock(BlockPos pos, boolean rotate) {
        if (pos == null) {
            return;
        }
        EnumFacing side = BlockUtils.getPlaceableSide(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d((Vec3i)neighbour).addVector(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtils.mc.world.func_180495_p(neighbour).getBlock();
        if (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock)) {
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (rotate) {
            BlockUtils.faceVectorPacketInstant(hitVec);
        }
        BlockUtils.mc.playerController.processRightClickBlock(BlockUtils.mc.player, BlockUtils.mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        BlockUtils.mc.player.swingArm(EnumHand.MAIN_HAND);
        if (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock)) {
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public static void placeBlockPacket(BlockPos pos, boolean rotate) {
        if (pos == null) {
            return;
        }
        EnumFacing side = BlockUtils.getPlaceableSide(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d((Vec3i)neighbour).addVector(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtils.mc.world.func_180495_p(neighbour).getBlock();
        if (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock)) {
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (rotate) {
            BlockUtils.faceVectorPacketInstant(hitVec);
        }
        float f = (float)(hitVec.x - (double)pos.func_177958_n());
        float f1 = (float)(hitVec.y - (double)pos.func_177956_o());
        float f2 = (float)(hitVec.z - (double)pos.func_177952_p());
        BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, opposite, EnumHand.MAIN_HAND, f, f1, f2));
        BlockUtils.mc.player.swingArm(EnumHand.MAIN_HAND);
        if (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock)) {
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public static boolean placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = BlockUtils.getFirstFacing(pos);
        if (side == null) {
            return false;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d((Vec3i)neighbour).addVector(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtils.mc.world.func_180495_p(neighbour).getBlock();
        if (!BlockUtils.mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtils.mc.player.func_70095_a(true);
        }
        if (rotate) {
            BlockUtils.faceVector(hitVec, true);
        }
        BlockUtils.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        if (BlockUtils.mc.player.isSneaking()) {
            ThreadManager.run(() -> {
                try {
                    Thread.sleep(50L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)BlockUtils.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                BlockUtils.mc.player.func_70095_a(false);
            });
        }
        return true;
    }

    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        ArrayList<EnumFacing> facings = new ArrayList<EnumFacing>();
        for (EnumFacing side : EnumFacing.values()) {
            IBlockState blockState;
            BlockPos neighbour = pos.offset(side);
            if (!BlockUtils.mc.world.func_180495_p(neighbour).getBlock().canCollideCheck(BlockUtils.mc.world.func_180495_p(neighbour), false) || (blockState = BlockUtils.mc.world.func_180495_p(neighbour)).func_185904_a().isReplaceable()) continue;
            facings.add(side);
        }
        return facings;
    }

    public static EnumFacing getFirstFacing(BlockPos pos) {
        Iterator<EnumFacing> iterator = BlockUtils.getPossibleSides(pos).iterator();
        if (iterator.hasNext()) {
            EnumFacing facing = iterator.next();
            return facing;
        }
        return null;
    }

    public static Vec3d getEyesPos() {
        return new Vec3d(BlockUtils.mc.player.field_70165_t, BlockUtils.mc.player.field_70163_u + (double)BlockUtils.mc.player.func_70047_e(), BlockUtils.mc.player.field_70161_v);
    }

    public static void faceVector(Vec3d vec, boolean normalizeAngle) {
        float[] rotations = BlockUtils.getLegitRotations(vec);
        BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(rotations[0], normalizeAngle ? (float)MathHelper.normalizeAngle((int)((int)rotations[1]), (int)360) : rotations[1], BlockUtils.mc.player.field_70122_E));
    }

    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float)(vec.x - (double)pos.func_177958_n());
            float f1 = (float)(vec.y - (double)pos.func_177956_o());
            float f2 = (float)(vec.z - (double)pos.func_177952_p());
            BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            BlockUtils.mc.playerController.processRightClickBlock(BlockUtils.mc.player, BlockUtils.mc.world, pos, direction, vec, hand);
        }
        BlockUtils.mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    public static BlockPos getRealPos(Vec3d vec3d) {
        return new BlockPos((int)(vec3d.x < 0.0 ? Math.floor(vec3d.x) : vec3d.x), (int)(vec3d.y < 0.0 ? Math.floor(vec3d.y) : vec3d.y), (int)(vec3d.z < 0.0 ? Math.floor(vec3d.z) : vec3d.z));
    }

    public static boolean clickOnBlock(BlockPos pos, EnumHand hand) {
        return BlockUtils.clickOnBlock(pos, hand, true);
    }

    public static boolean clickOnBlock(BlockPos pos, EnumHand hand, boolean rotate) {
        if (pos == null) {
            return false;
        }
        Vec3d hitVec = new Vec3d((Vec3i)pos).addVector(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.UP.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtils.mc.world.func_180495_p(pos).getBlock();
        if (rotate) {
            BlockUtils.faceVectorPacketInstant(hitVec);
        }
        float f = (float)(hitVec.x - (double)pos.func_177958_n());
        float f1 = (float)(hitVec.y - (double)pos.func_177956_o());
        float f2 = (float)(hitVec.z - (double)pos.func_177952_p());
        BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(BlockUtils.mc.player.field_71071_by.currentItem));
        BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, hand, f, f1, f2));
        BlockUtils.mc.player.swingArm(hand);
        return true;
    }

    public static void tick() {
        if (breaking != null) {
            BlockUtils.breakBlock(breaking, done);
        }
    }

    public static void breakBlock(BlockPos pos, Runnable done) {
        BlockUtils.lookAt(new Vec3d((Vec3i)pos));
        breaking = pos;
        BlockUtils.done = done;
        BlockUtils.mc.playerController.onPlayerDamageBlock(pos, EnumFacing.DOWN);
        BlockUtils.mc.playerController.getIsHittingBlock();
    }

    public static void lookAt(Vec3d pos) {
        BlockUtils.faceVectorPacketInstant(pos);
    }

    public static void lookCloserTo(Vec3d pos, float amountMax) {
        BlockUtils.mc.player.field_70177_z = MathHelper.wrapDegrees((float)BlockUtils.mc.player.field_70177_z);
        BlockUtils.mc.player.field_70125_A = MathHelper.wrapDegrees((float)BlockUtils.mc.player.field_70125_A);
        float[] rotations = BlockUtils.getLegitRotations(pos);
        rotations[0] = rotations[0] - BlockUtils.mc.player.field_70177_z;
        rotations[1] = rotations[1] - BlockUtils.mc.player.field_70125_A;
        float length = (float)Math.sqrt(rotations[0] * rotations[0] + rotations[1] * rotations[1]);
        rotations = BlockUtils.getLegitRotations(pos);
        rotations[0] = MathHelper.wrapDegrees((float)((rotations[0] + 180.0f) % 360.0f - (BlockUtils.mc.player.field_70177_z + 180.0f) % 360.0f));
        rotations[1] = MathHelper.wrapDegrees((float)((rotations[1] + 180.0f) % 360.0f - (BlockUtils.mc.player.field_70125_A + 180.0f) % 360.0f));
        if ((length = Math.min(length, (float)Math.sqrt(rotations[0] * rotations[0] + rotations[1] * rotations[1]))) > 1.0f) {
            rotations[0] = rotations[0] / length * amountMax;
            rotations[1] = rotations[1] / length * amountMax;
        } else {
            rotations[0] = rotations[0] / length * (amountMax / 18.0f);
            rotations[1] = rotations[1] / length * (amountMax / 18.0f);
        }
        BlockUtils.mc.player.field_70177_z += rotations[0];
        BlockUtils.mc.player.field_70125_A += rotations[1];
    }

    private static EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            IBlockState blockState;
            BlockPos neighbour = pos.offset(side);
            if (!BlockUtils.mc.world.func_180495_p(neighbour).getBlock().canCollideCheck(BlockUtils.mc.world.func_180495_p(neighbour), false) || (blockState = BlockUtils.mc.world.func_180495_p(neighbour)).func_185904_a().isReplaceable() || blockState.getBlock() instanceof BlockTallGrass || blockState.getBlock() instanceof BlockDeadBush) continue;
            return side;
        }
        return null;
    }

    private static Vec3d eyesPos() {
        return new Vec3d(BlockUtils.mc.player.field_70165_t, BlockUtils.mc.player.field_70163_u + (double)BlockUtils.mc.player.func_70047_e(), BlockUtils.mc.player.field_70161_v);
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = BlockUtils.eyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new float[]{(float)MathHelper.wrapDegrees((double)yaw), (float)MathHelper.wrapDegrees((double)pitch)};
    }

    public static Vec2f getLegitRotationsVector(Vec3d vec) {
        Vec3d eyesPos = BlockUtils.eyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new Vec2f((float)MathHelper.wrapDegrees((double)yaw), (float)MathHelper.wrapDegrees((double)pitch));
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = BlockUtils.getLegitRotations(vec);
        BlockUtils.mc.player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(BlockUtils.mc.player.field_70165_t, BlockUtils.mc.player.field_70163_u, BlockUtils.mc.player.field_70161_v, rotations[0], rotations[1], BlockUtils.mc.player.field_70122_E));
    }

    public static BlockPos findBlock(Block ... blocks) {
        World world = TTCp.world;
        BlockPos origin = TTCp.player.getPosition();
        for (int z = -5; z <= 5; ++z) {
            for (int y = -3; y <= 7; ++y) {
                for (int x = -5; x <= 5; ++x) {
                    BlockPos pos = origin.add(x, y, z);
                    for (Block block : blocks) {
                        if (world.getBlockState(pos).getBlock() != block) continue;
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
