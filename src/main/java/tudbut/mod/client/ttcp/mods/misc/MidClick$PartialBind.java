package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.MidClick;

interface MidClick$PartialBind
extends MidClick.Bind {
    @Override
    default public String getName() {
        return "";
    }

    @Override
    default public MidClick.Bind.Type getType() {
        return null;
    }

    @Override
    public void call(MidClick.Bind.Data var1);
}
