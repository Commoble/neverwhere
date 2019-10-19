package com.github.commoble.neverwhere.client;

import java.util.Random;

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
		long hash = (int) Math.abs(entity.getUniqueID().getLeastSignificantBits() & 0xFF);
		Random rand = new Random(hash);
		float frequencyA = rand.nextFloat()*0.025F + 0.05F;
		float frequencyB = rand.nextFloat()*0.025F + 0.05F;
		float frequencyC = rand.nextFloat()*0.3F + 0.05F;

		float time = entity.world.getDayTime() + partialTicks + hash;
		GlStateManager.blendFunc(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO);
		//float f = MathHelper.sin((float)Math.pow(Math.E, Math.sin(0.05D*(time)) * Math.sin(0.05D*(time))));
		float f = MathHelper.sin(frequencyC * (float)Math.pow(Math.E, Math.sin(frequencyA*(time)) + (float)Math.pow(Math.E, Math.cos(frequencyB*(time)))));
		f = f*f*f;
		GlStateManager.color4f(f,f,f, 0.5F);
		//GlStateManager.translated(x, y, z);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}