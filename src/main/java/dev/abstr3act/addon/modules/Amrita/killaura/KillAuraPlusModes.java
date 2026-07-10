package dev.abstr3act.addon.modules.Amrita.killaura;

public enum KillAuraPlusModes {
    None,
    Matrix;

    @Override
    public String toString() {
        return super.toString().replaceAll("Plus", "+").replaceAll("_", " ");
    }
}
