package engine.core.graph;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.joml.Vector3f;

import engine.core.Utils;

public class HeightMap {
	
	private final static int MAX_COLOR = 255*255*255;
	
	public final static float STARTX = -0.5f;
	
	public final static float STARTZ = -0.5f;
	
	private final float minY;
	
	private final float maxY;
	
	private final int width;
	
	private final int height;
	
	private final Mesh mesh;
	
	private HeightData heightData;
	
	private float[] lowestLevel = new float[]{0, 100000, 0};
	
	private final float[][] heightArray;

	public HeightMap(float minY, float maxY, String map, String texturePath, int increase) throws Exception{
		
		this.minY = minY;
		this.maxY = maxY;
		
		BufferedImage bufferedImage = ImageIO.read(getClass().getResourceAsStream(map));
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
		
		Texture texture = new Texture(texturePath);
		
		heightData = new HeightData();
		heightArray = new float[height][width];
		
		float incx = getXLength() / (width-1);
		float incz = getZLength() / (height-1);
		
		List<Float> positions = new ArrayList<Float>();
		List<Float> textures = new ArrayList<Float>();
		List<Integer> indices = new ArrayList<Integer>();
		
		for(int row=0; row<height; row++){
			for(int col=0; col<width; col++){
				
				float currentHeight = getHeight(col, row, bufferedImage, incx, incz);
				heightArray[row][col] = currentHeight;
				
				positions.add(STARTX + col*incx);
				positions.add(getHeight(col, row, bufferedImage, incx, incz));
				positions.add(STARTZ + row*incz);
				
				textures.add((float) increase * (float) col/(float)width);
				textures.add((float) increase * (float) row/(float)height);
				
				if(col < width - 1 && row < height - 1){
					
					int leftTop = row*width + col;
					int leftBottom = (row+1) * width + col;
					int rightTop = row*width + col + 1;
					int rightBottom = (row+1)*width + col + 1;
					
					indices.add(leftTop);
					indices.add(leftBottom);
					indices.add(rightTop);
					
					indices.add(rightTop);
					indices.add(leftBottom);
					indices.add(rightBottom);
				}
			}
		}
		
		float[] positionsArray = Utils.floatListToArray(positions);
		float[] texturesArray = Utils.floatListToArray(textures);
		float[] normalsArray = getNormals(positionsArray, width, height);
		int[] indicesArray = Utils.intListToArray(indices);
		
		this.mesh = new Mesh(positionsArray, texturesArray, normalsArray, indicesArray);
		mesh.setMaterial(new Material(texture, 0.0f));
	}
	
	public Mesh getMesh(){
		return mesh;
	}
	
	public static float getXLength(){
		return Math.abs(-STARTX*2);
	}
	
	public static float getZLength(){
		return Math.abs(-STARTZ*2);
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public float getHeight(int row, int col){
        float result = 0;
        if ( row >= 0 && row < heightArray.length ) {
            if ( col >= 0 && col < heightArray[row].length ) {
                result = heightArray[row][col];
            }
        }
        result = heightArray[row][col];
        return result;
	}
	
	private float getHeight(int x, int z, BufferedImage bufferedImage, float incx, float incz){
		float result = 0;
		
		if(x >= 0 && x < bufferedImage.getWidth() && z >= 0 && z < bufferedImage.getHeight()){
			
			int rgb = bufferedImage.getRGB(x, z);
			result = this.minY + Math.abs(this.maxY - this.minY) * ((float) rgb/(float)MAX_COLOR);
			
			heightData.addHeight(x, z, result);
		}
		
		if(result<lowestLevel[1]){
			
			lowestLevel[0] = STARTX + x*incx;
			lowestLevel[1] = result;
			lowestLevel[2] = STARTZ + z*incz;
		}
		
		return result;
	}
	
	public float getHeightForPoint(float x, float z, float scale){
		return heightData.getHeight(x, z, scale);
	}
	
	public float[] getLowestLevel(){
		return lowestLevel;
	}
	
	private float[] getNormals(float[] positionsArray, int width, int height){
		
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f v3 = new Vector3f();
		Vector3f v4 = new Vector3f();
		
		Vector3f v12 = new Vector3f();
		Vector3f v23 = new Vector3f();
		Vector3f v34 = new Vector3f();
		Vector3f v41 = new Vector3f();
		
		List<Float> normals = new ArrayList<Float>();
		
		Vector3f normal = new Vector3f();
		
		for(int row=0; row<height; row++){
			for(int col=0; col<width; col++){
				if(row>0 && row<height-1 && col>0 && col<width-1){
					
					int i0 = row*width*3 + col*3;
					
					v0.x = positionsArray[i0];
					v0.y = positionsArray[i0 + 1];
					v0.z = positionsArray[i0 + 2];
					
					int i1 = row*width*3 + (col-1)*3;
					v1.x = positionsArray[i1];
					v1.y = positionsArray[i1 + 1];
					v1.z = positionsArray[i1 + 2];
					v1 = v1.sub(v0);
					
					int i2 = (row+1)*width*3 + col*3;
					v2.x = positionsArray[i2];
					v2.y = positionsArray[i2 + 1];
					v2.z = positionsArray[i2 + 2];
					v2 = v2.sub(v0);
					
					int i3 = row*width*3 + (col+1)*3;
					v3.x = positionsArray[i3];
					v3.y = positionsArray[i3 + 1];
					v3.z = positionsArray[i3 + 2];
					v3 = v3.sub(v0);
					
					int i4 = (row-1)*width*3 + col*3;
					v4.x = positionsArray[i4];
					v4.y = positionsArray[i4 + 1];
					v4.z = positionsArray[i4 + 2];
					v4 = v4.sub(v0);
					
					v1.cross(v2, v12);
					v12.normalize();
					
					v2.cross(v3, v23);
					v23.normalize();
					
					v3.cross(v4, v34);
					v34.normalize();
					
					v4.cross(v1, v41);
					v41.normalize();
					
					normal = v12.add(v23).add(v34).add(v41);
					normal.normalize();
				}else{
					normal.x = 0;
					normal.y = 0;
					normal.z = 0;
				}
				
				normal.normalize();
				normals.add(normal.x);
				normals.add(normal.y);
				normals.add(normal.z);
			}
		}
		
		return Utils.floatListToArray(normals);
	}
	
	private class HeightData{
		private Map<List<Integer>, Float> heightMap;
		
		public HeightData(){
		    heightMap = new HashMap<List<Integer>, Float>();
		}
		
		public void addHeight(int x, int z, float height){
			List<Integer> XZCoordinates = new ArrayList<Integer>();
			XZCoordinates.add(x);
			XZCoordinates.add(z);
			heightMap.put(XZCoordinates, height);
	    }
		
		public float getHeight(float worldx, float worldz, float scale){
			int x = width/2 + (int) (worldx/scale);
			int z = height/2 + (int) (worldz/scale);
			List<Integer> key = new ArrayList<Integer>();
			key.add(x);
			key.add(z);
			
			return heightMap.get(key)*scale;
		}
	}
}
