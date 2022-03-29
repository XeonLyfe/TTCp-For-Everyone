package tudbut.mod.client.ttcp.mods.combat;

import tudbut.mod.client.ttcp.mods.combat.KillAura;

class KillAura$1 {
    static final int[] $SwitchMap$tudbut$mod$client$ttcp$mods$combat$KillAura$SwitchType;

    static {
        $SwitchMap$tudbut$mod$client$ttcp$mods$combat$KillAura$SwitchType = new int[KillAura.SwitchType.values().length];
        try {
            KillAura$1.$SwitchMap$tudbut$mod$client$ttcp$mods$combat$KillAura$SwitchType[KillAura.SwitchType.HOTBAR.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            KillAura$1.$SwitchMap$tudbut$mod$client$ttcp$mods$combat$KillAura$SwitchType[KillAura.SwitchType.SWAP.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
