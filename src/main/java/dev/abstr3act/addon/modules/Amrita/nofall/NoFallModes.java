package dev.abstr3act.addon.modules.Amrita.nofall;

public enum NoFallModes {
    Matrix_New,
    Vulcan,
    Vulcan_2dot7dot7,
    Verus,
    Elytra_Clip,
    Elytra_Fly;

    @Override
    public String toString() {
        return super.toString().replace('_', ' ').replaceAll("dot", ".");
    }
}
