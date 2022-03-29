package tudbut.mod.client.ttcp.mods.misc;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.mods.combat.KillAura;
import tudbut.mod.client.ttcp.mods.combat.SmoothAura;
import tudbut.mod.client.ttcp.mods.command.Friend;
import tudbut.mod.client.ttcp.mods.rendering.Notifications;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;
import tudbut.mod.client.ttcp.utils.category.Misc;
import tudbut.obj.Save;

@Misc
public class MidClick
extends Module {
    public static Bind bindBlock = null;
    public static Bind bindPlayer = null;
    public static Bind bindEntity = null;
    @Save
    private static Bind cbb = CustomBindsBlock.NONE;
    @Save
    private static Bind cbp = CustomBindsPlayer.NONE;
    @Save
    private static Bind cbe = CustomBindsPlayer.NONE;
    boolean down = false;

    @Override
    public void updateBinds() {
        this.subComponents.clear();
        if (bindBlock != null) {
            this.subComponents.add(new Button("ModuleBindBlock " + bindBlock.getName(), it -> {
                bindBlock = null;
                MidClick.reload();
            }));
        } else {
            this.subComponents.add(new Button("ModuleBindBlock NONE", it -> {}));
        }
        if (bindPlayer != null) {
            this.subComponents.add(new Button("ModuleBindPlayer: " + bindPlayer.getName(), it -> {
                bindPlayer = null;
                MidClick.reload();
            }));
        } else {
            this.subComponents.add(new Button("ModuleBindPlayer: NONE", it -> {}));
        }
        if (bindEntity != null) {
            this.subComponents.add(new Button("ModuleBindEntity: " + bindEntity.getName(), it -> {
                bindEntity = null;
                MidClick.reload();
            }));
        } else {
            this.subComponents.add(new Button("ModuleBindEntity: NONE", it -> {}));
        }
        this.subComponents.add(Setting.createEnum(CustomBindsBlock.class, "CustomBindBlock", this, "cbb"));
        this.subComponents.add(Setting.createEnum(CustomBindsPlayer.class, "CustomBindPlayer", this, "cbp"));
        this.subComponents.add(Setting.createEnum(CustomBindsEntity.class, "CustomBindEntity", this, "cbe"));
    }

    @Override
    public void onSubTick() {
        if (Mouse.isButtonDown((int)2) && MidClick.mc.currentScreen == null) {
            if (!this.down) {
                this.run();
            }
            this.down = true;
        } else {
            this.down = false;
        }
    }

    private void run() {
        RayTraceResult hover = MidClick.mc.objectMouseOver;
        if (hover.entityHit != null) {
            if (bindPlayer != null && hover.entityHit instanceof EntityPlayer) {
                bindPlayer.call(this.createData(hover));
                return;
            }
            if (bindEntity != null) {
                bindEntity.call(this.createData(hover));
                return;
            }
            if (this.runCustomEntityBinds(hover)) {
                return;
            }
        }
        if (hover.getBlockPos() != null) {
            if (bindBlock != null) {
                bindBlock.call(this.createData(hover));
                return;
            }
            cbb.call(this.createData(hover));
        }
    }

    private boolean runCustomEntityBinds(RayTraceResult hover) {
        if (hover.entityHit instanceof EntityPlayer) {
            cbp.call(this.createData(hover));
        } else {
            cbe.call(this.createData(hover));
        }
        return cbe != CustomBindsEntity.NONE.toDo && cbp != CustomBindsPlayer.NONE.toDo;
    }

    private Bind.Data createData(final RayTraceResult hover) {
        return new Bind.Data(){

            @Override
            public BlockPos block() {
                return hover.getBlockPos();
            }

            @Override
            public Entity entity() {
                return hover.entityHit;
            }
        };
    }

    public static void set(Bind bind) {
        switch (bind.getType()) {
            case BLOCK: {
                bindBlock = bind;
                break;
            }
            case PLAYER: {
                bindPlayer = bind;
                break;
            }
            case ENTITY: {
                bindEntity = bind;
            }
        }
        MidClick.reload();
    }

    public static void reload() {
        TTCp.getModule(MidClick.class).updateBinds();
    }

    private static interface PartialBind
    extends Bind {
        @Override
        default public String getName() {
            return "";
        }

        @Override
        default public Bind.Type getType() {
            return null;
        }

        @Override
        public void call(Bind.Data var1);
    }

    public static interface Bind {
        public Type getType();

        public String getName();

        public void call(Data var1);

        public static interface Data {
            public BlockPos block();

            public Entity entity();
        }

        public static enum Type {
            BLOCK,
            ENTITY,
            PLAYER;

        }
    }

    public static enum CustomBindsEntity implements PartialBind
    {
        NONE(data -> {});

        public final Bind toDo;

        private CustomBindsEntity(PartialBind toDo) {
            this.toDo = toDo;
        }

        @Override
        public void call(Bind.Data data) {
            this.toDo.call(data);
        }
    }

    public static enum CustomBindsPlayer implements PartialBind
    {
        NONE(data -> {}),
        Friend(data -> {
            ArrayList<String> names = tudbut.mod.client.ttcp.mods.command.Friend.getInstance().names;
            if (names.contains(data.entity().getName())) {
                names.remove(data.entity().getName());
                Notifications.add(new Notifications.Notification(data.entity().getName() + " removed from your friends."));
            } else {
                names.add(data.entity().getName());
                Notifications.add(new Notifications.Notification(data.entity().getName() + " added to your friends."));
            }
        }),
        Target(data -> {
            KillAura.getInstance().targets.clear();
            while (SmoothAura.getInstance().targets.hasNext()) {
                SmoothAura.getInstance().targets.next();
            }
            KillAura.getInstance().targets.add(data.entity().getName());
            SmoothAura.getInstance().targets.add(data.entity().getName());
        }),
        Message(data -> Minecraft.getMinecraft().displayGuiScreen((GuiScreen)new GuiChat(TTCp.prefix + "msg " + data.entity().getName() + " ")));

        public final Bind toDo;

        private CustomBindsPlayer(PartialBind toDo) {
            this.toDo = toDo;
        }

        @Override
        public void call(Bind.Data data) {
            this.toDo.call(data);
        }
    }

    public static enum CustomBindsBlock implements PartialBind
    {
        NONE(data -> {});

        public final Bind toDo;

        private CustomBindsBlock(PartialBind toDo) {
            this.toDo = toDo;
        }

        @Override
        public void call(Bind.Data data) {
            this.toDo.call(data);
        }
    }
}
