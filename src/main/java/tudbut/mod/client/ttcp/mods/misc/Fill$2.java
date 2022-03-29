package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.MidClick;

class Fill$2
implements MidClick.Bind {
    Fill$2() {
    }

    @Override
    public MidClick.Bind.Type getType() {
        return MidClick.Bind.Type.BLOCK;
    }

    @Override
    public String getName() {
        return "Fill END";
    }

    @Override
    public void call(MidClick.Bind.Data data) {
        Fill.this.posCallback(data);
    }
}
