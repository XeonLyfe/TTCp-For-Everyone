package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.File;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;

interface ISBPLCallable {
    public void call(File var1, Stack<ISBPLObject> var2);
}
