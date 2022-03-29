package tudbut.mod.client.ttcp.mods.misc;

import java.io.PrintStream;
import tudbut.debug.DebugProfiler;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.DebugProfilerAdapter;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Misc;

@Misc
public class Debug
extends Module {
    static Debug instance;

    public Debug() {
        instance = this;
    }

    public static Debug getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        for (DebugProfilerAdapter profiler : TTCp.getProfilers()) {
            profiler.fallthrough = false;
        }
    }

    @Override
    public void onDisable() {
        for (DebugProfilerAdapter profiler : TTCp.getProfilers()) {
            profiler.fallthrough = true;
        }
    }

    @Override
    public void init() {
        for (DebugProfilerAdapter profiler : TTCp.getProfilers()) {
            profiler.fallthrough = !this.enabled;
        }
    }

    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onSubTick() {
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        PrintStream out = ChatUtils.chatPrinter();
        DebugProfilerAdapter[] profilers = TTCp.getProfilers();
        for (int i = 0; i < profilers.length; ++i) {
            out.println(((DebugProfiler)profilers[i]).toString());
        }
    }
}
