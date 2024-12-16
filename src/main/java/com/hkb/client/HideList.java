package com.hkb.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.hkb.client.HideList.Entry;
import com.kbp.client.impl.IKeyMappingImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@OnlyIn( Dist.CLIENT )
final class HideList extends ContainerObjectSelectionList< Entry >
{
	private static final int WHITE = Objects.requireNonNull( TextColor.fromLegacyFormat( ChatFormatting.WHITE ) ).getValue();
	
	private final Button save_btn;
	private final int max_label_width;
	
	private final HashSet< String > hidden = Sets.newHashSet( HKBModConfig.HIDE_KEY_BINDINGS.get() );
	private final HashSet< String > delta = new HashSet<>();
	
	HideList( Screen parent, Button save_btn )
	{
		super( parent.getMinecraft(), parent.width + 45, parent.height, 20, parent.height - 32, 20 );
		
		this.save_btn = save_btn;
		
		var stream = Arrays.stream( this.minecraft.options.keyMappings );
		if ( ModList.get().isLoaded( "key_binding_patch" ) ) {
			stream = stream.filter( km -> !IKeyMappingImpl.isShadowKeyMapping( km ) );
		}
		final var km_arr = stream.toArray( KeyMapping[]::new );
		
		final var grouped = Arrays.stream( km_arr ).collect(
			Collectors.groupingBy( KeyMapping::getCategory,
				Collectors.mapping( HideEntry::new, Collectors.toList() )
			)
		);
		
		Arrays.stream( km_arr )
			.map( KeyMapping::getCategory )
			.distinct()
			.forEachOrdered( category -> {
				final var label = Component.translatable( category );
				this.addEntry( new CategoryEntry( label ) );
				grouped.get( category ).stream()
					.sorted( Comparator.comparing( e -> e.label.getString() ))
					.forEachOrdered( this::addEntry );
			} );
		
		this.max_label_width = (
			grouped.values().stream()
			.flatMap( Collection::stream )
			.map( e -> e.label )
			.mapToInt( this.minecraft.font::width )
			.max()
			.orElse( 0 )
		);
	}
	
	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 15 + 20;
	}
	
	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 32;
	}
	
	void _applyConfigChange()
	{
		assert !this.delta.isEmpty();
		HKBModConfig.HIDE_KEY_BINDINGS.set( ImmutableList.copyOf( this.hidden ) );
		HKBModConfig.HIDE_KEY_BINDINGS.save();
	}
	
	
	static abstract class Entry extends ContainerObjectSelectionList.Entry< Entry > {
	}
	
	
	private final class CategoryEntry extends Entry
	{
		private final Component label;
		private final int width;
		
		private CategoryEntry( Component label )
		{
			this.label = label;
			this.width = HideList.this.minecraft.font.width( label );
		}
		
		@Override
		public void render(
			@NotNull GuiGraphics graphics,
			int x,
			int y,
			int p_281333_,
			int p_282287_,
			int slot_height,
			int mouse_x,
			int mouse_y,
			boolean is_selected,
			float partial_ticks
		) {
			final var mc = HideList.this.minecraft;
			final var screen = Objects.requireNonNull( mc.screen );
			final var font = mc.font;
			final var pos_x = screen.width / 2 - this.width / 2;
			final var pos_y = y + slot_height - font.lineHeight - 1;
			graphics.drawString( font, this.label, pos_x, pos_y, WHITE, false );
		}
		
		@Nullable
		@Override
		public ComponentPath nextFocusPath( @NotNull FocusNavigationEvent p_265672_ ) {
			return null;
		}
		
		@NotNull
		@Override
		public List< ? extends GuiEventListener > children() {
			return List.of();
		}
		
		@NotNull
		@Override
		public List< ? extends NarratableEntry > narratables() {
			return List.of();
		}
	}
	
	
	private final Component text_show = Component.translatable( "hkb.gui.show" );
	private final Component text_hide = Component.translatable( "hkb.gui.hide" );
	private final class HideEntry extends Entry
	{
		private final String km_name;
		private final Component label;
		private final Button hide_btn;
		
		private HideEntry( KeyMapping km )
		{
			final var name = km.getName();
			this.km_name = name;
			this.label = Component.translatable( name );
			this.hide_btn = (
				Button.builder( this.__getButtonText(), btn -> {
					final var hidden = HideList.this.hidden;
					if ( !hidden.add( this.km_name ) ) {
						hidden.remove( this.km_name );
					}
					final var delta = HideList.this.delta;
					if ( !delta.add( this.km_name ) ) {
						delta.remove( this.km_name );
					}
					HideList.this.save_btn.active = !delta.isEmpty();
					btn.setMessage( this.__getButtonText() );
				} )
				.bounds( 0, 0, 60, 20 )
				.build()
			);
		}
		
		private Component __getButtonText()
		{
			final var is_hidden = HideList.this.hidden.contains( this.km_name );
			return is_hidden ? HideList.this.text_hide : HideList.this.text_show;
		}
		
		@Override
		public void render(
			@NotNull GuiGraphics graphics,
			int x,
			int y,
			int p_281373_,
			int p_283433_,
			int slot_height,
			int mouse_x,
			int mouse_y,
			boolean is_selected,
			float partial_ticks
		) {
			final var font = HideList.this.minecraft.font;
			final var pos_x = p_281373_ + 90 - HideList.this.max_label_width;
			final var pos_y = y + slot_height / 2 - font.lineHeight / 2;
			graphics.drawString( font, this.label, pos_x, pos_y, WHITE, false );
			
			final var btn = this.hide_btn;
			btn.setX( p_281373_ + 105 );
			btn.setY( y );
			btn.render( graphics, mouse_x, mouse_y, partial_ticks );
		}
		
		@NotNull
		@Override
		public List< ? extends GuiEventListener > children() {
			return List.of( this.hide_btn );
		}
		
		@NotNull
		@Override
		public List< ? extends NarratableEntry > narratables() {
			return List.of();
		}
	}
}
