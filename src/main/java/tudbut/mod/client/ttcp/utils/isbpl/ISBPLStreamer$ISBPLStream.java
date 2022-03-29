package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ISBPLStreamer$ISBPLStream {
    final InputStream in;
    final OutputStream out;
    static int gid = 0;
    final int id = gid++;

    public ISBPLStreamer$ISBPLStream(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void close() throws IOException {
        this.in.close();
        this.out.close();
    }
}
