package tudbut.mod.client.ttcp.events;

import java.util.Date;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.events.ModuleEventRegistry;
import tudbut.mod.client.ttcp.events.ParticleLoop;
import tudbut.mod.client.ttcp.mods.chat.ChatColor;
import tudbut.mod.client.ttcp.mods.chat.ChatSuffix;
import tudbut.mod.client.ttcp.mods.chat.DM;
import tudbut.mod.client.ttcp.mods.chat.DMChat;
import tudbut.mod.client.ttcp.mods.chat.TPAParty;
import tudbut.mod.client.ttcp.mods.combat.AutoCrystal;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.rendering.Freecam;
import tudbut.mod.client.ttcp.mods.rendering.HUD;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.DebugProfilerAdapter;
import tudbut.mod.client.ttcp.utils.KillSwitch;
import tudbut.mod.client.ttcp.utils.ThreadManager;
import tudbut.mod.client.ttcp.utils.Utils;

public class EventHandler {
    public static long[] ping = new long[]{-1L, 1L, 1L};
    public static float tps = 20.0f;
    private static long lastTick = -1L;
    private static long joinTime = 0L;
    private boolean isDead = true;
    public static final DebugProfilerAdapter profilerPackets = new DebugProfilerAdapter("Packets", "idle");
    public static final DebugProfilerAdapter profilerTicks = new DebugProfilerAdapter("Ticks", "idle");
    public static final DebugProfilerAdapter profilerChat = new DebugProfilerAdapter("Chat", "idle");
    public static final DebugProfilerAdapter profilerChatReceive = new DebugProfilerAdapter("ChatReceive", "idle");
    public static final DebugProfilerAdapter profilerRenderHUD = new DebugProfilerAdapter("RenderHUD", "idle");
    boolean allowHUDRender = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean onPacket(Packet<?> packet) {
        DebugProfilerAdapter debugProfilerAdapter = profilerPackets;
        synchronized (debugProfilerAdapter) {
            boolean b = false;
            if (packet instanceof SPacketTimeUpdate) {
                long time = System.currentTimeMillis();
                if (lastTick != -1L && new Date().getTime() - joinTime > 5000L) {
                    long diff = time - lastTick;
                    if (diff > 50L) {
                        tps = (tps + 1000.0f / (float)diff * 20.0f) / 2.0f;
                    }
                } else {
                    tps = 20.0f;
                }
                lastTick = time;
            }
            for (int i = 0; i < TTCp.modules.length; ++i) {
                if (!TTCp.modules[i].enabled) continue;
                try {
                    profilerPackets.next(TTCp.modules[i] + " " + packet.getClass().getName());
                    if (!TTCp.modules[i].onPacket(packet)) continue;
                    b = true;
                    continue;
                }
                catch (Exception e) {
                    e.printStackTrace(ChatUtils.chatPrinterDebug());
                }
            }
            profilerPackets.next("idle");
            return b;
        }
    }

