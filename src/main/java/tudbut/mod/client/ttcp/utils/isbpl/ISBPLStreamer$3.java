package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLStreamer;

class ISBPLStreamer$3
extends InputStream {
    final ServerSocket val$server;

    ISBPLStreamer$3(ServerSocket serverSocket) {
        this.val$server = serverSocket;
    }

    @Override
    public int read() throws IOException {
        Socket socket = this.val$server.accept();
        ISBPLStreamer.ISBPLStream stream = new ISBPLStreamer.ISBPLStream(socket.getInputStream(), socket.getOutputStream());
        return stream.id;
    }
}
