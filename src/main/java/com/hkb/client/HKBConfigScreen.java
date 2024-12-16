package com.hkb.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@OnlyIn( Dist.CLIENT )
final class HKBConfigScreen extends Screen
{
	private static final int WHITE = Objects.requireNonNull( TextColor.fromLegacyFormat( ChatFormatting.WHITE ) ).getValue();
	
	private final Screen parent;
	private HideList hide_list;
	
	HKBConfigScreen( Screen parent )
	{
		super( Component.translatable( "hkb.gui.config_title" ) );
		
		this.parent = parent;
	}
	
	@Override
	protected void init()
	{
		final var cancel_btn = (
			Button.builder( CommonComponents.GUI_CANCEL, btn -> Objects.requireNonNull( this.minecraft ).setScreen( this.parent ) )
			.bounds( this.width / 2 - 155, this.height - 29, 150, 20 )
			.build()
		);
		this.addRenderableWidget( cancel_btn );
		
		final var save_btn = (
			Button.builder( CommonComponents.GUI_DONE, btn -> {
				this.hide_list._applyConfigChange();
				Objects.requireNonNull( this.minecraft ).setScreen( this.parent );
			} )
			.bounds( this.width / 2 - 155 + 160, this.height - 29, 150, 20 )
			.build()
		);
		save_btn.active = false;
		this.addRenderableWidget( save_btn );
		
		final var hide_lst = new HideList( this, save_btn );
		this.hide_list = hide_lst;
		this.addWidget( hide_lst );
	}
	
	@Override
	public void render( @NotNull GuiGraphics graphics, int p_281550_, int p_282878_, float partial_ticks )
	{
		this.renderBackground( graphics );
		this.hide_list.render( graphics, p_281550_, p_282878_, partial_ticks );
		graphics.drawCenteredString( this.font, this.title, this.width / 2, 8, WHITE );
		
		super.render( graphics, p_281550_, p_282878_, partial_ticks );
	}
}
