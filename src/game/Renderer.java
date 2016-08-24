package game;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
import engine.core.graph.ShadowMap;
import engine.core.graph.SkyBox;
import engine.core.graph.SpotLight;
import engine.core.graph.SunLight;
import engine.core.graph.Texture;
import engine.core.graph.animation.AnimatedFrame;
import engine.core.graph.animation.AnimatedGameItem;
import engine.core.Transformation;
import engine.core.GameItem;
import engine.core.IHud;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Renderer {
	
	public static final float FOV = (float) Math.toRadians(60.f);
	
	public static final float Z_NEAR = 0.01f;
	
	public static final float Z_FAR = 1000.f;
	
	private static final int MAX_POINT_LIGHTS = 15;
	
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private float specularPower;
	
	private Matrix4f projectionMatrix;
	private Transformation transformation;
	
	private ShaderProgram shaderProgram;
	private ShaderProgram mousePickerShader;
	private ShaderProgram hudShaderProgram;
	private ShaderProgram skyBoxShaderProgram;
	private ShaderProgram depthMapShader;
	private ShaderProgram GUITextureShader;

	
	private ShadowMap shadowMap;

	public Renderer(){
		
		transformation = new Transformation();
		specularPower = 10f;
	}
	
	public Transformation getTransformation(){
		return transformation;
	}
	
	public void init(Display display, Camera camera) throws Exception{
		
		shadowMap = new ShadowMap();
		
		setupSceneShader();
		setupSkyBoxShader();
		setupHudShader();
		setupDepthMapShader();
		setupGUITextureShader();
	}
	
	private void setupGUITextureShader() throws Exception{
		GUITextureShader = new ShaderProgram();
		
		GUITextureShader.createVertexShader(Utils.loadResource("/shaders/GUITexture_vertex.vs"));
		GUITextureShader.createFragmentShader(Utils.loadResource("/shaders/GUITexture_fragment.fs"));
		GUITextureShader.link();
		
		GUITextureShader.createUniform("projModelMatrix");
		
	}
	
	private void setupDepthMapShader() throws Exception{
		
		depthMapShader = new ShaderProgram();
		
		depthMapShader.createVertexShader(Utils.loadResource("/shaders/depthmap_vertex.vs"));
		depthMapShader.createFragmentShader(Utils.loadResource("/shaders/depthmap_fragment.fs"));
		depthMapShader.link();
		
		depthMapShader.createUniform("lightSpaceMatrix");
		depthMapShader.createUniform("model");
		
		depthMapShader.createUniform("jointsMatrix");
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
		shaderProgram.createUniform("viewMatrix");
		shaderProgram.createUniform("modelMatrix");
		shaderProgram.createUniform("texture_sampler");
		
		shaderProgram.createMaterialUniform("material");
		
		shaderProgram.createUniform("camera_pos");
		shaderProgram.createUniform("specularPower");
		shaderProgram.createUniform("ambientLight");
		shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		shaderProgram.createSunLightUniform("sunLight");
		shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		
		shaderProgram.createFogUniform("fog");
		

		shaderProgram.createUniform("lightSpaceMatrix");
		shaderProgram.createUniform("shadowMap");
		
		shaderProgram.createUniform("jointsMatrix");
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
		
		renderDepthMap(display, camera, scene);
		
		glViewport(0, 0, display.getWidth(), display.getHeight());
		
		renderScene(display, camera, scene);
		renderSkyBox(display, camera, scene);
		renderHud(display, hud);
		//renderGUITextures(display, camera, scene, hud);
	}
	
	public void renderGUITextures(Display display, Camera camera, Scene scene, IHud hud){
		GUITextureShader.bind();
		
		Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, display.getWidth(), display.getHeight(), 0);
		
		for(GameItem gameItem: hud.getGUITextures()){
			Mesh mesh = gameItem.getMesh();
			
			Matrix4f projModelMatrix = transformation.buildOrthoProjectionModelMatrix(gameItem, ortho);
			GUITextureShader.setUniform("projModelMatrix", projModelMatrix);
		
			mesh.render();
		}
		GUITextureShader.unbind();
	}
	
	public void renderDepthMap(Display display, Camera camera, Scene scene){
		
		depthMapShader.bind();
		
		glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
		glViewport(0,0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		Matrix4f lightProjection = new Matrix4f();
		Vector3f campos = new Vector3f(camera.getPosition());
		lightProjection.identity().ortho(-10f, 10f, -10f, 10f, -10f, 20f);
		
		Vector3f lightDir = new Vector3f(scene.getSceneLight().getSunLight().getDirection());

		if(lightDir.x > 0.83f){
			lightDir.x = 0.8300f;
		}else if(lightDir.x < -0.8300f){
			lightDir.x = -0.8300f;
		}
		
		if(lightDir.y < 0.21f){
			lightDir.y = 0.21f;
		}
		Matrix4f lightView = new Matrix4f();
		lightView.identity().lookAt(new Vector3f(campos.x + lightDir.x, campos.y + lightDir.y, campos.z + lightDir.z), new Vector3f(campos), new Vector3f(0.0f, 1.0f, 0.0f));		
		
		Matrix4f lightSpaceMatrix = new Matrix4f();
		
		lightProjection.mul(lightView, lightSpaceMatrix);
		depthMapShader.setUniform("lightSpaceMatrix", lightSpaceMatrix);
		transformation.setLightSpaceMatrix(lightSpaceMatrix);
		
		for(GameItem item: scene.getTerrain()){
			Matrix4f model = transformation.buildModelWorldMatrix(item);
			depthMapShader.setUniform("model", model);
			item.getMesh().render();
		}
		
		Map<Mesh, List<GameItem>> mapMeshes = scene.getMeshMap();
		for(Mesh mesh: mapMeshes.keySet()){
			mesh.renderList(mapMeshes.get(mesh), (GameItem item) -> {
				Matrix4f model = transformation.buildModelWorldMatrix(item);
				depthMapShader.setUniform("model", model);
				
				if(item instanceof AnimatedGameItem){
					AnimatedGameItem animItem = (AnimatedGameItem) item;
					AnimatedFrame frame = animItem.getCurrentFrame();
					depthMapShader.setUniform("jointsMatrix", frame.getJointMatrices());
				}
			});
		}
		
		depthMapShader.unbind();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void renderScene(Display display, Camera camera, Scene scene){
		
		shaderProgram.bind();
		
		Matrix4f projectionMatrix = transformation.updateProjectionMatrix(FOV, display.getWidth(), display.getHeight(), Z_NEAR, Z_FAR);
		shaderProgram.setUniform("projectionMatrix", projectionMatrix);

		shaderProgram.setUniform("fog", scene.getFog());

		
		Matrix4f viewMatrix = transformation.updateViewMatrix(camera);
		shaderProgram.setUniform("viewMatrix", viewMatrix);

		
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(viewMatrix, sceneLight);
		
		shaderProgram.setUniform("texture_sampler", 0);
		shaderProgram.setUniform("shadowMap", 1);
		shaderProgram.setUniform("lightSpaceMatrix", transformation.getLightSpaceMatrix());
		
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getID());
		
		for(GameItem item: scene.getTerrain()){
			
			Mesh mesh = item.getMesh();
			shaderProgram.setUniform("material", item.getMaterial());
			
			Matrix4f modelMatrix = transformation.buildModelWorldMatrix(item);
			shaderProgram.setUniform("modelMatrix", modelMatrix);
		
			mesh.render();
		}
		
		Map<Mesh, List<GameItem>> meshMap = scene.getMeshMap();
		for(Mesh mesh: meshMap.keySet()){

			mesh.renderList(meshMap.get(mesh), (GameItem gameItem) ->{
				
				Texture texture;
				if(gameItem.getMeshes().length == 1){
					texture = gameItem.getMaterial().getTexture();
				}else{
					texture = mesh.getMaterial().getTexture();
				}
				
				if(texture != null){
					glActiveTexture(GL_TEXTURE0);
					glBindTexture(GL_TEXTURE_2D, texture.getID());
				}
				
				if(!gameItem.isCullFace()){
					glDisable(GL_CULL_FACE);
					glCullFace(0);
				}else{
					glEnable(GL_CULL_FACE);
					glCullFace(GL_BACK);
				}
				
				if(gameItem.getMaterial() != null){
					shaderProgram.setUniform("material", gameItem.getMaterial());
				}else{
					shaderProgram.setUniform("material", mesh.getMaterial());
				}
				Matrix4f modelMatrix = transformation.buildModelWorldMatrix(gameItem);
				shaderProgram.setUniform("modelMatrix", modelMatrix);
				
				if(gameItem instanceof AnimatedGameItem){
					AnimatedGameItem animItem = (AnimatedGameItem) gameItem;
					AnimatedFrame frame = animItem.getCurrentFrame();
					shaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
				}
			});
		}
		
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
		
		Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, display.getWidth(), display.getHeight(), 0);
		for(GameItem gameItem: hud.getGameItems()){
			for(Mesh mesh: gameItem.getMeshes()){
				Matrix4f projModelMatrix = transformation.buildOrthoProjectionModelMatrix(gameItem, ortho);
				hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
				hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getColor());
				hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1:0);
			
				mesh.render();
			}
		}
		
		if(hud.isMenuOpen()){
			for(GameItem gameItem: hud.getTempItems()){
				for(Mesh mesh: gameItem.getMeshes()){
					Matrix4f projModelMatrix = transformation.buildOrthoProjectionModelMatrix(gameItem, ortho);
					hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
					hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getColor());
					hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1:0);
				
					mesh.render();
				}
			}
		}

		hudShaderProgram.unbind();
	}
	
	public ShadowMap getShadowMap(){
		return shadowMap;
	}
	
	public void cleanup(){
		if(shaderProgram != null){
			shaderProgram.cleanup();
		}
		
		if(hudShaderProgram != null){
			hudShaderProgram.cleanup();
		}
		
		if(skyBoxShaderProgram != null){
			skyBoxShaderProgram.cleanup();
		}
	}
}
