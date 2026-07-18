package us.kayla.zeppelinmusthave.integration.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Renders Aeronautics' native armor texture when the goggles occupy a Curios head slot. */
public final class AviatorsGogglesCurioRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "aeronautics",
            "textures/models/armor/aviators_goggles_layer_1.png"
    );

    private final HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)
    );

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> parentModel)) {
            return;
        }

        this.armorModel.setAllVisible(false);
        this.armorModel.head.visible = true;
        this.armorModel.hat.visible = true;
        this.armorModel.head.copyFrom(parentModel.head);
        this.armorModel.hat.copyFrom(parentModel.hat);

        VertexConsumer armorBuffer = buffer.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
        this.armorModel.renderToBuffer(
                poseStack,
                armorBuffer,
                light,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
        if (stack.hasFoil()) {
            this.armorModel.renderToBuffer(
                    poseStack,
                    buffer.getBuffer(RenderType.armorEntityGlint()),
                    light,
                    OverlayTexture.NO_OVERLAY
            );
        }
    }
}
