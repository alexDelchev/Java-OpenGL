package engine.core.graph;

import java.awt.geom.Rectangle2D;

import org.joml.Vector3f;

import engine.core.GameItem;
import engine.core.graph.HeightMap;

public class Terrain {
	
	private final GameItem[] gameItems;
	
	private float[] lowestLevel = new float[3];
	
	private final HeightMap heightMap;
	
	private final float scale;
	
	private final int terrainSize;
	
	private final int vertsPerRow;
	
	private final int vertsPerCol;
	
	private final Rectangle2D.Float[][] boundingBoxes;
	
	public Terrain(int blocksPerRow, float scale, float minY, float maxY,
			String map, String texture, int increase) throws Exception{
		
		terrainSize = blocksPerRow;
		
		gameItems = new GameItem[blocksPerRow * blocksPerRow];
		
		heightMap = new HeightMap(minY, maxY, map, texture, increase);
		
		boundingBoxes = new Rectangle2D.Float[blocksPerRow][blocksPerRow];
		
		vertsPerCol = heightMap.getWidth() - 1;
		vertsPerRow = heightMap.getHeight() - 1;
		
		this.scale = scale;
		
		for(int row=0; row<blocksPerRow; row++){
			for(int col=0; col<blocksPerRow; col++){
				
				float xDisplacement = (col - ((float) blocksPerRow -1)/(float)2)*scale*HeightMap.getXLength();
				float zDisplacement = (row - ((float) blocksPerRow -1)/(float)2)*scale*HeightMap.getZLength();
				
				GameItem terrainChunk = new GameItem(heightMap.getMesh());
				terrainChunk.setScale(scale);
				terrainChunk.setPosition(xDisplacement, 0, zDisplacement);
				gameItems[row*blocksPerRow + col] = terrainChunk;
				
				boundingBoxes[row][col] = getBoundingBox(terrainChunk);
			}
		}
		
		for(int i=0; i<lowestLevel.length; i++){
			lowestLevel[i] = heightMap.getLowestLevel()[i]*scale;
		}
	}
	
	public float getHeight(Vector3f position){
        float result = Float.MIN_VALUE;

        Rectangle2D.Float boundingBox = null;
        boolean found = false;
        GameItem terrainBlock = null;
        for (int row = 0; row < terrainSize && !found; row++) {
            for (int col = 0; col < terrainSize && !found; col++) {
                terrainBlock = gameItems[row * terrainSize + col];
                boundingBox = boundingBoxes[row][col];
                found = boundingBox.contains(position.x, position.z);
            }
        }

        // If we have found a terrain block that contains the position we need
        // to calculate the height of the terrain on that position
        if (found) {
            Vector3f[] triangle = getTriangle(position, boundingBox, terrainBlock);
            result = interpolateHeight(triangle[0], triangle[1], triangle[2], position.x, position.z);
        }

        return result;
	}
	
	protected Vector3f[] getTriangle(Vector3f position, Rectangle2D.Float boundingBox, GameItem terrainBlock){
        // Get the column and row of the heightmap associated to the current position
        float cellWidth = boundingBox.width / (float) vertsPerCol;
        float cellHeight = boundingBox.height / (float) vertsPerRow;
        int col = (int) ((position.x - boundingBox.x) / cellWidth);
        int row = (int) ((position.z - boundingBox.y) / cellHeight);

        Vector3f[] triangle = new Vector3f[3];
        triangle[1] = new Vector3f(
                boundingBox.x + col * cellWidth,
                getWorldHeight(row + 1, col, terrainBlock),
                boundingBox.y + (row + 1) * cellHeight);
        triangle[2] = new Vector3f(
                boundingBox.x + (col + 1) * cellWidth,
                getWorldHeight(row, col + 1, terrainBlock),
                boundingBox.y + row * cellHeight);
        if (position.z < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, position.x)) {
            triangle[0] = new Vector3f(
                    boundingBox.x + col * cellWidth,
                    getWorldHeight(row, col, terrainBlock),
                    boundingBox.y + row * cellHeight);
        } else {
            triangle[0] = new Vector3f(
                    boundingBox.x + (col + 1) * cellWidth,
                    getWorldHeight(row + 2, col + 1, terrainBlock),
                    boundingBox.y + (row + 1) * cellHeight);
        }

        return triangle;
	}
	
	protected float getDiagonalZCoord(float x1, float z1, float x2, float z2, float x){
        float z = ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
        return z;
	}
	
	protected float getWorldHeight(int row, int col, GameItem item){
		float y = heightMap.getHeight(row, col);
		return y * item.getScale();
	}
	
	protected float interpolateHeight(Vector3f p1, Vector3f p2, Vector3f p3, float x,  float z){
		
        /*// Plane equation ax+by+cz+d=0
        float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
        float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
        float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
        float d = -(a * pA.x + b * pA.y + c * pA.z);
        // y = (-d -ax -cz) / b
        float y = (-d - a * x - c * z) / b;
        return y;*/
		
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);

        float l1 = ((p2.z - p3.z) * (x - p3.x) + (p3.x - p2.x) * (z - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (x - p3.x) + (p1.x - p3.x) * (z - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;

        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	private Rectangle2D.Float getBoundingBox(GameItem terrainChunk){
		float scale = terrainChunk.getScale();
		Vector3f position = terrainChunk.getPosition();
		
		float topLeftX = heightMap.STARTX * scale + position.x;
		float topLeftZ = heightMap.STARTZ * scale + position.z;
		float width = Math.abs(heightMap.STARTX * 2) * scale;
		float height = Math.abs(heightMap.STARTZ * 2) * scale;
		Rectangle2D.Float boundingBox = new Rectangle2D.Float(topLeftX, topLeftZ, width, height);
		return boundingBox;
	}
	
	public float getHeightForPoint(float x, float z){
		return heightMap.getHeightForPoint(x, z, scale);
	}
	
	public float[] getLowestLevel(){
		return lowestLevel;
	}
	
	public GameItem[] getGameItems(){
		return gameItems;
	}
}
