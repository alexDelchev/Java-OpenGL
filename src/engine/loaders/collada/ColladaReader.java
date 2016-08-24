package engine.loaders.collada;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import engine.core.Utils;
import engine.core.graph.Mesh;
import engine.core.graph.animation.AnimatedFrame;
import engine.core.graph.animation.AnimatedGameItem;

public class ColladaReader {
	
	private final static int POSITIONS_KEY = 1;
	private final static int NORMALS_KEY = 2;
	private final static int TEXCOORDS_KEY = 3;

	public static AnimatedGameItem loadModel(String path) throws Exception{
		
		String positionSource;
		String normalSource;
		String texcoordSource;
		
		String positions;
		String normals;
		String texcoords;
		
		TextureValuesNumber textVertValuesNumber = new TextureValuesNumber();
		textVertValuesNumber.setTextureValuesNumber(0);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bld = factory.newDocumentBuilder();
		Document doc = bld.parse(Utils.class.getClass().getResourceAsStream(path));
		
		Node geoLib = doc.getElementsByTagName("library_geometries").item(0);
		Node geometries = null;
		for(int i=0; i<geoLib.getChildNodes().getLength(); i++){
			if(geoLib.getChildNodes().item(i).hasChildNodes()){
				geometries = geoLib.getChildNodes().item(i);
			}
		}
		
		AnimatedGameItem item = null;
		
		positionSource = null;
		normalSource = null;
		texcoordSource = null;
		
		positions = null;
		normals = null;
		texcoords = null;
		List<Index> indices = new ArrayList<>();
		
		List<Joint> bones = new ArrayList<>();
		Node lib_vis = doc.getElementsByTagName("library_visual_scenes").item(0);
		Node visual_scene = searchForNode(lib_vis.getChildNodes(), "visual_scene").get(0);
		bones = getBones(visual_scene);
		
		List<Mesh> itemMeshes = new ArrayList<>();
		List<Node> meshes = searchForNode(geometries.getChildNodes(), "mesh");
		for(Node mesh: meshes){
			NodeList children = mesh.getChildNodes();
			
			positionSource = getSource(children, "POSITION", positionSource);
			normalSource = getSource(children, "NORMAL", normalSource);
			texcoordSource = getSource(children, "TEXCOORD", texcoordSource);
			
			positions = getValue(children, positionSource, positions, POSITIONS_KEY, textVertValuesNumber);
			
			normals = getValue(children, normalSource, normals, NORMALS_KEY, textVertValuesNumber);
			if(normals == null){	
				normals = getValue(children, NORMALS_KEY, positionSource, textVertValuesNumber);
			}
			
			texcoords = getValue(children, texcoordSource, texcoords, TEXCOORDS_KEY, textVertValuesNumber);
			if(texcoords == null){
				texcoords = getValue(children, TEXCOORDS_KEY, positionSource, textVertValuesNumber);
			}

			indices = getIndices(children);
		}
		
		NodeList lib_contr = doc.getElementsByTagName("library_controllers");
		List<Node> controllers = searchForNode(lib_contr.item(0).getChildNodes(), "controller");
		for(Node controller: controllers){
			List<Node> skins = searchForNode(controller.getChildNodes(), "skin");
			for(Node skin: skins){
						
				String jointSource = null;
				String invJointBindMatricesSource = null;
				String weightSource = null;
				
				String[] joints = null;
				List<Matrix4f> invJointBindMatrs = new ArrayList<>();
				float[] weights = null;
				
				int[] vcount = new int[] {};
				int[] v = new int[] {};
				
				int jointOffset = 0;
				int weightOffset = 1;
				
				List<Node> jointsList = searchForNode(skin.getChildNodes(), "joints");
				for(Node joint: jointsList){
					List<Node> inputs = searchForNode(joint.getChildNodes(), "input");
					for(Node input: inputs){
						Node semantic = getAttribute(input.getAttributes(), "semantic");
						Node source = getAttribute(input.getAttributes(), "source");
						
						if(semantic.getNodeValue().equals("JOINT")){
							jointSource = source.getNodeValue();
						}else if(semantic.getNodeValue().equals("INV_BIND_MATRIX")){
							invJointBindMatricesSource = source.getNodeValue();
						}
					}
				}
				List<Node> vertex_weightsList = searchForNode(skin.getChildNodes(), "vertex_weights");
				for(Node vertex_weights: vertex_weightsList){
					List<Node> inputs = searchForNode(vertex_weights.getChildNodes(), "input");
					for(Node input: inputs){
						Node semantic = getAttribute(input.getAttributes(), "semantic");
						Node source = getAttribute(input.getAttributes(), "source");
						Node offset = getAttribute(input.getAttributes(), "offset");
						if(semantic.getNodeValue().equals("WEIGHT")){
							weightSource = source.getNodeValue();
							weightOffset = Integer.parseInt(offset.getNodeValue());
						}else if(semantic.getNodeValue().equals("JOINT")){
							jointOffset = Integer.parseInt(offset.getNodeValue());
						}
					}
					
					Node vcountNode = searchForNode(vertex_weights.getChildNodes(), "vcount").get(0);
					Node vNode = searchForNode(vertex_weights.getChildNodes(), "v").get(0);
					
					String[] temp = vcountNode.getTextContent().trim().split("\\s+");
					vcount = new int[temp.length];
					for(int i=0; i<temp.length; i++){
						vcount[i] = Integer.parseInt(temp[i]);
					}
					
					temp = vNode.getTextContent().trim().split("\\s+");
					v = new int[temp.length];
					for(int i=0; i<temp.length; i++){
						v[i] = Integer.parseInt(temp[i]);
					}
				}
				
				if(jointSource != null){
					List<Node> sources = searchForNode(skin.getChildNodes(), "source");
					for(Node source: sources){
						Node id = getAttribute(source.getAttributes(), "id");
						if(id.getNodeValue().equals(jointSource.split("#")[1])){
							List<Node> name_array = searchForNode(source.getChildNodes(), "Name_array");
							String[] str = name_array.get(0).getTextContent().trim().split("\\s+");
							if(str[0].isEmpty()){
								joints = new String[str.length - 1];
								for(int i=1; i<str.length; i++){
									joints[i-1] = str[i];
								}
							}else{
								joints = str;
							}
						}								
					}
				}
				
				if(invJointBindMatricesSource != null){
					List<Node> sources = searchForNode(skin.getChildNodes(), "source");
					for(Node source: sources){
						Node id = getAttribute(source.getAttributes(), "id");
						if(id.getNodeValue().equals(invJointBindMatricesSource.split("#")[1])){
							List<Node> float_array = searchForNode(source.getChildNodes(), "float_array");
							String[] temp = float_array.get(0).getTextContent().trim().split("\\s+");
							String[] temp2 = new String[temp.length - 1];
							if(temp[0].isEmpty()){
								for(int i=1; i<temp.length; i++){
									temp2[i-1] = temp[i];
								}
								
								for(int i=0; i<temp2.length/16; i++){
									float[] values = new float[16];
									for(int j=0; j<values.length; j++){
										if(!temp2[i*16 + j].isEmpty()){
											values[j] = Float.parseFloat(temp2[i*16 + j]);
										}
									}
									
									Matrix4f mat = createMatrix(values);
									invJointBindMatrs.add(mat);
								}
							}else{
								for(int i=0; i<temp.length/16; i++){
									float[] values = new float[16];
									for(int j=0; j<values.length; j++){
										if(!temp[i*16 + j].isEmpty()){
											values[j] = Float.parseFloat(temp[i*16 + j]);
										}
									}
									
									Matrix4f mat = createMatrix(values);
									invJointBindMatrs.add(mat);
								}
							}
						}
					}
				}
				
				for(int i=0; i<joints.length; i++){
					Joint joint = getJointByName(joints[i], bones);
					joint.matrixInv = new Matrix4f(invJointBindMatrs.get(i));
				}
				
				if(weightSource != null){
					List<Node> sources = searchForNode(skin.getChildNodes(), "source");
					for(Node source: sources){
						Node id = getAttribute(source.getAttributes(), "id");
						if(id.getNodeValue().equals(weightSource.split("#")[1])){
							List<Node> float_array = searchForNode(source.getChildNodes(), "float_array");
							String[] temp = float_array.get(0).getTextContent().trim().split("\\s+");
							weights = new float[temp.length];
							for(int i=0; i<temp.length; i++){
								weights[i] = Float.parseFloat(temp[i]);
							}
						}
					}
				}
				
				String[] temp = positions.trim().split("\\s+");
				float[] pos = new float [temp.length];
				for(int i=0; i<temp.length; i++){
					pos[i] = Float.parseFloat(temp[i]);
				}
				
				temp = normals.trim().split("\\s+");
				float[] norm = new float[temp.length];
				for(int i=0; i<temp.length; i++){
					norm[i] = Float.parseFloat(temp[i]);
				}
				
				temp = texcoords.trim().split("\\s+");
				List<Float> textsList = new ArrayList<>();
				for(int i=0; i<temp.length/textVertValuesNumber.getTextureValuesNumber(); i++){
					textsList.add(Float.parseFloat(temp[i*textVertValuesNumber.getTextureValuesNumber()]));
					textsList.add(1 - Float.parseFloat(temp[i*textVertValuesNumber.getTextureValuesNumber() + 1]));
				}
				float[] texts = Utils.floatListToArray(textsList);
				
				List<AnimationVertex> vertices = new ArrayList<>();
				int startInd = 0;
				for(int i=0; i<vcount.length; i++){
					AnimationVertex vert = new AnimationVertex();
					
					Vector3f position = new Vector3f();
					position.x = pos[i*3];
					position.y = pos[i*3 + 1];
					position.z = pos[i*3 + 2];
					vert.position = position;
					
					int jointNum = vcount[i];

					for(int j=0; j<jointNum; j++){
						if(j<4){
							vert.jointIndices[j] = getJointByName(joints[v[startInd*2 + jointOffset]], bones).index;
				
							vert.weights[j] = weights[v[startInd*2 + weightOffset]];
						
							startInd++;
						}
					}
					
					vertices.add(vert);
				}
				
				Mesh mesh = createRiggedMesh(vertices, norm, texts, indices);
				itemMeshes.add(mesh);
				
				Mesh[] meshesArray = new Mesh[itemMeshes.size()];
				for(int i=0; i<itemMeshes.size(); i++){
					meshesArray[i] = itemMeshes.get(i);
				}			
				
				item = new AnimatedGameItem(meshesArray, invJointBindMatrs);
			}
		}
		
		return item;
	}
	
