package dev.abstr3act.addon.modules.Amrita.spider;

public enum SpiderModes {
    Matrix,
    Vulcan,
    Elytra_clip;

    @Override
    public String toString() {
        return super.toString().replace('_', ' ').replaceAll("_Lower_", "<");
    }
}
