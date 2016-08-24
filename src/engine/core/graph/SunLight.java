package engine.core.graph;

import org.joml.Vector3f;

public class SunLight {
	
	private Vector3f color;
	private Vector3f direction;
	private float step;
	
	private float intensity;
	
	private OrthoCoords orthoCoords;
	
	private float shadowPosMult;
	
	public SunLight (Vector3f color, Vector3f direction, float intensity){
	
		this.color = color;
		this.direction = direction;
		this.intensity = intensity;
		orthoCoords = new OrthoCoords();
	}
	
	public SunLight(SunLight sunLight){
		
		this(new Vector3f(sunLight.getColor()), new Vector3f(sunLight.getDirection()), sunLight.getIntensity());
	}
	
	public float getShadowPosMult(){
		return shadowPosMult;
	}
	
	public void setShadowPosMult(float shadowPosMult){
		this.shadowPosMult = shadowPosMult;
	}
	
	public OrthoCoords getOrthoCoords(){
		return orthoCoords;
	}
	
	public void setOrthoCoords(float left, float right, float bottom, float top, float near, float far){
		orthoCoords.left = left;
		orthoCoords.right = right;
		orthoCoords.bottom = bottom;
		orthoCoords.top = top;
		orthoCoords.near = near;
		orthoCoords.far = far;
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
	
	public static class OrthoCoords{
		public float left, right, bottom, top, near, far;
	}
	
	public void setStep(float step){
		this.step = step;
	}
	
	public float getStep(){
		return step;
	}
}
