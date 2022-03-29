package tudbut.mod.client.ttcp.utils;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.ICrashReportDetail;

class WorldGeneratorV2$3
implements ICrashReportDetail<String> {
    WorldGeneratorV2$3() {
    }

    public String call() throws Exception {
        String s = ClientBrandRetriever.getClientModName();
        if (!s.equals("vanilla")) {
            return "Definitely; Client brand changed to '" + s + "'";
        }
        s = WorldGeneratorV2.this.getServerModName();
        if (!"vanilla".equals(s)) {
            return "Definitely; Server brand changed to '" + s + "'";
        }
        return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.";
    }
}
