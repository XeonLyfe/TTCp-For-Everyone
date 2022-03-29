package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.mods.misc.MidClick;

class Break$1
implements MidClick.Bind {
    Break$1() {
    }

    @Override
    public MidClick.Bind.Type getType() {
        return MidClick.Bind.Type.BLOCK;
    }

    @Override
    public String getName() {
        return "Break START";
    }

    @Override
    public void call(MidClick.Bind.Data data) {
        Break.this.posCallback(data);
    }
}
