package tudbut.mod.client.ttcp.utils.isbpl;

import java.io.IOException;
import java.io.OutputStream;
import tudbut.mod.client.ttcp.utils.isbpl.ISBPLError;

class ISBPLStreamer$4
extends OutputStream {
    ISBPLStreamer$4() {
    }

    @Override
    public void write(int b) throws IOException {
        throw new ISBPLError("IllegalArgument", "Can't write to a SERVER stream!");
    }
}
