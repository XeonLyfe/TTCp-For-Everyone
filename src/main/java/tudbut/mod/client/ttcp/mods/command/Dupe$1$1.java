// ERROR: Unable to apply inner class name fixup
package tudbut.mod.client.ttcp.mods.command;

import java.io.IOException;
import java.io.InputStream;

class Dupe.1
extends InputStream {
    Dupe.1() {
    }

    @Override
    public int read() throws IOException {
        return (int)(Math.random() * 255.0);
    }
}
