package com.hkb.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
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
		super( new TranslatableComponent( "hkb.gui.config_title" ) );
		
		this.parent = parent;
	}
	
	@Override
	protected void init()
	{
		final var cancel_btn = new Button(
			this.width / 2 - 155, this.height - 29,
			150, 20,
			CommonComponents.GUI_CANCEL,
			btn -> Objects.requireNonNull( this.minecraft ).setScreen( this.parent )
		);
		this.addRenderableWidget( cancel_btn );
		
		final var save_btn = new Button(
			this.width / 2 - 155 + 160, this.height - 29,
			150, 20,
			CommonComponents.GUI_DONE,
			btn -> {
				this.hide_list._applyConfigChange();
				Objects.requireNonNull( this.minecraft ).setScreen( this.parent );
			}
		);
		save_btn.active = false;
		this.addRenderableWidget( save_btn );
		
		final var hide_lst = new HideList( this, save_btn );
		this.hide_list = hide_lst;
		this.addWidget( hide_lst );
	}
	
	@Override
	public void render( @NotNull PoseStack pose, int p_96563_, int p_96564_, float p_96565_ )
	{
		this.renderBackground( pose );
		this.hide_list.render( pose, p_96563_, p_96564_, p_96565_ );
		drawCenteredString( pose, this.font, this.title, this.width / 2, 8, WHITE );
		
		super.render( pose, p_96563_, p_96564_, p_96565_ );
	}
}
