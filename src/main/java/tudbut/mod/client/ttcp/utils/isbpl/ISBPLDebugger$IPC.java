package tudbut.mod.client.ttcp.utils.isbpl;

import java.util.HashMap;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;

class ISBPLDebugger$IPC {
    long threadID = -1L;
    String until = null;
    HashMap<Long, Integer> run = new HashMap();
    HashMap<Long, Stack<ISBPLObject>> stack = new HashMap();

    ISBPLDebugger$IPC() {
    }
}
