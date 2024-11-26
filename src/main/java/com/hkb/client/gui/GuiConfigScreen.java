package com.hkb.client.gui;

import com.hkb.client.HKBMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;

@SideOnly( Side.CLIENT )
final class GuiConfigScreen extends GuiScreen
{
	private final GuiScreen parent;
	private String title;
	private GuiHideList hide_list;
	
	GuiConfigScreen( GuiScreen parent ) {
		this.parent = parent;
	}
	
	@Override
	public void initGui()
	{
		this.title = I18n.format( "gui.hkb.config_title" );
		
		final GuiButton cancel_btn = new GuiButton(
			0,
			this.width / 2 - 155, this.height - 29,
			150, 20,
			I18n.format( "gui.cancel" )
		);
		this.addButton( cancel_btn );
		
		final GuiButton save_btn = new GuiButton(
			1,
			this.width / 2 - 155 + 160, this.height - 29,
			150, 20,
			I18n.format( "gui.done" )
		);
		save_btn.enabled = false;
		this.addButton( save_btn );
		
		this.hide_list = new GuiHideList( this, save_btn );
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		this.hide_list.handleMouseInput();
	}
	
	@Override
	protected void actionPerformed( GuiButton button ) throws IOException
	{
		switch ( button.id )
		{
		case 0:
			this.mc.displayGuiScreen( this.parent );
			break;
		case 1:
			this.hide_list._applyConfigChange();
			
			final boolean is_world_running = this.mc.world != null;
			final OnConfigChangedEvent event = new OnConfigChangedEvent( HKBMod.MODID, null, is_world_running, false );
			MinecraftForge.EVENT_BUS.post( event );
			if ( event.getResult() != Result.DENY )
			{
				final PostConfigChangedEvent event1 = new PostConfigChangedEvent( HKBMod.MODID, null, is_world_running, false );
				MinecraftForge.EVENT_BUS.post( event1 );
			}
			
			this.mc.displayGuiScreen( this.parent );
			break;
		default:
			super.actionPerformed( button );
		}
	}
	
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton ) throws IOException
	{
		if ( mouseButton != 0 || !this.hide_list.mouseClicked( mouseX, mouseY, mouseButton ) ) {
			super.mouseClicked( mouseX, mouseY, mouseButton );
		}
	}
	
	@Override
	protected void mouseReleased( int mouseX, int mouseY, int state )
	{
		if ( state != 0 || !this.hide_list.mouseReleased( mouseX, mouseY, state ) ) {
			super.mouseReleased( mouseX, mouseY, state );
		}
	}
	
	@Override
	public void drawScreen( int mouseX, int mouseY, float partialTicks )
	{
		this.drawDefaultBackground();
		this.hide_list.drawScreen( mouseX, mouseY, partialTicks );
		this.drawCenteredString( this.fontRenderer, this.title, this.width / 2, 8, MathHelper.rgb( 255, 255, 255 ) );
		
		super.drawScreen( mouseX, mouseY, partialTicks );
	}
}