	public static String getSource(NodeList children, String key, String dest){

		String temp = dest;

		List<Node> vertices = searchForNode(children, "vertices");
		for(Node vert: vertices){
			List<Node> inputs = searchForNode(vert.getChildNodes(), "input");
			for(Node input: inputs){
				Node inputSemantic = getAttribute(input.getAttributes(), "semantic");
				if(inputSemantic.getNodeValue().equals(key)){
					Node inputSource = getAttribute(input.getAttributes(), "source");
					if(temp == null){
						temp = inputSource.getNodeValue();
					}
				}
			}
		}
		
		return temp;
	}
	
	public static String getValue(NodeList children, String source, String dest, int key, TextureValuesNumber textVertValuesNumber){
		
		String temp = dest;

		if(source != null){
			List<Node> sources = searchForNode(children, "source");
			for(Node sourceNode: sources){
				Node sourceID = getAttribute(sourceNode.getAttributes(), "id");
				if(sourceID.getNodeValue().equals(source.split("#")[1])){
					List<Node> float_arrays = searchForNode(sourceNode.getChildNodes(), "float_array");
					for(Node float_array: float_arrays){
						if(temp == null){
							temp = float_array.getTextContent();
						}
					}
					
					if(key == TEXCOORDS_KEY /*&& textVertValuesNumber == -1*/){
						List<Node> technique_commons = searchForNode(sourceNode.getChildNodes(), "technique_common");
						for(Node technique_common: technique_commons){
							List<Node> accessors = searchForNode(technique_common.getChildNodes(), "accessor");
							for(Node accessor: accessors){
								List<Node> params = searchForNode(accessor.getChildNodes(), "param");
								textVertValuesNumber.setTextureValuesNumber(params.size());
							}
						}
					}
				}
			}
		}
		
		return temp;
	}
	
