package com.github.commoble.neverwhere.client;

import com.github.commoble.neverwhere.NeverwasEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeverwasRenderer extends BipedRenderer<NeverwasEntity, BipedModel<NeverwasEntity>>
{

	private static final ResourceLocation TEXTURE = new ResourceLocation("neverwhere:textures/entity/neverwas.png");

	protected NeverwasRenderer(EntityRendererManager renderManager)
	{
		super(renderManager, new BipedModel<NeverwasEntity>(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(NeverwasEntity entity)
	{
		return TEXTURE;
	}

	@Override
	public void doRender(NeverwasEntity entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();

		GlStateManager.blendFunc(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO);
		float f = MathHelper.sin((float)Math.pow(1.4D, -(entity.world.getDayTime() + partialTicks)));
		GlStateManager.color4f(f,f,f, 2f);
		//GlStateManager.translated(x, y, z);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}