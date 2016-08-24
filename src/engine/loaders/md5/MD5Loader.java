package engine.loaders.md5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.core.Utils;
import engine.core.graph.Material;
import engine.core.graph.Mesh;
import engine.core.graph.Texture;
import engine.core.GameItem;
import engine.core.graph.animation.AnimatedGameItem;
import engine.core.graph.animation.AnimationVertex;
import engine.core.graph.animation.AnimatedFrame;

public class MD5Loader {

	private static final String NORMAL_FILE_SUFFIX = "_normal";
	
	public static AnimatedGameItem process(MD5Model md5Model, MD5AnimModel animModel, Vector3f defaultColor) throws Exception{
		
		List<Matrix4f> invJointMatrices = calculateJointMatrices(md5Model);
		List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);
		
		List<Mesh> list = new ArrayList<>();
		for(MD5Mesh md5Mesh: md5Model.getMeshes()){
			Mesh mesh = generateMesh(md5Model, md5Mesh, defaultColor);
			handleTexture(mesh, md5Mesh, defaultColor);
			list.add(mesh);
		}
		
		Mesh[] meshes = new Mesh[list.size()];
		meshes = list.toArray(meshes);
		AnimatedGameItem item = new AnimatedGameItem(meshes, animatedFrames, invJointMatrices);
		
