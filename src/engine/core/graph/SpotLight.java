package engine.core.graph;

import org.joml.Vector3f;

public class SpotLight {
	
	private PointLight pointLight;
	
	private Vector3f coneDirection;
	
	private float cutOffAngle;
	
	public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutOffAngle){
		
		this.pointLight = pointLight;
		this.coneDirection = coneDirection;
		this.cutOffAngle = cutOffAngle;
	}
	
	public SpotLight(SpotLight spotLight){
		
		this(new PointLight(spotLight.getPointLight()), new Vector3f(spotLight.getConeDirection()), spotLight.getCutoff());
	}

	public PointLight getPointLight(){
		return pointLight;
	}
	
	public void setPointLight(PointLight pointLight){
		this.pointLight = pointLight;
	}
	
	public Vector3f getConeDirection(){
		return coneDirection;
	}
	
	public void setConeDirection(Vector3f coneDirection){
		this.coneDirection = coneDirection;
	}
	
	public float getCutoff(){
		return cutOffAngle;
	}
	
	public void setCutOff(float cutOff){
		this.cutOffAngle = cutOff;
	}
	
	public void setCutOffAngle(float cutOffAngle){
		this.cutOffAngle = (float) (Math.cos(Math.toRadians(cutOffAngle)));
	}
}
