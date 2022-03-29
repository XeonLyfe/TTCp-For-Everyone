package tudbut.mod.client.ttcp.utils.isbpl;

import java.util.HashMap;
import java.util.function.Supplier;

class ISBPLThreadLocal<T> {
    HashMap<Long, T> map = new HashMap();
    Supplier<? extends T> supplier;

    public ISBPLThreadLocal(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> ISBPLThreadLocal<T> withInitial(Supplier<? extends T> supplier) {
        return new ISBPLThreadLocal<T>(supplier);
    }

    public T get() {
        long tid = Thread.currentThread().getId();
        if (!this.map.containsKey(tid)) {
            this.map.put(tid, this.supplier.get());
        }
        return this.map.get(tid);
    }

    public T set(T t) {
        return this.map.put(Thread.currentThread().getId(), t);
    }
}
