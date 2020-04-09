package localareaexportmod;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import com.wurmonline.client.game.World;
import com.wurmonline.client.plugins.deedexport.DeedExporter;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;

import javassist.ClassPool;
import javassist.CtClass;

public class LocalAreaExportMod implements WurmClientMod, Initable
{
	public static HeadsUpDisplay mHud;
	public static World mWorld;
	public static DeedExporter mExporter;
	public static Logger mLogger = Logger.getLogger( "LocalAreaExportMod" );
	
	public static boolean handleInput( final String pCommand, final String[] data ) 
	{
		if ( pCommand.toLowerCase().contains("localexport") ) 
		{
			String[] lTmp = pCommand.split( "," );
			int lN = 0;
			int lE = 0;
			int lS = 0;
			int lW = 0;
			
			if ( lTmp.length == 5 )
			{
				if ( lTmp[1] != null )
				{
					lN = Integer.parseInt( lTmp[1] );				
				}
				if ( lTmp[2] != null )
				{			
					lE = Integer.parseInt( lTmp[2] );
				}
				if ( lTmp[3] != null )
				{			
					lS = Integer.parseInt( lTmp[3] );
				}
				if ( lTmp[4] != null )
				{			
					lW = Integer.parseInt( lTmp[4] );
				}
			}
			mLogger.log( Level.INFO, "PlayerPos:" + mWorld.getPlayerPosX() + "/" + mWorld.getPlayerPosY()  );
			DeedExporter.export( mWorld, "LOCAL AREA", (int)( ( mWorld.getPlayerPosX() / 4 ) - lW ), (int)( ( mWorld.getPlayerPosY() / 4 ) - lN ), (int)( ( mWorld.getPlayerPosX() / 4 ) + lE ), (int)( ( mWorld.getPlayerPosY() / 4 ) + lS ), 
					(int)( mWorld.getPlayerPosX() / 4 ), (int)( mWorld.getPlayerPosY() / 4 ), 0 );
			return true;
		}
		return false;
	}

	@Override
	public void init() 
	{
		mLogger.log( Level.INFO, "Init LocalAreaExportMod" );
		try 
		{
			ClassPool lClassPool = HookManager.getInstance().getClassPool();

			CtClass lCtWurmConsole = lClassPool.getCtClass( "com.wurmonline.client.console.WurmConsole" );
			lCtWurmConsole.getMethod( "handleDevInput", "(Ljava/lang/String;[Ljava/lang/String;)Z" ).insertBefore("if (localareaexportmod.LocalAreaExportMod.handleInput($1,$2)) return true;");
			
			HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V", () -> ( pProxy, pMethod, pArgs ) -> 
			{
				pMethod.invoke( pProxy, pArgs );
				mHud = ( HeadsUpDisplay ) pProxy;
				return null;
			});
			
			HookManager.getInstance().registerHook( "com.wurmonline.client.renderer.WorldRender", "renderPickedItem", "(Lcom/wurmonline/client/renderer/backend/Queue;)V", () -> ( pProxy, pMethod, pArgs ) -> 
			{
				pMethod.invoke(pProxy, pArgs);
				Class<?> lCls = pProxy.getClass();

				mWorld = ReflectionUtil.getPrivateField( pProxy, ReflectionUtil.getField( lCls, "world" ) );

				return null;
			});
		} 
		catch ( Throwable e ) 
		{
			mLogger.log( Level.SEVERE, "Error LocalAreaExportMod", e.getMessage() );
		}
	}
}
