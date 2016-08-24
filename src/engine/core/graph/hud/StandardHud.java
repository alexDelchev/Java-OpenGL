package engine.core.graph.hud;

import java.util.ArrayList;
import java.util.List;

import engine.core.GameItem;
import engine.core.IHud;
import engine.core.Utils;
import engine.core.graph.Material;
import engine.core.graph.Mesh;
import engine.core.graph.Texture;

public class StandardHud {
	
	public final static int TOP_MAIN_ICON = 0;
	
	public final static int TOP_SECOND_ICON = 1;
	
	public final static int TOP_THIRD_ICON = 2;
	
	private final static int TOP_ROW = 10000;
	
	public final static int BOTTOM_MAIN_ICON = -1;
	
	public final static int BOTTOM_SECOND_ICON = -2;
	
	public final static int BOTTOM_THIRD_ICON = -3;
	
	private final static int BOTTOM_ROW = 10001;
	
	public final static int RIGHT_FIRST_ICON = -10;
	
	public final static int RIGHT_SECOND_ICON = -20;
	
	public final static int RIGHT_THIRD_ICON = -30;
	
	public final static int RIGHT_FOURTH_ICON = -40;
	
	public final static int RIGHT_FIFTH_ICON = -50;
	
	private final static int RIGHT_COLUMN = 10002;
	
	private final static int LEFT_COLUMN = 10003;
	
	private float screenWidth;
	
	private float screenHeight;
	
	public List<Mesh> hud;
	
	public List<Mesh> tempHud;
	
	public List<Integer[]> iconMap;
	
	public List<Integer[]> tempIconMap;
	
	private Mesh mesh;
	
	private Texture texture;
	
	private Material material;
	
	private boolean menuOpen = false;
	
	public StandardHud(int screenWidth, int screenHeight){
		
		this.screenWidth = (float) screenWidth;
		this.screenHeight = (float) screenHeight;
		
		hud = new ArrayList<Mesh>();
		tempHud = new ArrayList<Mesh>();
		iconMap = new ArrayList<Integer[]>();
		tempIconMap = new ArrayList<Integer[]>();
	}
	
	public void createChunk(int key) throws Exception{
		
		if(key == TOP_MAIN_ICON){
			texture = new Texture("/hud_textures/sun_speed_icon.png");
			createIcon(TOP_ROW, 70, 70, TOP_MAIN_ICON, iconMap, hud);
		}
		
		if(key == TOP_SECOND_ICON){
			texture = new Texture("/hud_textures/stop_sun_icon.png");
			createIcon(TOP_ROW, 70, 70, TOP_SECOND_ICON, iconMap, hud);
		}
		
		if(key == TOP_THIRD_ICON){
			texture = new Texture("/hud_textures/third_icon.png");
			createIcon(TOP_ROW, 70, 70, TOP_THIRD_ICON, iconMap, hud);
		}
		
		if(key == BOTTOM_MAIN_ICON){
			texture = new Texture("/hud_textures/build_icon.png");
			createIcon(BOTTOM_ROW, 60, 60, BOTTOM_MAIN_ICON, iconMap, hud);
			
			texture = new Texture("/hud_textures/tower_icon.png");
			createIcon(RIGHT_COLUMN, 60, 60, RIGHT_FIRST_ICON, tempIconMap, tempHud);
			
			texture = new Texture("/hud_textures/house_icon.png");
			createIcon(RIGHT_COLUMN, 60, 60, RIGHT_SECOND_ICON, tempIconMap, tempHud);
			
			texture = new Texture("/hud_textures/house2_icon.png");
			createIcon(RIGHT_COLUMN, 60, 60, RIGHT_THIRD_ICON, tempIconMap, tempHud);
			
			texture = new Texture("/hud_textures/lamp_icon.png");
			createIcon(RIGHT_COLUMN, 60, 60, RIGHT_FOURTH_ICON, tempIconMap, tempHud);
			
			texture = new Texture("/hud_textures/sun_well_icon.png");
			createIcon(RIGHT_COLUMN, 60, 60, RIGHT_FIFTH_ICON, tempIconMap, tempHud);
		}
	}
	
