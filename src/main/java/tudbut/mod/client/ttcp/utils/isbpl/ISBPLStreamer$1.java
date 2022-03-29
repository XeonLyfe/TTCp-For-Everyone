package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.IOException;
import java.io.OutputStream;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;

class ISBPLStreamer$1
extends OutputStream {
    ISBPLStreamer$1() {
    }

    @Override
    public void write(int b) throws IOException {
        throw new ISBPLError("IllegalArgument", "Can't write to a FILE_IN stream!");
    }
}
