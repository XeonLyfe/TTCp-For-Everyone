package tudbut.mod.client.ttcp.utils;

import tudbut.net.ic.PBIC;

public class TTCIC {
    public static synchronized PBIC.Packet getPacketSC(PacketsSC packetType, String content) {
        return () -> packetType.name() + " " + content;
    }

    public static synchronized PBIC.Packet getPacketCS(PacketsCS packetType, String content) {
        return () -> packetType.name() + " " + content;
    }

    public static synchronized PacketSC getPacketSC(PBIC.Packet packet) {
        String content = packet.getContent();
        final PacketsSC type = PacketsSC.valueOf(content.split(" ")[0]);
        final String finalContent = content = content.substring(content.indexOf(" ") + 1);
        return new PacketSC(){

            @Override
            public PacketsSC type() {
                return type;
            }

            @Override
            public String content() {
                return finalContent;
            }
        };
    }

    public static synchronized PacketCS getPacketCS(PBIC.Packet packet) {
        String content = packet.getContent();
        final PacketsCS type = PacketsCS.valueOf(content.split(" ")[0]);
        final String finalContent = content = content.substring(content.indexOf(" ") + 1);
        return new PacketCS(){

            @Override
            public PacketsCS type() {
                return type;
            }

            @Override
            public String content() {
                return finalContent;
            }
        };
    }

    public static interface PacketCS {
        public PacketsCS type();

        public String content();
    }

    public static interface PacketSC {
        public PacketsSC type();

        public String content();
    }

    public static enum PacketsCS {
        NAME,
        UUID,
        KEEPALIVE,
        LOST;

    }

    public static enum PacketsSC {
        INIT,
        NAME,
        UUID,
        TPA,
        EXECUTE,
        LIST,
        KILL,
        FOLLOW,
        STOP,
        CONFIG,
        WALK,
        ELYTRA,
        KEEPALIVE,
        POSITION;

    }
}
