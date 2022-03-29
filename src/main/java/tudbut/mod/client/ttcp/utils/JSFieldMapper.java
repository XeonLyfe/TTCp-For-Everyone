package tudbut.mod.client.ttcp.utils;

import java.util.ArrayList;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.RuntimeNameMapper;
import tudbut.mod.client.ttcp.TTCp;

public class JSFieldMapper
implements RuntimeNameMapper {
    public static Context createMapperContext() {
        Context context = Context.newBuilder(new String[0]).allowExperimentalOptions(true).allowHostAccess(HostAccess.ALL).allowAllAccess(true).allowCreateThread(true).allowIO(true).option("engine.EnableMultithreading", "true").runtimeNameMapper(new JSFieldMapper()).build();
        return context;
    }

    private boolean isObfClass(Class<?> clazz) {
        return clazz.getName().startsWith("net.minecraft") && TTCp.isObfEnv();
    }

    private boolean isObfClass(String clazz) {
        return clazz.startsWith("net.minecraft") && TTCp.isObfEnv();
    }

    private String[] splitClassName(Class<?> clazz) {
        ArrayList<String> classNames = new ArrayList<String>();
        Class<?> currentClass = clazz;
        while (currentClass.getDeclaringClass() != null) {
            classNames.add(0, currentClass.getSimpleName());
            currentClass = currentClass.getDeclaringClass();
        }
        classNames.add(clazz.getName());
        return classNames.toArray(new String[0]);
    }

    private String getDeobfClassName(Class<?> obfClass) {
        String name = obfClass.getName();
        String rename = name.replaceAll("[.$]", "/");
        if ((rename = TTCp.obfMap.get(rename)) == null) {
            return name;
        }
        String ord = name.replaceAll("[^$.]", "");
        String[] spl = rename.split("/");
        rename = "";
        for (int i = 0; i < spl.length; ++i) {
            rename = rename + spl[i];
            if (i >= spl.length - 1) continue;
            rename = rename + ord.charAt(i);
        }
        return rename;
    }

    private String getObfClassName(String clazz) {
        String rename = clazz.replaceAll("[.$]", "/");
        if ((rename = TTCp.deobfMap.get(rename)) == null) {
            return clazz;
        }
        String ord = clazz.replaceAll("[^$.]", "");
        String[] spl = rename.split("/");
        rename = "";
        for (int i = 0; i < spl.length; ++i) {
            rename = rename + spl[i];
            if (i >= spl.length - 1) continue;
            rename = rename + ord.charAt(i);
        }
        return rename;
    }

    private void allInClassTree(Class<?> clazz, ArrayList<Class<?>> list) {
        list.add(clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            this.allInClassTree(interfaces[i], list);
        }
        if (clazz.getSuperclass() != null) {
            this.allInClassTree(clazz.getSuperclass(), list);
        }
    }

    @Override
    public String getClass(String name) {
        if (name.startsWith("ttc.")) {
            name = "tudbut.mod.client.ttcp." + name.substring("ttc.".length());
        }
        System.out.println("JS loaded class " + name);
        if (this.isObfClass(name)) {
            return this.getObfClassName(name);
        }
        return name;
    }

    @Override
    public String getClass(Class<?> clazz, String name) {
        System.out.println("JS loaded class " + clazz.getName() + "$" + name);
        if (this.isObfClass(clazz)) {
            String fullName = this.getObfClassName(this.getDeobfClassName(clazz) + "$" + name);
            return fullName.substring(fullName.lastIndexOf("$") + 1);
        }
        return name;
    }

    @Override
    public String getField(Class<?> clazz, String name) {
        System.out.println("JS loaded field " + clazz.getName() + "#" + name);
        if (this.isObfClass(clazz)) {
            ArrayList list = new ArrayList();
            this.allInClassTree(clazz, list);
            String fullName = null;
            for (int i = 0; i < list.size() && fullName == null; ++i) {
                fullName = TTCp.deobfMap.get(this.getDeobfClassName(list.get(i)).replaceAll("[.$]", "/") + "/" + name);
            }
            if (fullName == null) {
                return name;
            }
            return fullName.substring(fullName.lastIndexOf("/") + 1);
        }
        return name;
    }

    @Override
    public String getMethod(Class<?> clazz, String name) {
        System.out.println("JS loaded method " + clazz.getName() + "#" + name);
        if (this.isObfClass(clazz)) {
            ArrayList list = new ArrayList();
            this.allInClassTree(clazz, list);
            String fullName = null;
            for (int i = 0; i < list.size() && fullName == null; ++i) {
                fullName = TTCp.deobfMap.get(this.getDeobfClassName(list.get(i)).replaceAll("[.$]", "/") + "/" + name);
            }
            if (fullName == null) {
                return name;
            }
            return fullName.substring(fullName.lastIndexOf("/") + 1);
        }
        return name;
    }
}
