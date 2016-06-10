package engine.core;

public class GameEngine implements Runnable {

	public static final int TARGET_FPS = 75;
	public static final int TARGET_UPS = 30;
	
	private final Display display;
	
	private final Thread gameLoopThread;
	
	private final Timer timer;
	
	private final IGameLogic gameLogic;
	
	private final MouseInput mouseInput;
	
	public GameEngine(String windowTitle, int width, int height, boolean vSync,
			IGameLogic gameLogic) throws Exception{
		
		gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
		display = new Display(windowTitle, width, height, vSync);
		mouseInput = new MouseInput();
		this.gameLogic = gameLogic;
		timer = new Timer();
	}
	
	public void start(){
		gameLoopThread.start();
	}
	
	@Override
	public void run(){
		
		try{
			init();
			gameLoop();
		}catch(Exception error){
			error.printStackTrace();
		}finally{
			cleanup();
		}
	}
	
	protected void init() throws Exception{
		
		display.init();
		timer.init();
		mouseInput.init(display);
		gameLogic.init(display);
	}
	
	protected void gameLoop(){
		
		float ellapsedTime;
		float accumulator = 0f;
		float interval = 1f/TARGET_UPS;
		
		boolean running = true;
		
		while(running && !display.windowShouldClose()){
			
			ellapsedTime = timer.getEllapsedTime();
			accumulator += ellapsedTime;
			
			input();
			
			while(accumulator >= interval){
				update(interval);
				accumulator -= interval;
			}
			
			render();
			
			if(!display.isvSync()){
				sync();
			}
		}
	}
	
	private void sync(){
		
		float loopSlot = 1f/TARGET_FPS;
		double endTime = timer.getLastLoopTime() + loopSlot;
		
		while(timer.getTime() < endTime){
			try{
				Thread.sleep(1);
			}catch(InterruptedException ie){
				
			}
		}
	}
	
	protected void input(){
		mouseInput.input(display);
		gameLogic.input(display, mouseInput);
	}
	
	protected void update(float interval){
		gameLogic.update(interval, mouseInput);
	}
	
	protected void render(){
		gameLogic.render(display);
		display.update();
	}
	
	protected void cleanup(){
		gameLogic.cleanup();
	}
}
