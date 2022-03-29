package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.MidClick;

public enum MidClick$CustomBindsEntity implements MidClick.PartialBind
{
    NONE(data -> {});

    public final MidClick.Bind toDo;

    private MidClick$CustomBindsEntity(MidClick.PartialBind toDo) {
        this.toDo = toDo;
    }

    @Override
    public void call(MidClick.Bind.Data data) {
        this.toDo.call(data);
    }
}
