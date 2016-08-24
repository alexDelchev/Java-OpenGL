package engine.core.graph.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import engine.core.GameItem;
import engine.core.Timer;
import engine.core.graph.Mesh;
import engine.loaders.collada.ColladaReader;
import engine.loaders.collada.ColladaReader.Joint;

public class AnimatedGameItem extends GameItem{

	private int currentFrame;
	
	private Map<String, List<AnimatedFrame>> animations;
	
	private List<AnimatedFrame> frames;
	
	private List<Matrix4f> invJointMatrices;
	
	public List<Vector3f> bonepositions;
	
	public Timer animationTimer;
	
	private boolean loop = false;
	
	public AnimatedGameItem(Mesh[] meshes, List<AnimatedFrame> frames, List<Matrix4f> invJointMatrices){
		super(meshes);
		this.frames = frames;
		this.invJointMatrices = invJointMatrices;
		currentFrame = 0;
		
		animationTimer = new Timer();
	}
	
	public AnimatedGameItem(Mesh[] meshes, List<Matrix4f> invJointMatrices){
		super(meshes);
		List<AnimatedFrame> frames = new ArrayList<>();
		frames.add(new AnimatedFrame());
		this.frames = frames;
		this.invJointMatrices = invJointMatrices;
		currentFrame = 0;
		
		animations = new HashMap<String, List<AnimatedFrame>>();
		animationTimer = new Timer();
		
		
	}
	
	public void setAnimation(String name, List<AnimatedFrame> frames){
		animations.put(name, frames);
	}
	
	public void playAnimation(String key){
		this.frames = animations.get(key);
		currentFrame = 0;
	}
	
	public List<AnimatedFrame> getAnimation(String key){
		return animations.get(key);
	}
	
	public List<Matrix4f> getInvJointMatrices(){
		return invJointMatrices;
	}
	
	public List<AnimatedFrame> getFrames(){
		return frames;
	}
	
	public void setFrames(List<AnimatedFrame> frames){
		this.frames = frames;
	}
	
	public AnimatedFrame getCurrentFrame(){
		
		if(currentFrame == 0 && loop){
			loop = false;
			animationTimer.init();
		}
		
		
		float currentTime = animationTimer.getTimeSinceStart();
		float startTime = frames.get(currentFrame).getTime();
		float endTime = getNextFrame().getTime();
		
		if(startTime != AnimatedFrame.NO_TIME && endTime != AnimatedFrame.NO_TIME){
			
			if(currentTime > startTime && currentTime < endTime){
				
				float delta = (currentTime - startTime)/(endTime - startTime);
				
				AnimatedFrame interpolatedFrame = interpolateFrames(frames.get(currentFrame), getNextFrame(), delta);
				
				return interpolatedFrame;
			}else if(currentTime > startTime && currentTime > endTime){
				nextFrame();
				return getCurrentFrame();
			}else if(currentTime == startTime){
				return frames.get(currentFrame);
			}else if(currentTime == endTime){
				currentFrame ++;
				return getNextFrame();
			}else{
				return frames.get(currentFrame);
			}
		}else{
			int currentFrameTemp = currentFrame;
			int nextFrame = currentFrame + 1;
			if(nextFrame > frames.size() - 1){
				nextFrame = 0;
			}
			currentFrame = nextFrame;
			return frames.get(currentFrameTemp);
		}
	}
	
	public AnimatedFrame getNextFrame(){
		int nextFrame = currentFrame + 1;
		if(nextFrame > frames.size() - 1){
			nextFrame = 0;
		}
		
		return this.frames.get(nextFrame);
	}
	
	public void nextFrame(){
		int nextFrame = currentFrame + 1;

		if(nextFrame > frames.size() - 1){
			loop = true;
			currentFrame = 0;
		}else{
			currentFrame = nextFrame;
		}
	}
	
	public void setJoints(List<Vector3f> bonepos){
		this.bonepositions = bonepos;
	}
	
	public List<Vector3f> getJointsPos(){
		return bonepositions;
	}
	