		return item;
	}
	
	private static List<Matrix4f> calculateJointMatrices(MD5Model md5Model){
		List<Matrix4f> result = new ArrayList<>();
		
		List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
		for(MD5JointInfo.MD5JointData joint: joints){
			Matrix4f mat = new Matrix4f().translate(joint.getPosition()).rotate(joint.getOrientation()).invert();
			
			result.add(mat);
		}
		
		return result;
	}
	
	private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh, Vector3f defaultColor){
		
		List<AnimationVertex> animVerts = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		List<MD5Mesh.MD5Vertex> vertices = md5Mesh.getVertices();
		List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();
		List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
		
		for(MD5Mesh.MD5Vertex vertex : vertices){
			AnimationVertex vert= new AnimationVertex();
			animVerts.add(vert);
			
			vert.position = new Vector3f();
			vert.textCoords = vertex.getTextCoords();
			
			int startWeight = vertex.getStartWeight();
			int numWeights = vertex.getWeightCount();
			
			vert.jointIndices = new int[numWeights];
			Arrays.fill(vert.jointIndices, -1);
			vert.weights = new float[numWeights];
			Arrays.fill(vert.weights, -1);
			
			for(int i = startWeight; i < startWeight + numWeights; i++){
				MD5Mesh.MD5Weight weight = weights.get(i);
				MD5JointInfo.MD5JointData joint = joints.get(weight.getJointIndex());
				Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
				Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
				
				acumPos.mul(weight.getBias());
				vert.position.add(acumPos);
				vert.jointIndices[i - startWeight] = weight.getJointIndex();
				vert.weights[i - startWeight] = weight.getBias();
			}
		}
		
		for(MD5Mesh.MD5Triangle tri: md5Mesh.getTriangles()){
			indices.add(tri.getVertex0());
			indices.add(tri.getVertex1());
			indices.add(tri.getVertex2());
			
			//normals calculation
			AnimationVertex v0 = animVerts.get(tri.getVertex0());
			AnimationVertex v1 = animVerts.get(tri.getVertex1());
			AnimationVertex v2 = animVerts.get(tri.getVertex2());
			
			Vector3f pos0 = v0.position;
			Vector3f pos1 = v1.position;
			Vector3f pos2 = v2.position;
			
			Vector3f normal = new Vector3f(pos2).sub(pos0).cross(new Vector3f(pos1).sub(pos0));
			normal.normalize();
			
			v0.normal.add(normal).normalize();
			v1.normal.add(normal).normalize();
			v2.normal.add(normal).normalize();
		}
		
		Mesh mesh = createMesh(animVerts, indices);
		
		return mesh;
	}
	
	private static Mesh createMesh(List<AnimationVertex> vertices, List<Integer> indices){
		
		List<Float> positions = new ArrayList<>();
		List<Float> textCoords = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> jointIndices = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		
		for(AnimationVertex vertex: vertices){
			
			positions.add(vertex.position.x);
			positions.add(vertex.position.y);
			positions.add(vertex.position.z);
			
			textCoords.add(vertex.textCoords.x);
			textCoords.add(vertex.textCoords.y);
			
			normals.add(vertex.normal.x);
			normals.add(vertex.normal.y);
			normals.add(vertex.normal.z);
			
			int numWeights = vertex.weights.length;
			for(int i=0; i< Mesh.MAX_WEIGHTS; i++){
				if(i < numWeights){
					jointIndices.add(vertex.jointIndices[i]);
					weights.add(vertex.weights[i]);
					//weights.add(-1.0f);
				}else{
					jointIndices.add(-1);
					weights.add(-1.0f);
				}
			}
		}
		
		float[] positionsArray = Utils.floatListToArray(positions);
		float[] textCoordsArray = Utils.floatListToArray(textCoords);
		float[] normalsArray = Utils.floatListToArray(normals);
		int[] indicesArray = Utils.intListToArray(indices);
		int[] jointIndicesArray = Utils.intListToArray(jointIndices);
		float[] weightsArray = Utils.floatListToArray(weights);
		
		Mesh result = new Mesh(positionsArray, textCoordsArray, normalsArray, indicesArray, jointIndicesArray, weightsArray);
		result.setBoundingBox(Mesh.calculateBoundingBox(positionsArray));
		
		return result;
	}
	
	private static void handleTexture(Mesh mesh, MD5Mesh md5Mesh, Vector3f defaultColor) throws Exception{
		
		String texturePath = md5Mesh.getTexture();
		if(texturePath != null && texturePath.length() > 0){
			Texture texture = new Texture("/textures/animatedmodels/" + texturePath + ".png");
			Material material = new Material(texture);
			
			/*//Normal Maps
			int pos = texturePath.lastIndexOf(".");
			if(pos > 0){
				String basePath = texturePath.substring(0,pos);
				String extension = texturePath.substring(pos, texturePath.length());
				String normalMapFileName  = basePath + NORMAL_FILE_SUFFIX + extension;
				if(Utils.existsResourceFile(normalMapFileName)){
					Texture normalMap = new Texture(normalMapname);
					material.setNormalMap(normalMap);
				}
			}*/
			
			mesh.setMaterial(material);
		}
	}
	
	private static List<AnimatedFrame> processAnimationFrames(MD5Model md5Model, MD5AnimModel animModel, List<Matrix4f> invJointMatrices){
		
		List<AnimatedFrame> animatedFrames = new ArrayList<>();
		List<MD5Frame> frames = animModel.getFrames();
		
		for(MD5Frame frame: frames){
			AnimatedFrame data = processAnimationFrame(md5Model, animModel, frame, invJointMatrices);
			animatedFrames.add(data);
		}
		
		return animatedFrames;
	}
	
	private static AnimatedFrame processAnimationFrame(MD5Model md5Model, MD5AnimModel animModel, MD5Frame frame, List<Matrix4f> invJointMatrices){
		
		AnimatedFrame result = new AnimatedFrame();
		
		MD5BaseFrame baseFrame = animModel.getBaseFrame();
		List<MD5Hierarchy.MD5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyDataList();
		
		List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
		int numJoints = joints.size();
		float[] frameData = frame.getFrameData();
		
		for(int i=0; i<numJoints; i++){
			MD5JointInfo.MD5JointData joint = joints.get(i);
			MD5BaseFrame.MD5BaseFrameData baseFrameData = baseFrame.getFrameDataList().get(i);
			
			Vector3f position = baseFrameData.getPosition();
			Quaternionf orientation = baseFrameData.getOrientation();
			
			int flags = hierarchyList.get(i).getFlags();
			int startIndex = hierarchyList.get(i).getStartIndex();
			
			if((flags & 1) > 0){
				position.x = frameData[startIndex++];
			}
			if((flags & 2) > 0){
				position.y = frameData[startIndex++];
			}
			if((flags & 4) > 0){
				position.z = frameData[startIndex++];
			}
			if((flags & 8) > 0){
				orientation.x = frameData[startIndex++];
			}
			if((flags & 16) > 0){
				orientation.y = frameData[startIndex++];
			}
			if((flags & 32) > 0){
				orientation.z = frameData[startIndex++];
			}
			
			orientation = MD5Utils.calculateQuaternion(new Vector3f(orientation.x, orientation.y, orientation.z));
			
			Matrix4f translateMatrix = new Matrix4f().translate(position);
			Matrix4f rotationMatrix = new Matrix4f().rotate(orientation);
			Matrix4f jointMatrix = new Matrix4f();
			translateMatrix.mul(rotationMatrix, jointMatrix);
			
			if(joint.getParentIndex() > -1){
				Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
				jointMatrix = new Matrix4f(parentMatrix).mul(jointMatrix);
			}
			
			result.setMatrix(i, jointMatrix, invJointMatrices.get(i));
		}
		
		return result;
	}
}