	private static String getValue(NodeList children, int key, String positionSource, TextureValuesNumber textVertValuesNumber){
		String temp = null;
		
		List<Node> sources = searchForNode(children, "source");
		
		for(Node sourceNode:sources){
			Node sourceID = getAttribute(sourceNode.getAttributes(), "id");
			if(!sourceID.getNodeValue().equals(positionSource.split("#")[1])){
				List<Node> technique_commons = searchForNode(sourceNode.getChildNodes(), "technique_common");
				for(Node technique_common: technique_commons){
					List<Node> accessors = searchForNode(technique_common.getChildNodes(), "accessor");
					for(Node accessor: accessors){
						List<Node> params = searchForNode(accessor.getChildNodes(), "param");
						for(Node param: params){
							Node paramName = getAttribute(param.getAttributes(), "name");
							if(key == NORMALS_KEY && paramName.getNodeValue().equals("X")){
								NodeList list = sourceNode.getChildNodes();
								for(int j=0; j<list.getLength(); j++){
									if(list.item(j).getNodeName().equals("float_array")){
									    temp = list.item(j).getTextContent();
									}
								}
							}else if(key == TEXCOORDS_KEY && paramName.getNodeValue().equals("S")){
								NodeList list = sourceNode.getChildNodes();
								for(int j=0; j<list.getLength(); j++){
									if(list.item(j).getNodeName().equals("float_array")){
									    temp = list.item(j).getTextContent();
									    textVertValuesNumber.setTextureValuesNumber(params.size());
									}
								}
							}
						}	
					}
				}
			}
		}
	
		
		return temp;
	}
	
	private static List<Index> getIndices(NodeList list){
		
		List<Index> indices = new ArrayList<>();
		List<Node> trianglesList = searchForNode(list, "triangles");
		
		if(trianglesList.size() == 0){
			trianglesList = searchForNode(list, "polygons");
		}
		
		if(trianglesList.size() == 0){
			trianglesList = searchForNode(list, "polylist");
		}
		for(Node triangles: trianglesList){
			List<Node> inputs = searchForNode(triangles.getChildNodes(), "input");
			List<Node> ps = searchForNode(triangles.getChildNodes(), "p");
			
			if(inputs.size() > 1){
				int posOffset = -1;
				int texOffset = -1;
				int normOffset = -1;
				for(Node input: inputs){
					Node inputSemantic = getAttribute(input.getAttributes(), "semantic");
					Node inputOffset = getAttribute(input.getAttributes(), "offset");
					if(inputSemantic.getNodeValue().equals("VERTEX")){
						
						posOffset = Integer.parseInt(inputOffset.getNodeValue());
						
					}else if(inputSemantic.getNodeValue().equals("TEXCOORD")){
						
						texOffset = Integer.parseInt(inputOffset.getNodeValue());
						
					}else if(inputSemantic.getNodeValue().equals("NORMAL")){
						
						normOffset = Integer.parseInt(inputOffset.getNodeValue());
					}
				}
				
				int numOfSources = inputs.size();
				
				for(Node pNode: ps){
					String[] p = pNode.getTextContent().trim().split("\\s+");
					
					if(texOffset == -1){
						for(int i=0; i<p.length/numOfSources; i++){
							int pos = Integer.parseInt(p[i*numOfSources + posOffset]);
							int norm = Integer.parseInt(p[i*numOfSources + normOffset]);
							Index index = new Index(pos, norm, false);
							indices.add(index);
						}
					}else if(normOffset == -1){
						for(int i=0; i<p.length/numOfSources; i++){
							int pos = Integer.parseInt(p[i*numOfSources + posOffset]);
							int tex = Integer.parseInt(p[i*numOfSources + texOffset]);
							Index index = new Index(pos, tex, true);
							indices.add(index);
						}
					}else{
						for(int i=0; i<p.length/numOfSources; i++){
							int pos = Integer.parseInt(p[i*numOfSources + posOffset]);
							int norm = Integer.parseInt(p[i*numOfSources + normOffset]);
							int tex = Integer.parseInt(p[i*numOfSources + texOffset]);
							Index index = new Index(pos, norm, tex);
							indices.add(index);
						}
					}
				}
			}else if(inputs.size() == 1){
				for(Node pNode: ps){
					String[] p = pNode.getTextContent().trim().split("\\s+");
				    for(int i=0; i<p.length; i++){
					    int pos = Integer.parseInt(p[i]);
					    Index index = new Index(pos);
					    indices.add(index);
				    }
				}
			}
		}

		return indices;
	}
	
