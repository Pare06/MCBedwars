package org.bedwars.config;

public class ExperienceConfig {
    private ExperienceConfig() { }

    public static final int WIN_XP = 100;
    public static final int KILL_XP = 5;
    public static final int FINAL_XP = 10; // sommato alla kill
    public static final int BED_XP = 20;

    public static final double SCALING_FACTOR = 1.5;
    public static final double UNKNOWN_FACTOR = 0.04; // non ho idea di come fare una formula sensata per l'exp, e questa variabile era in quella che ho rubato
}
