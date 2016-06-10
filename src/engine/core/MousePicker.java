package engine.core;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import engine.core.graph.Camera;
import engine.core.graph.Mesh;
import engine.core.graph.Scene;
import engine.core.graph.Terrain;

public class MousePicker {
	
	private Vector3f currentRay;
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	
	private Camera camera;
	
	private Transformation transformation;
	
	private int width;
	private int height;
	
	private int selectedItem;
	
	private GameItem[] itemss;
	
	private Terrain terrain;
	
	private boolean itemSelected = false;
	
	private final int RECURSION_COUNT = 200;
	private final float RAY_RANGE = 600;
	
	private Vector3f cursorOnTerrain;
	
	private double currentX;
	private double currentY;
	
	
	//Ray tracing: get screen coordinates of mouse
	//screen coordinates -> NDC coordinates -> clip coords -> eye coords -> world coords
	
	public void init(Transformation transformation, Camera camera, Terrain terrain){
		this.camera = camera;
		this.transformation = transformation;
		this.projectionMatrix = transformation.getProjectionMatrix();
		this.viewMatrix = transformation.updateViewMatrix(camera);
		this.terrain = terrain;
		
		selectedItem = -1;
		
		cursorOnTerrain = new Vector3f();
	}
	
	public void updateSize(Display display){
		this.width = display.getWidth();
		this.height = display.getHeight();
	}
	
	public void setGameItems(GameItem[] gameItems){
		itemss = new GameItem[gameItems.length];
		
		for(int i=0; i<gameItems.length; i++){
			itemss[i] = gameItems[i];
		}
	}
	
	public Vector3f getCurrentRay(){
		return currentRay;
	}
	
