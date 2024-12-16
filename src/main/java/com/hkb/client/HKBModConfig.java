package com.hkb.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

@OnlyIn( Dist.CLIENT )
final class HKBModConfig
{
	static final ForgeConfigSpec CONFIG_SPEC;
	static final ConfigValue< List< ? extends String > > HIDE_KEY_BINDINGS;
	
	static
	{
		final var builder = new Builder();
		HIDE_KEY_BINDINGS = (
			builder.comment(
				"Key bindings listed here will be hidden from the key bindings menu.",
				"Use this to hide key bindings that are not used by the player."
			)
			.defineList( "hide_key_bindings", List.of(), String.class::isInstance )
		);
		CONFIG_SPEC = builder.build();
	}
	
	private HKBModConfig() {
	}
}
