package com.aurumkinetics.speedup;

public enum SpeedupType {
    // GOLD — эффективность (productivity-бонус)
    GOLD("gold", 1.5f, 0xFFD700, 1.0f, 0.84f, 0.0f, "efficiency"),
    // QUARTZ — чистая скорость
    QUARTZ("quartz", 2.0f, 0xFFFFFF, 1.0f, 1.0f, 1.0f, "speed"),
    // AZURE — продуктивность (efficiency-бонус: меньше топлива)
    AZURE("azure", 3.0f, 0x4169E1, 0.25f, 0.41f, 0.88f, "productivity");

    public final String id;
    public final float multiplier;
    public final int color;
    public final float pr, pg, pb;
    public final String advancementId;

    SpeedupType(String id, float multiplier, int color, float pr, float pg, float pb, String advancementId) {
        this.id = id;
        this.multiplier = multiplier;
        this.color = color;
        this.pr = pr;
        this.pg = pg;
        this.pb = pb;
        this.advancementId = advancementId;
    }

    public static SpeedupType byId(String id) {
        for (SpeedupType t : values()) if (t.id.equals(id)) return t;
        return null;
    }

    public String displayName() {
        if (multiplier == (int) multiplier) return "×" + (int) multiplier;
        return "×" + multiplier;
    }
}
