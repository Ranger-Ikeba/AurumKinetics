package com.aurumkinetics.item;

import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.world.item.Item;

public class SpeedupItem extends Item {
    private final SpeedupType type;

    public SpeedupItem(Properties props, SpeedupType type) {
        super(props);
        this.type = type;
    }

    public SpeedupType getSpeedupType() { return type; }
}