	public void interpolateAnimation(String key, int times){
		List<AnimatedFrame> newList = new ArrayList<AnimatedFrame>();
		
		List<AnimatedFrame> frames = animations.get(key);
		
		float step = 1f/(times + 2);
		for(int i=0; i<frames.size()-1; i++){
			AnimatedFrame a = frames.get(i);
			AnimatedFrame b = frames.get(i + 1);
			
			newList.add(a);
			
			for(int timeFactor = 1; timeFactor <= times; timeFactor++){
				
				System.out.println("factor");
				System.out.println(step*timeFactor);
				
				AnimatedFrame newFrame = new AnimatedFrame();
				
				for(int matIndex = 0; matIndex < a.getJointMatrices().length; matIndex++){
					
					Matrix4f matA = a.getJointMatrices()[matIndex];
					Matrix4f matB = b.getJointMatrices()[matIndex];
					
					Matrix4f newMat = new Matrix4f();
					
					Vector3f matATransl = new Vector3f();
					matA.getTranslation(matATransl);
					Quaternionf matARot = new Quaternionf();
					matA.getUnnormalizedRotation(matARot);
					Vector3f matAScale = new Vector3f();
					matA.getScale(matAScale);
					
					Vector3f matBTransl = new Vector3f();
					matB.getTranslation(matBTransl);
					Quaternionf matBRot = new Quaternionf();
					matB.getUnnormalizedRotation(matBRot);
					Vector3f matBScale = new Vector3f();
					matB.getScale(matBScale);
					
					Vector3f newTransl = new Vector3f();
					matATransl.lerp(matBTransl, step*timeFactor, newTransl);
					Quaternionf newRot = new Quaternionf();
					matARot.slerp(matBRot, step*timeFactor, newRot);
					
					Vector3f newScale = new Vector3f();
					matAScale.lerp(matBScale, step*timeFactor, newScale);
					
					newRot.normalize();
					newMat.set(newRot);
					newMat.setTranslation(newTransl);
					newMat.scale(newScale);
					newMat.normalize3x3();
					
					newFrame.setMatrix(matIndex, newMat, new Matrix4f());
				}
				
				newList.add(newFrame);
			}
			
			if(i == frames.size()-2){
				//newList.add(b);
			}
		}
		
		animations.put(key, newList);
	}
	
	private AnimatedFrame interpolateFrames(AnimatedFrame a, AnimatedFrame b, float delta){
		
		AnimatedFrame newFrame = new AnimatedFrame();
		
		for(int matIndex = 0; matIndex < a.getJointMatrices().length; matIndex++){
			
			Matrix4f matA = a.getJointMatrices()[matIndex];
			Matrix4f matB = b.getJointMatrices()[matIndex];
			
			Matrix4f newMat = new Matrix4f();
			
			Vector3f matATransl = new Vector3f();
			matA.getTranslation(matATransl);
			Quaternionf matARot = new Quaternionf();
			matA.getUnnormalizedRotation(matARot);
			Vector3f matAScale = new Vector3f();
			matA.getScale(matAScale);
			
			Vector3f matBTransl = new Vector3f();
			matB.getTranslation(matBTransl);
			Quaternionf matBRot = new Quaternionf();
			matB.getUnnormalizedRotation(matBRot);
			Vector3f matBScale = new Vector3f();
			matB.getScale(matBScale);
			
			Vector3f newTransl = new Vector3f();
			matATransl.lerp(matBTransl, delta, newTransl);
			Quaternionf newRot = new Quaternionf();
			matARot.slerp(matBRot, delta, newRot);
			
			Vector3f newScale = new Vector3f();
			matAScale.lerp(matBScale, delta, newScale);
		
			newRot.normalize();
			newMat.set(newRot);
			newMat.setTranslation(newTransl);
			newMat.scale(newScale);
			newMat.normalize3x3();
			
			newFrame.setMatrix(matIndex, newMat, new Matrix4f());
		}
		
		return newFrame;
	}
}
