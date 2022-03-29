package tudbut.mod.client.ttcp.utils;

import org.graalvm.polyglot.Context;
import tudbut.mod.client.ttcp.utils.JSFieldMapper;
import tudbut.mod.client.ttcp.utils.JSModule;

public class JSModule$Loader {
    public static JSModule createFromJS(String js, String id) {
        try {
            Context context = JSFieldMapper.createMapperContext();
            return new JSModule(context, context.eval("js", "const jsModuleObj = (function(){" + js + "})(); jsModuleObj"), id);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
