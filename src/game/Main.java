package game;

import engine.core.GameEngine;
import engine.core.IGameLogic;

public class Main {

	public static void main(String[] args){
		
		try{
			
			boolean vSync = true;
			IGameLogic gameLogic = new SimpleGame();
			GameEngine gameEngine = new GameEngine("No name yet", 1024, 768, vSync, gameLogic);
			gameEngine.start();
			
		}catch(Exception error){
			error.printStackTrace();
			System.exit(-1);
		}
	}
}
