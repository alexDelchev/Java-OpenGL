package game;


import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.core.IGameLogic;
import engine.core.Display;
import engine.core.MouseInput;
import engine.core.MousePicker;
import engine.core.graph.Camera;
import engine.core.graph.Fog;
import engine.core.graph.Material;
import engine.core.graph.Mesh;
import engine.core.graph.PointLight;
import engine.core.graph.SceneLight;
import engine.core.graph.SkyBox;
import engine.core.graph.SpotLight;
import engine.core.graph.SunLight;
import engine.core.graph.Terrain;
import engine.core.graph.Texture;
import engine.core.graph.Scene;
import engine.core.graph.hud.StandardHud;
import engine.loaders.OBJLoader;
import engine.loaders.OBJReader;
import engine.core.GameItem;


public class SimpleGame implements IGameLogic{

	private static final float MOUSE_SENSITIVITY = 0.5f;
	
	private final Vector3f cameraInc;
	
	private final Renderer renderer;
	
	private List<GameItem> gameItems;
	
	private Camera camera;
	
	private Scene scene;
	
	private Hud hud;
	
	private final MousePicker mousePicker;
	
	private float lightAngle;
	
	private final float CAMERA_POS_STEP = 0.05f;
	
	private float sunSpeed = 1.0f;
	
	private float angleInc = -0.05f;
	
	private boolean sunIsActive = true;
	
	private boolean sunLeftToRight = true;
	
	private boolean sunToUp = true;
	
	private boolean placingItem = false;
	
	private boolean dragging = false;
	
	private boolean sunToNorth = true;
	
	private Terrain terrain;
	
	private int selectedItemIndex;
	
	private int currentItemID;
	
	private Map<String, Texture> textureMap;
	
	private Map<String, Mesh> modelMap;
	
	private Vector2f cursorBuffer;
	
	public SimpleGame(){
		
		renderer = new Renderer();
		camera = new Camera();
		cameraInc = new Vector3f(0,0,0);
		
		mousePicker = new MousePicker();
		
		lightAngle = 0;
		
		selectedItemIndex = -1;
		
		currentItemID = 0;
		
		cursorBuffer = new Vector2f();
	}
	
	@Override
	public void init(Display display) throws Exception{
		renderer.init(display, camera);
		
		scene = new Scene();
		SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);
		
		scene.setFog(new Fog(true, new Vector3f(0.3f, 0.3f, 0.3f), 0.003f));
		
		hud = new Hud("Кирилица, Latin, Set on 4/12/2016, 21:36 EET");
		hud.createStandardHud(display);
		hud.createGUITexture(display, renderer.getShadowMap().getDepthMapTexture().getID());
		
		float terrainScale = 500f;
		int terrainSize = 1;
		float minY = 0.15f;
		float maxY = 0.30f;
		int increase = 75;
		
        terrain = new Terrain(terrainSize, terrainScale, minY, maxY,
				"/textures/heightmap7.png", "/textures/grass4.png", increase);
        
		List<GameItem> items = new ArrayList<GameItem>();
		for(int i=0; i<terrain.getGameItems().length; i++){
			scene.addTerrain(terrain.getGameItems()[i]);
		}
        
		mousePicker.init(renderer.getTransformation(), camera, terrain);
		mousePicker.updateSize(display);
		
		SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/skybox_texture.png");
		skyBox.setScale(500.0f);
		scene.setSkyBox(skyBox);
		
		textureMap = new HashMap<String, Texture>();
		modelMap = new HashMap<String, Mesh>();
		
		textureMap.put("tower", new Texture("/textures/tower_texture.png"));
		textureMap.put("house", new Texture("/textures/casa.png"));
		textureMap.put("house2", new Texture("/textures/house2_texture.png"));
		textureMap.put("chair", new Texture("/textures/chair_texture.png"));
		
		modelMap.put("tower", OBJReader.loadModel("/models/towerII.obj"));
		modelMap.put("house", OBJReader.loadModel("/models/casa.obj"));
		modelMap.put("lamp", OBJReader.loadModel("/models/lamp.obj"));
		modelMap.put("sunwell", OBJReader.loadModel("/models/lightwell.obj"));
		modelMap.put("house2", OBJReader.loadModel("/models/house2.obj"));
		modelMap.put("chair", OBJReader.loadModel("/models/chair.obj"));
		
		setupLights();
		
		camera.getPosition().y = terrain.getLowestLevel()[1] + 5f;
		camera.getPosition().z = 10f;
		
		Mesh mesh = modelMap.get("tower");
		mesh.setMaterial(new Material());
		
