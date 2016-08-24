package engine.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3f;

import engine.core.Utils;
import engine.core.graph.Mesh;

import org.joml.Vector2f;

public class OBJReader {
	
	private static int vertCount = 1;
	
	private  Vector3f radius;
	
	public static Mesh loadModel(String fileName) throws Exception{
		
	    URL url = OBJReader.class.getResource(fileName);
		File file = new File(url.toURI());
		FileReader fileReader = new FileReader(file);
		
		BufferedReader reader = new BufferedReader(fileReader);
		
		List<String> lines = Utils.readAllLines(fileName);
		
		List<Vertex> positions = new ArrayList<Vertex>();
		List<Vertex> textures = new ArrayList<Vertex>();
		List<Vertex> normals = new ArrayList<Vertex>();
		List<Integer> inds = new ArrayList<Integer>();
		List<Index> indices = new ArrayList<Index>();
		List<Face> faces = new ArrayList<Face>();
		
		int indexNum = 0;
		
		for(String currentLine: lines){
			String[] line= currentLine.split("\\s+");
			
			if(line[0].equals("v")){
				
				Vertex vertex = new Vertex(Float.parseFloat(line[1]),
						Float.parseFloat(line[2]), Float.parseFloat(line[3]));
				positions.add(vertex);
				vertCount++;
				
			}else if(line[0].equals("vt")){
				
				Vertex vertex = new Vertex(Float.parseFloat(line[1]),
						Float.parseFloat(line[2]));
				textures.add(vertex);
				
			}else if(line[0].equals("vn")){
				
				Vertex vertex = new Vertex(Float.parseFloat(line[1]),
						Float.parseFloat(line[2]), Float.parseFloat(line[3]));
				normals.add(vertex);
				
			}else if(line[0].equals("f")){
				
				Face face = new Face();
				
				for(int i=1; i<line.length; i++){
					
					String[] currentIndex = line[i].split("/");
					
					if(Integer.parseInt(currentIndex[0]) > 0){
						if(currentIndex[1].equals("")){
							
							Index index = new Index(Integer.parseInt(currentIndex[0]), 
									Integer.parseInt(currentIndex[2]), false);
							face.addIndex(index);
						}else if(currentIndex.length == 2){
							Index index = new Index(Integer.parseInt(currentIndex[0]),
									Integer.parseInt(currentIndex[1]), true);
							face.addIndex(index);
						}else{
							
							Index index = new Index(Integer.parseInt(currentIndex[0]),
									Integer.parseInt(currentIndex[1]), Integer.parseInt(currentIndex[2]));
							face.addIndex(index);
						}
					}else if(Integer.parseInt(currentIndex[0]) < 0){
						if(currentIndex[1].equals("")){
							Index index = new Index(positions.size() + Integer.parseInt(currentIndex[0]) + 1,
									normals.size() + Integer.parseInt(currentIndex[2]) + 1, false);
							face.addIndex(index);
						}else if(currentIndex.length == 2){
							Index index = new Index(positions.size() + Integer.parseInt(currentIndex[0]) + 1,
									textures.size() + Integer.parseInt(currentIndex[1]) + 1, true);
							face.addIndex(index);
						}else{
							Index index = new Index(positions.size() + Integer.parseInt(currentIndex[0]) + 1,
									textures.size() + Integer.parseInt(currentIndex[1]) + 1,
									normals.size() + Integer.parseInt(currentIndex[2]) + 1);
							indices.add(index);
							face.addIndex(index);				
						}
					}
				}
				
				if(line.length == 5){
					Face[] triangulatedQuad = triangulateFace(face, positions);
					
					indices.add(triangulatedQuad[0].indices.get(0));
					indices.add(triangulatedQuad[0].indices.get(1));
					indices.add(triangulatedQuad[0].indices.get(2));
					
					indices.add(triangulatedQuad[1].indices.get(0));
					indices.add(triangulatedQuad[1].indices.get(1));
					indices.add(triangulatedQuad[1].indices.get(2));
					
					faces.add(triangulatedQuad[0]);
					faces.add(triangulatedQuad[1]);
				}else{
					
					indices.add(face.indices.get(0));
					indices.add(face.indices.get(1));
					indices.add(face.indices.get(2));
					
					faces.add(face);

				}
			}
		}
		reader.close();
		
		return createMesh(positions, textures, normals, indices, inds);
	}
	
