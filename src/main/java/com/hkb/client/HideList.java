package com.hkb.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.kbp.client.impl.IKeyBindingImpl;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.gui.widget.list.KeyBindingList.Entry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn( Dist.CLIENT )
final class HideList extends AbstractOptionList< Entry >
{
	private static final int WHITE = Objects.requireNonNull( Color.fromLegacyFormat( TextFormatting.WHITE ) ).getValue();
	
	private final Button save_btn;
	private final int max_label_width;
	
	private final HashSet< String > hidden = Sets.newHashSet( HKBModConfig.HIDE_KEY_BINDINGS.get() );
	private final HashSet< String > delta = new HashSet<>();
	
	HideList( Screen parent, Button save_btn )
	{
		super( parent.getMinecraft(), parent.width + 45, parent.height, 23, parent.height - 32, 20 );
		
		this.save_btn = save_btn;
		
		Stream< KeyBinding > stream = Arrays.stream( this.minecraft.options.keyMappings );
		if ( ModList.get().isLoaded( "key_binding_patch" ) ) {
			stream = stream.filter( kb -> !IKeyBindingImpl.isShadowKeyBinding( kb ) );
		}
		final KeyBinding[] kb_arr = stream.toArray( KeyBinding[]::new );
		
		final Map< String, List< HideEntry > > grouped = Arrays.stream( kb_arr ).collect(
			Collectors.groupingBy( KeyBinding::getCategory,
				Collectors.mapping( HideEntry::new, Collectors.toList() )
			)
		);
		
		Arrays.stream( kb_arr )
			.map( KeyBinding::getCategory )
			.distinct()
			.forEachOrdered( category -> {
				final TranslationTextComponent label = new TranslationTextComponent( category );
				this.addEntry( new CategoryEntry( label ) );
				grouped.get( category ).stream()
					.sorted( Comparator.comparing( e -> e.label_text.getString() ) )
					.forEachOrdered( this::addEntry );
			} );
		
		this.max_label_width = (
			grouped.values().stream()
			.flatMap( Collection::stream )
			.map( e -> e.label_text )
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
	
	
	private final class CategoryEntry extends KeyBindingList.Entry
	{
		private final ITextComponent label;
		private final int label_width;
		
		private CategoryEntry( ITextComponent label )
		{
			this.label = label;
			this.label_width = HideList.this.minecraft.font.width( label );
		}
		
		@Override
		public void render(
			@Nonnull MatrixStack matrix,
			int x,
			int y,
			int p_230432_4_,
			int p_230432_5_,
			int slot_height,
			int mouse_x,
			int mouse_y,
			boolean is_selected,
			float partial_ticks
		) {
			final Minecraft mc = HideList.this.minecraft;
			final Screen screen = Objects.requireNonNull( mc.screen );
			final FontRenderer font = mc.font;
			final int pos_x = screen.width / 2 - this.label_width / 2;
			final int pos_y = y + slot_height - font.lineHeight - 1;
			font.draw( matrix, this.label, pos_x, pos_y, WHITE );
		}
		
		@Override
		public boolean changeFocus( boolean p_231049_1_ ) {
			return false;
		}
		
		@Nonnull
		@Override
		public List< ? extends IGuiEventListener > children() {
			return Collections.emptyList();
		}
	}
	
	
	private final ITextComponent text_show = new TranslationTextComponent( "hkb.gui.show" );
	private final ITextComponent text_hide = new TranslationTextComponent( "hkb.gui.hide" );
	private final class HideEntry extends KeyBindingList.Entry
	{
		private final String kb_name;
		private final ITextComponent label_text;
		private final Button hide_btn;
		
		private HideEntry( KeyBinding kb )
		{
			final String name = kb.getName();
			this.kb_name = name;
			this.label_text = new TranslationTextComponent( name );
			this.hide_btn = new Button(
				0, 0,
				60, 20,
				this.__getButtonText(),
				btn -> {
					final HashSet< String > hidden = HideList.this.hidden;
					if ( !hidden.add( this.kb_name ) ) {
						hidden.remove( this.kb_name );
					}
					final HashSet< String > delta = HideList.this.delta;
					if ( !delta.add( this.kb_name ) ) {
						delta.remove( this.kb_name );
					}
					HideList.this.save_btn.active = !delta.isEmpty();
					btn.setMessage( this.__getButtonText() );
				}
			);
		}
		
		private ITextComponent __getButtonText()
		{
			final boolean is_hide = HideList.this.hidden.contains( this.kb_name );
			return is_hide ? HideList.this.text_hide : HideList.this.text_show;
		}
		
		@Override
		public void render(
			@Nonnull MatrixStack matrix,
			int x,
			int y,
			int p_230432_4_,
			int p_230432_5_,
			int slot_height,
			int mouse_x,
			int mouse_y,
			boolean is_selected,
			float partial_ticks
		) {
			final FontRenderer font = HideList.this.minecraft.font;
			final int pos_x = p_230432_4_ + 90 - HideList.this.max_label_width;
			final int pos_y = y + slot_height / 2 - font.lineHeight / 2;
			font.draw( matrix, this.label_text, pos_x, pos_y, WHITE );
			
			final Button btn = this.hide_btn;
			btn.x = p_230432_4_ + 105;
			btn.y = y;
			btn.render( matrix, mouse_x, mouse_y, partial_ticks );
		}
		
		@Nonnull
		@Override
		public List< ? extends IGuiEventListener > children() {
			return ImmutableList.of( this.hide_btn );
		}
		
		@Override
		public boolean mouseClicked( double p_231044_1_, double p_231044_3_, int p_231044_5_ ) {
			return this.hide_btn.mouseClicked( p_231044_1_, p_231044_3_, p_231044_5_ );
		}
		
		@Override
		public boolean mouseReleased( double p_231048_1_, double p_231048_3_, int p_231048_5_ ) {
			return this.hide_btn.mouseReleased( p_231048_1_, p_231048_3_, p_231048_5_ );
		}
	}
}