    @SubscribeEvent
    public void onEvent(Event event) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        DebugProfilerAdapter debugProfilerAdapter = profilerChat;
        synchronized (debugProfilerAdapter) {
            if (event.getOriginalMessage().startsWith(TTCp.prefix)) {
                profilerChat.next("command " + event.getOriginalMessage());
                event.setCanceled(true);
                ChatUtils.print("Blocked message");
                ChatUtils.history(event.getOriginalMessage());
                String s = event.getOriginalMessage().substring(TTCp.prefix.length());
                try {
                    int i;
                    if (s.startsWith("t ")) {
                        for (i = 0; i < TTCp.modules.length; ++i) {
                            if (!TTCp.modules[i].toString().equalsIgnoreCase(s.substring("t ".length()))) continue;
                            ChatUtils.print(String.valueOf(!TTCp.modules[i].enabled));
                            TTCp.modules[i].enabled = !TTCp.modules[i].enabled;
                            if (TTCp.modules[i].enabled) {
                                TTCp.modules[i].onEnable();
                                continue;
                            }
                            TTCp.modules[i].onDisable();
                        }
                    }
                    if (s.startsWith("say ")) {
                        TTCp.player.sendChatMessage(s.substring("say ".length()));
                        ChatUtils.history(event.getOriginalMessage());
                    }
                    if (s.equals("help")) {
                        ChatUtils.print("Unable retrieve help message! Check your connection!");
                    }
                    if (s.startsWith("say ")) {
                        TTCp.player.sendChatMessage(s.substring("say ".length()));
                        ChatUtils.history(event.getOriginalMessage());
                    }
                    for (i = 0; i < TTCp.modules.length; ++i) {
                        if (!s.toLowerCase().startsWith(TTCp.modules[i].toString().toLowerCase())) continue;
                        System.out.println("Passing command to " + TTCp.modules[i].toString());
                        try {
                            String args = s.substring(TTCp.modules[i].toString().length() + 1);
                            if (TTCp.modules[i].enabled) {
                                TTCp.modules[i].onChat(args, args.split(" "));
                            }
                            TTCp.modules[i].onEveryChat(args, args.split(" "));
                            continue;
                        }
                        catch (StringIndexOutOfBoundsException e) {
                            String args = "";
                            if (TTCp.modules[i].enabled) {
                                TTCp.modules[i].onChat(args, new String[0]);
                            }
                            TTCp.modules[i].onEveryChat(args, new String[0]);
                        }
                    }
                }
                catch (Exception e) {
                    ChatUtils.print("Command failed!");
                    e.printStackTrace(ChatUtils.chatPrinterDebug());
                }
                profilerChat.next("idle");
            } else if (DM.getInstance().enabled) {
                profilerChat.next("dm");
                event.setCanceled(true);
                ChatUtils.history(event.getOriginalMessage());
                ThreadManager.run(() -> {
                    for (int i = 0; i < DM.getInstance().users.length; ++i) {
                        TTCp.player.sendChatMessage("/tell " + DM.getInstance().users[i] + " " + event.getOriginalMessage());
                        try {
                            Thread.sleep(1000L);
                            continue;
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                            e.printStackTrace(ChatUtils.chatPrinterDebug());
                        }
                    }
                });
                profilerChat.next("idle");
            } else if (DMChat.getInstance().enabled) {
                profilerChat.next("dm");
                event.setCanceled(true);
                ChatUtils.history(event.getOriginalMessage());
                ThreadManager.run(() -> {
                    ChatUtils.print("<" + TTCp.player.func_70005_c_() + "> " + event.getOriginalMessage());
                    for (int i = 0; i < DMChat.getInstance().users.length; ++i) {
                        TTCp.player.sendChatMessage("/tell " + DMChat.getInstance().users[i] + " " + event.getOriginalMessage());
                        try {
                            Thread.sleep(1000L);
                            continue;
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                profilerChat.next("idle");
            } else if (!(event.getOriginalMessage().startsWith("/") || event.getOriginalMessage().startsWith(".") || event.getOriginalMessage().startsWith("#"))) {
                profilerChat.next("command");
                event.setCanceled(true);
                TTCp.player.sendChatMessage(ChatColor.getInstance().get() + event.getMessage() + ChatSuffix.getInstance().get(ChatSuffix.getInstance().chance));
                ChatUtils.history(event.getOriginalMessage());
                profilerChat.next("idle");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onServerChat(ClientChatReceivedEvent event) {
        DebugProfilerAdapter debugProfilerAdapter = profilerChatReceive;
        synchronized (debugProfilerAdapter) {
            String key;
            profilerChatReceive.next("checkCaptcha");
            if (event.getMessage().getUnformattedText().startsWith("BayMax") && event.getMessage().getUnformattedText().contains("Please type '")) {
                key = event.getMessage().getUnformattedText().substring("BayMax _ Please type '".length(), "BayMax _ Please type '".length() + 4);
                TTCp.player.sendChatMessage(key);
                ChatUtils.print("Auto-solved");
            }
            if (event.getMessage().getUnformattedText().startsWith("Please type '") && event.getMessage().getUnformattedText().endsWith("' to continue sending messages/commands.")) {
                key = event.getMessage().getUnformattedText().substring("Please type '".length(), "Please type '".length() + 6);
                TTCp.player.sendChatMessage(key);
                ChatUtils.print("Auto-solved");
            }
            for (int i = 0; i < TTCp.modules.length; ++i) {
                if (!TTCp.modules[i].enabled) continue;
                profilerChatReceive.next("module " + TTCp.modules[i]);
                if (!TTCp.modules[i].onServerChat(event.getMessage().getUnformattedText(), event.getMessage().getFormattedText())) continue;
                event.setCanceled(true);
            }
            profilerChatReceive.next("idle");
        }
    }

    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChatUtils.print("§a§lTTC has a Discord server: https://discord.gg/2WsVCQDpwy!");
        tps = 20.0f;
        lastTick = -1L;
        joinTime = new Date().getTime();
        ModuleEventRegistry.onNewPlayer();
        ThreadManager.run(() -> {
            try {
                Thread.sleep(10000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                e.printStackTrace(ChatUtils.chatPrinterDebug());
            }
            block6: while (TTCp.mc.world != null) {
                String s = Utils.getLatestVersion();
                if (s == null) {
                    ChatUtils.print("Unable to check for a new version! Check your connection!");
                } else if (!s.equals("vB1.9.0")) {
                    ChatUtils.print("§a§lA new TTCp version was found! Current: vB1.9.0, New: " + s);
                }
                try {
                    for (int i = 0; i < 60; ++i) {
                        Thread.sleep(1000L);
                        if (i % 5 == 0) {
                            try {
                                ServerData serverData = TTCp.mc.getCurrentServerData();
                                if (serverData != null) {
                                    new Thread(() -> {
                                        long[] ping = Utils.getPingToServer(serverData);
                                        if (ping[0] != -1L) {
                                            ping = ping;
                                        }
                                    }).start();
                                } else {
                                    ping = new long[]{0L, 1L, 1L};
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (TTCp.mc.world == null) continue block6;
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    e.printStackTrace(ChatUtils.chatPrinterDebug());
                }
            }
        });
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        TTCp.player = Minecraft.getMinecraft().player;
        TTCp.world = Minecraft.getMinecraft().world;
    }

    public void onDeath(EntityPlayer player) {
        if (TPAParty.getInstance().disableOnDeath) {
            TPAParty.getInstance().enabled = false;
            TPAParty.getInstance().onDisable();
        }
        AutoCrystal.getInstance().enabled = false;
        AutoCrystal.getInstance().onDisable();
        KillAura.getInstance().enabled = false;
        KillAura.getInstance().onDisable();
        if (Freecam.getInstance().enabled) {
            Freecam.getInstance().enabled = false;
            Freecam.getInstance().onDisable();
        }
        ModuleEventRegistry.onNewPlayer();
        BlockPos pos = player.func_180425_c();
        ChatUtils.print("§c§l§k|||§c§l You died at " + pos.func_177958_n() + " " + pos.func_177956_o() + " " + pos.func_177952_p());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onHUDRender(RenderGameOverlayEvent.Post event) {
        DebugProfilerAdapter debugProfilerAdapter = profilerRenderHUD;
        synchronized (debugProfilerAdapter) {
            profilerRenderHUD.next("render");
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && this.allowHUDRender) {
                this.allowHUDRender = false;
                HUD.getInstance().renderHUD();
            }
            profilerRenderHUD.next("idle");
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        this.allowHUDRender = true;
    }

    @SubscribeEvent
    public void onOverlay(RenderBlockOverlayEvent event) {
        event.setCanceled(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onSubTick(TickEvent event) {
        try {
            DebugProfilerAdapter debugProfilerAdapter = profilerTicks;
            synchronized (debugProfilerAdapter) {
                if (TTCp.mc.world == null || TTCp.mc.player == null) {
                    return;
                }
                EntityPlayerSP player = TTCp.player;
                if (player == null || event.side == Side.SERVER) {
                    return;
                }
                for (int i = 0; i < TTCp.modules.length; ++i) {
                    TTCp.modules[i].player = player;
                    profilerTicks.next("Tick " + TTCp.modules[i]);
                    if (TTCp.modules[i].enabled) {
                        try {
                            TTCp.modules[i].onSubTick();
                        }
                        catch (Exception e) {
                            e.printStackTrace(ChatUtils.chatPrinterDebug());
                        }
                    }
                    TTCp.modules[i].onEverySubTick();
                }
                profilerTicks.next("idle");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        try {
            DebugProfilerAdapter debugProfilerAdapter = profilerTicks;
            synchronized (debugProfilerAdapter) {
                if (TTCp.mc.world == null || TTCp.mc.player == null) {
                    return;
                }
                if (event.phase != TickEvent.Phase.START) {
                    return;
                }
                if (event.type != TickEvent.Type.CLIENT) {
                    return;
                }
                EntityPlayerSP player = TTCp.player;
                if (player == null || event.side == Side.SERVER) {
                    return;
                }
                profilerTicks.next("TPS");
                long time = System.currentTimeMillis();
                long diff = time - lastTick;
                float f = 1000.0f / (float)diff * 20.0f;
                if (f < tps - 2.0f) {
                    tps = (tps + f) / 2.0f;
                }
                profilerTicks.next("KillSwitchCheck");
                if (KillSwitch.running && !KillSwitch.lock.isLocked()) {
                    throw new RuntimeException("KillSwitch triggered!");
                }
                profilerTicks.next("DeathCheck");
                if (player.func_110143_aJ() <= 0.0f) {
                    if (!this.isDead) {
                        this.isDead = true;
                        this.onDeath((EntityPlayer)player);
                    }
                } else {
                    this.isDead = false;
                }
                profilerTicks.next("ParticleLoop");
                ParticleLoop.run();
                for (int i = 0; i < TTCp.modules.length; ++i) {
                    TTCp.modules[i].player = player;
                    profilerTicks.next("Keybinds");
                    TTCp.modules[i].key.onTick();
                    try {
                        for (String key : TTCp.modules[i].customKeyBinds.keys()) {
                            if (!TTCp.modules[i].enabled && !TTCp.modules[i].customKeyBinds.get((String)key).alwaysOn) continue;
                            TTCp.modules[i].customKeyBinds.get(key).onTick();
                        }
                        profilerTicks.next("Tick " + TTCp.modules[i]);
                        if (TTCp.modules[i].enabled) {
                            TTCp.modules[i].onTick();
                        }
                        TTCp.modules[i].onEveryTick();
                        continue;
                    }
                    catch (NullPointerException nullPointerException) {
                        continue;
                    }
                    catch (Exception e) {
                        e.printStackTrace(ChatUtils.chatPrinterDebug());
                    }
                }
                profilerTicks.next("idle");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static {
        TTCp.registerProfiler(profilerPackets);
        TTCp.registerProfiler(profilerTicks);
        TTCp.registerProfiler(profilerChat);
        TTCp.registerProfiler(profilerChatReceive);
        TTCp.registerProfiler(profilerRenderHUD);
    }
}
