package tudbut.mod.client.ttcp.mods.rendering;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

class CustomTheme$1
implements WindowListener {
    CustomTheme$1() {
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        CustomTheme.this.show = false;
        CustomTheme.this.selectedColor = null;
        CustomTheme.this.updateBinds();
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {
    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
    }
}