	public void handleEvent(double x, double y, List<GameItem> items){
		projectionMatrix = transformation.getProjectionMatrix();
		viewMatrix = transformation.getViewMatrix();
		
		Vector3f origin = camera.getPosition();
		currentRay = calculateMouseRay((float) x, height - (float) y);
		boolean itemhit = false;
		for(int i=0; i<items.size(); i++){
			
			GameItem item = items.get(i);
			
			Matrix4f m = transformation.buildModelWorldMatrix(item);
			
			List<Vector3f> box = item.getBoundingBox();
			
			Vector3f min = new Vector3f(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
			Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
			
			for(Vector3f currentVertex: box){
				Vector3f vertex = new Vector3f(currentVertex.x, currentVertex.y, currentVertex.z);
				vertex = m.transformPosition(vertex);
				
				min.x = Math.min(min.x, vertex.x);
				min.y = Math.min(min.y, vertex.y);
				min.z = Math.min(min.z, vertex.z);
				
				max.x = Math.max(max.x, vertex.x);
				max.y = Math.max(max.y, vertex.y);
				max.z = Math.max(max.z, vertex.z);
			}		
			
			if(traceBoundingBox(currentRay, origin, min, max)){
				if(selectedItem != i){
				    System.out.println("Detected. Object scale: " + item.getScale());
				    itemhit = true;
				    itemSelected = true;
				    selectedItem = i;
				}
				break;
			}
			
		}
		
		if(!itemhit && itemSelected){
			if(sectionInRange(0, RAY_RANGE, currentRay)){
				Vector3f position = binarySearch(0,0, RAY_RANGE, currentRay);
				
				System.out.println("Moving, target: x:" + position.x + " y:" + position.y + " z:" + position.z);
				
				items.get(selectedItem).moveTo(position);
			}
		}
	}
	
	public int getSelectedItem(){
		return selectedItem;
	}
	
	public Vector3f traceCursorOnTerrain(double x, double y){
		
		
		if(currentX != x || currentY != y){
		    currentRay = calculateMouseRay((float) x, height - (float) y);
		
		    cursorOnTerrain = binarySearch(0,0, RAY_RANGE, currentRay);
		}
		
		currentX = x;
		currentY = y;
		return cursorOnTerrain;
	}
	
	public Vector3f getCursorOnTerrain(){
		return cursorOnTerrain;
	}
	
	public boolean traceBoundingBox(Vector3f ray, Vector3f origin,  Vector3f min, Vector3f max){
		
		float dx = 1.0f/ray.x;
		float dy = 1.0f/ray.y;
		float dz = 1.0f/ray.z;
		
		float tx1 = (min.x - origin.x)*dx;
		float tx2 = (max.x - origin.x)*dx;
		
		float minValue = tx1 < tx2 ? tx1 : tx2;
		float maxValue = tx1 > tx2 ? tx1 : tx2;
		
		float tmin = minValue;
		float tmax = maxValue;
		
		float ty1 = (min.y - origin.y)*dy;
		float ty2 = (max.y - origin.y)*dy;
		
		minValue = ty1 < ty2 ? ty1 : ty2;
		maxValue = ty1 > ty2 ? ty1 : ty2;
		
		tmin = tmin > minValue ? tmin : minValue;
		tmax = tmax < maxValue ? tmax : maxValue;
		
		float tz1 = (min.z - origin.z)*dz;
		float tz2 = (max.z - origin.z)*dz;
		
		minValue = tz1 < tz2 ? tz1 : tz2;
		maxValue = tz1 > tz2 ? tz1 : tz2;
		
		tmin = tmin > minValue ? tmin : minValue;
		tmax = tmax < maxValue ? tmax : maxValue;
		
		return tmax >= Math.max(0, tmin) && tmin < 1000;
	}
	
	private Vector3f calculateMouseRay(float x, float y){
		Vector2f normalizedDeviceCoords = getNormalizedDeviceCoordinates(x,y);
		Vector4f clipCoords = new Vector4f(normalizedDeviceCoords.x, normalizedDeviceCoords.y, -1f, 1f);
		Vector4f eyeCoords = clipToEyeSpace(clipCoords);
		
		return eyeToWorld(eyeCoords);
	}
	
	private Vector2f getNormalizedDeviceCoordinates(float x, float y){
		float normalizedX = (2f*x)/width-1f;
		float normalizedY = (2f*y)/height-1f;
		
		return new Vector2f(normalizedX, normalizedY);
	}
	
	private Vector4f clipToEyeSpace(Vector4f clipCoords){
		Matrix4f invertedProjection = projectionMatrix.invert();
		Vector4f eyeCoords = invertedProjection.transform(clipCoords);
		
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}
	
	private Vector3f eyeToWorld(Vector4f eyeCoords){
		Matrix4f invertedView = viewMatrix.invert();
		Vector4f worldRay = invertedView.transform(eyeCoords);
		Vector3f finalRay = new Vector3f(worldRay.x, worldRay.y, worldRay.z);
		finalRay.normalize();
		
		return finalRay;
	}
	
	private Vector3f getPointOnRay(Vector3f ray, float distance){
		Vector3f origin = camera.getPosition();
		Vector3f start = new Vector3f(origin.x, origin.y, origin.z);
		Vector3f scaledRay = new Vector3f(ray.x*distance, ray.y*distance, ray.z*distance);
		
		scaledRay.add(start);
		
		return scaledRay;
	}
	
	private Vector3f binarySearch(int count, float start, float finish, Vector3f ray){
		
		float half = start + ((finish - start)/2f);
		
		if(count >= RECURSION_COUNT){
			Vector3f endPoint = getPointOnRay(ray, half);
			if(terrain != null){
				return endPoint;
			}else{
				return null;
			}
		}
		
		if(sectionInRange(start, half, ray)){
			return binarySearch(count + 1, start, half, ray);
		}else{
			return binarySearch(count + 1, half, finish, ray);
		}
	}
	
	private boolean sectionInRange(float start, float finish, Vector3f ray){
		Vector3f startPoint  = getPointOnRay(ray, start);
		Vector3f endPoint = getPointOnRay(ray, finish);
		
		if(!isUnderground(startPoint) && isUnderground(endPoint)){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isUnderground(Vector3f testPoint){
		float height = 0;
		
		if(terrain != null){
			//height = terrain.getHeightForPoint(testPoint.x, testPoint.z);
			height = terrain.getHeight(testPoint);
		}
		
		if(testPoint.y < height){
			return true;
		}else{
			return false;
		}
	}
}
