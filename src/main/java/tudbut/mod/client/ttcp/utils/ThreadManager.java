package tudbut.mod.client.ttcp.utils;

public class ThreadManager {
    public static Thread run(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.start();
        return t;
    }

    public static Thread run(String name, Runnable runnable) {
        Thread t = new Thread(runnable, name);
        t.start();
        return t;
    }
}
