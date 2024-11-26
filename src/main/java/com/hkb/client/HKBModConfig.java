package com.hkb.client;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config( modid = HKBMod.MODID )
public final class HKBModConfig
{
	@Comment( {
		"Key bindings listed here will be hidden from the key bindings menu.",
		"Use this to hide key bindings that are not used by the player."
	} )
	public static String[] hide_key_bindings = { };
	
	
	private HKBModConfig() {
	}
}
