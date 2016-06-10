package game;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import engine.core.GameItem;
import engine.core.IHud;
import engine.core.Display;

import engine.core.graph.hud.FontTexture;
import engine.core.graph.hud.StandardHud;
import engine.core.graph.hud.TextItem;

public class Hud implements IHud{

	private static final Font FONT = new Font("Cambria", Font.PLAIN, 25);
	
	private static final String CHARSET = "iso-8859-5";
	
	private List<GameItem> gameItems = new ArrayList<GameItem>();
	
	private List<GameItem> tempItems = new ArrayList<GameItem>();
	
	private final TextItem statusTextItem;
	
	private StandardHud mainHud;
	
	private boolean menuOpen;
	
	public Hud(String statusText) throws Exception{
		FontTexture fontTexture = new FontTexture(FONT, CHARSET);
		this.statusTextItem = new TextItem(statusText, fontTexture);
		this.statusTextItem.getMesh().getMaterial().setColor((new Vector3f(1.1f,1.1f,1.1f)));
		gameItems.add(statusTextItem);
	}
	
	public void setStatusText(String statusText){
		this.statusTextItem.setText(statusText);
	}
	
	public void createStandardHud(Display display) throws Exception{
		
		mainHud = new StandardHud(display.getWidth(), display.getHeight());
		mainHud.createChunk(StandardHud.TOP_MAIN_ICON);
		mainHud.createChunk(StandardHud.TOP_SECOND_ICON);
		mainHud.createChunk(StandardHud.BOTTOM_MAIN_ICON);
		gameItems.add(statusTextItem);
		for(GameItem item: mainHud.getGameItems()){
			gameItems.add(item);
		}
		
		for(GameItem item: mainHud.getMenuItems()){
			tempItems.add(item);
		}
	}
	
	public int handleMousePress(double cX, double cY){
		
		return(mainHud.handleMousePress((int) cX, (int) cY));
	}
	
	public void toggleStandardHudMenu(){
		if(!mainHud.isMenuOpen()){
			mainHud.toggleMenu(true);
		}else{
			mainHud.toggleMenu(false);
		}
	}
	
	@Override
	public boolean isMenuOpen(){
		return mainHud.isMenuOpen();
	}
	
	@Override
	public List<GameItem> getGameItems(){
		return gameItems;
	}
	
	@Override
	public List<GameItem> getTempItems(){
		return tempItems;
	}
	
	public void updateSize(Display display){
		this.statusTextItem.setPosition(10f, display.getHeight() - 50f, 0);
	}
	
	public void listFonts(){
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for(int i=0; i<fonts.length; i++){
			System.out.println(fonts[i]);
		}
	}
}
