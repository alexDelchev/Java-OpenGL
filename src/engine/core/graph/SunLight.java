package engine.core.graph;

import org.joml.Vector3f;

public class SunLight {
	
	private Vector3f color;
	private Vector3f direction;
	
	private float intensity;
	
	public SunLight (Vector3f color, Vector3f direction, float intensity){
	
		this.color = color;
		this.direction = direction;
		this.intensity = intensity;
	}
	
	public SunLight(SunLight sunLight){
		
		this(new Vector3f(sunLight.getColor()), new Vector3f(sunLight.getDirection()), sunLight.getIntensity());
	}
	
	public Vector3f getColor(){
		return color;
	}
	
	public void setColor(Vector3f color){
		this.color = color;
	}
	
	public Vector3f getDirection(){
		return direction;
	}
	
	public void setDirection(Vector3f direction){
		this.direction = direction;
	}
	
	public float getIntensity(){
		return intensity;
	}
	
	public void setIntensity(float intensity){
		this.intensity = intensity;
	}
}
