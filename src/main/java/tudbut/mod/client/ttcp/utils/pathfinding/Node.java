package tudbut.mod.client.ttcp.utils.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Node
extends BlockPos {
    int f = 0;
    int g = 0;
    int h = 0;
    Node parent;

    public Node(int x, int y, int z) {
        super(x, y, z);
    }

    public Node(double x, double y, double z) {
        super(x, y, z);
    }

    public Node(Entity source) {
        super(source);
    }

    public Node(Vec3d vec) {
        super(vec);
    }

    public Node(Vec3i source) {
        super(source);
    }

    void calcFGH(Node end, int i) {
        if (this.parent != null) {
            this.g = this.parent.g;
        }
        this.g += this.g + i;
        double dx = end.func_177958_n() - this.func_177958_n();
        double dy = end.func_177956_o() - this.func_177956_o();
        double dz = end.func_177952_p() - this.func_177952_p();
        this.h = (int)(dx * dx + dy * dy + dz * dz);
        this.f = this.g + this.h;
    }

    public Node getParent() {
        return this.parent;
    }
}