	private static List<Joint> getBones(Node visual_scene){
		List<Joint> joints = new ArrayList<>();
		List<Node> nodes = searchForNode(visual_scene.getChildNodes(), "node");
		for(Node node: nodes){
			List<Node> children = searchForNode(node.getChildNodes(), "node");
			if(children.size()> 0){
				int index = -1;
				Joint joint = new Joint();
				List<Node> matrix = searchForNode(node.getChildNodes(), "matrix");
				if(matrix.size() == 1){
					String[] matStr = matrix.get(0).getTextContent().trim().split("\\s+");
					float[] matValues = new float[matStr.length];
					for(int i=0; i<matStr.length; i++){
						matValues[i] = Float.parseFloat(matStr[i]);
					}
					Matrix4f tempMat = createMatrix(matValues);
					joint.matrix = tempMat;
					joint.parent = index;
					joint.index = index + 1;
					joint.worldMatrix = new Matrix4f(joint.matrix);
					joint.matrixInv = new Matrix4f();
					
					Node sid = getAttribute(node.getAttributes(), "sid");
					if(sid != null){
						joint.sid = sid.getNodeValue();
					}
					
					Node id = getAttribute(node.getAttributes(), "id");
					if(id != null){
						joint.id = null;
					}
					
					joints.add(joint);
					for(Node child: children){
						getBone(joint, child, joints, index);
					}
				}else{
					joints = getBones(node);
				}
			}
		}
		
		return joints;
	}
	
	private static void getBone(Joint parent, Node node, List<Joint> bones, int index){
		
		Joint joint = new Joint();
		
		List<Node> nodes = searchForNode(node.getChildNodes(), "node");
		
		List<Node> matrix = searchForNode(node.getChildNodes(), "matrix");
		if(matrix.size() == 1){
			String[] matStr = matrix.get(0).getTextContent().trim().split("\\s+");
			float[] matValues = new float[matStr.length];
			for(int i=0; i<matStr.length; i++){
				matValues[i] = Float.parseFloat(matStr[i]);
			}
			Matrix4f tempMat = createMatrix(matValues);
			index++;
			joint.parent = parent.index;
			joint.index = bones.size();
			joint.parentID = parent.id;
			joint.matrix = tempMat;
			joint.worldMatrix = new Matrix4f();
		    joint.matrixInv = new Matrix4f();
			
			parent.childIndices.add(joint.index);
			
			Node sid = getAttribute(node.getAttributes(), "sid");
			if(sid == null){
				sid = getAttribute(node.getAttributes(), "name");
			}
			if(sid != null){
				joint.sid = sid.getNodeValue();
			}
			Node id = getAttribute(node.getAttributes(), "id");
			if(id != null){
				joint.id = id.getNodeValue();
			}
			
			parent.childIDs.add(joint.id);
		}
		bones.add(joint);
		for(int i=0; i<nodes.size(); i++){
			Node currNode = nodes.get(i);
			index += i;
			getBone(joint, currNode, bones, index);
		}
	}
	
	private static void calculateJoints(Joint root, Matrix4f jointMat, List<Joint> bones){
		
		root.worldMatrix = new Matrix4f(jointMat);
		
		if(root.childIndices.size() > 0){
			for(Integer index: root.childIndices){
				Joint child = getJointByIndex(index, bones);
				calculateJoints(child, root.worldMatrix, bones);
			}
		}
	}
	
	private static Joint getJointByName(String name, List<Joint> joints){
		Joint joint = null;
		for(Joint currJoint: joints){
			if(currJoint.sid != null && currJoint.sid.equals(name)){
				joint = currJoint;
			}
		}
		
		return joint;
	}
	
	private static Joint getJointById(String id, List<Joint> joints){
		Joint joint = null;
		for(Joint currJoint: joints){
			if(currJoint.id != null && currJoint.id.equals(id)){
				joint = currJoint;
			}
		}
		
		return joint;
	}
	
	private static Joint getJointByIndex(int index, List<Joint> joints){
		Joint joint = null;
		for(Joint currJoint: joints){
			if(currJoint.index == index){
				joint = currJoint;
			}
		}
		
		return joint;
	}
	