	private static Face[] triangulateFace(Face face, List<Vertex> positions){
		Face[] result = new Face[] {new Face(), new Face()};
		
			Vertex v0x = positions.get(face.indices.get(0).position);
			Vertex v1x = positions.get(face.indices.get(1).position);
			Vertex v2x = positions.get(face.indices.get(2).position);
			Vertex v3x = positions.get(face.indices.get(3).position);
			
			Vector3f v0 = new Vector3f(v0x.x, v0x.y, v0x.z);
			Vector3f v1 = new Vector3f(v1x.x, v1x.y, v1x.z);
			Vector3f v2 = new Vector3f(v2x.x, v2x.y, v2x.z);
			Vector3f v3 = new Vector3f(v3x.x, v3x.y, v3x.z);
			
			float d1 = v0.distanceSquared(v2);
			float d2 = v1.distanceSquared(v3);
			float d3 = v0.distanceSquared(v3);
			float d4 = v1.distanceSquared(v2);
			
			if(d1 > d3){
				if(d1 < d2){
					result[0].addIndex(face.indices.get(0));
					result[0].addIndex(face.indices.get(2));
					result[0].addIndex(face.indices.get(3));
					
					result[1].addIndex(face.indices.get(1));
					result[1].addIndex(face.indices.get(2));
					result[1].addIndex(face.indices.get(0));
				}else{
					result[0].addIndex(face.indices.get(1));
					result[0].addIndex(face.indices.get(2));
					result[0].addIndex(face.indices.get(3));
					
					result[1].addIndex(face.indices.get(0));
					result[1].addIndex(face.indices.get(1));
					result[1].addIndex(face.indices.get(3));
			    }
			}else{
				if(d3 < d4){
					result[0].addIndex(face.indices.get(0));
					result[0].addIndex(face.indices.get(1));
					result[0].addIndex(face.indices.get(3));
					
					result[1].addIndex(face.indices.get(1));
					result[1].addIndex(face.indices.get(3));
					result[1].addIndex(face.indices.get(2));
				}else{
					result[0].addIndex(face.indices.get(0));
					result[0].addIndex(face.indices.get(1));
					result[0].addIndex(face.indices.get(2));
					
					result[1].addIndex(face.indices.get(1));
					result[1].addIndex(face.indices.get(3));
					result[1].addIndex(face.indices.get(2));
			    }
			}
		
		return result;
	}
	
	private static Mesh createMesh(List<Vertex> pos, List<Vertex> tex, List<Vertex> norm, List<Index> ind, List<Integer> inds){
		
		List<Float> positions = new ArrayList<Float>();
		List<Float> textures = new ArrayList<Float>();
		List<Float> normals = new ArrayList<Float>();
		List<Integer> indices = new ArrayList<Integer>();
		List<Index> indList = new ArrayList<Index>();
		
		int duplicates = 0;
		for(int i=0; i<ind.size(); i++){
			boolean duplicate = false;
			for(int j=0; j<indList.size(); j++){
				if(ind.get(i).equalsTo(indList.get(j))){
					duplicate = true;
					indices.add(j);
					duplicates++;
				}
			}
			
			if(!duplicate){
				indList.add(ind.get(i));
				indices.add(i-duplicates);
			}
		}
		
		for(Index index: indList){
			
			positions.add(pos.get(index.position).x);
			positions.add(pos.get(index.position).y);
			positions.add(pos.get(index.position).z);
			
			if(index.texture >= 0){
			    textures.add(tex.get(index.texture).x);
			    textures.add(1f - tex.get(index.texture).y);
			}else{
				textures.add(0f);
			    textures.add(0f);
			}
			
			normals.add(norm.get(index.normal).x);
			normals.add(norm.get(index.normal).y);
			normals.add(norm.get(index.normal).z);
		}
		
		Mesh mesh = new Mesh(Utils.floatListToArray(positions), Utils.floatListToArray(textures), Utils.floatListToArray(normals), Utils.intListToArray(indices));
		mesh.setBoundingBox(calculateBoundingBox(pos));
		return mesh;
	}
	
	private static List<Vector3f> calculateBoundingBox(List<Vertex> positions){
		List<Vector3f> box = new ArrayList<Vector3f>();
		
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
		
		for(Vertex vertex: positions){
			
			min.x = min.x < vertex.x ? min.x : vertex.x;
			min.y = min.y < vertex.y ? min.y : vertex.y;
			min.z = min.z < vertex.z ? min.z : vertex.z;
			
			max.x = max.x > vertex.x ? max.x : vertex.x;
			max.y = max.y > vertex.y ? max.y : vertex.y;
			max.z = max.z > vertex.z ? max.z : vertex.z;
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
	
	public static class Vertex{
		float x, y, z;
		
		public Vertex(float x, float y, float z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Vertex(float x, float y){
			this.x = x;
			this.y = y;
		}
	}
	
	protected static class Index{
		int position, texture, normal;
		
		public Index(int position, int texture, int normal){
			this.position = position - 1;
			this.texture = texture-1;
			this.normal = normal - 1;
		}
		
		public Index(int position, int secondIndex, boolean hasTexture){
			if(!hasTexture){
				this.position = position - 1;
				this.texture = -1;
				this.normal = secondIndex - 1;
			}else{
				this.position = position - 1;
				this.texture = secondIndex - 1;
				this.normal = -1;
			}
		}
		
		public boolean equalsTo(Index index){
			if(position == index.position && texture == index.texture && normal == index.normal){
				return true;
			}else{
				return false;
			}
		}
	}
	
	protected static class Face{
		List<Index> indices = new ArrayList<Index>();
		
		public void addIndex(Index index){
			indices.add(index);
		}
		
		public List<Index> getIndices(){
			return indices;
		}
	}
}
