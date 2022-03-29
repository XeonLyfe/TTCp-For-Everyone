package tudbut.mod.client.ttcp.gui.lib.component;

import tudbut.mod.client.ttcp.gui.lib.component.Component;

public class Button
extends Component {
    private final ClickEvent event;

    public Button(String s, ClickEvent event) {
        this.green = true;
        this.text = s;
        this.event = event;
    }

    @Override
    public void click(int x, int y, int mouseButton) {
        super.click(x, y, mouseButton);
        if (mouseButton == 0) {
            this.green = true;
            this.event.click(this);
        }
    }

    public static interface ClickEvent {
        public void click(Button var1);
    }
}
