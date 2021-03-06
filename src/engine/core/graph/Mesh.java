package engine.core.graph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3f;

import org.lwjgl.BufferUtils;

import engine.core.GameItem;
import engine.loaders.OBJReader.Vertex;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
	
	private static final Vector3f DEFAULT_COLOR = new Vector3f (1.1f, 1.1f, 1.1f);
	
	public static final int MAX_WEIGHTS = 4;
	
	private final int vaoID;
	
	private final List<Integer> vboIDList;
	
	private final int vertexCount;
	
	private Material material;
	
	private List<Vector3f> boundingBox;
	
	private float[] positions;
	private float[] textCoords;
	private float[] normals;
	private int[] indices;

	public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights){
		
		this.positions = positions;
		this.textCoords = textCoords;
		this.normals = normals;
		this.indices = indices;
		
		vertexCount = indices.length;
		
		vboIDList = new ArrayList();
		
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		
	    //positions
		int vboID = glGenBuffers();
		vboIDList.add(vboID);
		FloatBuffer posBuffer = BufferUtils.createFloatBuffer(positions.length);
		posBuffer.put(positions).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		//textures
		vboID = glGenBuffers();
		vboIDList.add(vboID);
		FloatBuffer textCoordsBuffer = BufferUtils.createFloatBuffer(textCoords.length);
		textCoordsBuffer.put(textCoords).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		
		//normals
		vboID = glGenBuffers();
		vboIDList.add(vboID);
		FloatBuffer vecNormalsBuffer = BufferUtils.createFloatBuffer(normals.length);
		vecNormalsBuffer.put(normals).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		
		//weights
		vboID = glGenBuffers();
		vboIDList.add(vboID);
		FloatBuffer weightsBuffer = BufferUtils.createFloatBuffer(weights.length);
		weightsBuffer.put(weights).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);
		
		//joint indices
		vboID = glGenBuffers();
		vboIDList.add(vboID);
		IntBuffer jointIndicesBuffer = BufferUtils.createIntBuffer(jointIndices.length);
		jointIndicesBuffer.put(jointIndices).flip();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);
		
		//index VBO
		vboID = glGenBuffers();
		vboIDList.add(vboID);
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
		indicesBuffer.put(indices).flip();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
	}
	
	public Mesh(float[] positions, float[] textures, float[] normals, int[] indices){
		this(positions, textures, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length/3, 0),
				createEmptyFloatArray(MAX_WEIGHTS * positions.length/3, 0));
		//setBoundingBox(calculateBoundingBox(p));
	}
	
	public Mesh(Mesh mesh){
		this(mesh.getPositions(), mesh.getTextCoords(), mesh.getNormals(), mesh.getIndices(),
				createEmptyIntArray(MAX_WEIGHTS * mesh.positions.length/3, 0),
				createEmptyFloatArray(MAX_WEIGHTS * mesh.positions.length/3, 0));
		setBoundingBox(calculateBoundingBox(mesh.getPositions()));
	}
	
	public float[] getPositions(){
		return positions;
	}
	
	public float[] getTextCoords(){
		return textCoords;
	}
	
	public float[] getNormals(){
		return normals;
	}
	
	public int[] getIndices(){
		return indices;
	}
	
	public void setBoundingBox(List<Vector3f> box){
		boundingBox = new ArrayList<Vector3f>();
		
		for(Vector3f vertex: box){
			Vector3f newVertex = new Vector3f(vertex.x, vertex.y, vertex.z);
			boundingBox.add(newVertex);
		}
	}
	
	public static List<Vector3f> calculateBoundingBox(float[] positions){
		List<Vector3f> box = new ArrayList<Vector3f>();
		
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
		
		for(int i=0; i<positions.length/3; i++){
			
			min.x = min.x < positions[i] ? min.x : positions[i];
			min.y = min.y < positions[i+1] ? min.y : positions[i+1];
			min.z = min.z < positions[i+2] ? min.z : positions[i+2];
			
			max.x = max.x > positions[i] ? max.x : positions[i];
			max.y = max.y > positions[i+1] ? max.y : positions[i+1];
			max.z = max.z > positions[i+2] ? max.z : positions[i+2];
		}
		
		box.add(new Vector3f(min.x, min.y, min.z));
		box.add(new Vector3f(max.x, min.y, min.z));
		box.add(new Vector3f(min.x, min.y, max.z));
		box.add(new Vector3f(max.x, min.y, max.z));
		
		box.add(new Vector3f(min.z, max.y, min.z));
		box.add(new Vector3f(max.x, max.y, min.z));
		box.add(new Vector3f(min.x, max.y, max.z));
		box.add(new Vector3f(max.x, max.y, max.z));
		
		return box;
	}
	
	public List<Vector3f> useMinBaseBoundingBox(float[] positions){
		List<Vector3f> box = new ArrayList<Vector3f>();
		
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		min.x = -0.1f;
		min.y = -0.01f;
		min.z = -0.1f;
		
		max.x = 0.1f;
		max.y = 0.3f;
		max.z = 0.1f;
		
		box.add(new Vector3f(min.x, min.y, min.z));
		box.add(new Vector3f(max.x, min.y, min.z));
		box.add(new Vector3f(min.x, min.y, max.z));
		box.add(new Vector3f(max.x, min.y, max.z));
		
		box.add(new Vector3f(min.z, max.y, min.z));
		box.add(new Vector3f(max.x, max.y, min.z));
		box.add(new Vector3f(min.x, max.y, max.z));
		box.add(new Vector3f(max.x, max.y, max.z));
		
		return box;
	}
	
	public List<Vector3f> getBoundingBox(){
		return boundingBox;
	}
	
	public int getVaoID(){
		return vaoID;
	}
	
	public int getVertexCount(){
		return vertexCount;
	}
	
	public Material getMaterial(){
		return material;
	}
	
	public void setMaterial(Material material){
		this.material = material;
	}
	
	public void initRender(){
		
		Texture texture = material.getTexture();
		if(texture != null){
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texture.getID());
		}
		
		glBindVertexArray(getVaoID());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glEnableVertexAttribArray(4);
	}
	
	public void render(){
		initRender();
		
		glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		
		endRender();
	}
	
	public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer){
		initRender();
		
		for(GameItem gameItem: gameItems){
			consumer.accept(gameItem);
			glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		}
		
		endRender();
	}
	
	public void endRender(){
		
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glDisableVertexAttribArray(4);
	}
	
	public void cleanup(){
		glDisableVertexAttribArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		for(int vboID: vboIDList){
			glDeleteBuffers(vboID);
		}
		
		Texture texture = material.getTexture();
		if(texture!= null){
			texture.cleanup();
		}
		
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
	}
	
	public void deleteBuffers(){
		glDisableVertexAttribArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for(int vboID: vboIDList){
			glDeleteBuffers(vboID);
		}
		
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
	}
	
	private static float[] createEmptyFloatArray(int length, float defaultValue){
		float[] result = new float[length];
		Arrays.fill(result, defaultValue);
		return result;
	}
	
	private static int[] createEmptyIntArray(int length, int defaultValue){
		int[] result = new int[length];
		Arrays.fill(result, defaultValue);
		return result;
	}
}
