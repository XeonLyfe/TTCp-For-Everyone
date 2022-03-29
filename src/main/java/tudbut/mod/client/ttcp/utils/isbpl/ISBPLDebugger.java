package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPL;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLCallable;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLStop;

class ISBPLDebugger
extends Thread {
    private ISBPL isbpl;
    int port = -1;
    long mainID = Thread.currentThread().getId();

    public ISBPLDebugger(ISBPL isbpl) {
        this.isbpl = isbpl;
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(Integer.parseInt(System.getenv().getOrDefault("DEBUG", "9736")));
                this.port = socket.getLocalPort();
                System.err.println("Debugger listening on :" + socket.getLocalPort());
            }
            catch (BindException e) {
                while (socket == null) {
                    try {
                        socket = new ServerSocket((int)(Math.random() * 5000.0 + 5000.0));
                        this.port = socket.getLocalPort();
                        System.err.println("Debugger listening on :" + socket.getLocalPort());
                    }
                    catch (BindException bindException) {}
                }
            }
            this.isbpl.debuggerIPC.threadID = Thread.currentThread().getId();
            while (true) {
                Socket s = socket.accept();
                new Thread(() -> {
                    try {
                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        long tid = this.mainID;
                        block57: while ((line = reader.readLine()) != null) {
                            try {
                                switch (line.split(" ")[0]) {
                                    case "continue": 
                                    case "cont": 
                                    case "c": {
                                        this.isbpl.debuggerIPC.run.put(tid, -1);
                                        break;
                                    }
                                    case "stop": 
                                    case "s": {
                                        this.isbpl.debuggerIPC.run.put(tid, 0);
                                        break;
                                    }
                                    case "next": 
                                    case "n": {
                                        this.isbpl.debuggerIPC.run.put(tid, 1);
                                        break;
                                    }
                                    case "nextall": 
                                    case "na": {
                                        this.isbpl.debuggerIPC.run.replaceAll((i, v) -> 1);
                                        break;
                                    }
                                    case "do": 
                                    case "d": {
                                        this.isbpl.debuggerIPC.run.put(tid, Integer.parseInt(line.split(" ")[1]));
                                        break;
                                    }
                                    case "until": 
                                    case "u": {
                                        this.isbpl.debuggerIPC.until = line.split(" ")[1];
                                        this.isbpl.debuggerIPC.run.put(tid, -2);
                                        break;
                                    }
                                    case "eval": {
                                        this.isbpl.debuggerIPC.run.put(tid, -3);
                                        this.isbpl.debuggerIPC.threadID = Thread.currentThread().getId();
                                        try {
                                            this.isbpl.interpret(new File("_debug").getAbsoluteFile(), line.substring(5), this.isbpl.debuggerIPC.stack.get(tid));
                                            break;
                                        }
                                        catch (ISBPLStop stop) {
                                            System.exit(this.isbpl.exitCode);
                                            break;
                                        }
                                        catch (Throwable e) {
                                            e.printStackTrace();
                                            boolean fixed = false;
                                            while (!fixed) {
                                                try {
                                                    System.err.println("Stack recovered: " + this.isbpl.debuggerIPC.stack.get(tid));
                                                    fixed = true;
                                                }
                                                catch (Exception e1) {
                                                    e.printStackTrace();
                                                    System.err.println("!!! STACK CORRUPTED!");
                                                    this.isbpl.debuggerIPC.stack.get(tid).pop();
                                                    System.err.println("Popped. Trying again.");
                                                }
                                            }
                                            continue block57;
                                        }
                                    }
                                    case "dump": {
                                        try {
                                            System.err.println("VAR DUMP\n----------------");
                                            for (HashMap map : (Stack)this.isbpl.functionStack.map.get(tid)) {
                                                for (String key : map.keySet()) {
                                                    if (!key.startsWith("=")) continue;
                                                    ((ISBPLCallable)map.get(key.substring(1))).call(new File("_debug").getAbsoluteFile(), this.isbpl.debuggerIPC.stack.get(tid));
                                                    System.err.println("\t" + key.substring(1) + ": \t" + this.isbpl.debuggerIPC.stack.get(tid).pop());
                                                }
                                                System.err.println("----------------");
                                            }
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            System.err.println("!!! VARS CORRUPTED! CANNOT FIX AUTOMATICALLY.");
                                        }
                                    }
                                    case "stack": {
                                        boolean fixed = false;
                                        while (!fixed) {
                                            try {
                                                System.err.println("STACK DUMP");
                                                for (ISBPLObject object : this.isbpl.debuggerIPC.stack.get(tid)) {
                                                    System.err.println("\t" + object);
                                                }
                                                fixed = true;
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                                System.err.println("!!! STACK CORRUPTED!");
                                                this.isbpl.debuggerIPC.stack.get(tid).pop();
                                                System.err.println("Popped. Trying again.");
                                            }
                                        }
                                        break;
                                    }
                                    case "son": {
                                        ISBPL.debug = true;
                                        break;
                                    }
                                    case "soff": {
                                        ISBPL.debug = false;
                                        break;
                                    }
                                    case "exit": {
                                        System.exit(255);
                                        break;
                                    }
                                    case "threads": {
                                        System.err.println("THREAD DUMP");
                                        for (Thread thread : Thread.getAllStackTraces().keySet()) {
                                            if (!this.isbpl.debuggerIPC.stack.containsKey(thread.getId())) continue;
                                            System.err.println(thread.getId() + "\t" + thread.getName());
                                        }
                                        continue block57;
                                    }
                                    case "setthread": 
                                    case "st": {
                                        long l = Long.parseLong(line.split(" ")[1]);
                                        if (this.isbpl.debuggerIPC.stack.containsKey(l)) {
                                            tid = l;
                                            System.err.println("Set TID=" + l);
                                            break;
                                        }
                                        System.err.println("Thread not valid");
                                        break;
                                    }
                                }
                            }
                            catch (Exception e) {
                                try {
                                    e.printStackTrace(new PrintStream(s.getOutputStream()));
                                }
                                catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    catch (IOException e) {
                        try {
                            s.close();
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    static class IPC {
        long threadID = -1L;
        String until = null;
        HashMap<Long, Integer> run = new HashMap();
        HashMap<Long, Stack<ISBPLObject>> stack = new HashMap();

        IPC() {
        }
    }
}
