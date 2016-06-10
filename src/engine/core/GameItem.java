package engine.core;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import engine.core.graph.Material;
import engine.core.graph.Mesh;

public class GameItem {
	
	private final int ID;
	
	private Mesh mesh;
	
	private final Vector3f position;
	
	private float scale;
	
	private final List<Vector3f> boundingBox;
	
	private final Vector3f rotation;
	
	private Vector3f target;
	
	private Vector3f startpos;
	
	private int stepsToMove;
	
	private final float speed = 30f;
	
	private Vector3f lightOffset;
	
	private Vector3f defaultColor;
	
	private boolean moving = false;
	
	private boolean hasLight = false;
	
	private boolean hasTexture;
	
	private boolean moveable = false;
	
	private Material material;
	
	public GameItem(){
		
		ID = (int) (10000*Math.random());
		
		position = new Vector3f(0,0,0);
		target = new Vector3f(position.x, position.y, position.z);
		scale = 1;
		rotation = new Vector3f(0,0,0);
		boundingBox = new ArrayList<Vector3f>();
		lightOffset = new Vector3f();
		defaultColor = new Vector3f();
	}
	
	public GameItem(Mesh mesh){
		
		ID = (int) (10000*Math.random());
		
		this.mesh = mesh;
		position = new Vector3f(0,0,0);
		target = new Vector3f(position.x, position.y, position.z);
		scale = 1;
		rotation = new Vector3f(0,0,0);
		lightOffset = new Vector3f();
		defaultColor = new Vector3f();
		
		if(mesh.getBoundingBox() != null){
			boundingBox = setBoundingBox(mesh.getBoundingBox());
		}else{
			boundingBox = new ArrayList<Vector3f>();
		}
	}
	
	public void moveTo(Vector3f target){
		if(moveable){
			moving = true;
			
			startpos = new Vector3f();
			
			startpos.x = this.position.x;
			startpos.y = this.position.y;
			startpos.z = this.position.z;
			
			Vector3f temp = new Vector3f();
			target.sub(startpos, temp);
			float factor = 1f/(temp.length()*60f);
			stepsToMove = (int) Math.abs(temp.length()*60);
			
			this.target = new Vector3f();
			startpos.lerp(target, factor, this.target);
			this.target.sub(startpos);
		}
		
	}
	
	public void move(){
		if(moving){
			if(stepsToMove > 0){
				
				this.position.x += target.x;
				this.position.y += target.y;
				this.position.z += target.z;
				
				stepsToMove --;
				
			}else{
				moving = false;
			}
		}
	}
	
	public List<Vector3f> setBoundingBox(List<Vector3f> box){
		List<Vector3f> bb = new ArrayList<Vector3f>();
		
		for(Vector3f vertex: box){
			Vector3f newVertex = new Vector3f(vertex.x, vertex.y, vertex.z);
			bb.add(newVertex);
		}
		
		return bb;
	}
	
	public Vector3f getBBMin(){
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		
		for(Vector3f vertex: boundingBox){
			if(min.x > vertex.x){
				min.x = vertex.x;
			}
			if(min.y > vertex.y){
				min.y = vertex.y;
			}
			if(min.z > vertex.z){
				min.z = vertex.z;
			}
		}
		
		return min;
	}
	
	public Vector3f getBBMax(){
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		for(Vector3f vertex: boundingBox){
			if(max.x < vertex.x){
				max.x = vertex.x;
			}
			if(max.y < vertex.y){
				max.y = vertex.y;
			}
			if(max.z < vertex.z){
				max.z = vertex.z;
			}
		}
		
		return max;
	}
	
	public void setMaterial(Material material){
		this.material = material;
	}
	
	public Material getMaterial(){
		
		if(material == null){
			return mesh.getMaterial();
		}else{
			return material;
		}
	}
	
	public List<Vector3f> getBoundingBox(){
		return boundingBox;
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public void setPosition(float x, float y, float z){
		
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		
		this.target.x = x;
		this.target.y = y;
		this.target.z = z;
	}
	
	public float getScale(){
		return scale;
	}
	
	public void setScale(float scale){
		this.scale = scale;
	}
	
	public Vector3f getRotation (){
		return rotation;
	}
	
	public void setRotation(float x, float y, float z){
		
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
	}
	
	public void setMesh(Mesh mesh){
		this.mesh = mesh;
	}
	
	public Mesh getMesh(){
		return mesh;
	}
	
	public int getID(){
		return ID;
	}
	
	public boolean isMoving(){
		return moving;
	}
	
	public void giveLight(boolean state){
		hasLight = state;
	}
	
	public boolean hasLight(){
		return hasLight;
	}
	
	public void setLightPosition(float x, float y, float z){
		if(hasLight){
		    lightOffset = new Vector3f(x,y,z);
		}
	}
	
	public Vector3f getLightPosition(){
		return lightOffset;
	}
	
	public void setDefaultColor(Vector3f color){
		this.defaultColor.x = color.x;
		this.defaultColor.y = color.y;
		this.defaultColor.z = color.z;
	}
	
	public Vector3f getDefaultColor(){
		return defaultColor;
	}
	
	public boolean hasTexture(){
		return this.material.isTextured();
	}
	
	public void setMoveable(boolean state){
		moveable = state;
	}
	
	public boolean isMoveable(){
		return moveable;
	}
}
