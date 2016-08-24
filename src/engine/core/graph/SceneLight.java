package engine.core.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import engine.core.graph.PointLight;
import engine.core.graph.SpotLight;
import engine.core.graph.SunLight;

public class SceneLight {
	
	//if changed, change fragment shader value as well
	public final static int MAX_POINT_LIGHTS = 15;
	
	private Vector3f ambientLight;
	
	private List<PointLight> pointLights;
	
	private Map<Integer, PointLight> engagedLights;
	
	private List<SpotLight> spotLights;
	
	private SunLight sunLight;
	
	public SceneLight(){
		engagedLights = new HashMap<Integer, PointLight>();
		pointLights = new ArrayList<PointLight>();
		spotLights  = new ArrayList<SpotLight>();
	}
	
	public void addEngagedLight(int ID, PointLight light){
		engagedLights.put(ID, light);
		pointLights.add(light);
	}
	
	public PointLight getEngagedLight(Integer ID){
		return engagedLights.get(ID);
	}
	
	public Vector3f getAmbientLight(){
		return ambientLight;
	}
	
	public void setAmbientLight(Vector3f ambientLight){
		this.ambientLight = ambientLight;
	}
	
	public List<PointLight> getPointLights(){

		return pointLights;
	}
	
	public void setPointLights(PointLight[] pointLights){
		this.pointLights = new ArrayList<PointLight>();
		for(PointLight light: pointLights){
			this.pointLights.add(light);
		}
	}
	
	public void addPointLight(PointLight light){
		pointLights.add(light);
	}
	
	public List<SpotLight> getSpotLights(){
		return spotLights;
	}
	
	public void setSpotLights(SpotLight[] spotLights){
		this.spotLights = new ArrayList<SpotLight>();
		for(SpotLight light: spotLights){
			this.spotLights.add(light);
		}
	}
	
	public void addSpotLight(SpotLight light){
		spotLights.add(light);
	}
	
	public SunLight getSunLight(){
		return sunLight;
	}
	
	public void setSunLight(SunLight sunLight){
		this.sunLight = sunLight;
	}
}