	private void createIcon(int position, int width, int height, int key, List<Integer[]> boundingStorage, List<Mesh> hudStorage){
		
		List<Float> positions = new ArrayList();
		List<Float> textures = new ArrayList();
		List<Integer> indices = new ArrayList();
		
		float startX = 0;
		float startY = 0;
		
		float paddingX = 0;
		float paddingY = 0;
		
		
		//Place button, 5 pixels gap between each one
		if(position == TOP_ROW){
			startX = key*(width+5);
			startY = 0;
			
			paddingX = screenWidth/100f;
			paddingY = screenWidth/100f;
			
		}else if(position == BOTTOM_ROW){
			startX = screenWidth + ((1+key)*(width+5)) - width;
			startY = screenHeight - height;
			
			paddingX = -screenWidth/100f;
			paddingY = -screenWidth/100f;
		}else if(position == RIGHT_COLUMN){
			int tempkey = key/10;
			
			startX = screenWidth - width;
			startY = screenHeight + (tempkey*(height+5)) - height;
			
			paddingX = -screenWidth/100f;
			paddingY = -screenWidth/100f;
		}
		
		//Icon bounding
		Integer[] bounding = new Integer[5];
		
		//LeftTop
		positions.add(paddingX + startX);
		positions.add(paddingY + startY);
		positions.add(0f);
		textures.add(0f);
		textures.add(0f);
		indices.add(0);
		
		bounding[0] =(int) (paddingX + startX);
		bounding[1] =(int) (paddingY + startY);
		
		//Left Bottom
		positions.add(paddingX + startX);
		positions.add(paddingY + height + startY);
		positions.add(0f);
		textures.add(0f);
		textures.add(1f);
		indices.add(1);
		
		//Right Bottom
		positions.add(paddingX + width + startX);
		positions.add(paddingY + height + startY);
		positions.add(0f);
		textures.add(1f);
		textures.add(1f);
		indices.add(2);
		
		bounding[2] =(int) (paddingX + width + startX);
		bounding[3] =(int) (paddingY + height + startY);
		
		//Right Top
		positions.add(paddingX + width + startX);
		positions.add(paddingY + startY);
		positions.add(0f);
		textures.add(1f);
		textures.add(0f);
		indices.add(3);
		
		indices.add(0);
		indices.add(2);
		
		bounding[4] = key;
		boundingStorage.add(bounding);
		
		float[] posArray = Utils.floatListToArray(positions);
		float[] textsArray = Utils.floatListToArray(textures);
		float[] normals = new float[0];
		int[] indicesArray = Utils.intListToArray(indices);
		
		
		mesh = new Mesh(posArray, textsArray, normals, indicesArray);
		mesh.setMaterial(new Material(texture));
		hudStorage.add(mesh);
	}
	
	public int handleMousePress(int cursorX, int cursorY){
		
		int x = cursorX;
		int y = cursorY;
		int key = 100;
		
		if(menuOpen){
			for(Integer[] bounding: tempIconMap){
				if(x>bounding[0] && x<bounding[2] && y>bounding[1] && y<bounding[3]){
					key = bounding[4];
					System.out.println(key);
				}
			}
		}
		
		for(Integer[] bounding: iconMap){
			if(x>bounding[0] && x<bounding[2] && y>bounding[1] && y<bounding[3]){
				key = bounding[4];
				System.out.println(key);
			}
		}
		
		return key;
	}
	
	public GameItem[] getGameItems(){
		GameItem[] hudItems = new GameItem[hud.size()];
		
		int i=0;
		for(Mesh mesh: hud){
			hudItems[i] = new GameItem();
			hudItems[i].setMesh(mesh);
			i++;
		}
		
		return hudItems;
	}
	
	public List<GameItem> getMenuItems(){

		GameItem item;
		List<GameItem> hudItems = new ArrayList<GameItem>();
		
		for(Mesh mesh: tempHud){
			item = new GameItem();
			item.setMesh(mesh);
			hudItems.add(item);
		}
		
		return hudItems;
	}
	
	public void toggleMenu(boolean state){
		menuOpen = state;
	}
	
	public boolean isMenuOpen(){
		return menuOpen;
	}
}
