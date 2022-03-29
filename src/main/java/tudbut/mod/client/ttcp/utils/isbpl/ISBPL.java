package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLCallable;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLDebugger;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLKeyword;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLStop;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLStreamer;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLThreadLocal;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLType;
import tudbut.mod.client.ttcp.utils.isbpl.Interop;

public class ISBPL {
    static boolean debug = false;
    public ISBPLDebugger.IPC debuggerIPC = new ISBPLDebugger.IPC();
    ArrayList<ISBPLType> types = new ArrayList();
    final ISBPLThreadLocal<Stack<HashMap<String, ISBPLCallable>>> functionStack = ISBPLThreadLocal.withInitial(Stack::new);
    HashMap<Object, ISBPLObject> vars = new HashMap();
    final ISBPLThreadLocal<ArrayList<String>> lastWords = ISBPLThreadLocal.withInitial(() -> new ArrayList(16));
    int exitCode;
    private final ISBPLStreamer streamer = new ISBPLStreamer(this);
    ArrayList<String> included = new ArrayList();
    HashMap<String, ISBPLCallable> natives = new HashMap();
    static final ISBPLType defaultTypeInt = new ISBPLType("int");
    static final ISBPLType defaultTypeString = new ISBPLType("string");

    public ISBPL() {
        this.functionStack.get().push(new HashMap());
    }

    private ISBPLKeyword getKeyword(String word) {
        switch (word) {
            case "native": {
                return (idx, words, file, stack) -> {
                    this.addNative(words[++idx]);
                    return idx;
                };
            }
            case "func": {
                return (i1, words1, stack1, stack12) -> this.createFunction(i1, words1);
            }
            case "def": {
                return (idx, words, file, stack) -> {
                    Object var = new Object();
                    this.functionStack.get().peek().put(words[++idx], (file1, stack1) -> stack1.push(this.vars.get(var)));
                    this.functionStack.get().peek().put("=" + words[idx], (file1, stack1) -> this.vars.put(var, (ISBPLObject)stack1.pop()));
                    return idx;
                };
            }
            case "if": {
                return (idx, words, file, stack) -> {
                    AtomicInteger i = new AtomicInteger(++idx);
                    ISBPLCallable callable = this.readBlock(i, words, false);
                    if (((ISBPLObject)stack.pop()).isTruthy()) {
                        callable.call(file, stack);
                    }
                    return i.get();
                };
            }
            case "while": {
                return (idx, words, file, stack) -> {
                    AtomicInteger i = new AtomicInteger(++idx);
                    ISBPLCallable cond = this.readBlock(i, words, false);
                    i.getAndIncrement();
                    ISBPLCallable block = this.readBlock(i, words, false);
                    cond.call(file, stack);
                    while (((ISBPLObject)stack.pop()).isTruthy()) {
                        block.call(file, stack);
                        cond.call(file, stack);
                    }
                    return i.get();
                };
            }
            case "stop": {
                return (idx, words, file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkType(this.getType("int"));
                    throw new ISBPLStop((Integer)o.object);
                };
            }
            case "try": {
                return (idx, words, file, stack) -> {
                    String[] allowed;
                    ++idx;
                    ISBPLObject array = (ISBPLObject)stack.pop();
                    array.checkTypeMulti(this.getType("array"), this.getType("string"));
                    if (array.type.name.equals("string")) {
                        allowed = new String[]{this.toJavaString(array)};
                    } else {
                        ISBPLObject[] arr = (ISBPLObject[])array.object;
                        allowed = new String[arr.length];
                        for (int i = 0; i < arr.length; ++i) {
                            allowed[i] = this.toJavaString(arr[i]);
                        }
                    }
                    AtomicInteger i = new AtomicInteger(idx);
                    ISBPLCallable block = this.readBlock(i, words, false);
                    i.getAndIncrement();
                    ISBPLCallable catcher = this.readBlock(i, words, false);
                    try {
                        block.call(file, stack);
                    }
                    catch (ISBPLError error) {
                        if (Arrays.asList(allowed).contains(error.type) || allowed.length != 1 && allowed[0].equals("all")) {
                            stack.push(this.toISBPLString(error.message));
                            stack.push(this.toISBPLString(error.type));
                            catcher.call(file, stack);
                        }
                        throw error;
                    }
                    return i.get();
                };
            }
            case "do": {
                return (idx, words, file, stack) -> {
                    AtomicInteger i = new AtomicInteger(++idx);
                    ISBPLCallable block = this.readBlock(i, words, false);
                    i.getAndIncrement();
                    ISBPLCallable catcher = this.readBlock(i, words, false);
                    try {
                        block.call(file, stack);
                    }
                    finally {
                        catcher.call(file, stack);
                    }
                    return i.get();
                };
            }
            case "fork": {
                return (idx, words, file, stack) -> {
                    AtomicInteger i = new AtomicInteger(++idx);
                    Stack s = (Stack)stack.clone();
                    ISBPLCallable block = this.readBlock(i, words, false);
                    long tid = Thread.currentThread().getId();
                    Stack fs = (Stack)this.functionStack.get().clone();
                    new Thread(() -> {
                        block2: {
                            this.debuggerIPC.run.put(Thread.currentThread().getId(), this.debuggerIPC.run.get(tid) == -1 ? -1 : 0);
                            this.debuggerIPC.stack.put(Thread.currentThread().getId(), s);
                            this.functionStack.set(fs);
                            try {
                                block.call(file, s);
                            }
                            catch (ISBPLStop stop) {
                                if (stop.amount != -1) break block2;
                                System.exit(this.exitCode);
                            }
                        }
                    }).start();
                    return i.get();
                };
            }
        }
        return null;
    }

