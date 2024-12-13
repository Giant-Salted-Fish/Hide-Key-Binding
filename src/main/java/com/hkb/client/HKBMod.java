package com.hkb.client;

import com.google.common.collect.ImmutableSet;
import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

@Mod(
	modid = HKBMod.MODID,
	version = "1.0.0.4",
	clientSideOnly = true,
	updateJSON = "https://raw.githubusercontent.com/Giant-Salted-Fish/Hide-Key-Binding/1.12.2/update.json",
	acceptedMinecraftVersions = "[1.12,1.13)",
	guiFactory = "com.hkb.client.gui.ConfigGuiFactory"
)
@EventBusSubscriber
public final class HKBMod
{
	public static final String MODID = "hide_key_binding";
	
	
	// Internal implementations that should not be accessed by other mods.
	private static final Field KeyBinding$KEYBIND_ARRAY = ObfuscationReflectionHelper.findField( KeyBinding.class, "field_151473_c" );
	@SuppressWarnings( "unchecked" )
	private static Set< String > getKeyBindArray()
	{
		try {
			return ( Set< String > ) KeyBinding$KEYBIND_ARRAY.get( null );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}
	
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
		
		final Minecraft mc = Minecraft.getMinecraft();
		final ImmutableSet< String > hidden = ImmutableSet.copyOf( HKBModConfig.hide_key_bindings );
		hidden_kb_arr = (
			Arrays.stream( mc.gameSettings.keyBindings )
			.filter( kb -> hidden.contains( kb.getKeyDescription() ) )
			.map( HiddenEntry::new )
			.toArray( HiddenEntry[]::new )
		);
		
		for ( HiddenEntry entry : hidden_kb_arr ) {
			entry.kb.setKeyConflictContext( CONTEXT_HIDE );
		}
	}
	
	
	public HKBMod()
	{
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
	private static String[] ori_categories;
	
	@SubscribeEvent
	static void onInitGui$Pre( InitGuiEvent.Pre evt )
	{
		if ( evt.getGui() instanceof GuiControls )
		{
			final Minecraft mc = Minecraft.getMinecraft();
			final GameSettings settings = mc.gameSettings;
			ori_kb_arr = settings.keyBindings;
			
			final Function< KeyBinding, String > to_raw_name;
			if ( Loader.isModLoaded( "key_binding_patch" ) )
			{
				to_raw_name = kb -> {
					final String name = kb.getKeyDescription();
					return IKeyBindingImpl.getShadowTarget( name ).orElse( name );
				};
			}
			else {
				to_raw_name = KeyBinding::getKeyDescription;
			}
			
			final ImmutableSet< String > hidden = ImmutableSet.copyOf( HKBModConfig.hide_key_bindings );
			settings.keyBindings = (
				Arrays.stream( ori_kb_arr )
				.filter( kb -> !hidden.contains( to_raw_name.apply( kb ) ) )
				.toArray( KeyBinding[]::new )
			);
			
			final Set< String > categories = getKeyBindArray();
			ori_categories = categories.toArray( new String[ 0 ] );
			categories.clear();
			Arrays.stream( settings.keyBindings )
				.map( KeyBinding::getKeyCategory )
				.distinct()
				.forEachOrdered( categories::add );
		}
	}
	
	@SubscribeEvent
	static void onInitGui$Post( InitGuiEvent.Post evt )
	{
		if ( evt.getGui() instanceof GuiControls )
		{
			final Minecraft mc = Minecraft.getMinecraft();
			final GameSettings settings = mc.gameSettings;
			settings.keyBindings = ori_kb_arr;
			ori_kb_arr = null;
			
			final Set< String > categories = getKeyBindArray();
			categories.addAll( Arrays.asList( ori_categories ) );
		}
	}
	
	@SubscribeEvent
	static void onConfigChanged( OnConfigChangedEvent evt )
	{
		if ( evt.getModID().equals( MODID ) )
		{
			ConfigManager.sync( MODID, Config.Type.INSTANCE );
			__refreshAndDisableHidden();
		}
	}
}
