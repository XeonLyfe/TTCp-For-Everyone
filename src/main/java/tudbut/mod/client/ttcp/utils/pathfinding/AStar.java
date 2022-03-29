package tudbut.mod.client.ttcp.utils.pathfinding;

import java.util.ArrayList;
import java.util.Date;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import tudbut.mod.client.ttcp.utils.pathfinding.Node;

public class AStar {
    public static Node[][] calculate(BlockPos start, BlockPos end, World world, int timeout) {
        long sa = new Date().getTime();
        try {
            ArrayList<Node> open = new ArrayList<Node>();
            ArrayList<Node> closed = new ArrayList<Node>();
            Node startNode = new Node((Vec3i)start);
            Node endNode = new Node((Vec3i)end);
            startNode.calcFGH(endNode, 0);
            open.add(startNode);
            while (closed.stream().noneMatch(node -> node.equals((Object)endNode))) {
                if (new Date().getTime() - sa >= (long)timeout) {
                    return new Node[2][0];
                }
                Node current = (Node)((Object)open.get(0));
                for (int i = 1; i < open.size(); ++i) {
                    Node node2 = (Node)((Object)open.get(i));
                    if (node2.f >= current.f) continue;
                    current = node2;
                }
                open.remove((Object)current);
                closed.add(current);
                ArrayList<BlockPos> next = AStar.getAdjacent3D(current);
                for (int i = 0; i < next.size(); ++i) {
                    BlockPos bp = next.get(i);
                    int work = AStar.calcWork(world, bp);
                    if (work != -1) {
                        Node n = new Node((Vec3i)bp);
                        n.parent = current;
                        n.calcFGH(endNode, work);
                        if (open.contains((Object)n)) {
                            if (n.g < ((Node)((Object)open.get((int)open.indexOf((Object)((Object)n))))).g) {
                                open.remove((Object)n);
                                open.add(n);
                            }
                        } else {
                            open.add(n);
                        }
                    }
                    if (new Date().getTime() - sa < (long)timeout) continue;
                    return new Node[2][0];
                }
            }
            ArrayList<Node> list = new ArrayList<Node>();
            Node n = (Node)((Object)closed.get(closed.size() - 1));
            while (n.getParent() != null) {
                list.add(n);
                n = n.getParent();
            }
            list.add(startNode);
            Node[] nodes = new Node[list.size()];
            for (int i = 0; i < nodes.length; ++i) {
                nodes[i] = (Node)((Object)list.get(nodes.length - 1 - i));
            }
            return new Node[][]{nodes, closed.toArray(new Node[0])};
        }
        catch (IndexOutOfBoundsException e) {
            return new Node[2][0];
        }
    }

    private static int calcWork(World world, BlockPos bp) {
        BlockPos[] positions = AStar.getAdjacent3D(bp).toArray(new BlockPos[0]);
        int work = 0;
        boolean infinite = false;
        for (int i = 0; i < positions.length; ++i) {
            bp = positions[i].add(0, -1, 0);
            IBlockState blockState = world.isBlockLoaded(bp) ? world.getBlockState(bp) : Blocks.OBSIDIAN.getDefaultState();
            float h = blockState.func_185887_b(world, bp);
            if (h == -1.0f) {
                infinite = true;
                continue;
            }
            float damage = 1.0f / h / 30.0f;
            work = (int)((float)work + 100.0f / damage);
        }
        if (infinite) {
            return -1;
        }
        return work;
    }

    public static ArrayList<BlockPos> getAdjacent3D(BlockPos pos) {
        ArrayList<BlockPos> r = new ArrayList<BlockPos>();
        r.add(pos.add(-1, 1, -1));
        r.add(pos.add(-1, 1, 0));
        r.add(pos.add(-1, 1, 1));
        r.add(pos.add(0, 1, -1));
        r.add(pos.add(0, 1, 0));
        r.add(pos.add(0, 1, 1));
        r.add(pos.add(1, 1, -1));
        r.add(pos.add(1, 1, 0));
        r.add(pos.add(1, 1, 1));
        r.add(pos.add(-1, 0, -1));
        r.add(pos.add(-1, 0, 0));
        r.add(pos.add(-1, 0, 1));
        r.add(pos.add(0, 0, -1));
        r.add(pos.add(0, 0, 1));
        r.add(pos.add(1, 0, -1));
        r.add(pos.add(1, 0, 0));
        r.add(pos.add(1, 0, 1));
        r.add(pos.add(-1, -1, -1));
        r.add(pos.add(-1, -1, 0));
        r.add(pos.add(-1, -1, 1));
        r.add(pos.add(0, -1, -1));
        r.add(pos.add(0, -1, 0));
        r.add(pos.add(0, -1, 1));
        r.add(pos.add(1, -1, -1));
        r.add(pos.add(1, -1, 0));
        r.add(pos.add(1, -1, 1));
        return r;
    }
}
