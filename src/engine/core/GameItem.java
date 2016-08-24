package engine.core;

import java.util.ArrayList;
import java.util.List;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import engine.core.graph.Material;
import engine.core.graph.Mesh;
import engine.core.graph.animation.AnimatedGameItem;

public class GameItem {
	
	private final int ID;
	
	private Mesh[] meshes;
	
	private final Vector3f position;
	
	private float scale;
	
	private final List<List<Vector3f>> boundingBoxes;
	
	private final Vector3f rotation;
	
	private Vector3f target;
	
	private Vector3f startpos;
	
	private int stepsToMove;
	
	private final float speed = 30f;
	
	private float zOrientation;
	
	private float yOffset;
	
	private Vector3f lightOffset;
	
	private Vector3f defaultColor;
	
	private boolean moving = false;
	
	private boolean hasLight = false;
	
	private boolean hasTexture;
	
	private boolean moveable = false;
	
	private boolean cullFace = true;
	
	private boolean yUp = true;
	
	private Material material;
	
	public GameItem(){
		
		ID = (int) (10000*Math.random());
		
		position = new Vector3f(0,0,0);
		target = new Vector3f(position.x, position.y, position.z);
		scale = 1;
		rotation = new Vector3f(0,0,0);
		zOrientation = 0;
		boundingBoxes = new ArrayList<List<Vector3f>>();
		lightOffset = new Vector3f();
		defaultColor = new Vector3f();
	}
	
	public GameItem(Mesh mesh){
		
		ID = (int) (10000*Math.random());
		
		this.meshes = new Mesh[] {mesh};
		position = new Vector3f(0,0,0);
		target = new Vector3f(position.x, position.y, position.z);
		scale = 1;
		rotation = new Vector3f(0,0,0);
		zOrientation = 0;
		lightOffset = new Vector3f();
		defaultColor = new Vector3f();
		
		boundingBoxes = new ArrayList<List<Vector3f>>();
		if(mesh.getBoundingBox() != null){
			ArrayList<Vector3f> boundingBox = (ArrayList<Vector3f>) setBoundingBox(mesh.getBoundingBox());
			boundingBoxes.add(boundingBox);
		}else{
			boundingBoxes.add(new ArrayList<Vector3f>());
		}
	}
	
	public GameItem(Mesh[] meshes){
		
		ID = (int) (10000*Math.random());
		
		this.meshes = meshes;
		
		position = new Vector3f(0,0,0);
		target = new Vector3f(position.x, position.y, position.z);
		scale = 1;
		rotation = new Vector3f(0,0,0);
		zOrientation = 0;
		lightOffset = new Vector3f();
		defaultColor = new Vector3f();
		
		boundingBoxes = new ArrayList<List<Vector3f>>();
		for(int i=0; i<meshes.length; i++){
			Mesh mesh = meshes[i];
			
			if(mesh.getBoundingBox() != null){
				ArrayList<Vector3f> boundingBox = (ArrayList<Vector3f>) setBoundingBox(mesh.getBoundingBox());
				boundingBoxes.add(boundingBox);
			}else{
				boundingBoxes.add(new ArrayList<Vector3f>());
			}
		}
	}
	
	public void moveTo(Vector3f target){
		if(moveable){
			moving = true;
			
			if(this instanceof AnimatedGameItem){
				AnimatedGameItem item = (AnimatedGameItem) this;
				if(item.getAnimation("walk") != null){
					item.playAnimation("walk");
				}
			}
			
			startpos = new Vector3f();
			
			startpos.x = this.position.x;
			startpos.y = this.position.y;
			startpos.z = this.position.z;
			
			target.y += yOffset;
			
			Vector3f temp = new Vector3f();
			target.sub(startpos, temp);
			float factor = 1f/(temp.length()*40f);
			stepsToMove = (int) Math.abs(temp.length()*40);
			
			this.target = new Vector3f();
			startpos.lerp(target, factor, this.target);
			this.target.sub(startpos);
		    
		    Vector3f tempDir = new Vector3f();
		    tempDir.x = target.x - position.x;
		    tempDir.y = target.y - position.y;
		    tempDir.z = target.z - position.z;
		    
		    if(yUp){
		    	float angle =(float) Math.atan2(tempDir.z, tempDir.x);
			     
	            this.rotation.z = -90 + (float) ((double) angle*(180/Math.PI)) + zOrientation;
		    }else{
		    	float angle =(float) Math.atan2(tempDir.z, tempDir.x);
			    
	            
	            this.rotation.y = -90 + (float) ((double) angle*(180/Math.PI)) + zOrientation;
		    }
            
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
				
				if(this instanceof AnimatedGameItem){
					AnimatedGameItem item = (AnimatedGameItem) this;
					if(item.getAnimation("idle") != null){
						item.playAnimation("idle");
					}
				}
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
		
		for(List<Vector3f> boundingBox: boundingBoxes){
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
		}
		
		return min;
	}
	
	public Vector3f getBBMax(){
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		for(List<Vector3f> boundingBox: boundingBoxes){
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
		}
		
		return max;
	}
	
	public void setMaterial(Material material){
		this.material = material;
	}
	
	public Material getMaterial(){
		
	    return material;
	}
	
	public List<List<Vector3f>> getBoundingBox(){
		return boundingBoxes;
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public void setPosition(float x, float y, float z){
		
		this.position.x = x;
		this.position.y = y + yOffset;
		this.position.z = z;
		
		this.target.x = x;
		this.target.y = y;
		this.target.z = z;
	}
	
	public void setPosition(Vector3f position){
		
		this.position.x = position.x;
		this.position.y = position.y + yOffset;
		this.position.z = position.z;
		
		this.target.x = position.x;
		this.target.y = position.y;
		this.target.z = position.z;
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
	
	public void setRotation(Vector3f rotation){
		
		this.rotation.x = rotation.x;
		this.rotation.y = rotation.y;
		this.rotation.z = rotation.z;
	}
	
	public float getZOrientation(){
		return zOrientation;
	}
	
	public void setZOrientation(float zOrientation){
		this.zOrientation = zOrientation;
	}
	
	public float getYOffset(){
		return yOffset;
	}
	
	public void setYOffset(float yOffset){
		this.yOffset = yOffset;
	}
	
	public void setYUp(boolean state){
		this.yUp = state;
	}
	
	public boolean isYUp(){
		return yUp;
	}
	
	public void setMesh(Mesh mesh){
		if(this.meshes != null){
			for(Mesh currMesh: meshes){
				currMesh.cleanup();
			}
		}
		
		this.meshes = new Mesh[] {mesh};
	}
	
	public Mesh getMesh(){
		return meshes[0];
	}
	
	public void setMeshes(Mesh[] meshes){
		this.meshes = meshes;
	}
	
	public Mesh[] getMeshes(){
		return meshes;
	}
	
	public int getID(){
		return ID;
	}
	
	public boolean isMoving(){
		return moving;
	}
	
	public void setMoving(boolean state){
		this.moving = state;
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
	
	public void setCullFace(boolean state){
		cullFace = state;
	}
	
	public boolean isCullFace(){
		return cullFace;
	}
}