		/*GameItem item = new GameItem(mesh);
		item.setMaterial(new Material(textureMap.get("tower"), 1f));
		Vector3f position = new Vector3f(0, terrain.getLowestLevel()[1], 0);
		item.setPosition(position.x, position.y, position.z);
		item.setScale(0.15f);
		item.setRotation(0, -20, 0);
		
		scene.addGameItem(item);*/
	}
	
	public void setupLights(){
		
		SceneLight sceneLight = scene.getSceneLight();
		
		//sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
		//sceneLight.setAmbientLight(new Vector3f(97f/255f, 0f, 225f/255f));
		//sceneLight.setAmbientLight(new Vector3f((1f/3f)/3f, 0f, 0.3f).mul(2));
		sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
		
		float lightIntensity = 0f;
		Vector3f lightPosition = new Vector3f(1f,0f,0.3f);
		sceneLight.setSunLight(new SunLight(new Vector3f(1f/3f,0f,1f), lightPosition, lightIntensity));

	    
		Vector3f startPos = new Vector3f(-1f, 0f, 0f);
		Vector3f endPos = new Vector3f(0f, 1f, 0f);
		
		Vector3f temp = new Vector3f();
		endPos.sub(startPos, temp);
		
		float factor = 1f/(temp.length()*180);
		
		Vector3f step = new Vector3f();
		
		startPos.lerp(endPos, factor, step);
		step.sub(startPos);
		
		float stepp = 1f/3600f;
		sceneLight.getSunLight().setStep(stepp);
		
		
		
		//
	    //SpotLight setup
		//
		
		/*lightPosition = new Vector3f(0.0f, 0.0f, 10f);
		float spotLightIntensity = 0.0f;
		PointLight spotPointLight = new PointLight(new Vector3f(1,1,1), lightPosition, spotLightIntensity);
		PointLight.Attenuation spotAtt = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
		spotPointLight.setAttenuation(spotAtt);
    	Vector3f coneDir = new Vector3f(0,0,-1);
		float cutOff = (float) Math.cos(Math.toRadians(140));
		SpotLight spotLight = new SpotLight(spotPointLight, coneDir, cutOff);
		spotLightList = new SpotLight[]{spotLight};
		sceneLight.setSpotLights(spotLightList);*/
	}
	
	@Override
	public void input (Display display, MouseInput mouseInput){

		cameraInc.set(0,0,0);
		
		if(display.isKeyPressed(GLFW_KEY_W)){
			cameraInc.z = -5f;
		}else if(display.isKeyPressed(GLFW_KEY_S)){
			cameraInc.z = 5f;
		}
		
		if(display.isKeyPressed(GLFW_KEY_A)){
			cameraInc.x = -5f;
		} else if(display.isKeyPressed(GLFW_KEY_D)){
			cameraInc.x = 5f;
		}
		
		if(display.isKeyPressed(GLFW_KEY_Z)){
			cameraInc.y = -5f;
		}else if(display.isKeyPressed(GLFW_KEY_X)){
			cameraInc.y = 5f;
		}
	}
	
	@Override
	public void update (float interval, MouseInput mouseInput){
		
		//update camera
		Vector3f prevPos = new Vector3f(camera.getPosition());
		camera.movePosition(cameraInc.x * CAMERA_POS_STEP,
				cameraInc.y * CAMERA_POS_STEP, 
				cameraInc.z * CAMERA_POS_STEP);
		float height = terrain.getHeight(camera.getPosition());
		if(prevPos.y < height + 0.1f){
			camera.getPosition().y = height + 0.1f;
		}
		
		//move newly added item to cursor position
		if(placingItem && !dragging){
			Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
			scene.getItem(currentItemID).setPosition(position.x, position.y, position.z);
		}else if(dragging){
			Vector3f rotation = scene.getItem(currentItemID).getRotation();
			
			scene.getItem(currentItemID).setRotation(rotation.x, rotation.y + (cursorBuffer.x - (float)mouseInput.getCursorPosition().x), rotation.z);
		}
		
		//update camera according to mouse movement
		if(mouseInput.isRightButtonPressed()){
			
			Vector2f rotVec = mouseInput.getDisplVec();
			camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
		}
		
		if(mouseInput.isLeftButtonReleased()){
			int event = hud.handleMousePress(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);

			if(event == StandardHud.TOP_MAIN_ICON){
				if(sunSpeed != 1.0f){
					sunSpeed = 1.0f;
				}else{
					sunSpeed = 10.0f;
				}
			}else if(event == StandardHud.TOP_SECOND_ICON){
				if(sunIsActive){
					sunIsActive = false;
				}else{
					sunIsActive = true;
				}
			}else if(event == StandardHud.BOTTOM_MAIN_ICON){
				hud.toggleStandardHudMenu();
			}else if(event == StandardHud.RIGHT_FIRST_ICON){
				Mesh mesh = modelMap.get("tower");
				mesh.setMaterial(new Material());
				
				GameItem item = new GameItem(mesh);
				item.setMaterial(new Material(textureMap.get("tower"), 1f));
				Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
				item.setPosition(position.x, position.y, position.z);
				item.setScale(0.15f);
				item.setRotation(0, -20, 0);
				
				scene.addGameItem(item);
				
				scene.getItem(item.getID()).getMaterial().useTexture(false);
				scene.getItem(item.getID()).getMaterial().setColor(new Vector3f(1.0f, 0.5f, 0.1f));
				
				currentItemID = item.getID();
				placingItem = true;
				
			}else if(event == StandardHud.RIGHT_SECOND_ICON){
				Mesh mesh = modelMap.get("house");
				mesh.setMaterial(new Material());
				
				GameItem item = new GameItem(mesh);
				item.setMaterial(new Material(textureMap.get("house"), 1f));
				Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
				item.setPosition(position.x, position.y, position.z);
				item.setScale(0.09f);
				
				scene.addGameItem(item);
				
				scene.getItem(item.getID()).getMaterial().useTexture(false);
				scene.getItem(item.getID()).getMaterial().setColor(new Vector3f(1.0f, 0.5f, 0.1f));
				
				currentItemID = item.getID();
				placingItem = true;
				
			}else if(event == StandardHud.RIGHT_THIRD_ICON){
				Mesh mesh = modelMap.get("house2");
				mesh.setMaterial(new Material());
				
				GameItem item = new GameItem(mesh);
				item.setMaterial(new Material(textureMap.get("house2"), 1f));
				Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
				item.setPosition(position.x, position.y, position.z);
				item.setRotation(0, 180, 0);
				item.setScale(0.018f);
				
				scene.addGameItem(item);
				
				scene.getItem(item.getID()).getMaterial().useTexture(false);
				scene.getItem(item.getID()).getMaterial().setColor(new Vector3f(1.0f, 0.5f, 0.1f));
				
				currentItemID = item.getID();
				placingItem = true;
			}else if(event == StandardHud.RIGHT_FOURTH_ICON){
			
				if(scene.getSceneLight().getPointLights().size() < SceneLight.MAX_POINT_LIGHTS){
					Mesh mesh = modelMap.get("lamp");
					mesh.setMaterial(new Material());
					
					GameItem item = new GameItem(mesh);
					item.setMaterial(new Material(new Vector3f(), 1f));
					Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
					item.setPosition(position.x, position.y, position.z);
					item.setDefaultColor(new Vector3f(0.1f, 0.1f, 0.1f));
					item.giveLight(true);
					item.setLightPosition(0, 0.55f, 0);
					item.setScale(0.05f);
					
					scene.addGameItem(item);
					
					scene.getItem(item.getID()).getMaterial().useTexture(false);
					scene.getItem(item.getID()).getMaterial().setColor(new Vector3f(1.0f, 0.5f, 0.1f));
					
					currentItemID = item.getID();
					placingItem = true;
					
					Vector3f lightColor = new Vector3f(1f, 0.5f, 0.0f);
					float lightIntensity = 0.02f;
					Vector3f lightPosition = new Vector3f(item.getPosition().x + item.getLightPosition().x, item.getPosition().y + item.getLightPosition().y, item.getPosition().z + item.getLightPosition().z);
					PointLight.Attenuation attenuation = new PointLight.Attenuation(0, 0, 0.01f);
					
					PointLight light = new PointLight(lightColor, lightPosition, lightIntensity);
					light.setAttenuation(attenuation);
					scene.getSceneLight().addEngagedLight(item.getID(), light);
				}
				
			}else if(event == StandardHud.RIGHT_FIFTH_ICON){
				if(scene.getSceneLight().getPointLights().size() < SceneLight.MAX_POINT_LIGHTS){
					Mesh mesh = modelMap.get("sunwell");
					mesh.setMaterial(new Material());
					
					GameItem item = new GameItem(mesh);
					item.setMaterial(new Material(new Vector3f(), 1f));
					Vector3f position = mousePicker.traceCursorOnTerrain(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y);
					item.setPosition(position.x, position.y, position.z);
					item.setDefaultColor(new Vector3f(0.1f, 0.1f, 0.1f));
					item.giveLight(true);
					item.setLightPosition(0, 0.25f, 0);
					item.setScale(0.1f);
					item.setMoveable(true);
					
					scene.addGameItem(item);
					
					scene.getItem(item.getID()).getMaterial().useTexture(false);
					scene.getItem(item.getID()).getMaterial().setColor(new Vector3f(1.0f, 0.5f, 0.1f));
					
					currentItemID = item.getID();
					placingItem = true;
					
					Vector3f lightColor = new Vector3f(1f, 0.5f, 0.0f);
					float lightIntensity = 2f;
					Vector3f lightPosition = new Vector3f(item.getPosition().x + item.getLightPosition().x, item.getPosition().y + item.getLightPosition().y, item.getPosition().z + item.getLightPosition().z);
					PointLight.Attenuation attenuation = new PointLight.Attenuation(0, 0, 1.0f);
					
					PointLight light = new PointLight(lightColor, lightPosition, lightIntensity);
					light.setAttenuation(attenuation);
					scene.getSceneLight().addEngagedLight(item.getID(), light);
				}
				
			}else if(placingItem){

				if(scene.getItem(currentItemID).getMaterial().getTexture() != null){
					scene.getItem(currentItemID).getMaterial().useTexture(true);
				}else{
					scene.getItem(currentItemID).getMaterial().setColor(scene.getItem(currentItemID).getDefaultColor());
				}
				currentItemID = 0;
				placingItem = false;
				
				if(dragging){
					dragging = false;
				}
			}else{
				mousePicker.handleEvent(mouseInput.getCursorPosition().x, mouseInput.getCursorPosition().y, scene.getGameItems());
			}
		}
		
		if(mouseInput.isLeftButtonPressed()){
			if(placingItem){
				dragging = true;
				
				cursorBuffer.x = (float)mouseInput.getCursorPosition().x;
				cursorBuffer.y = (float)mouseInput.getCursorPosition().y;
			}
		}
		
		updateLight();
		
		for(GameItem item: scene.getGameItems()){
			item.move();
			Vector3f position = new Vector3f(item.getPosition());
			if(scene.getSceneLight().getEngagedLight(item.getID()) != null){
				Vector3f lightPosition = item.getLightPosition();
				scene.getSceneLight().getEngagedLight(item.getID()).setPosition(
						new Vector3f(position.x + lightPosition.x, position.y + lightPosition.y, position.z + lightPosition.z));
			}
		}
		
		if(mousePicker.getSelectedItem() != selectedItemIndex){
			hud.setStatusText("Object ID: " + scene.getGameItems().get(mousePicker.getSelectedItem()).getID());
			
			selectedItemIndex = mousePicker.getSelectedItem();
		}
	}
	
	public void updateLight(){
		
		if(sunIsActive){
			
			Vector3f dir = scene.getSceneLight().getSunLight().getDirection();
			float step = scene.getSceneLight().getSunLight().getStep();
			
			if(sunLeftToRight){
				step = step * 5;
			}
			
			if(sunLeftToRight){
				dir.x += step*sunSpeed;
			}else{
				dir.x -= step*sunSpeed;
			}
			
			if(sunToUp){
				dir.y += step*sunSpeed;
			}else{
				dir.y -= step*sunSpeed;
			}
			
			if(dir.x > 0.999f){
				sunLeftToRight = false;
				scene.getSceneLight().getSunLight().setIntensity(0f);
				
				float sunZOffset = 0.3f/60f;
				Vector3f lightDir = scene.getSceneLight().getSunLight().getDirection();
				
				if(sunToNorth){
					lightDir.z -= sunZOffset;
				}else{
					lightDir.z += sunZOffset;
				}
				
				if(lightDir.z <= 0.3f - (0.3f/60)*59){
					sunToNorth = false;
				}else if(lightDir.z >= 0.299){
					sunToNorth = true;
				}
				
			}else if(dir.x < -0.999f){
				sunLeftToRight = true;
			}
			
			if(dir.y > 0.999f){
				sunToUp = false;
			}else if(dir.y < 0.001){
				sunToUp = true;
			}
			
			float intensity = scene.getSceneLight().getSunLight().getIntensity();
			Vector3f color = scene.getSceneLight().getSunLight().getColor();
			//Vector3f ambientColor = scene.getSceneLight().getAmbientLight();
			
			if(!sunLeftToRight && sunToUp){
				scene.getSceneLight().getSunLight().setIntensity(intensity + (step * sunSpeed));
				
				color.x += ((step*2)/3)*sunSpeed;
				color.y += step*sunSpeed;
				
				//ambientColor.x += ((((step*2)/3)*sunSpeed)/3)*2;
				//ambientColor.y += ((step*sunSpeed)/3)*2;
			}else if(!sunLeftToRight && !sunToUp){
				scene.getSceneLight().getSunLight().setIntensity(intensity - (step * sunSpeed));
				
				color.x -= ((step*2)/3)*sunSpeed;
				color.y -= step*sunSpeed;
				
				//ambientColor.x -= ((((step*2)/3)*sunSpeed)/3)*2;
				//ambientColor.y -= ((step*sunSpeed)/3)*2;
			}
			
		}
		
	}
	
	@Override
	public void render(Display display){
		
		renderer.render(display, camera, scene, hud);
		hud.updateSize(display);
		mousePicker.updateSize(display);
	}

	@Override
	public void cleanup() {
		renderer.cleanup();
		
		for(GameItem gameItem : scene.getGameItems()){
			gameItem.getMesh().cleanup();
		}
		
		hud.cleanup();
	}
}
