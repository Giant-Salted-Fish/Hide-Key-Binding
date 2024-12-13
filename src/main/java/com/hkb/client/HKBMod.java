package com.hkb.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;

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
	
	private static final class HiddenEntry
	{
		private final KeyBinding kb;
		private final IKeyConflictContext cc;
		
		private HiddenEntry( KeyBinding kb )
		{
			this.kb = kb;
			this.cc = kb.getKeyConflictContext();
		}
	}
	private static HiddenEntry[] hidden_kb_arr = { };
	
	private static void __refreshAndDisableHidden()
	{
		for ( HiddenEntry entry : hidden_kb_arr ) {
			entry.kb.setKeyConflictContext( entry.cc );
		}
		
		final Minecraft mc = Minecraft.getInstance();
		final ImmutableSet< String > hidden = ImmutableSet.copyOf( HKBModConfig.HIDE_KEY_BINDINGS.get() );
		hidden_kb_arr = (
			Arrays.stream( mc.options.keyMappings )
			.filter( km -> hidden.contains( km.getName() ) )
			.map( HiddenEntry::new )
			.toArray( HiddenEntry[]::new )
		);
		
		for ( HiddenEntry entry : hidden_kb_arr ) {
			entry.kb.setKeyConflictContext( CONTEXT_HIDE );
		}
	}
	
	
	public HKBMod()
	{
		// Make sure the mod being absent on the other network side does not
		// cause the client to display the server as incompatible.
		final ModLoadingContext load_ctx = ModLoadingContext.get();
		load_ctx.registerExtensionPoint(
			ExtensionPoint.DISPLAYTEST,
			() -> Pair.of(
				() -> "This is a client only mod.",
				( remote_version_string, network_bool ) -> network_bool
			)
		);
		
		// Setup mod config settings.
		load_ctx.registerConfig( Type.CLIENT, HKBModConfig.CONFIG_SPEC );
		load_ctx.registerExtensionPoint(
			ExtensionPoint.CONFIGGUIFACTORY,
			() -> ( mc, screen ) -> new HKBConfigScreen( screen )
		);
		
		MinecraftForge.EVENT_BUS.register( new Object() {
			@SubscribeEvent
			void onGuiOpen( GuiOpenEvent evt )
			{
				__refreshAndDisableHidden();
				MinecraftForge.EVENT_BUS.unregister( this );
			}
		} );
	}
	
	
	private static KeyBinding[] ori_kb_arr;
	
	@SubscribeEvent
	static void onInitGui$Pre( InitGuiEvent.Pre evt )
	{
		if ( evt.getGui() instanceof SettingsScreen )
		{
			final Minecraft mc = Minecraft.getInstance();
			final GameSettings settings = mc.options;
			ori_kb_arr = settings.keyMappings;
			
			final Function< KeyBinding, String > to_raw_name;
			if ( ModList.get().isLoaded( "key_binding_patch" ) )
			{
				to_raw_name = kb -> {
					final String name = kb.getName();
					return IKeyBindingImpl.getShadowTarget( name ).orElse( name );
				};
			}
			else {
				to_raw_name = KeyBinding::getName;
			}
			
			final ImmutableSet< String > hidden = ImmutableSet.copyOf( HKBModConfig.HIDE_KEY_BINDINGS.get() );
			settings.keyMappings = (
				Arrays.stream( ori_kb_arr )
				.filter( kb -> !hidden.contains( to_raw_name.apply( kb ) ) )
				.toArray( KeyBinding[]::new )
			);
		}
	}
	
	@SubscribeEvent
	static void onInitGui$Post( InitGuiEvent.Post evt )
	{
		if ( evt.getGui() instanceof SettingsScreen )
		{
			final Minecraft mc = Minecraft.getInstance();
			final GameSettings settings = mc.options;
			settings.keyMappings = ori_kb_arr;
			ori_kb_arr = null;
		}
	}
	
	@SubscribeEvent
	static void onConfigReload( ModConfig.Reloading evt ) {
		__refreshAndDisableHidden();
	}
}
