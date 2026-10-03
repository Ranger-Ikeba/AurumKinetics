package com.aurumkinetics.entity;

import com.aurumkinetics.item.GoldCoatingItem;
import com.aurumkinetics.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class GoldCoatingEntity extends Entity {

    private static final EntityDataAccessor<Direction> DATA_FACING =
            SynchedEntityData.defineId(GoldCoatingEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(GoldCoatingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> DATA_POS =
            SynchedEntityData.defineId(GoldCoatingEntity.class, EntityDataSerializers.BLOCK_POS);

    public GoldCoatingEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_FACING, Direction.NORTH);
        this.entityData.define(DATA_TYPE, 0);
        this.entityData.define(DATA_POS, BlockPos.ZERO);
    }

    public void attachTo(BlockPos pos, Direction face, GoldCoatingItem.Type type) {
        this.entityData.set(DATA_POS, pos);
        this.entityData.set(DATA_FACING, face);
        this.entityData.set(DATA_TYPE, type.ordinal());
        refreshPosition();
    }

    /** Позиция ровно на поверхности блока. */
    private void refreshPosition() {
        BlockPos pos = getAttachedPos();
        Direction face = getFacing();
        double x = pos.getX() + 0.5 + face.getStepX() * 0.5;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.5;
        this.setPos(x, y, z);
    }

    public BlockPos getAttachedPos() { return this.entityData.get(DATA_POS); }
    public Direction getFacing() { return this.entityData.get(DATA_FACING); }

    public GoldCoatingItem.Type getCoatingType() {
        int i = this.entityData.get(DATA_TYPE);
        GoldCoatingItem.Type[] v = GoldCoatingItem.Type.values();
        return (i >= 0 && i < v.length) ? v[i] : GoldCoatingItem.Type.GOLD;
    }

    public void setCoatingType(GoldCoatingItem.Type type) {
        this.entityData.set(DATA_TYPE, type.ordinal());
    }

    public static AABB getBoundingBoxFor(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5 + face.getStepX() * 0.5;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.5;
        return new AABB(x - 0.1, y - 0.1, z - 0.1, x + 0.1, y + 0.1, z + 0.1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        BlockPos pos = getAttachedPos();
        Direction face = getFacing();

        if (this.level().isEmptyBlock(pos)) { dropAndKill(); return; }
        if (!this.level().isEmptyBlock(pos.relative(face))) { dropAndKill(); return; }
    }

    private void dropAndKill() {
        ItemStack drop = new ItemStack(switch (getCoatingType()) {
            case QUARTZ -> ModItems.QUARTZ_COATING.get();
            case BLUE -> ModItems.BLUE_COATING.get();
            default -> ModItems.GOLD_COATING.get();
        });
        this.spawnAtLocation(drop);
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Facing"))
            this.entityData.set(DATA_FACING, Direction.from3DDataValue(tag.getInt("Facing")));
        if (tag.contains("Type"))
            this.entityData.set(DATA_TYPE, tag.getInt("Type"));
        if (tag.contains("AttachedX"))
            this.entityData.set(DATA_POS,
                    new BlockPos(tag.getInt("AttachedX"), tag.getInt("AttachedY"), tag.getInt("AttachedZ")));
        refreshPosition();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Facing", getFacing().get3DDataValue());
        tag.putInt("Type", getCoatingType().ordinal());
        BlockPos pos = getAttachedPos();
        tag.putInt("AttachedX", pos.getX());
        tag.putInt("AttachedY", pos.getY());
        tag.putInt("AttachedZ", pos.getZ());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean canBeCollidedWith() { return false; }
}
