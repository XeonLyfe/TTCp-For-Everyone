package tudbut.mod.client.ttcp.gui;

import de.tudbut.type.Vector3d;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.EventHandler;
import tudbut.mod.client.ttcp.mods.combat.AutoTotem;
import tudbut.mod.client.ttcp.mods.combat.HopperAura;
import tudbut.mod.client.ttcp.mods.combat.PopCount;
import tudbut.mod.client.ttcp.mods.misc.PlayerSelector;
import tudbut.mod.client.ttcp.mods.rendering.HUD;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Utils;
import tudbut.obj.Vector2i;

public class GuiTTCIngame
extends Gui {
    public static void draw() {
        new GuiTTCIngame().drawImpl();
    }

    public static void drawOffhandSlot(int x, int y) {
        new GuiTTCIngame().drawOffhandSlot0(x, y);
    }

    public void drawOffhandSlot0(int x, int y) {
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        TTCp.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));
        this.drawTexturedModalRect(x, y, 24, 22, 29, 24);
    }

    public static void drawItem(int x, int y, float partialTicks, EntityPlayer player, ItemStack stack) {
        Method m = Utils.getMethods(GuiIngame.class, Integer.TYPE, Integer.TYPE, Float.TYPE, EntityPlayer.class, ItemStack.class)[0];
        m.setAccessible(true);
        try {
            GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();
            m.invoke(Minecraft.getMinecraft().ingameGUI, x, y, Float.valueOf(partialTicks), player, stack);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void drawImpl() {
        ScaledResolution sr = new ScaledResolution(TTCp.mc);
        Vector2i screenSize = new Vector2i(sr.getScaledWidth(), sr.getScaledHeight());
        int y = sr.getScaledHeight() - (5 + TTCp.mc.fontRenderer.FONT_HEIGHT);
        int x = screenSize.getX() - 5;
        if (!TTCp.isIngame()) {
            return;
        }
        y = this.drawPos((Entity)TTCp.player, "Player", x, y);
        if (TTCp.mc.getRenderViewEntity() != TTCp.player) {
            y = this.drawPos(Objects.requireNonNull(TTCp.mc.getRenderViewEntity()), "Camera", x, y);
        }
        this.drawString("Ping: " + EventHandler.ping[0] + " | TPS: " + Utils.roundTo(EventHandler.tps, 2) + " | Players: " + EventHandler.ping[1] + "/" + EventHandler.ping[2], x, y, -16711936);
        y -= 10;
        y -= 10;
        for (int i = 0; i < TTCp.modules.length; ++i) {
            Module module = TTCp.modules[i];
            if (module == null) {
                return;
            }
            if (!module.enabled || !module.displayOnClickGUI()) continue;
            int color = 0;
            switch (module.danger()) {
                case 0: {
                    color = 65280;
                    break;
                }
                case 1: {
                    color = 0x80FF00;
                    break;
                }
                case 2: {
                    color = 0xFFFF00;
                    break;
                }
                case 3: {
                    color = 0xFF8000;
                    break;
                }
                case 4: {
                    color = 0xFF0000;
                    break;
                }
                case 5: {
                    color = 0xFF00FF;
                }
            }
            this.drawString(module.toString(), x, y, color);
            y -= 10;
        }
        if (HopperAura.getInstance().enabled) {
            String s = "HopperAura state:";
            this.drawString(s, sr.getScaledWidth() / 2 + TTCp.mc.fontRenderer.getStringWidth(s) / 2, sr.getScaledHeight() / 2 + 60, -65536);
            s = HopperAura.state.toString();
            this.drawString(s, sr.getScaledWidth() / 2 + TTCp.mc.fontRenderer.getStringWidth(s) / 2, sr.getScaledHeight() / 2 + 70, -65536);
        }
        Notifications notifications = TTCp.getModule(Notifications.class);
        if (notifications.enabled) {
            x = sr.getScaledWidth() / 2 - 150;
            y = sr.getScaledHeight() / 4;
            Notifications.Notification[] notifs = Notifications.getNotifications().toArray(new Notifications.Notification[0]);
            for (int i = 0; i < notifs.length; ++i) {
                GuiTTCIngame.drawRect((int)x, (int)y, (int)(x + 300), (int)(y + 30), (int)-2145378240);
                this.drawStringL(notifs[i].text, x + 10, y + 11, -1);
                y -= 35;
            }
        }
        if (TTCp.getModule(PlayerSelector.class).enabled) {
            try {
                PlayerSelector.render();
            }
            catch (Exception notifs) {
                // empty catch block
            }
        }
        AutoTotem autoTotem = TTCp.getModule(AutoTotem.class);
        if (HUD.getInstance().showPopPredict) {
            PopCount popCount = TTCp.getModule(PopCount.class);
            PopCount.Counter counter = popCount.counters.get((EntityPlayer)TTCp.player);
            if (counter != null && counter.isPopping()) {
                x = sr.getScaledWidth() / 2 - 100;
                y = sr.getScaledHeight() - sr.getScaledHeight() / 3;
                GuiTTCIngame.drawRect((int)(x - 1), (int)(y - 1), (int)(x + 200 + 1), (int)(y + 20 + 1), (int)0x40202040);
                float f = counter.predictPopProgress();
                if ((double)f >= 0.95) {
                    GuiTTCIngame.drawRect((int)x, (int)y, (int)(x + 200), (int)(y + 20), (int)-65536);
                } else {
                    GuiTTCIngame.drawRect((int)x, (int)y, (int)((int)((float)x + f * 200.0f)), (int)(y + 20), (int)(Integer.MIN_VALUE + (255 << (int)Math.ceil(f * 16.0f))));
                }
                this.drawStringL((int)(f * 100.0f) + "%", x + 6, y + 6, -1);
            } else if (counter == null) {
                System.out.println("PopCount counter null?");
                ChatUtils.chatPrinterDebug().println("PopCount counter null? ");
            }
        }
    }

    private void drawString(String s, int x, int y, int color) {
        this.drawString(TTCp.mc.fontRenderer, s, x - TTCp.mc.fontRenderer.getStringWidth(s), y, color);
    }

    private void drawStringL(String s, int x, int y, int color) {
        this.drawString(TTCp.mc.fontRenderer, s, x, y, color);
    }

    private int drawPos(Entity e, String s, int x, int y) {
        Vector3d p = new Vector3d(e.posX, e.posY, e.posZ);
        p.setX((double)Math.round(p.getX() * 10.0) / 10.0);
        p.setY((double)Math.round(p.getY() * 10.0) / 10.0);
        p.setZ((double)Math.round(p.getZ() * 10.0) / 10.0);
        if (TTCp.mc.world.field_73011_w.getDimension() == -1) {
            this.drawString(s + " Overworld " + (double)Math.round(p.getX() * 8.0 * 10.0) / 10.0 + " " + (double)Math.round(p.getY() * 10.0) / 10.0 + " " + (double)Math.round(p.getZ() * 8.0 * 10.0) / 10.0, x, y, -16711936);
        }
        if (TTCp.mc.world.field_73011_w.getDimension() == 0) {
            this.drawString(s + " Nether " + (double)Math.round(p.getX() / 8.0 * 10.0) / 10.0 + " " + (double)Math.round(p.getY() * 10.0) / 10.0 + " " + (double)Math.round(p.getZ() / 8.0 * 10.0) / 10.0, x, y, -16711936);
        }
        this.drawString(s + " " + p.getX() + " " + p.getY() + " " + p.getZ(), x, y -= 10, -16711936);
        return y - 10;
    }
}
