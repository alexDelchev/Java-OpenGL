package engine.core.graph;

import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.BufferUtils;

import java.util.HashMap;
import java.util.Map;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ShaderProgram {
	
	private final Map<String, Integer> uniforms;
	
	private final int programID;
	
	private int vertexShaderID;
	
	private int fragmentShaderID;
	
	public ShaderProgram() throws Exception{
		
		programID = glCreateProgram();
		
		if(programID == 0){
			throw new Exception("Could not create Shader");
		}
		
		uniforms = new HashMap<String, Integer>();
	}
	
	public void createUniform(String uniformName) throws Exception{
		
		int uniformLocation = glGetUniformLocation(programID, uniformName);
		
		if(uniformLocation < 0){
			throw new Exception("Could not find uniform: " + uniformName);
		}
		
		uniforms.put(uniformName, uniformLocation);
	}
	
	public void createSunLightUniform(String uniformName) throws Exception{
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".direction");
		createUniform(uniformName + ".intensity");
	}
	
    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
	}
    
    public void createSpotLightUniform(String uniformName) throws Exception{
    	createPointLightUniform(uniformName + ".pl");
    	createUniform(uniformName + ".coneDir");
    	createUniform(uniformName + ".cutOff");
    }
	
	public void createMaterialUniform(String uniformName) throws Exception{
		
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".useColor");
		createUniform(uniformName + ".reflectance");
	}
	
	public void createPointLightListUniform(String uniformName, int size) throws Exception{
		for(int i=0; i<size; i++){
			createPointLightUniform(uniformName + "[" + i + "]");
		}
	}
	
	public void createSpotLightListUniform(String uniformName, int size) throws Exception{
		for(int i=0; i<size; i++){
			createSpotLightUniform(uniformName + "[" + i + "]");
		}
	}
	
	public void createFogUniform(String uniformName) throws Exception{
		createUniform(uniformName + ".active");
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".density");
	}
	
	public void setUniform(String uniformName, Matrix4f value){
		
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.get(fb);
        glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
	}
	
	public void setUniform(String uniformName, Matrix4f[] matrices){
		
		int length = matrices != null ? matrices.length : 0;
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(16 * length);
		
		for(int i=0; i<length; i++){
			matrices[i].get(16 * i, fb);
		}
		
		glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
	}
	
	public void setUniform(String uniformName, Vector3f value){
		
		glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}
	
	public void setUniform(String uniformName, int value){
		glUniform1i(uniforms.get(uniformName), value);
	}
	
	public void setUniform(String uniformName, float value){
		glUniform1f(uniforms.get(uniformName), value);
	}
	
	public void setUniform(String uniformName, PointLight pointLight){
		
		setUniform(uniformName + ".color", pointLight.getColor());
		setUniform(uniformName + ".position", pointLight.getPosition());
		setUniform(uniformName + ".intensity", pointLight.getIntensity());
		
		PointLight.Attenuation att = pointLight.getAttenuation();
		setUniform(uniformName + ".att.constant", att.getConstant());
		setUniform(uniformName + ".att.linear", att.getLinear());
		setUniform(uniformName + ".att.exponent", att.getExponent());
	}
	
	public void setUniform(String uniformName, PointLight[] pointLights){
		int numLights = pointLights != null ? pointLights.length : 0;
		for(int i=0; i<numLights; i++){
			setUniform(uniformName, pointLights[i], i);
		}
	}
	
	public void setUniform(String uniformName, PointLight pointLight, int pos){
		setUniform(uniformName + "[" + pos + "]", pointLight);
	}
	
	public void setUniform(String uniformName, Material material){
		
		setUniform(uniformName + ".color", material.getColor());
		setUniform(uniformName + ".useColor", material.isTextured() ? 0 : 1);
		setUniform(uniformName + ".reflectance", material.getReflectance());
	}
	
	public void setUniform(String uniformName, SunLight sunLight){
		setUniform(uniformName + ".color", sunLight.getColor());
		setUniform(uniformName + ".direction", sunLight.getDirection());
		setUniform(uniformName + ".intensity", sunLight.getIntensity());
	}
	
	public void setUniform(String uniformName, SpotLight spotLight){
		setUniform(uniformName + ".pl", spotLight.getPointLight());
		setUniform(uniformName + ".coneDir", spotLight.getConeDirection());
		setUniform(uniformName + ".cutOff", spotLight.getCutoff());
	}
	
	public void setUniform(String uniformName, SpotLight[] spotLights){
		int numLights = spotLights != null ? spotLights.length : 0;
		for(int i=0; i<numLights; i++){
			setUniform(uniformName, spotLights[i], i);
		}
	}
	
	public void setUniform(String uniformName, SpotLight spotLight, int pos){
		setUniform(uniformName + "[" + pos + "]", spotLight);
	}
	
	public void setUniform(String uniformName, Fog fog){
		setUniform(uniformName + ".active", fog.isActive() ? 1 : 0);
		setUniform(uniformName + ".color", fog.getColor());
		setUniform(uniformName + ".density", fog.getDensity());
	}
	
	public void createVertexShader(String shaderCode) throws Exception{
		
		vertexShaderID = createShader(shaderCode, GL_VERTEX_SHADER);
	}
	
	public void createFragmentShader(String shaderCode) throws Exception{
		
		fragmentShaderID = createShader(shaderCode, GL_FRAGMENT_SHADER);
	}
	
	protected int createShader(String shaderCode, int shaderType) throws Exception{
		
		int shaderID = glCreateShader(shaderType);
		if(shaderID == 0){
			throw new Exception("Error creating shader. Code: " + shaderID);
		}
		
		glShaderSource(shaderID, shaderCode);
		glCompileShader(shaderID);
		
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0){
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderID, 1024));
		}
		
		glAttachShader(programID, shaderID);
		
		return shaderID;
	}
	
	public void link() throws Exception{
		
		glLinkProgram(programID);
		if(glGetProgrami(programID, GL_LINK_STATUS) == 0){
			throw new Exception("Error linking Shader code: " + glGetShaderInfoLog(programID, 1024));
		}
		
		glValidateProgram(programID);
		if(glGetProgrami(programID, GL_VALIDATE_STATUS) == 0){
			throw new Exception("Error validating Shader code: " + glGetShaderInfoLog(programID, 1024));
		}
	}
	
	public void bind(){
		
		glUseProgram(programID);
	}
	
	public void unbind(){
		
		glUseProgram(0);
	}
	
	public void cleanup(){
		
		unbind();
		
		if(programID != 0){
			if(vertexShaderID != 0){
				glDetachShader(programID, vertexShaderID);
			}
			
			if(fragmentShaderID != 0){
				glDetachShader(programID, fragmentShaderID);
			}
			
			glDeleteProgram(programID);
		}
	}
}
