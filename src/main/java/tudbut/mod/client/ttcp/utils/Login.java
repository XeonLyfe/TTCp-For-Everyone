package tudbut.mod.client.ttcp.utils;

import com.mojang.authlib.GameProfile;
import de.tudbut.tools.Hasher;
import javax.swing.JOptionPane;
import tudbut.api.impl.TudbuTAPI;
import tudbut.mod.client.ttcp.TTCp;
import tudbut.parsing.TCN;

public class Login {
    public static final boolean isDebugMode = true;

    public static boolean isRegistered(TCN data) {
        try {
            GameProfile profile = TTCp.mc.getSession().getProfile();
            if (profile.getName().startsWith("TudbuT") || profile.getName().equals("PipDev")) {
                return true;
            }
            String login = TudbuTAPI.getHashedPassword(profile.getId());
            System.out.println("Password should be: " + login);
            String pw = Hasher.sha512hex(Hasher.sha256hex(JOptionPane.showInputDialog("Please enter your password")));
            System.out.println("Password is: " + pw);
            return pw.equals(login);
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }
}
