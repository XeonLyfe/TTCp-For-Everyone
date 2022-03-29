package tudbut.mod.client.ttcp.mods.misc;

import tudbut.mod.client.ttcp.gui.lib.component.Button;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.Setting;

class AltControl$1
extends Button {
    AltControl$1(String s, Button.ClickEvent event) {
        super(s, event);
        this.subComponents.add(Setting.createKey("Kill", (Module.KeyBind)AltControl.this.customKeyBinds.get("kill")));
        this.subComponents.add(Setting.createKey("Follow", (Module.KeyBind)AltControl.this.customKeyBinds.get("follow")));
    }
}
