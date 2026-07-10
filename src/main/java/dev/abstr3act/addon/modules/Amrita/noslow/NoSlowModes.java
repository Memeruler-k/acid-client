package dev.abstr3act.addon.modules.Amrita.noslow;

public enum NoSlowModes {
    Vanilla,
    NCP_Strict,
    Grim_1dot8,
    Grim_New,
    Matrix;

    @Override
    public String toString() {
        return super.toString().replace('_', ' ').replaceAll("dot", ".");
    }
}
