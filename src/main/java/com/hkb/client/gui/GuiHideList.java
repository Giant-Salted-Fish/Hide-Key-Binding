package com.hkb.client.gui;

import com.google.common.collect.Sets;
import com.hkb.client.HKBModConfig;
import com.kbp.client.impl.IKeyBindingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SideOnly( Side.CLIENT )
final class GuiHideList extends GuiListExtended
{
	private final GuiScreen parent;
	private final GuiButton save_btn;
	private final IGuiListEntry[] entries;
	private final int max_label_width;
	
	private final HashSet< String > hidden = Sets.newHashSet( HKBModConfig.hide_key_bindings );
	private final HashSet< String > delta = new HashSet<>();
	
	GuiHideList( GuiConfigScreen parent, GuiButton save_all_btn )
	{
		super( parent.mc, parent.width + 45, parent.height, 23, parent.height - 32, 20 );
		
		this.parent = parent;
		this.save_btn = save_all_btn;
		
		Stream< KeyBinding > stream = Arrays.stream( this.mc.gameSettings.keyBindings );
		if ( Loader.isModLoaded( "key_binding_patch" ) ) {
			stream = stream.filter( kb -> !IKeyBindingImpl.isShadowKeyBinding( kb ) );
		}
		final KeyBinding[] kb_arr = stream.toArray( KeyBinding[]::new );
		
		final Map< String, List< HideEntry > > grouped = Arrays.stream( kb_arr ).collect(
			Collectors.groupingBy( KeyBinding::getKeyCategory,
				Collectors.mapping( HideEntry::new, Collectors.toList() )
			)
		);
		
		this.entries = (
			Arrays.stream( kb_arr )
			.map( KeyBinding::getKeyCategory )
			.distinct()
			.flatMap( category -> Stream.concat(
				Stream.of( new CategoryEntry( category ) ),
				grouped.get( category ).stream().sorted( Comparator.comparing( e -> e.label_text ) )
			) )
			.toArray( IGuiListEntry[]::new )
		);
		
		this.max_label_width = (
			grouped.values().stream()
			.flatMap( Collection::stream )
			.map( e -> e.label_text )
			.mapToInt( this.mc.fontRenderer::getStringWidth )
			.max()
			.orElseThrow( IllegalStateException::new )
		);
	}
	
	@Override
	protected int getSize() {
		return this.entries.length;
	}
	
	@Nonnull
	@Override
	public IGuiListEntry getListEntry( int index ) {
		return this.entries[ index ];
	}
	
	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 35;
	}
	
	@Override
	public int getListWidth() {
		return super.getListWidth() + 32;
	}
	
	void  _applyConfigChange()
	{
		assert !this.delta.isEmpty();
		HKBModConfig.hide_key_bindings = this.hidden.toArray( new String[ 0 ] );
	}
	
	
	private final class CategoryEntry implements IGuiListEntry
	{
		private final String label_text;
		private final int label_width;
		
		private CategoryEntry( String name )
		{
			this.label_text = I18n.format( name );
			
			final Minecraft mc = GuiHideList.this.mc;
			this.label_width = mc.fontRenderer.getStringWidth( this.label_text );
		}
		
		@Override
		public void updatePosition( int slotIndex, int x, int y, float partialTicks ) {
		}
		
		public void drawEntry(
			int slotIndex,
			int x,
			int y,
			int listWidth,
			int slotHeight,
			int mouseX,
			int mouseY,
			boolean isSelected,
			float partialTicks
		) {
			final FontRenderer font_renderer = GuiHideList.this.mc.fontRenderer;
			final int pos_x = GuiHideList.this.parent.width / 2 - this.label_width / 2;
			final int pos_y = y + slotHeight - font_renderer.FONT_HEIGHT - 1;
			font_renderer.drawString( this.label_text, pos_x, pos_y, MathHelper.rgb( 255, 255, 255 ) );
		}
		
		@Override
		public boolean mousePressed(
			int slotIndex,
			int mouseX,
			int mouseY,
			int mouseEvent,
			int relativeX,
			int relativeY
		) {
			return false;
		}
		
		@Override
		public void mouseReleased(
			int slotIndex,
			int x,
			int y,
			int mouseEvent,
			int relativeX,
			int relativeY
		) { }
	}
	
	
	private final class HideEntry implements IGuiListEntry
	{
		private final String kb_name;
		private final String label_text;
		private final GuiButton hide_btn;
		
		private HideEntry( KeyBinding kb )
		{
			final String name = kb.getKeyDescription();
			this.kb_name = name;
			this.label_text = I18n.format( name );
			this.hide_btn = new GuiButton( 0, 0, 0, 60, 20, this.__getButtonText() );
		}
		
		private String __getButtonText()
		{
			final boolean is_hidden = GuiHideList.this.hidden.contains( this.kb_name );
			return I18n.format( is_hidden ? "hkb.gui.hide" : "hkb.gui.show" );
		}
		
		@Override
		public void updatePosition( int slotIndex, int x, int y, float partialTicks ) {
		}
		
		@Override
		public void drawEntry(
			int slotIndex,
			int x, int y,
			int listWidth, int slotHeight,
			int mouseX, int mouseY,
			boolean isSelected,
			float partialTicks
		) {
			final Minecraft mc = GuiHideList.this.mc;
			final int pos_x = x + 90 - GuiHideList.this.max_label_width;
			final int pos_y = y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2;
			mc.fontRenderer.drawString( this.label_text, pos_x, pos_y, MathHelper.rgb( 255, 255, 255 ) );
			
			final GuiButton btn = this.hide_btn;
			btn.x = x + 105;
			btn.y = y;
			btn.drawButton( mc, mouseX, mouseY, partialTicks );
		}
		
		@Override
		public boolean mousePressed(
			int slotIndex,
			int mouseX, int mouseY,
			int mouseEvent,
			int relativeX, int relativeY
		) {
			final Minecraft mc = GuiHideList.this.mc;
			if ( this.hide_btn.mousePressed( mc, mouseX, mouseY ) )
			{
				this.hide_btn.playPressSound( mc.getSoundHandler() );
				
				final HashSet< String > hidden = GuiHideList.this.hidden;
				if ( !hidden.add( this.kb_name ) ) {
					hidden.remove( this.kb_name );
				}
				final HashSet< String > delta = GuiHideList.this.delta;
				if ( !delta.add( this.kb_name ) ) {
					delta.remove( this.kb_name );
				}
				GuiHideList.this.save_btn.enabled = !delta.isEmpty();
				
				this.hide_btn.displayString = this.__getButtonText();
				return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		public void mouseReleased( int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY ) {
			this.hide_btn.mouseReleased( x, y );
		}
	}
}