	private static Mesh createRiggedMesh(List<AnimationVertex> vertices, float[] norm, float[] texts, List<Index> inds){
		
		List<Float> positions = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Float> textures = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		List<Integer> jointIndices = new ArrayList<>();

		for(int i=0; i<inds.size(); i++){
			Index index = inds.get(i);
			AnimationVertex vert = vertices.get(index.position);
			positions.add(vert.position.x);
			positions.add(vert.position.y);
			positions.add(vert.position.z);
			
			if(index.normal > -1){
				normals.add(norm[index.normal*3]);
				normals.add(norm[index.normal*3 + 1]);
				normals.add(norm[index.normal*3 + 2]);
			}
			
			if(index.texture > -1){
				textures.add(texts[index.texture*2]);
				textures.add(texts[index.texture*2 + 1]);
			}
			
			indices.add(i);
			
			for(int j=0; j<4; j++){
				weights.add(vert.weights[j]);
				jointIndices.add(vert.jointIndices[j]);
			}
		}
		
		float[] positionsArray = Utils.floatListToArray(positions);
		float[] texturesArray = Utils.floatListToArray(textures);
		float[] normalsArray = Utils.floatListToArray(normals);
		int[] indicesArray = Utils.intListToArray(indices);
		float[] weightsArray = Utils.floatListToArray(weights);
		int[] jointIndicesArray = Utils.intListToArray(jointIndices);
		
		Mesh mesh = new Mesh(positionsArray, texturesArray, normalsArray, indicesArray, jointIndicesArray, weightsArray);
		mesh.setBoundingBox(mesh.calculateBoundingBox(positionsArray));
		
		return mesh;
	}
	
	public static List<AnimatedFrame> loadAnimation(String path) throws Exception{
		
		List<AnimatedFrame> frames = new ArrayList<AnimatedFrame>();
		List<Joint> bones = new ArrayList<Joint>();
		Matrix4f bindShapeMat = new Matrix4f();
		String invJointBindMatricesSource = null;
		List<Matrix4f> invJointBindMatrs = new ArrayList<>();
		String jointSource = null;
		String[] joints = null;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bld = factory.newDocumentBuilder();
		Document doc = bld.parse(Utils.class.getClass().getResourceAsStream(path));
		
		Node lib_vis = doc.getElementsByTagName("library_visual_scenes").item(0);
		Node visual_scene = searchForNode(lib_vis.getChildNodes(), "visual_scene").get(0);
		bones = getBones(visual_scene);
		
		NodeList lib_contr = doc.getElementsByTagName("library_controllers");
		List<Node> controllers = searchForNode(lib_contr.item(0).getChildNodes(), "controller");
		for(Node controller: controllers){
			List<Node> skins = searchForNode(controller.getChildNodes(), "skin");
			for(Node skin: skins){
				
				String[] bindShapeString = searchForNode(skin.getChildNodes(), "bind_shape_matrix").get(0).getTextContent().trim().split("\\s+");
				float[] bindShape = new float[bindShapeString.length];
				for(int i=0; i<bindShape.length; i++){
					bindShape[i] = Float.parseFloat(bindShapeString[i]);
				}
				bindShapeMat = createMatrix(bindShape);
				
				List<Node> jointsList = searchForNode(skin.getChildNodes(), "joints");
				for(Node joint: jointsList){
					List<Node> inputs = searchForNode(joint.getChildNodes(), "input");
					for(Node input: inputs){
						Node semantic = getAttribute(input.getAttributes(), "semantic");
						Node source = getAttribute(input.getAttributes(), "source");
						
						if(semantic.getNodeValue().equals("JOINT")){
							jointSource = source.getNodeValue();
						}else if(semantic.getNodeValue().equals("INV_BIND_MATRIX")){
							invJointBindMatricesSource = source.getNodeValue();
						}
					}
				}
				
				if(invJointBindMatricesSource != null){
					List<Node> sources = searchForNode(skin.getChildNodes(), "source");
					for(Node source: sources){
						Node id = getAttribute(source.getAttributes(), "id");
						if(id.getNodeValue().equals(invJointBindMatricesSource.split("#")[1])){
							List<Node> float_array = searchForNode(source.getChildNodes(), "float_array");
							String[] temp = float_array.get(0).getTextContent().trim().split("\\s+");
							String[] temp2 = new String[temp.length - 1];
							if(temp[0].isEmpty()){
								for(int i=1; i<temp.length; i++){
									temp2[i-1] = temp[i];
								}
								
								for(int i=0; i<temp2.length/16; i++){
									float[] values = new float[16];
									for(int j=0; j<values.length; j++){
										if(!temp2[i*16 + j].isEmpty()){
											values[j] = Float.parseFloat(temp2[i*16 + j]);
										}
									}
									
									Matrix4f mat = createMatrix(values);
									invJointBindMatrs.add(mat);
								}
							}else{
								for(int i=0; i<temp.length/16; i++){
									float[] values = new float[16];
									for(int j=0; j<values.length; j++){
										if(!temp[i*16 + j].isEmpty()){
											values[j] = Float.parseFloat(temp[i*16 + j]);
										}
										//values[j] = Float.parseFloat(temp[i*16 + j]);
									}
									
									Matrix4f mat = createMatrix(values);
									invJointBindMatrs.add(mat);
								}
							}
						}
					}
				}
				
				if(jointSource != null){
					List<Node> sources = searchForNode(skin.getChildNodes(), "source");
					for(Node source: sources){
						Node id = getAttribute(source.getAttributes(), "id");
						if(id.getNodeValue().equals(jointSource.split("#")[1])){
							List<Node> name_array = searchForNode(source.getChildNodes(), "Name_array");
							String[] str = name_array.get(0).getTextContent().trim().split("\\s+");
							if(str[0].isEmpty()){
								joints = new String[str.length - 1];
								for(int i=1; i<str.length; i++){
									joints[i-1] = str[i];
								}
							}else{
								joints = str;
							}
						}								
					}
				}
				
				for(int i=0; i<joints.length; i++){
					Joint joint = getJointByName(joints[i], bones);
					
					joint.matrixInv = new Matrix4f(invJointBindMatrs.get(i));
				}
			}
		}
		
		
		
		Node anim_lib = doc.getElementsByTagName("library_animations").item(0);
		frames= loadAnimationData(anim_lib, bones, bindShapeMat);
		
		return frames;
	}
	
