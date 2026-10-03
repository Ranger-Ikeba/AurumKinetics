package com.aurumkinetics.item;

import com.aurumkinetics.entity.GoldCoatingEntity;
import com.aurumkinetics.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class GoldCoatingItem extends Item {

    public enum Type { GOLD, QUARTZ, BLUE }

    private final Type type;

    public GoldCoatingItem(Properties props, Type type) {
        super(props);
        this.type = type;
    }

    public Type getType() { return type; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        AABB checkBox = GoldCoatingEntity.getBoundingBoxFor(pos, face);
        if (!level.getEntitiesOfClass(GoldCoatingEntity.class, checkBox).isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            GoldCoatingEntity entity = new GoldCoatingEntity(ModEntities.GOLD_COATING.get(), level);
            entity.attachTo(pos, face, type);
            level.addFreshEntity(entity);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
