package com.hkb.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

@OnlyIn( Dist.CLIENT )
final class HKBModConfig
{
	static final ForgeConfigSpec CONFIG_SPEC;
	static final ConfigValue< List< ? extends String > > HIDE_KEY_BINDINGS;
	static final BooleanValue ENABLE_CUSTOM_CONFIG_RELOAD_EVENT;
	
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
		ENABLE_CUSTOM_CONFIG_RELOAD_EVENT = (
			builder.comment(
				"The default file event handler used by the Forge config system",
				"seems to have bug and cannot reliably trigger the update when config",
				"is saved by the game. Enable this will manually trigger the update",
				"to avoid this issue."
			)
			.define( "enable_custom_config_reload_event", true )
		);
		CONFIG_SPEC = builder.build();
	}
	
	private HKBModConfig() {
	}
}
