package com.github.commoble.neverwhere.client;

import com.github.commoble.neverwhere.NeverPortalBlock;
import com.github.commoble.neverwhere.NeverPortalTileEntity;
import com.github.commoble.neverwhere.Neverwhere;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.PositionTextureVertex;
import net.minecraft.client.renderer.model.TexturedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class NeverPortalTileEntityRenderer extends TileEntityRenderer<NeverPortalTileEntity>
{
	// private CubeModel model = new CubeModel();
	private static final ResourceLocation texture = new ResourceLocation("neverwhere:textures/entity/neverportal.png");

	public final PositionTextureVertex[] vertices;
	public final TexturedQuad[] quads;
	float posX1 = 0f;
	float posY1 = 0f;
	float posZ1 = 0f;
	float posX2 = 16f;
	float posY2 = 16f;
	float posZ2 = 16f;

	public NeverPortalTileEntityRenderer()
	{
		int texU = 16;
		int texV = 16;
		float textureWidth = texU;
		float textureHeight = texV;

		float x = 0f;
		float y = 0f;
		float z = 0f;
		int dx = 16;
		int dy = 16;
		int dz = 16;
		float delta = 0f;
		this.posX1 = x;
		this.posY1 = y;
		this.posZ1 = z;
		this.posX2 = x + dx;
		this.posY2 = y + dy;
		this.posZ2 = z + dz;
		this.vertices = new PositionTextureVertex[8];
		this.quads = new TexturedQuad[6];
		float f = x + dx;
		float f1 = y + dy;
		float f2 = z + dz;
//		if (false)
//		{
//			float f3 = f;
//			f = x;
//			x = f3;
//		}

		PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
		PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
		PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
		PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
		PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
		this.vertices[0] = positiontexturevertex7;
		this.vertices[1] = positiontexturevertex;
		this.vertices[2] = positiontexturevertex1;
		this.vertices[3] = positiontexturevertex2;
		this.vertices[4] = positiontexturevertex3;
		this.vertices[5] = positiontexturevertex4;
		this.vertices[6] = positiontexturevertex5;
		this.vertices[7] = positiontexturevertex6;
		this.quads[0] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex4, positiontexturevertex, positiontexturevertex1,
						positiontexturevertex5 },
				texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, textureWidth, textureHeight);
		this.quads[1] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex7, positiontexturevertex3, positiontexturevertex6,
						positiontexturevertex2 },
				texU, texV + dz, texU + dz, texV + dz + dy, textureWidth, textureHeight);
		this.quads[2] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex4, positiontexturevertex3, positiontexturevertex7,
						positiontexturevertex },
				texU + dz, texV, texU + dz + dx, texV + dz, textureWidth, textureHeight);
		this.quads[3] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex1, positiontexturevertex2, positiontexturevertex6,
						positiontexturevertex5 },
				texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, textureWidth, textureHeight);
		this.quads[4] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex, positiontexturevertex7, positiontexturevertex2,
						positiontexturevertex1 },
				texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, textureWidth, textureHeight);
		this.quads[5] = new TexturedQuad(
				new PositionTextureVertex[] { positiontexturevertex3, positiontexturevertex4, positiontexturevertex5,
						positiontexturevertex6 },
				texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, textureWidth, textureHeight);
//		if (false)
//		{
//			for (TexturedQuad texturedquad : this.quads)
//			{
//				texturedquad.flipFace();
//			}
//		}
	}
	
	private static final float DEG_2_RAD = (float)Math.PI / 180F;
	
	// returns a value normalized to the range [0F, 1F]
	private static float normalizeRotation(float rot, float scale, float offset)
	{
		return MathHelper.sin((rot * DEG_2_RAD + offset) * scale) * 0.5F + 0.5F;
	}
	
	private static float PI = (float)Math.PI;

	@Override
	public void render(NeverPortalTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage)
	{
		this.bindTexture(texture);
		BlockState state = te.getBlockState();
		if (state.getBlock() != Neverwhere.neverPortalBlock.get())
		{
			return;	// make very sure the block is correct to prevent crashing when we check the state properties
		}
		
		ClientPlayerEntity player = Minecraft.getInstance().player;
		
		//float yaw = player.getYaw(partialTicks);	// I think this is in degrees
		float yaw = Minecraft.getInstance().world.getGameTime() + partialTicks;
		
		float r = normalizeRotation(yaw, 5F, 0F) * 1F - 0.2F;
		float g = normalizeRotation(yaw, 11F, 10F) * r - 0.1F;
		float b = normalizeRotation(yaw, 19F, 20F) * g - 0.1F;

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();

		GlStateManager.blendFunc(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO);
		 GlStateManager.color4f(r,g,b, 1f);
		// GlStateManager.translated(x + 0.5D, y + 1.5D, z + 0.5D);
		GlStateManager.translated(x, y, z);
		// GlStateManager.scalef(1f, -1f, -1f);
		// GlStateManager.scalef(1f, 1f, 1f);

		// this.model.render(0.0625F);

		float scale = 0.0625f;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		for (int i=0; i<this.quads.length; i++)
		{
			if (!state.get(NeverPortalBlock.propertiesByRenderDirection[i]).booleanValue())	// state is true if rendering blocked on that side
			{

				TexturedQuad quad = this.quads[i];
				quad.draw(bufferBuilder, scale);
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}
