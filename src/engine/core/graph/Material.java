package engine.core.graph;

import org.joml.Vector3f;

public class Material {
	
	private static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);
	
	private Vector3f color;
	
	private float reflectance;
	
	private Texture texture;
	
	private boolean useTexture = true;
	
	public Material(){
		color = DEFAULT_COLOR;
		reflectance = 0;
	}
	
	public Material(Vector3f color, float reflectance){
		this();
		this.color = color;
		this.reflectance = reflectance;
	}
	
	public Material(Texture texture, float reflectance){
		this();
		this.texture = texture;
		this.reflectance = reflectance;
	}
	
	public Material(Texture texture){
		this();
		this.texture = texture;
	}
	
	public Vector3f getColor(){
		return color;
	}
	
	public void setColor(Vector3f color){
		this.color = color;
	}
	
	public float getReflectance(){
		return reflectance;
	}
	
	public void setReflectance(float reflectance){
		this.reflectance = reflectance;
	}
	
	public boolean isTextured(){
		if(!useTexture){
			return false;
		}else{	
		    return this.texture != null;
		}
	}
	
	public Texture getTexture(){
		return texture;
	}
	
	public void useTexture(boolean state){
		useTexture = state;
	}
	
	public void setTexture(Texture texture){
		this.texture = texture;
	}
}
