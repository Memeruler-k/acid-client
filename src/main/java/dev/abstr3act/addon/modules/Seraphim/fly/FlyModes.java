package dev.abstr3act.addon.modules.Seraphim.fly;

public enum FlyModes {
    Vulcan_Clip,
    Matrix_Exploit_2,
    Matrix_Exploit,
    Damage,
    Damage_OldFag;

    @Override
    public String toString() {
        String name = this.name();
        return name.replace('_', ' ');
    }
}
