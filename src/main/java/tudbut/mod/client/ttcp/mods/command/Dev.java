package tudbut.mod.client.ttcp.mods.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPI;
import tudbut.mod.client.ttcp.utils.ChatUtils;
import tudbut.mod.client.ttcp.utils.Module;
import tudbut.mod.client.ttcp.utils.category.Command;
import tudbut.net.http.HTTPHeader;
import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.net.http.HTTPUtils;
import tudbut.parsing.ArgumentParser;

@Command
public class Dev
extends Module {
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }

    @Override
    public void onEveryChat(String str, String[] args) {
        System.out.println(args.length);
        if (args.length == 0) {
            ChatUtils.print(",dev -rdesp -l <login> -n <name> [-x <pwd>]");
            ChatUtils.print("r: Remove premium");
            ChatUtils.print("d: Disable");
            ChatUtils.print("e: Enable");
            ChatUtils.print("s: Disable once");
            ChatUtils.print("p: Set premium password");
            ChatUtils.print("l: Admin password");
            ChatUtils.print("n: Name");
            ChatUtils.print("x: New password for -p");
            return;
        }
        ChatUtils.print(Arrays.toString(args));
        Map<String, String> arguments = ArgumentParser.parseDefault(args);
        boolean r = "true".equals(arguments.get("r"));
        boolean d = "true".equals(arguments.get("d"));
        boolean e = "true".equals(arguments.get("e"));
        boolean s = "true".equals(arguments.get("s"));
        boolean p = "true".equals(arguments.get("p"));
        String l = arguments.getOrDefault("l", " ");
        String password = arguments.getOrDefault("x", "-");
        ChatUtils.print("Asking mojang for the UUID of " + arguments.get("n") + " (with SSL)...");
        HTTPRequest uuidRequest = new HTTPRequest(HTTPRequestType.GET, "https://api.mojang.com", 443, "/users/profiles/minecraft/" + arguments.get("n"), new HTTPHeader[0]);
        String re = null;
        try {
            re = uuidRequest.send().parse().getBody();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        ChatUtils.print("Got: " + re);
        String[] resp = re.split("\"id\":\"");
        String uuidString = resp[resp.length - 1].split("\"")[0];
        uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" + uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
        ChatUtils.print("Recreated UUID: " + uuidString);
        UUID uuid = UUID.fromString(uuidString);
        ChatUtils.print(arguments.get("n") + " is " + uuid + ".");
        ChatUtils.print("Calling api.tudbut.de...");
        if (d) {
            Dev.d(uuid, l);
        }
        TudbuTAPI.awaitRateLimitEnd();
        if (s) {
            Dev.s(uuid, l);
        }
        TudbuTAPI.awaitRateLimitEnd();
        if (r) {
            Dev.r(uuid, l);
        }
        TudbuTAPI.awaitRateLimitEnd();
        if (e) {
            Dev.e(uuid, l);
        }
        TudbuTAPI.awaitRateLimitEnd();
        if (p) {
            Dev.p(uuid, l, password);
        }
        TudbuTAPI.awaitRateLimitEnd();
        ChatUtils.print("Done.");
    }

    private static void d(UUID uuid, String l) {
        try {
            TudbuTAPI.get("admin/deactivate", "uuid=" + uuid + "&key=" + HTTPUtils.encodeUTF8(l));
        }
        catch (IOException | RateLimit rateLimit) {
            rateLimit.printStackTrace();
        }
    }

    private static void s(UUID uuid, String l) {
        try {
            TudbuTAPI.get("admin/triggerDeactivate", "uuid=" + uuid + "&key=" + HTTPUtils.encodeUTF8(l));
        }
        catch (IOException | RateLimit rateLimit) {
            rateLimit.printStackTrace();
        }
    }

    private static void r(UUID uuid, String l) {
        try {
            TudbuTAPI.get("admin/remove", "uuid=" + uuid + "&key=" + HTTPUtils.encodeUTF8(l));
        }
        catch (IOException | RateLimit rateLimit) {
            rateLimit.printStackTrace();
        }
    }

    private static void p(UUID uuid, String l, String pwd) {
        try {
            TudbuTAPI.get("admin/setPassword", "uuid=" + uuid + "&key=" + HTTPUtils.encodeUTF8(l) + "&password=" + HTTPUtils.encodeUTF8(pwd));
        }
        catch (IOException | RateLimit rateLimit) {
            rateLimit.printStackTrace();
        }
    }

    private static void e(UUID uuid, String l) {
        try {
            TudbuTAPI.get("admin/enable", "uuid=" + uuid + "&key=" + HTTPUtils.encodeUTF8(l));
        }
        catch (IOException | RateLimit rateLimit) {
            rateLimit.printStackTrace();
        }
    }
}
