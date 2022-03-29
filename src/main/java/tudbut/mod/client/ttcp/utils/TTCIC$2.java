package tudbut.mod.client.ttcp.utils;

import tudbut.mod.client.ttcp.utils.TTCIC;

final class TTCIC$2
implements TTCIC.PacketCS {
    final TTCIC.PacketsCS val$type;
    final String val$finalContent;

    TTCIC$2(TTCIC.PacketsCS packetsCS, String string) {
        this.val$type = packetsCS;
        this.val$finalContent = string;
    }

    @Override
    public TTCIC.PacketsCS type() {
        return this.val$type;
    }

    @Override
    public String content() {
        return this.val$finalContent;
    }
}
