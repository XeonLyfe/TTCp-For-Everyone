package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.IOException;
import java.io.InputStream;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;

class ISBPLStreamer$2
extends InputStream {
    ISBPLStreamer$2() {
    }

    @Override
    public int read() throws IOException {
        throw new ISBPLError("IllegalArgument", "Can't read a FILE_OUT stream!");
    }
}