	private static List<AnimatedFrame> loadAnimationData(Node anim_lib, List<Joint> bones, Matrix4f bindShapeMatrix){
		List<AnimatedFrame> frames = new ArrayList<>();
		List<Node> animations = searchForNode(anim_lib.getChildNodes(), "animation");
		int channelsNum = searchForNode(animations.get(0).getChildNodes(), "channel").size();
		for(Node animation: animations){
			
			List<Node> channels = searchForNode(animation.getChildNodes(), "channel");
			List<Node> samplers = searchForNode(animation.getChildNodes(), "sampler");
			List<Node> sources = searchForNode(animation.getChildNodes(), "source");
			
			if(channels.size() == 0 && samplers.size() == 0 && sources.size() == 0){
				Node subAnimation = searchForNode(animation.getChildNodes(), "animation").get(0);
				
				channels = searchForNode(subAnimation.getChildNodes(), "channel");
				samplers = searchForNode(subAnimation.getChildNodes(), "sampler");
				sources = searchForNode(subAnimation.getChildNodes(), "source");
			}
			
			Node target = getAttribute(channels.get(0).getAttributes(), "target");
			
			Joint joint = getJointById(target.getNodeValue().trim().split("/")[0], bones);
			if(joint != null){
			List<float[]> matrices = new ArrayList<float[]>();
			
			int frameNum = getNumberOfKeyFrames(sources);
			joint.animationMatrices = new ArrayList<>();
			for(int i=0; i<frameNum; i++){
				joint.animationMatrices.add(new Matrix4f().identity());
				matrices.add(new float[16]);
				if(frames.size() != frameNum){
					frames.add(new AnimatedFrame());
				}
			}
			
			if(channels.size() == channelsNum && channels.size() == 16){
				int matValueIndex = 0;
				for(Node channel: channels){
					Node channel_source = getAttribute(channel.getAttributes(), "source");
					
					Node sampler = null;
					String ch_source_id = channel_source.getNodeValue();
					for(Node currSampler: samplers){
						Node id = getAttribute(currSampler.getAttributes(), "id");
						if(ch_source_id.split("#")[1].equals(id.getNodeValue())){
							sampler = currSampler;
						}
					}
					
					List<Node> inputs = searchForNode(sampler.getChildNodes(), "input");
					Node source_input = null;
					Node source_output = null;
					Node source_inter = null;
					for(Node input: inputs){
						Node semantic = getAttribute(input.getAttributes(), "semantic");
						Node input_source = getAttribute(input.getAttributes(), "source");
						if(semantic.getNodeValue().equals("INPUT")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_input = source;
								}
							}
						}else if(semantic.getNodeValue().equals("OUTPUT")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_output = source;
								}
							}
						}else if(semantic.getNodeValue().equals("INTERPOLATION")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_inter = source;
								}
							}
						}
					}
					
					String[] float_array_time = searchForNode(source_input.getChildNodes(), "float_array").get(0).getTextContent().trim().split("\\s+");
					if(float_array_time.length > 2){
						for(int i=0; i<float_array_time.length; i++){
							if(frames.get(i).getTime() == -10){
								frames.get(i).setTime(Float.parseFloat(float_array_time[i]));
							}
						}
					}
					
					String[] float_arrayTemp = searchForNode(source_output.getChildNodes(), "float_array").get(0).getTextContent().trim().split("\\s+");
					float[] float_array = new float[float_arrayTemp.length];
					for(int i=0; i<float_arrayTemp.length; i++){
						float_array[i] = Float.parseFloat(float_arrayTemp[i]);
					}
					
					for(int i=0; i<frameNum; i++){
						Matrix4f matr = new Matrix4f(joint.animationMatrices.get(i));
						
						if(float_array.length == frameNum){
							matrices.get(i)[matValueIndex] = float_array[i];
						}else if(float_array.length == 2){
							matrices.get(i)[matValueIndex] = float_array[0];
						}
						joint.animationMatrices.get(i).set(matr);
						
					}
					matValueIndex++;
				}
			}else if(channels.size() == channelsNum && channels.size() == 1){
				for(Node channel: channels){
					Node channel_source = getAttribute(channel.getAttributes(), "source");
					
					Node sampler = null;
					String ch_source_id = channel_source.getNodeValue();
					for(Node currSampler: samplers){
						Node id = getAttribute(currSampler.getAttributes(), "id");
						if(ch_source_id.split("#")[1].equals(id.getNodeValue())){
							sampler = currSampler;
						}
					}
					
					List<Node> inputs = searchForNode(sampler.getChildNodes(), "input");
					Node source_input = null;
					Node source_output = null;
					Node source_inter = null;
					for(Node input: inputs){
						Node semantic = getAttribute(input.getAttributes(), "semantic");
						Node input_source = getAttribute(input.getAttributes(), "source");
						if(semantic.getNodeValue().equals("INPUT")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_input = source;
								}
							}
						}else if(semantic.getNodeValue().equals("OUTPUT")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_output = source;
								}
							}
						}else if(semantic.getNodeValue().equals("INTERPOLATION")){
							for(Node source: sources){
								Node id = getAttribute(source.getAttributes(), "id");
								if(input_source.getNodeValue().split("#")[1].equals(id.getNodeValue())){
									source_inter = source;
								}
							}
						}
					}
					
					String[] float_array_time = searchForNode(source_input.getChildNodes(), "float_array").get(0).getTextContent().trim().split("\\s+");
					if(float_array_time.length > 2){
						for(int i=0; i<float_array_time.length; i++){
							if(frames.get(i).getTime() == -10){
								frames.get(i).setTime(Float.parseFloat(float_array_time[i]));
							}
						}
					}
					
					String[] float_arrayTemp = searchForNode(source_output.getChildNodes(), "float_array").get(0).getTextContent().trim().split("\\s+");
					float[] float_array = new float[float_arrayTemp.length];
					for(int i=0; i<float_arrayTemp.length/16; i++){
						for(int offset=0; offset<16; offset++){
							float_array[(i*16) + offset] = Float.parseFloat(float_arrayTemp[(i*16) + offset]);
						}
					}
					
					for(int i=0; i<frameNum; i++){
						for(int index=0; index<16; index++){
							matrices.get(i)[index] = float_array[(i*16) + index];
						}
					}
				}
			}
			
			for(int i=0; i<frameNum; i++){
				float[] matValues = matrices.get(i);
				Matrix4f matrix = createMatrix(matValues);
				joint.animationMatrices.get(i).set(matrix);
				
			}
			}
		}
		
		int numFrames = frames.size();
		for(int i=0; i<numFrames; i++){
			AnimatedFrame frame = frames.get(i);
			
			for(Joint joint: bones){
				joint.worldMatrix = new Matrix4f(joint.matrix);
				
				if(joint.animationMatrices.size() > i){
					
					joint.worldMatrix = new Matrix4f(joint.animationMatrices.get(i));
				}
				
				if(joint.parentID != null){
					Joint parent = getJointById(joint.parentID, bones);

					parent.worldMatrix.mul(joint.worldMatrix, joint.worldMatrix);
				}
				
				bindShapeMatrix.mul(joint.matrixInv, joint.matrixInv);

				joint.skinningMatrix = new Matrix4f();
				joint.worldMatrix.mul(joint.matrixInv, joint.skinningMatrix);

				frame.setMatrix(joint.index, joint.skinningMatrix, new Matrix4f().identity());
			}
		}
		
		return frames;
	}
	
	private static void removeStartEndIdenticalFrames(List<Joint> bones, List<AnimatedFrame> frames){
		
		boolean identicalFrame = false;
		boolean catchcase = false;
		
		for(Joint joint: bones){
			
			if(joint.animationMatrices.size() > 0){
				if(!catchcase && compareMatrices(joint.animationMatrices.get(0), joint.animationMatrices.get(joint.animationMatrices.size() - 1))){
					identicalFrame = true;
				}else if(!catchcase && !compareMatrices(joint.animationMatrices.get(0), joint.animationMatrices.get(joint.animationMatrices.size() - 1))){
					catchcase = true;
					identicalFrame = false;
				}
			}
		}
		
		if(identicalFrame){
			frames.remove(frames.size() - 1);
			removeStartEndIdenticalFrames(bones, frames);
		}
	}
	
	private static boolean compareMatrices(Matrix4f a, Matrix4f b){
		boolean result = false;
		boolean catchcase = false;
		
		float[] av = new float[16];
		a.get(av);
		float[] bv = new float[16];
		b.get(bv);
		
		for(int i=0; i<16; i++){
			
			if(!catchcase && av[i] == bv[i]){
				result = true;
			}else if(!catchcase && av[i] != bv[i]){
				catchcase = true;
				result = false;
			}
		}
		
		return result;
	}
	
	private static int getNumberOfKeyFrames(List<Node> sources){
		
		int result = 0;
		
		for(Node source: sources){
			Node float_array = searchForNode(source.getChildNodes(), "float_array").get(0);
			
			String[] temp = float_array.getTextContent().trim().split("\\s+");
			if(temp.length > 2){
				result = temp.length;
				break;
			}
		}
		
		return result;
	}
	
	private static Mesh createStaticMesh(String pos, String norm, String tex, List<Index> ind, TextureValuesNumber textVertValuesNumber){
		
		List<Vector3f> positionsList = new ArrayList<>();
		List<Vector3f> normVecList = new ArrayList<>();
		List<Vector2f> texVecList = new ArrayList<>();
		List<Float> posList = new ArrayList<>();
		List<Float> normalsList = new ArrayList<>();
		List<Float> texList = new ArrayList<>();
		List<Index> indList = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		String[] posArray = pos.trim().split("\\s+");
		for(int i=0; i<posArray.length/3; i++){
			Vector3f vert = new Vector3f();
			vert.x = Float.parseFloat(posArray[i*3]);
			vert.y = Float.parseFloat(posArray[i*3+1]);
			vert.z = Float.parseFloat(posArray[i*3+2]);
			positionsList.add(vert);
		}
		
		if(norm != null){
			String[] normArray = norm.trim().split("\\s+");
			for(int i=0; i<normArray.length/3; i++){
				Vector3f normal = new Vector3f();
				normal.x = Float.parseFloat(normArray[i*3]);
				normal.y = Float.parseFloat(normArray[i*3+1]);
				normal.z = Float.parseFloat(normArray[i*3+2]);
				normVecList.add(normal);
			}
		}
		
		if(tex != null){
			String[] texArray = tex.trim().split("\\s+");
			for(int i=0; i<texArray.length/textVertValuesNumber.getTextureValuesNumber(); i++){
				Vector2f texture = new Vector2f();
				texture.x = Float.parseFloat(texArray[i*textVertValuesNumber.getTextureValuesNumber()]);
				texture.y = Float.parseFloat(texArray[i*textVertValuesNumber.getTextureValuesNumber()+1]);
				texVecList.add(texture);
			}
		}
		
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
			
			posList.add(positionsList.get(index.position).x);
			posList.add(positionsList.get(index.position).y);
			posList.add(positionsList.get(index.position).z);
			
			if(index.texture >= 0){
			    texList.add(texVecList.get(index.texture).x);
			    texList.add(1f - texVecList.get(index.texture).y);
			}else{
				texList.add(0f);
			    texList.add(0f);
			}
			
			normalsList.add(normVecList.get(index.normal).x);
			normalsList.add(normVecList.get(index.normal).y);
			normalsList.add(normVecList.get(index.normal).z);
		}
		
		Mesh mesh = new Mesh(Utils.floatListToArray(posList), Utils.floatListToArray(texList), Utils.floatListToArray(normalsList), Utils.intListToArray(indices));
		mesh.setBoundingBox(calculateBoundingBox(positionsList));
		return mesh;
	}
	
	private static List<Vector3f> calculateBoundingBox(List<Vector3f> positions){
		List<Vector3f> box = new ArrayList<Vector3f>();
		
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY);
		
		for(Vector3f vertex: positions){
			
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
	
	private static List<Node> searchForNode(NodeList list, String nodeName){
		List<Node> nodes = new ArrayList<>();
		for(int i=0; i<list.getLength(); i++){
			if(list.item(i).getNodeName().equals(nodeName)){
				nodes.add(list.item(i));
			}
		}
		
		return nodes;
	}
	
	private static Node getAttribute(NamedNodeMap map, String name){
		Node node = null;
		for(int i=0; i<map.getLength(); i++){
			if(map.item(i).getNodeName().equals(name)){
				node = map.item(i);
			}
		}
		
		return node;
	}
	
	private static Matrix4f createMatrix(float[] array){
		Matrix4f mat = new Matrix4f(); 
		if(array.length == 16){
			float m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10,
			m11, m12, m13, m14, m15;
			
			m0 = array[0];
			m1 = array[4];
			m2 = array[8];
			m3 = array[12];
			m4 = array[1];
			m5 = array[5];
			m6 = array[9];
			m7 = array[13];
			m8 = array[2];
			m9 = array[6];
			m10 = array[10];
			m11 = array[14];
			m12 = array[3];
			m13 = array[7];
			m14 = array[11];
			m15 = array[15];
			
			mat = new Matrix4f(m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, 
					m11, m12, m13, m14, m15);
		}
		
		mat.normalize3x3();

		mat.m33 = 1;
		
		return mat;
	}
	
	private static class Index{
		
		int position, normal, texture;
		
		public Index(int position, int normal, int texture){
			this.position = position;
			this.normal = normal;
			this.texture = texture;
		}
		
		public Index(int position, int value, boolean hasTexture){
			if(hasTexture){
				this.position = position;
				this.texture = value;
				this.normal = -1;
			}else{
				this.position = position;
				this.texture = -1;
				this.normal = value;
			}
		}
		
		public Index(int position){
			this.position = position;
			this.texture = -1;
			this.normal = -1;
		}
		
		public boolean equalsTo(Index index){
			if(position == index.position && texture == index.texture && normal == index.normal){
				return true;
			}else{
				return false;
			}
		}
		
	}
	
	private static class AnimationVertex {

		public Vector3f position;
		
		public float[] weights = new float[] {-1,-1,-1,-1};
		
		public int[] jointIndices = new int[] {-1,-1,-1,-1};
		
	}
	
	public static class Joint{
		
		int parent;
		
		int index;
		
		String id;
		
		String sid;
		
		String parentID;
		
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f matrixInv = new Matrix4f();
		
		Matrix4f worldMatrix = new Matrix4f();
		
		Matrix4f skinningMatrix = new Matrix4f();
		
		List<Integer> childIndices = new ArrayList<>();
		
		List<Matrix4f> animationMatrices = new ArrayList<>();
		
		List<String> childIDs = new ArrayList<>();
	}
	
	private static class TextureValuesNumber{
	
		int textureValuesNumber;
		
		public void setTextureValuesNumber(int value){
			this.textureValuesNumber = value;
		}
		
		public int getTextureValuesNumber(){
			return textureValuesNumber;
		}
	}
}
