package tudbut.mod.client.ttcp.utils;

import tudbut.mod.client.ttcp.utils.TTCIC;

final class TTCIC$1
implements TTCIC.PacketSC {
    final TTCIC.PacketsSC val$type;
    final String val$finalContent;

    TTCIC$1(TTCIC.PacketsSC packetsSC, String string) {
        this.val$type = packetsSC;
        this.val$finalContent = string;
    }

    @Override
    public TTCIC.PacketsSC type() {
        return this.val$type;
    }

    @Override
    public String content() {
        return this.val$finalContent;
    }
}
