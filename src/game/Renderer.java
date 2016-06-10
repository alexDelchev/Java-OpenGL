package game;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.List;
import java.util.Map;

import engine.core.Display;
import engine.core.Utils;
import engine.core.graph.Camera;
import engine.core.graph.Mesh;
import engine.core.graph.PointLight;
import engine.core.graph.Scene;
import engine.core.graph.SceneLight;
import engine.core.graph.ShaderProgram;
import engine.core.graph.SkyBox;
import engine.core.graph.SpotLight;
import engine.core.graph.SunLight;
import engine.core.graph.Texture;
import engine.core.Transformation;
import engine.core.GameItem;
import engine.core.IHud;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer {
	
	private static final float FOV = (float) Math.toRadians(60.f);
	
	private static final float Z_NEAR = 0.01f;
	
	private static final float Z_FAR = 1000.f;
	
	private static final int MAX_POINT_LIGHTS = 15;
	
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private float specularPower;
	
	private Matrix4f projectionMatrix;
	private Transformation transformation;
	
	private ShaderProgram shaderProgram;
	private ShaderProgram hudShaderProgram;
	private ShaderProgram skyBoxShaderProgram;

	public Renderer(){
		
		transformation = new Transformation();
		specularPower = 10f;
	}
	
	public Transformation getTransformation(){
		return transformation;
	}
	
	public void init(Display display) throws Exception{
		
		setupSceneShader();
		setupSkyBoxShader();
		setupHudShader();
	}
	
	private void setupSkyBoxShader() throws Exception{
		
		skyBoxShaderProgram = new ShaderProgram();
		
		skyBoxShaderProgram.createVertexShader(Utils.loadResource("/shaders/skybox_vertex.vs"));
		skyBoxShaderProgram.createFragmentShader(Utils.loadResource("/shaders/skybox_fragment.fs"));
		skyBoxShaderProgram.link();
		
		skyBoxShaderProgram.createUniform("projectionMatrix");
		skyBoxShaderProgram.createUniform("modelViewMatrix");
		skyBoxShaderProgram.createUniform("texture_sampler");
		skyBoxShaderProgram.createUniform("ambientLight");
	}
	
	public void setupSceneShader() throws Exception{
		
		shaderProgram = new ShaderProgram();
		
		shaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.vs"));
		shaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.fs"));
		shaderProgram.link();
	    
		shaderProgram.createUniform("projectionMatrix");
		shaderProgram.createUniform("modelViewMatrix");
		shaderProgram.createUniform("texture_sampler");
		
		shaderProgram.createMaterialUniform("material");
		
		shaderProgram.createUniform("camera_pos");
		shaderProgram.createUniform("specularPower");
		shaderProgram.createUniform("ambientLight");
		shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		shaderProgram.createSunLightUniform("sunLight");
		shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
	}
	
	public void setupHudShader() throws Exception{
		
		hudShaderProgram = new ShaderProgram();
		
		hudShaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.vs"));
		hudShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.fs"));
		hudShaderProgram.link();
		
		hudShaderProgram.createUniform("projModelMatrix");
		hudShaderProgram.createUniform("colour");
		hudShaderProgram.createUniform("hasTexture");
	}
	
	public void clear(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(Display display, Camera camera, Scene scene, IHud hud){
		clear();
		
		if(display.isResized()){
			glViewport(0, 0, display.getWidth(), display.getHeight());
			display.setResized(false);
		}
		
		renderScene(display, camera, scene);
		renderSkyBox(display, camera, scene);
		renderHud(display, hud);
	}
	
	public void renderScene(Display display, Camera camera, Scene scene){
		
		shaderProgram.bind();
		
		Matrix4f projectionMatrix = transformation.updateProjectionMatrix(FOV, display.getWidth(), display.getHeight(), Z_NEAR, Z_FAR);
		shaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		Matrix4f viewMatrix = transformation.updateViewMatrix(camera);

		shaderProgram.setUniform("camera_pos", camera.getPosition());
		
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(viewMatrix, sceneLight);
		
		shaderProgram.setUniform("texture_sampler", 0);
		
		for(GameItem item: scene.getTerrain()){
			Mesh mesh = item.getMesh();
			shaderProgram.setUniform("material", mesh.getMaterial());
			
			Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(item, viewMatrix);
			shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			
			mesh.render();
		}
		
		Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
		for(Mesh mesh: meshMap.keySet()){

			mesh.renderList(meshMap.get(mesh), (GameItem gameItem) ->{
				
				Texture texture = gameItem.getMaterial().getTexture();
				
				if(texture != null){
					glActiveTexture(GL_TEXTURE0);
					glBindTexture(GL_TEXTURE_2D, texture.getID());
				}
				
				Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
				
				shaderProgram.setUniform("material", gameItem.getMaterial());
				shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			});
		}
		
		/*for(GameItem item: scene.getGameItems()){
			Mesh mesh = item.getMesh();
			shaderProgram.setUniform("material", mesh.getMaterial());
			Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(item, viewMatrix);
			shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			mesh.render();
		}*/
		
		shaderProgram.unbind();
	}
	
	private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight){
		
		shaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
		shaderProgram.setUniform("specularPower", specularPower);
		
		int lightIndex = 0;
		for(PointLight light: sceneLight.getPointLights()){
			PointLight currentPointLight = new PointLight(light);
			Vector3f lightPos = currentPointLight.getPosition();
			Vector4f aux = new Vector4f(lightPos, 1);
			aux.mul(viewMatrix);
			
			lightPos.x = aux.x;
			lightPos.y = aux.y;
			lightPos.z = aux.z;
			
			shaderProgram.setUniform("pointLights", currentPointLight, lightIndex);
			lightIndex++;
		}
		
		lightIndex = 0;
		for(SpotLight light: sceneLight.getSpotLights()){
			SpotLight currentSpotLight = new SpotLight(light);
			Vector4f dir = new Vector4f(currentSpotLight.getConeDirection(), 0);
			dir.mul(viewMatrix);
			currentSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
			
			Vector3f lightPos = currentSpotLight.getPointLight().getPosition();
			Vector4f lightAux = new Vector4f(lightPos, 1);
			lightAux.mul(viewMatrix);
			lightPos.x = lightAux.x;
			lightPos.y = lightAux.y;
			lightPos.z = lightAux.z;
			
			shaderProgram.setUniform("spotLights", currentSpotLight, lightIndex);
			lightIndex++;
		}
		
		SunLight currentSunLight = new SunLight(sceneLight.getSunLight());
		Vector4f dir = new Vector4f(currentSunLight.getDirection(), 0);
		dir.mul(viewMatrix);
		currentSunLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
		
		shaderProgram.setUniform("sunLight", currentSunLight);
	}
	
	private void renderSkyBox(Display display, Camera camera, Scene scene){
		
		skyBoxShaderProgram.bind();
		
		skyBoxShaderProgram.setUniform("texture_sampler", 0);
		
		Matrix4f projectionMatrix = transformation.updateProjectionMatrix(FOV, display.getWidth(), display.getHeight(), Z_NEAR, Z_FAR);
		skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		SkyBox skyBox = scene.getSkyBox();
		Matrix4f viewMatrix = transformation.updateViewMatrix(camera);
		
		viewMatrix.m30 = 0;
		viewMatrix.m31 = 0;
		viewMatrix.m32 = 0;
		
		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
		skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
		skyBoxShaderProgram.setUniform("ambientLight", new Vector3f(1f,1f,1f));
		
		scene.getSkyBox().getMesh().render();
		
		skyBoxShaderProgram.unbind();
	}
	
	private void renderHud(Display display, IHud hud){
		
		hudShaderProgram.bind();
		
		Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, display.getWidth(), display.getHeight(), 0);
		for(GameItem gameItem: hud.getGameItems()){
			Mesh mesh = gameItem.getMesh();
			
			Matrix4f projModelMatrix = transformation.buildOrthoProjectionModelMatrix(gameItem, ortho);
			hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
			hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getColor());
			hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1:0);
		
			mesh.render();
		}
		
		if(hud.isMenuOpen()){
			for(GameItem gameItem: hud.getTempItems()){
				Mesh mesh = gameItem.getMesh();
				
				Matrix4f projModelMatrix = transformation.buildOrthoProjectionModelMatrix(gameItem, ortho);
				hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
				hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getColor());
				hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1:0);
				
				mesh.render();
			}
		}

		hudShaderProgram.unbind();
	}
	
	public void cleanup(){
		if(shaderProgram != null){
			shaderProgram.cleanup();
		}
	}
}
