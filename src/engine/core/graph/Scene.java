package engine.core.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.core.GameItem;

public class Scene {
	
	private Map<Mesh, List<GameItem>> meshMap;
	
	private Map<Integer, GameItem> itemMap;

	private List<GameItem> gameItems;
	
	private SkyBox skyBox;
	
	private List<GameItem> terrain;
	
	private SceneLight sceneLight;
	
	private Fog fog;
	
	public Scene(){
		meshMap = new HashMap();
		sceneLight = new SceneLight();
		gameItems = new ArrayList<GameItem>();
		terrain = new ArrayList<GameItem>();
		itemMap = new HashMap<>();
		fog = Fog.NO_FOG;
	}
	
	public List<GameItem> getGameItems(){
		return gameItems;
	}
	
	public void setGameItems(List<GameItem> gameItems){
		int gameItemsCount = gameItems != null ? gameItems.size() : 0;
		
		for(int i=0; i<gameItemsCount; i++){
			GameItem gameItem = gameItems.get(i);
			Mesh mesh = gameItem.getMesh();
			List<GameItem> list = meshMap.get(mesh);
			if(list == null){
				list = new ArrayList<>();
				meshMap.put(mesh, list);
			}
			list.add(gameItem);
			itemMap.put(gameItems.get(i).getID(), gameItems.get(i));
		}
		this.gameItems = gameItems;
	}
	
	public void addGameItem(GameItem item){
		
		Mesh mesh = item.getMesh();
		List<GameItem> list = meshMap.get(mesh);
		if(list == null){
			list = new ArrayList<>();
			meshMap.put(mesh, list);
		}
		list.add(item);
		itemMap.put(item.getID(), item);
		
		gameItems.add(item);
	}
	
	public GameItem getItem(int ID){
		return itemMap.get(ID);
	}
	
	public Map<Mesh, List<GameItem>> getMeshMap(){
		return meshMap;
	}
	
	public void addTerrain(GameItem item){
		terrain.add(item);
	}
	
	public List<GameItem> getTerrain(){
		return terrain;
	}
	
	public SkyBox getSkyBox(){
		return skyBox;
	}
	
	public void setSkyBox(SkyBox skyBox){
		this.skyBox = skyBox;
	}
	
	public SceneLight getSceneLight(){
		return sceneLight;
	}
	
	public void setSceneLight(SceneLight sceneLight){
		this.sceneLight = sceneLight;
	}
	
	public void setFog(Fog fog){
		this.fog = fog;
	}
	
	public Fog getFog(){
		return fog;
	}
}
