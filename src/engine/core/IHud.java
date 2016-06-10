package engine.core;

import java.util.List;

public interface IHud {
	
	List<GameItem> getGameItems();
	
	List<GameItem> getTempItems();
	
	boolean isMenuOpen();
	
	default void cleanup(){
		List<GameItem> gameItems = getGameItems();
		for(GameItem item : gameItems){
			item.getMesh().cleanup();
		}
	}

}
