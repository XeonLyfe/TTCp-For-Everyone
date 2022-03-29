package tudbut.mod.client.ttcp.utils;

import de.tudbut.type.Vector3d;
import java.awt.Color;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import tudbut.net.ic.PBIC;

public class Tesselator {
    static int mode;
    static int color;
    static Vector3d translated;
    static boolean depth;

    public static void ready() {
        GL11.glPushMatrix();
    }

    public static void translate(double x, double y, double z) {
        GL11.glTranslated((double)x, (double)y, (double)z);
        translated = new Vector3d(x, y, z);
    }

    public static void begin(int modeIn) {
        mode = modeIn;
        GL11.glBegin((int)mode);
    }

    public static void color(int argb) {
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2896);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)2884);
        GL11.glBlendFunc((int)770, (int)771);
        byte[] bytes = PBIC.putInt(argb);
        GL11.glColor4ub((byte)bytes[1], (byte)bytes[2], (byte)bytes[3], (byte)bytes[0]);
        color = argb;
    }

    public static void depth(boolean b) {
        depth = b;
        if (b) {
            GL11.glEnable((int)2929);
        } else {
            GL11.glClear((int)256);
        }
    }

    public static void put(double x, double y, double z) {
        GL11.glVertex3d((double)x, (double)y, (double)z);
    }

    public static void end() {
        translated = null;
        color = 0;
        depth = false;
        mode = 0;
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2884);
        GL11.glPopMatrix();
    }

    public static void next() {
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2884);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated((double)translated.getX(), (double)translated.getY(), (double)translated.getZ());
        Tesselator.color(color);
        Tesselator.depth(depth);
        GL11.glBegin((int)mode);
    }

    public static void drawAroundBlock(BlockPos pos, int color, Vec3d eyePos) {
        try {
            Tesselator.ready();
            Tesselator.translate(-eyePos.x, -eyePos.y, -eyePos.z);
            Tesselator.color(color);
            Tesselator.depth(false);
            Tesselator.begin(1);
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p());
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n(), pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.put(pos.func_177958_n() + 1, pos.func_177956_o() + 1, pos.func_177952_p() + 1);
            Tesselator.end();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawAroundBlock(BlockPos pos, Color color, Vec3d eyePos) {
        Tesselator.drawAroundBlock(pos, color.getRGB(), eyePos);
    }
}
