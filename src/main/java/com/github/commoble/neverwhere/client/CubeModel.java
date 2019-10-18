package com.github.commoble.neverwhere.client;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class CubeModel extends Model
{
	RendererModel cube;
	
	public CubeModel()
	{
		this.cube = new RendererModel(this, 0, 0);
		this.cube.addBox(0f, 0f, 0f, 16, 16, 16);
		this.cube.setRotationPoint(-8f, 8f, -8f);
		this.cube.setTextureSize(64, 32);
		this.cube.mirror=true;
		this.cube.rotateAngleX=0;
		this.cube.rotateAngleY=0;
		this.cube.rotateAngleZ=0;
	}
	
	public void render(float scale)
	{
		this.cube.render(scale);
	}
}
