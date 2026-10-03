package com.aurumkinetics.client;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.entity.GoldCoatingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public class CoatingRenderer extends EntityRenderer<GoldCoatingEntity> {

    private static final ResourceLocation TEX_GOLD =
            new ResourceLocation(AurumKinetics.MOD_ID, "textures/entity/gold_rubbish.png");
    private static final ResourceLocation TEX_QUARTZ =
            new ResourceLocation(AurumKinetics.MOD_ID, "textures/entity/quartz_coating.png");
    private static final ResourceLocation TEX_BLUE =
            new ResourceLocation(AurumKinetics.MOD_ID, "textures/entity/blue_coating.png");

    public CoatingRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public ResourceLocation getTextureLocation(GoldCoatingEntity entity) {
        return switch (entity.getCoatingType()) {
            case QUARTZ -> TEX_QUARTZ;
            case BLUE -> TEX_BLUE;
            default -> TEX_GOLD;
        };
    }

    @Override
    public void render(GoldCoatingEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();

        // Позиция энтити уже на поверхности блока.
        // Никаких translate — рисуем квадрат 1×1 в центре энтити, повёрнутый по face.
        Direction face = entity.getFacing();
        float nx = face.getStepX(), ny = face.getStepY(), nz = face.getStepZ();
        float e = 0.001f;

        float[][] corners = new float[4][3];
        if (face == Direction.UP || face == Direction.DOWN) {
            corners[0] = new float[]{-0.5f, 0, -0.5f};
            corners[1] = new float[]{-0.5f, 0,  0.5f};
            corners[2] = new float[]{ 0.5f, 0,  0.5f};
            corners[3] = new float[]{ 0.5f, 0, -0.5f};
        } else if (face == Direction.NORTH || face == Direction.SOUTH) {
            corners[0] = new float[]{-0.5f, -0.5f, 0};
            corners[1] = new float[]{-0.5f,  0.5f, 0};
            corners[2] = new float[]{ 0.5f,  0.5f, 0};
            corners[3] = new float[]{ 0.5f, -0.5f, 0};
        } else {
            corners[0] = new float[]{0, -0.5f, -0.5f};
            corners[1] = new float[]{0,  0.5f, -0.5f};
            corners[2] = new float[]{0,  0.5f,  0.5f};
            corners[3] = new float[]{0, -0.5f,  0.5f};
        }

        for (int i = 0; i < 4; i++) {
            if (face == Direction.UP)   corners[i][1] += e;
            if (face == Direction.DOWN) corners[i][1] -= e;
            if (face == Direction.NORTH) corners[i][2] -= e;
            if (face == Direction.SOUTH) corners[i][2] += e;
            if (face == Direction.WEST)  corners[i][0] -= e;
            if (face == Direction.EAST)  corners[i][0] += e;
        }

        Level level = entity.level();
        BlockPos lp = entity.getAttachedPos().relative(face);
        int bl = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, lp);
        int sl = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, lp);
        int packed = LightTexture.pack(bl, sl);

        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        Matrix4f m = pose.last().pose();

        float[][] uv = {{0,0}, {0,1}, {1,1}, {1,0}};
        for (int i = 0; i < 4; i++) {
            vc.vertex(m, corners[i][0], corners[i][1], corners[i][2])
                    .color(255, 255, 255, 255)
                    .uv(uv[i][0], uv[i][1])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packed)
                    .normal(nx, ny, nz)
                    .endVertex();
        }

        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
}
