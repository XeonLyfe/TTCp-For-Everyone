package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.MidClick;

class Break$2
implements MidClick.Bind {
    Break$2() {
    }

    @Override
    public MidClick.Bind.Type getType() {
        return MidClick.Bind.Type.BLOCK;
    }

    @Override
    public String getName() {
        return "Break END";
    }

    @Override
    public void call(MidClick.Bind.Data data) {
        Break.this.posCallback(data);
    }
}
