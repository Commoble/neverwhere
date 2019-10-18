package com.github.commoble.neverwhere;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Registrator<T extends IForgeRegistryEntry<T>>
{
	public IForgeRegistry<T> registry;
	
	public Registrator(IForgeRegistry<T> registry)
	{
		this.registry = registry;
	}
	
	public T register(String registryKey, T entry)
	{
		ResourceLocation loc = new ResourceLocation(Neverwhere.MODID, registryKey);
		entry.setRegistryName(loc);
		this.registry.register(entry);
		return entry;
		//return RegistryObject.of(loc.toString(), this.registry);
	}
}
