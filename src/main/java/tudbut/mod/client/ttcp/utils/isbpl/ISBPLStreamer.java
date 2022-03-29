package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPL;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLObject;

class ISBPLStreamer {
    public static final int CREATE_FILE_IN = 0;
    public static final int CREATE_FILE_OUT = 1;
    public static final int CREATE_SOCKET = 2;
    public static final int CLOSE = 3;
    public static final int READ = 4;
    public static final int WRITE = 5;
    public static final int AREAD = 6;
    public static final int AWRITE = 7;
    public static final int CREATE_SERVER = 9;
    final ISBPL isbpl;
    public ArrayList<ISBPLStream> streams = new ArrayList();

    public ISBPLStreamer(ISBPL isbpl) {
        this.isbpl = isbpl;
    }

    public synchronized void action(Stack<ISBPLObject> stack, int action) throws IOException {
        switch (action) {
            case 0: {
                ISBPLObject s = stack.pop();
                s.checkType(this.isbpl.getType("string"));
                File f = new File(this.isbpl.toJavaString(s));
                ISBPLStream stream = new ISBPLStream(new FileInputStream(f), new OutputStream(){

                    @Override
                    public void write(int b) throws IOException {
                        throw new ISBPLError("IllegalArgument", "Can't write to a FILE_IN stream!");
                    }
                });
                this.streams.add(stream);
                stack.push(new ISBPLObject(this.isbpl.getType("int"), stream.id));
                break;
            }
            case 1: {
                ISBPLObject s = stack.pop();
                s.checkType(this.isbpl.getType("string"));
                File f = new File(this.isbpl.toJavaString(s));
                ISBPLStream stream = new ISBPLStream(new InputStream(){

                    @Override
                    public int read() throws IOException {
                        throw new ISBPLError("IllegalArgument", "Can't read a FILE_OUT stream!");
                    }
                }, new FileOutputStream(f));
                this.streams.add(stream);
                stack.push(new ISBPLObject(this.isbpl.getType("int"), stream.id));
                break;
            }
            case 2: {
                ISBPLObject i = stack.pop();
                ISBPLObject s = stack.pop();
                i.checkType(this.isbpl.getType("int"));
                s.checkType(this.isbpl.getType("string"));
                Socket socket = new Socket(this.isbpl.toJavaString(s), (int)((Integer)i.object));
                ISBPLStream stream = new ISBPLStream(socket.getInputStream(), socket.getOutputStream());
                this.streams.add(stream);
                stack.push(new ISBPLObject(this.isbpl.getType("int"), stream.id));
                break;
            }
            case 9: {
                ISBPLObject i = stack.pop();
                i.checkType(this.isbpl.getType("int"));
                final ServerSocket server = new ServerSocket((Integer)i.object);
                ISBPLStream stream = new ISBPLStream(new InputStream(){

                    @Override
                    public int read() throws IOException {
                        Socket socket = server.accept();
                        ISBPLStream stream = new ISBPLStream(socket.getInputStream(), socket.getOutputStream());
                        return stream.id;
                    }
                }, new OutputStream(){

                    @Override
                    public void write(int b) throws IOException {
                        throw new ISBPLError("IllegalArgument", "Can't write to a SERVER stream!");
                    }
                });
                this.streams.add(stream);
                stack.push(new ISBPLObject(this.isbpl.getType("int"), stream.id));
                break;
            }
            case 4: {
                ISBPLObject i = stack.pop();
                i.checkType(this.isbpl.getType("int"));
                try {
                    stack.push(new ISBPLObject(this.isbpl.getType("int"), this.streams.get((int)((Integer)i.object).intValue()).in.read()));
                    break;
                }
                catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "streamid STREAM_READ stream called with non-existing stream argument");
                }
            }
            case 5: {
                ISBPLObject i = stack.pop();
                i.checkType(this.isbpl.getType("int"));
                ISBPLObject bte = stack.pop();
                bte.checkTypeMulti(this.isbpl.getType("int"), this.isbpl.getType("char"), this.isbpl.getType("byte"));
                try {
                    this.streams.get((int)((Integer)i.object).intValue()).out.write((int)bte.toLong());
                    break;
                }
                catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "byte streamid STREAM_WRITE stream called with non-existing stream argument");
                }
            }
            case 3: {
                ISBPLObject i = stack.pop();
                i.checkType(this.isbpl.getType("int"));
                try {
                    ISBPLStream strm = this.streams.get((Integer)i.object);
                    strm.close();
                    break;
                }
                catch (IndexOutOfBoundsException e) {
                    throw new ISBPLError("IllegalArgument", "streamid STREAM_CLOSE stream called with non-existing stream argument");
                }
            }
            default: {
                throw new ISBPLError("NotImplemented", "Not implemented");
            }
        }
    }

    static class ISBPLStream {
        final InputStream in;
        final OutputStream out;
        static int gid = 0;
        final int id = gid++;

        public ISBPLStream(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void close() throws IOException {
            this.in.close();
            this.out.close();
        }
    }
}
