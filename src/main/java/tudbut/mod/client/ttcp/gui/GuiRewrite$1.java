package tudbut.mod.client.ttcp.gui;

import tudbut.mod.client.ttcp.gui.lib.component.Category;
import tudbut.mod.client.ttcp.utils.Module;

class GuiRewrite$1
extends Category {
    final Module val$module;

    GuiRewrite$1(Module module) {
        this.val$module = module;
        this.text = this.val$module.category.getSimpleName();
    }
}
