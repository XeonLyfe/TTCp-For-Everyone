package tudbut.mod.client.ttcp.mods.rendering;

import java.util.ArrayList;
import java.util.Date;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Render;

@Render
public class Notifications
extends Module {
    private static final ArrayList<Notification> notifications = new ArrayList();

    @Override
    public void onTick() {
        for (int i = 0; i < notifications.size(); ++i) {
            if (new Date().getTime() - notifications.get(i).start < (long)notifications.get(i).time) continue;
            notifications.remove(i);
            --i;
        }
    }

    public static ArrayList<Notification> getNotifications() {
        return (ArrayList)notifications.clone();
    }

    public static void add(Notification notification) {
        notifications.add(0, notification);
    }

    public static class Notification {
        public String text;
        private final int time;
        private final long start = new Date().getTime();

        public Notification(String text) {
            this(text, 5000);
        }

        public Notification(String text, int ms) {
            this.text = text;
            this.time = ms;
        }
    }
}
