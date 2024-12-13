package com.hkb.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Objects;

@OnlyIn( Dist.CLIENT )
final class HKBConfigScreen extends Screen
{
	private static final int WHITE = Objects.requireNonNull( Color.fromLegacyFormat( TextFormatting.WHITE ) ).getValue();
	
	private final Screen parent;
	private HideList hide_list;
	
	HKBConfigScreen( Screen parent )
	{
		super( new TranslationTextComponent( "hkb.gui.config_title" ) );
		
		this.parent = parent;
	}
	
	@Override
	protected void init()
	{
		final Button cancel_btn = new Button(
			this.width / 2 - 155, this.height - 29,
			150, 20,
			DialogTexts.GUI_CANCEL,
			btn -> Objects.requireNonNull( this.minecraft ).setScreen( this.parent )
		);
		this.addButton( cancel_btn );
		
		final Button save_btn = new Button(
			this.width / 2 - 155 + 160, this.height - 29,
			150, 20,
			DialogTexts.GUI_DONE,
			btn -> {
				this.hide_list._applyConfigChange();
				Objects.requireNonNull( this.minecraft ).setScreen( this.parent );
			}
		);
		save_btn.active = false;
		this.addButton( save_btn );
		
		final HideList hide_lst = new HideList( this, save_btn );
		this.hide_list = hide_lst;
		this.children.add( hide_lst );
	}
	
	@Override
	public void render( @Nonnull MatrixStack matrix, int p_230430_2_, int p_230430_3_, float p_230430_4_ )
	{
		this.renderBackground( matrix );
		this.hide_list.render( matrix, p_230430_2_, p_230430_3_, p_230430_4_ );
		drawCenteredString( matrix, this.font, this.title, this.width / 2, 8, WHITE );
		
		super.render( matrix, p_230430_2_, p_230430_3_, p_230430_4_ );
	}
}
