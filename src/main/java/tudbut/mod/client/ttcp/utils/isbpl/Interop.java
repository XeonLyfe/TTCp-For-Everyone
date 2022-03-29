package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPL;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLCallable;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;

public class Interop {
    public static void make(ISBPL isbpl, HashMap<String, ISBPLCallable> natives) {
        natives.put("ttc_interop", (file, stack) -> Interop.ttc_interop(isbpl, file, stack));
    }

    private static void ttc_interop(ISBPL isbpl, File file, Stack<ISBPLObject> stack) {
    }
}
