package tudbut.mod.client.ttcp.mods.rendering;

import java.util.Date;

public class Notifications$Notification {
    public String text;
    private final int time;
    private final long start = new Date().getTime();

    public Notifications$Notification(String text) {
        this(text, 5000);
    }

    public Notifications$Notification(String text, int ms) {
        this.text = text;
        this.time = ms;
    }

    static long access$000(Notifications$Notification x0) {
        return x0.start;
    }

    static int access$100(Notifications$Notification x0) {
        return x0.time;
    }
}
