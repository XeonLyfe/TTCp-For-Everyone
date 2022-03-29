package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.File;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;

interface ISBPLKeyword {
    public int call(int var1, String[] var2, File var3, Stack<ISBPLObject> var4);
}
