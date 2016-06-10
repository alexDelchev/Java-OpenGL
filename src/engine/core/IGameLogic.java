package engine.core;

public interface IGameLogic {
	
	void init(Display display) throws Exception;
	
	void input(Display display, MouseInput input);
	
	void update (float interval, MouseInput input);
	
	void render(Display display);
	
	void cleanup();
}
