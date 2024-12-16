package com.hkb.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.impl.IKeyMappingImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Arrays;
import java.util.function.Function;

@Mod( "hide_key_binding" )
@EventBusSubscriber
public final class HKBMod
{
	private static final IKeyConflictContext CONTEXT_HIDE = new IKeyConflictContext() {
		@Override
		public boolean isActive() {
			return false;
		}
		
		@Override
		public boolean conflicts( IKeyConflictContext other ) {
			return false;
		}
	};
	
	private record HiddenEntry( KeyMapping km, IKeyConflictContext cc ) {
	}
	private static HiddenEntry[] hidden_kb_arr = { };
	
	private static void __refreshAndDisableHidden()
	{
		for ( var entry : hidden_kb_arr ) {
			entry.km.setKeyConflictContext( entry.cc );
		}
		
		final var mc = Minecraft.getInstance();
		final var hidden = ImmutableSet.copyOf( HKBModConfig.HIDE_KEY_BINDINGS.get() );
		hidden_kb_arr = (
			Arrays.stream( mc.options.keyMappings )
			.filter( km -> hidden.contains( km.getName() ) )
			.map( km -> new HiddenEntry( km, km.getKeyConflictContext() ) )
			.toArray( HiddenEntry[]::new )
		);
		
		for ( var entry : hidden_kb_arr ) {
			entry.km.setKeyConflictContext( CONTEXT_HIDE );
		}
	}
	
	public HKBMod()
	{
		// Make sure the mod being absent on the other network side does not
		// cause the client to display the server as incompatible.
		final ModLoadingContext load_ctx = ModLoadingContext.get();
		load_ctx.registerExtensionPoint(
			DisplayTest.class,
			() -> new DisplayTest(
				() -> "This is a client only mod.",
				( remote_version_string, network_bool ) -> network_bool
			)
		);
		
		// Setup mod config settings.
		load_ctx.registerConfig( Type.CLIENT, HKBModConfig.CONFIG_SPEC );
		load_ctx.registerExtensionPoint(
			ConfigGuiFactory.class,
			() -> new ConfigGuiFactory( ( mc, screen ) -> new HKBConfigScreen( screen ) )
		);
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onScreenOpen( ScreenOpenEvent evt )
			{
				__refreshAndDisableHidden();
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
	
	
	private static KeyMapping[] ori_km_arr;
	
	@SubscribeEvent
	static void onInitScreen$Pre( InitScreenEvent.Pre evt )
	{
		if ( evt.getScreen() instanceof KeyBindsScreen )
		{
			final var mc = Minecraft.getInstance();
			final var options = mc.options;
			ori_km_arr = options.keyMappings;
			
			final Function< KeyMapping, String > to_raw_name;
			if ( ModList.get().isLoaded( "key_binding_patch" ) )
			{
				to_raw_name = km -> {
					final var name = km.getName();
					return IKeyMappingImpl.getShadowTarget( name ).orElse( name );
				};
			}
			else {
				to_raw_name = KeyMapping::getName;
			}
			
			final var hidden = ImmutableSet.copyOf( HKBModConfig.HIDE_KEY_BINDINGS.get() );
			options.keyMappings = (
				Arrays.stream( ori_km_arr )
				.filter( km -> !hidden.contains( to_raw_name.apply( km ) ) )
				.toArray( KeyMapping[]::new )
			);
		}
	}
	
	@SubscribeEvent
	static void onInitScreen$Post( InitScreenEvent.Post evt )
	{
		if ( evt.getScreen() instanceof KeyBindsScreen )
		{
			final var mc = Minecraft.getInstance();
			final var options = mc.options;
			options.keyMappings = ori_km_arr;
			ori_km_arr = null;
		}
	}
	
	@SubscribeEvent
	static void onConfigReload( ModConfigEvent.Reloading evt ) {
		__refreshAndDisableHidden();
	}
}