    private void addNative(String name) {
        ISBPLCallable func = null;
        switch (name) {
            case "alen": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkType(this.getType("array"));
                    stack.push(new ISBPLObject(this.getType("int"), ((ISBPLObject[])o.object).length));
                };
                break;
            }
            case "aget": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    o.checkType(this.getType("array"));
                    stack.push(((ISBPLObject[])o.object)[(Integer)i.object]);
                };
                break;
            }
            case "aput": {
                func = (file, stack) -> {
                    ISBPLObject toPut = (ISBPLObject)stack.pop();
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    o.checkType(this.getType("array"));
                    ((ISBPLObject[])o.object)[((Integer)i.object).intValue()] = toPut;
                };
                break;
            }
            case "anew": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    stack.push(new ISBPLObject(this.getType("array"), new ISBPLObject[((Integer)i.object).intValue()]));
                };
                break;
            }
            case "_array": {
                func = (file, stack) -> {
                    ISBPLObject a = (ISBPLObject)stack.pop();
                    if (a.type.equals(this.getType("array"))) {
                        stack.push(a);
                    } else if (a.object instanceof ISBPLObject[]) {
                        stack.push(new ISBPLObject(this.getType("array"), a.object));
                    } else {
                        this.typeError(a.type.name, "array");
                    }
                };
                break;
            }
            case "_char": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("char"), Character.valueOf((char)o.toLong())));
                };
                break;
            }
            case "_byte": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("byte"), (byte)o.toLong()));
                };
                break;
            }
            case "_int": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("int"), (int)o.toLong()));
                };
                break;
            }
            case "_float": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("float"), Float.valueOf((float)o.toDouble())));
                };
                break;
            }
            case "_long": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("long"), o.toLong()));
                };
                break;
            }
            case "_double": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("double"), o.toDouble()));
                };
                break;
            }
            case "ischar": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("char")) ? 1 : 0));
                };
                break;
            }
            case "isbyte": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("byte")) ? 1 : 0));
                };
                break;
            }
            case "isint": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("int")) ? 1 : 0));
                };
                break;
            }
            case "isfloat": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("float")) ? 1 : 0));
                };
                break;
            }
            case "islong": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("long")) ? 1 : 0));
                };
                break;
            }
            case "isdouble": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("double")) ? 1 : 0));
                };
                break;
            }
            case "isarray": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.equals(this.getType("array")) ? 1 : 0));
                };
                break;
            }
            case "_layer_call": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    ISBPLObject s = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    ((ISBPLCallable)((HashMap)this.functionStack.get().get(this.functionStack.get().size() - 1 - (Integer)i.object)).get(this.toJavaString(s))).call(file, stack);
                };
                break;
            }
            case "reload": {
                func = (file, stack) -> {
                    String filepath = this.getFilePathForInclude(stack, file);
                    File f = new File(filepath).getAbsoluteFile();
                    try {
                        URL url = f.exists() ? f.toURI().toURL() : this.getClass().getResource(filepath);
                        this.interpret(f, ISBPL.readFile(url), stack);
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", "Couldn't find file " + filepath + " required by include keyword.");
                    }
                    this.included.add(filepath);
                };
                break;
            }
            case "include": {
                func = (file, stack) -> {
                    String filepath = this.getFilePathForInclude(stack, file);
                    if (!this.included.contains(filepath)) {
                        File f = new File(filepath).getAbsoluteFile();
                        try {
                            URL url = f.exists() ? f.toURI().toURL() : this.getClass().getResource(filepath);
                            this.interpret(f, ISBPL.readFile(url), stack);
                        }
                        catch (IOException e) {
                            throw new ISBPLError("IO", "Couldn't find file " + filepath + " required by include keyword.");
                        }
                        this.included.add(filepath);
                    }
                };
                break;
            }
            case "putchar": {
                func = (file, stack) -> {
                    ISBPLObject c = (ISBPLObject)stack.pop();
                    c.checkType(this.getType("char"));
                    System.out.print(((Character)c.object).charValue());
                };
                break;
            }
            case "eputchar": {
                func = (file, stack) -> {
                    ISBPLObject c = (ISBPLObject)stack.pop();
                    c.checkType(this.getType("char"));
                    System.err.print(((Character)c.object).charValue());
                };
                break;
            }
            case "_file": {
                func = (file, stack) -> {
                    ISBPLObject s = (ISBPLObject)stack.pop();
                    File f = new File(this.toJavaString(s));
                    stack.push(new ISBPLObject(this.getType("file"), f));
                };
                break;
            }
            case "read": {
                func = (file, stack) -> {
                    ISBPLObject end = (ISBPLObject)stack.pop();
                    ISBPLObject begin = (ISBPLObject)stack.pop();
                    ISBPLObject fileToRead = (ISBPLObject)stack.pop();
                    end.checkType(this.getType("int"));
                    begin.checkType(this.getType("int"));
                    fileToRead.checkType(this.getType("file"));
                    try {
                        FileInputStream f = new FileInputStream((File)fileToRead.object);
                        int b = (Integer)begin.object;
                        int e = (Integer)end.object;
                        byte[] bytes = new byte[e - b];
                        f.read(bytes, b, e);
                        ISBPLObject[] arr = new ISBPLObject[bytes.length];
                        for (int i = 0; i < arr.length; ++i) {
                            arr[i] = new ISBPLObject(this.getType("byte"), bytes[i]);
                        }
                        stack.push(new ISBPLObject(this.getType("array"), arr));
                    }
                    catch (FileNotFoundException e) {
                        throw new ISBPLError("FileNotFound", "File not found.");
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", "File couldn't be read from" + (e.getMessage() != null ? ": " + e.getMessage() : "."));
                    }
                };
                break;
            }
            case "flength": {
                func = (file, stack) -> {
                    ISBPLObject f = (ISBPLObject)stack.pop();
                    f.checkType(this.getType("file"));
                    stack.push(new ISBPLObject(this.getType("int"), (int)((File)f.object).length()));
                };
                break;
            }
            case "write": {
                func = (file, stack) -> {
                    ISBPLObject content = (ISBPLObject)stack.pop();
                    ISBPLObject fileToWrite = (ISBPLObject)stack.pop();
                    content.checkType(this.getType("array"));
                    fileToWrite.checkType(this.getType("file"));
                    throw new ISBPLError("NotImplemented", "_file write is not implemented");
                };
                break;
            }
            case "getos": {
                func = (file, stack) -> stack.push(this.toISBPLString("linux"));
                break;
            }
            case "mktype": {
                func = (file, stack) -> {
                    ISBPLObject s = (ISBPLObject)stack.pop();
                    ISBPLType type = this.registerType(this.toJavaString(s));
                    stack.push(new ISBPLObject(this.getType("int"), type.id));
                };
                break;
            }
            case "typename": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    stack.push(this.toISBPLString(this.types.get((int)((Integer)i.object).intValue()).name));
                };
                break;
            }
            case "gettype": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o.type.id));
                };
                break;
            }
            case "settype": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    stack.push(new ISBPLObject(this.types.get((Integer)i.object), o.object));
                };
                break;
            }
            case "throw": {
                func = (file, stack) -> {
                    ISBPLObject message = (ISBPLObject)stack.pop();
                    ISBPLObject type = (ISBPLObject)stack.pop();
                    String msg = this.toJavaString(message);
                    String t = this.toJavaString(type);
                    throw new ISBPLError(t, msg);
                };
                break;
            }
            case "exit": {
                func = (file, stack) -> {
                    ISBPLObject code = (ISBPLObject)stack.pop();
                    code.checkType(this.getType("int"));
                    this.exitCode = (Integer)code.object;
                    throw new ISBPLStop(0);
                };
                break;
            }
            case "eq": {
                func = (file, stack) -> {
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(this.getType("int"), o1.equals(o2) ? 1 : 0));
                };
                break;
            }
            case "gt": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("int"), o1.toDouble() > o2.toDouble() ? 1 : 0));
                };
                break;
            }
            case "lt": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(this.getType("int"), o1.toDouble() < o2.toDouble() ? 1 : 0));
                };
                break;
            }
            case "not": {
                func = (file, stack) -> stack.push(new ISBPLObject(this.getType("int"), ((ISBPLObject)stack.pop()).isTruthy() ? 0 : 1));
                break;
            }
            case "neg": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    o.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    stack.push(new ISBPLObject(o.type, o.negative()));
                };
                break;
            }
            case "or": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    if (o1.isTruthy()) {
                        stack.push(o1);
                    } else {
                        stack.push(o2);
                    }
                };
                break;
            }
            case "and": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    if (o1.isTruthy()) {
                        if (o2.isTruthy()) {
                            stack.push(new ISBPLObject(this.getType("int"), 1));
                        } else {
                            stack.push(o2);
                        }
                    } else {
                        stack.push(o1);
                    }
                };
                break;
            }
            case "+": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if (object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(this.getType("int"), (Integer)object1 + (Integer)object2);
                    }
                    if (object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(this.getType("long"), (Long)object1 + (Long)object2);
                    }
                    if (object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() + ((Character)object2).charValue())));
                    }
                    if (object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(this.getType("byte"), (byte)(Byte.toUnsignedInt((Byte)object1) + Byte.toUnsignedInt((Byte)object2)));
                    }
                    if (object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(this.getType("float"), Float.valueOf(((Float)object1).floatValue() + ((Float)object2).floatValue()));
                    }
                    if (object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(this.getType("double"), (Double)object1 + (Double)object2);
                    }
                    if (r != null) {
                        stack.push(r);
                    } else {
                        this.typeError(o1.type.name, o2.type.name);
                    }
                };
                break;
            }
            case "-": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if (object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(this.getType("int"), (Integer)object1 - (Integer)object2);
                    }
                    if (object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(this.getType("long"), (Long)object1 - (Long)object2);
                    }
                    if (object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() - ((Character)object2).charValue())));
                    }
                    if (object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(this.getType("byte"), (byte)(Byte.toUnsignedInt((Byte)object1) - Byte.toUnsignedInt((Byte)object2)));
                    }
                    if (object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(this.getType("float"), Float.valueOf(((Float)object1).floatValue() - ((Float)object2).floatValue()));
                    }
                    if (object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(this.getType("double"), (Double)object1 - (Double)object2);
                    }
                    if (r != null) {
                        stack.push(r);
                    } else {
                        this.typeError(o1.type.name, o2.type.name);
                    }
                };
                break;
            }
            case "/": {
                func = (file, stack) -> {
                    try {
                        ISBPLObject o2 = (ISBPLObject)stack.pop();
                        ISBPLObject o1 = (ISBPLObject)stack.pop();
                        o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                        o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                        Object object1 = o1.object;
                        Object object2 = o2.object;
                        ISBPLObject r = null;
                        if (object1 instanceof Integer && object2 instanceof Integer) {
                            r = new ISBPLObject(this.getType("int"), (Integer)object1 / (Integer)object2);
                        }
                        if (object1 instanceof Long && object2 instanceof Long) {
                            r = new ISBPLObject(this.getType("long"), (Long)object1 / (Long)object2);
                        }
                        if (object1 instanceof Character && object2 instanceof Character) {
                            r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() / ((Character)object2).charValue())));
                        }
                        if (object1 instanceof Byte && object2 instanceof Byte) {
                            r = new ISBPLObject(this.getType("byte"), (byte)(Byte.toUnsignedInt((Byte)object1) / Byte.toUnsignedInt((Byte)object2)));
                        }
                        if (object1 instanceof Float && object2 instanceof Float) {
                            r = new ISBPLObject(this.getType("float"), Float.valueOf(((Float)object1).floatValue() / ((Float)object2).floatValue()));
                        }
                        if (object1 instanceof Double && object2 instanceof Double) {
                            r = new ISBPLObject(this.getType("double"), (Double)object1 / (Double)object2);
                        }
                        if (r != null) {
                            stack.push(r);
                        } else {
                            this.typeError(o1.type.name, o2.type.name);
                        }
                    }
                    catch (ArithmeticException ex) {
                        throw new ISBPLError("Arithmetic", "Division by 0");
                    }
                };
                break;
            }
            case "*": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if (object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(this.getType("int"), (Integer)object1 * (Integer)object2);
                    }
                    if (object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(this.getType("long"), (Long)object1 * (Long)object2);
                    }
                    if (object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() * ((Character)object2).charValue())));
                    }
                    if (object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(this.getType("byte"), (byte)(Byte.toUnsignedInt((Byte)object1) * Byte.toUnsignedInt((Byte)object2)));
                    }
                    if (object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(this.getType("float"), Float.valueOf(((Float)object1).floatValue() * ((Float)object2).floatValue()));
                    }
                    if (object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(this.getType("double"), (Double)object1 * (Double)object2);
                    }
                    if (r != null) {
                        stack.push(r);
                    } else {
                        this.typeError(o1.type.name, o2.type.name);
                    }
                };
                break;
            }
            case "**": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if (object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(this.getType("int"), (int)Math.pow(((Integer)object1).intValue(), ((Integer)object2).intValue()));
                    }
                    if (object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(this.getType("long"), (long)Math.pow(((Long)object1).longValue(), ((Long)object2).longValue()));
                    }
                    if (object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(this.getType("char"), Character.valueOf((char)Math.pow(((Character)object1).charValue(), ((Character)object2).charValue())));
                    }
                    if (object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(this.getType("byte"), (byte)Math.pow(Byte.toUnsignedInt((Byte)object1), Byte.toUnsignedInt((Byte)object2)));
                    }
                    if (object1 instanceof Float && object2 instanceof Float) {
                        r = new ISBPLObject(this.getType("float"), Float.valueOf((float)Math.pow(((Float)object1).floatValue(), ((Float)object2).floatValue())));
                    }
                    if (object1 instanceof Double && object2 instanceof Double) {
                        r = new ISBPLObject(this.getType("double"), Math.pow((Double)object1, (Double)object2));
                    }
                    if (r != null) {
                        stack.push(r);
                    } else {
                        this.typeError(o1.type.name, o2.type.name);
                    }
                };
                break;
            }
            case "%": {
                func = (file, stack) -> {
                    try {
                        ISBPLObject o2 = (ISBPLObject)stack.pop();
                        ISBPLObject o1 = (ISBPLObject)stack.pop();
                        o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                        o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("float"), this.getType("long"), this.getType("double"));
                        Object object1 = o1.object;
                        Object object2 = o2.object;
                        ISBPLObject r = null;
                        if (object1 instanceof Integer && object2 instanceof Integer) {
                            r = new ISBPLObject(this.getType("int"), (Integer)object1 % (Integer)object2);
                        }
                        if (object1 instanceof Long && object2 instanceof Long) {
                            r = new ISBPLObject(this.getType("long"), (Long)object1 % (Long)object2);
                        }
                        if (object1 instanceof Character && object2 instanceof Character) {
                            r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() % ((Character)object2).charValue())));
                        }
                        if (object1 instanceof Byte && object2 instanceof Byte) {
                            r = new ISBPLObject(this.getType("byte"), (byte)(Byte.toUnsignedInt((Byte)object1) % Byte.toUnsignedInt((Byte)object2)));
                        }
                        if (object1 instanceof Float && object2 instanceof Float) {
                            r = new ISBPLObject(this.getType("float"), Float.valueOf(((Float)object1).floatValue() % ((Float)object2).floatValue()));
                        }
                        if (object1 instanceof Double && object2 instanceof Double) {
                            r = new ISBPLObject(this.getType("double"), (Double)object1 % (Double)object2);
                        }
                        if (r != null) {
                            stack.push(r);
                        } else {
                            this.typeError(o1.type.name, o2.type.name);
                        }
                    }
                    catch (ArithmeticException ex) {
                        throw new ISBPLError("Arithmetic", "Division by 0");
                    }
                };
                break;
            }
            case "^": {
                func = (file, stack) -> {
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    o1.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("long"));
                    o2.checkTypeMulti(this.getType("int"), this.getType("byte"), this.getType("char"), this.getType("long"));
                    Object object1 = o1.object;
                    Object object2 = o2.object;
                    ISBPLObject r = null;
                    if (object1 instanceof Integer && object2 instanceof Integer) {
                        r = new ISBPLObject(this.getType("int"), (Integer)object1 ^ (Integer)object2);
                    }
                    if (object1 instanceof Long && object2 instanceof Long) {
                        r = new ISBPLObject(this.getType("long"), (Long)object1 ^ (Long)object2);
                    }
                    if (object1 instanceof Character && object2 instanceof Character) {
                        r = new ISBPLObject(this.getType("char"), Character.valueOf((char)(((Character)object1).charValue() ^ ((Character)object2).charValue())));
                    }
                    if (object1 instanceof Byte && object2 instanceof Byte) {
                        r = new ISBPLObject(this.getType("byte"), Byte.toUnsignedInt((Byte)object1) ^ Byte.toUnsignedInt((Byte)object2));
                    }
                    if (r != null) {
                        stack.push(r);
                    } else {
                        this.typeError(o1.type.name, o2.type.name);
                    }
                };
                break;
            }
            case "dup": {
                func = (file, stack) -> {
                    ISBPLObject o = (ISBPLObject)stack.pop();
                    stack.push(new ISBPLObject(o.type, o.object));
                    stack.push(new ISBPLObject(o.type, o.object));
                };
                break;
            }
            case "pop": {
                func = (file, stack) -> {
                    ISBPLObject cfr_ignored_0 = (ISBPLObject)stack.pop();
                };
                break;
            }
            case "swap": {
                func = (file, stack) -> {
                    ISBPLObject o1 = (ISBPLObject)stack.pop();
                    ISBPLObject o2 = (ISBPLObject)stack.pop();
                    stack.push(o2);
                    stack.push(o1);
                };
                break;
            }
            case "_last_word": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    i.checkType(this.getType("int"));
                    int n = (Integer)i.object;
                    if (n >= this.lastWords.get().size()) {
                        throw new ISBPLError("IllegalArgument", "_last_words called with wrong argument");
                    }
                    stack.push(this.toISBPLString(this.lastWords.get().get(n)));
                };
                break;
            }
            case "time": {
                func = (file, stack) -> {
                    ISBPLObject i = (ISBPLObject)stack.pop();
                    long n = i.toLong();
                    try {
                        Thread.sleep(n);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stack.push(new ISBPLObject(this.getType("long"), System.currentTimeMillis()));
                };
                break;
            }
            case "stream": {
                func = (file, stack) -> {
                    ISBPLObject action = (ISBPLObject)stack.pop();
                    action.checkType(this.getType("int"));
                    int n = (Integer)action.object;
                    try {
                        this.streamer.action(stack, n);
                    }
                    catch (IOException e) {
                        throw new ISBPLError("IO", e.getMessage());
                    }
                };
                break;
            }
            case "_enable_debug": {
                func = (file, stack) -> {
                    if (this.debuggerIPC.threadID == -1L) {
                        ISBPLDebugger debugger = new ISBPLDebugger(this);
                        debugger.start();
                        try {
                            while (debugger.port == -1) {
                                Thread.sleep(1L);
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stack.push(new ISBPLObject(this.getType("int"), debugger.port));
                    } else {
                        stack.push(new ISBPLObject(this.getType("int"), 0));
                    }
                };
                break;
            }
            case "_getvars": {
                func = (file, stack) -> {
                    ISBPLObject[] objects = new ISBPLObject[this.functionStack.get().size()];
                    int i = 0;
                    for (HashMap hashMap : this.functionStack.get()) {
                        ArrayList<ISBPLObject> strings = new ArrayList<ISBPLObject>();
                        for (String key : hashMap.keySet()) {
                            if (!key.startsWith("=")) continue;
                            strings.add(this.toISBPLString(key.substring(1)));
                        }
                        objects[i++] = new ISBPLObject(this.getType("array"), strings.toArray(new ISBPLObject[0]));
                    }
                    ISBPLObject array = new ISBPLObject(this.getType("array"), objects);
                    stack.push(array);
                };
                break;
            }
            case "stacksize": {
                func = (file, stack) -> stack.push(new ISBPLObject(this.getType("int"), stack.size()));
                break;
            }
            default: {
                func = this.natives.get(name);
            }
        }
        this.functionStack.get().peek().put(name, func);
    }

    private String getFilePathForInclude(Stack<ISBPLObject> stack, File file) {
        ISBPLObject s = stack.pop();
        String filepath = this.toJavaString(s);
        if (!filepath.startsWith("/")) {
            filepath = filepath.startsWith("#") ? System.getenv().getOrDefault("ISBPL_PATH", "/usr/lib/isbpl") + "/" + filepath.substring(1) : (filepath.startsWith("$") ? filepath.substring(1) : file.getParentFile().getAbsolutePath() + "/" + filepath);
        }
        return filepath;
    }

    private int createFunction(int i, String[] words) {
        String name = words[++i];
        AtomicInteger integer = new AtomicInteger(++i);
        ISBPLCallable callable = this.readBlock(integer, words, true);
        i = integer.get();
        this.functionStack.get().peek().put(name, callable);
        return i;
    }

    private ISBPLCallable readBlock(AtomicInteger idx, String[] words, boolean isFunction) {
        ArrayList<String> newWords = new ArrayList<String>();
        int i = idx.get();
        ++i;
        int lvl = 1;
        while (i < words.length && lvl > 0) {
            String word = words[i];
            if (word.equals("{")) {
                ++lvl;
            }
            if (word.equals("}") && --lvl == 0) break;
            newWords.add(word);
            ++i;
        }
        idx.set(i);
        String[] theWords = newWords.toArray(new String[0]);
        return (file, stack) -> this.interpretRaw(file, theWords, stack, isFunction);
    }

    public String toJavaString(ISBPLObject string) {
        string.checkType(this.getType("string"));
        ISBPLObject[] array = (ISBPLObject[])string.object;
        char[] chars = new char[array.length];
        for (int i = 0; i < array.length; ++i) {
            chars[i] = ((Character)array[i].object).charValue();
        }
        return new String(chars);
    }

    public ISBPLObject toISBPLString(String s) {
        char[] chars = s.toCharArray();
        ISBPLObject[] objects = new ISBPLObject[chars.length];
        ISBPLType type = this.getType("char");
        for (int i = 0; i < chars.length; ++i) {
            objects[i] = new ISBPLObject(type, Character.valueOf(chars[i]));
        }
        return new ISBPLObject(this.getType("string"), objects);
    }

    public ISBPLType registerType(String name) {
        ISBPLType type = new ISBPLType(name);
        this.types.add(type);
        return type;
    }

    public ISBPLType getType(String name) {
        for (int i = 0; i < this.types.size(); ++i) {
            if (!this.types.get((int)i).name.equals(name)) continue;
            return this.types.get(i);
        }
        if (name.equals("int")) {
            return defaultTypeInt;
        }
        if (name.equals("string")) {
            return defaultTypeString;
        }
        return null;
    }

    public void typeError(String got, String wanted) {
        throw new ISBPLError("IncompatibleTypes", "Incompatible types: " + got + " - " + wanted);
    }

    public void interpret(File file, String code, Stack<ISBPLObject> stack) {
        code = this.cleanCode(code);
        String[] words = this.splitWords(code);
        this.interpretRaw(file, words, stack, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void interpretRaw(File file, String[] words, Stack<ISBPLObject> stack, boolean isFunction) {
        if (isFunction) {
            this.functionStack.get().push(new HashMap());
        }
        try {
            for (int i = 0; i < words.length; ++i) {
                String word = words[i];
                if (word.length() == 0) continue;
                if (debug) {
                    String s = "";
                    for (int x = 0; x < this.functionStack.get().size(); ++x) {
                        s = s + "\t";
                    }
                    System.err.println(s + word + "\t\t" + stack);
                }
                while (this.debuggerIPC.run.get(Thread.currentThread().getId()) == 0) {
                    Thread.sleep(1L);
                }
                if (this.debuggerIPC.run.get(Thread.currentThread().getId()) < 0) {
                    if (this.debuggerIPC.run.get(Thread.currentThread().getId()) < -1) {
                        if (this.debuggerIPC.run.get(Thread.currentThread().getId()) == -2 && word.equals(this.debuggerIPC.until)) {
                            this.debuggerIPC.run.put(Thread.currentThread().getId(), 0);
                            while (this.debuggerIPC.run.get(Thread.currentThread().getId()) == 0) {
                                Thread.sleep(1L);
                            }
                        }
                        if (this.debuggerIPC.run.get(Thread.currentThread().getId()) == -3 && Thread.currentThread().getId() != this.debuggerIPC.threadID) {
                            while (this.debuggerIPC.run.get(Thread.currentThread().getId()) == -3) {
                                Thread.sleep(1L);
                            }
                        }
                    }
                } else {
                    this.debuggerIPC.run.put(Thread.currentThread().getId(), this.debuggerIPC.run.get(Thread.currentThread().getId()) - 1);
                }
                this.lastWords.get().add(0, word);
                while (this.lastWords.get().size() > 16) {
                    this.lastWords.get().remove(this.lastWords.get().size() - 1);
                }
                ISBPLKeyword keyword = this.getKeyword(word);
                if (keyword != null) {
                    i = keyword.call(i, words, file, stack);
                    continue;
                }
                ISBPLCallable func = this.functionStack.get().peek().get(word);
                if (func != null) {
                    func.call(file, stack);
                    continue;
                }
                func = (ISBPLCallable)((HashMap)this.functionStack.get().get(0)).get(word);
                if (func != null) {
                    func.call(file, stack);
                    continue;
                }
                if (word.startsWith("\"")) {
                    stack.push(this.toISBPLString(word.substring(1)));
                    continue;
                }
                try {
                    stack.push(new ISBPLObject(this.getType("int"), Integer.parseInt(word)));
                    continue;
                }
                catch (Exception exception) {
                    try {
                        stack.push(new ISBPLObject(this.getType("long"), Long.parseLong(word)));
                        continue;
                    }
                    catch (Exception exception2) {
                        try {
                            stack.push(new ISBPLObject(this.getType("float"), Float.valueOf(Float.parseFloat(word))));
                            continue;
                        }
                        catch (Exception exception3) {
                            try {
                                stack.push(new ISBPLObject(this.getType("double"), Double.parseDouble(word)));
                                continue;
                            }
                            catch (Exception exception4) {
                                throw new ISBPLError("InvalidWord", word + " is not a function, object, or keyword.");
                            }
                        }
                    }
                }
            }
        }
        catch (ISBPLStop stop) {
            if (stop.amount == 0) {
                return;
            }
            throw new ISBPLStop(stop.amount);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            if (isFunction) {
                this.functionStack.get().pop();
            }
        }
    }

    private String[] splitWords(String code) {
        ArrayList<String> words = new ArrayList<String>();
        char[] chars = code.toCharArray();
        boolean isInString = false;
        boolean escaping = false;
        String word = "";
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (isInString) {
                if (c == '\\') {
                    boolean bl = escaping = !escaping;
                    if (escaping) continue;
                }
                if (c == 'n' && escaping) {
                    word = word + '\n';
                    escaping = false;
                    continue;
                }
                if (c == 'r' && escaping) {
                    escaping = false;
                    word = word + '\r';
                    continue;
                }
                if (c == '\"') {
                    if (escaping) {
                        escaping = false;
                    } else {
                        isInString = false;
                        continue;
                    }
                }
                word = word + c;
                if (!escaping) continue;
                throw new RuntimeException("Error parsing code: Invalid Escape.");
            }
            if (c == '\"' && word.length() == 0) {
                word = word + '\"';
                isInString = true;
                continue;
            }
            if (c == ' ') {
                words.add(word);
                word = "";
                continue;
            }
            word = word + c;
        }
        words.add(word);
        return words.toArray(new String[0]);
    }

    private String cleanCode(String code) {
        return code.replaceAll("\r", "\n").replaceAll("\n", " ");
    }

    public static ISBPL makeISBPL(Stack<ISBPLObject> stack) {
        ISBPL isbpl = new ISBPL();
        isbpl.debuggerIPC.stack.put(Thread.currentThread().getId(), stack);
        debug = !System.getenv().getOrDefault("DEBUG", "").equals("");
        isbpl.debuggerIPC.run.put(Thread.currentThread().getId(), -1);
        if (debug) {
            new ISBPLDebugger(isbpl).start();
            isbpl.debuggerIPC.run.put(Thread.currentThread().getId(), 0);
        }
        Interop.make(isbpl, isbpl.natives);
        try {
            File std = new File(System.getenv().getOrDefault("ISBPL_PATH", "/usr/lib/isbpl") + "/std.isbpl");
            if (!std.exists()) {
                throw new RuntimeException("Can't load ISBPL: std.isbpl not found. Download from: https://github.com/tudbut/isbpl");
            }
            URL url = std.toURI().toURL();
            isbpl.interpret(std, ISBPL.readFile(url), stack);
        }
        catch (ISBPLStop stop) {
            System.exit(isbpl.exitCode);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(stack);
        }
        return isbpl;
    }

    private static ISBPLObject argarray(ISBPL isbpl, String[] args) {
        ISBPLObject[] array = new ISBPLObject[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            array[i - 1] = isbpl.toISBPLString(args[i]);
        }
        return new ISBPLObject(isbpl.getType("array"), array);
    }

    private static String readFile(URL f) throws IOException {
        int len;
        InputStream fis = f.openStream();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] currentBytes = new byte[4096];
        while ((len = fis.read(currentBytes)) > 0) {
            bytes.write(currentBytes, 0, len);
        }
        return bytes.toString();
    }
}
