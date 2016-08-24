package engine.core.graph;

import engine.core.graph.Material;
import engine.core.graph.Mesh;
import engine.core.graph.Texture;
import engine.loaders.OBJLoader;
import engine.loaders.OBJReader;
import engine.core.GameItem;

public class SkyBox extends GameItem{

	public SkyBox(String model, String textureFile) throws Exception{
		
		super();
		Mesh skyBoxMesh = OBJLoader.loadModel(model);
		Texture skyTexture = new Texture(textureFile);
		skyBoxMesh.setMaterial(new Material(skyTexture, 0.0f));
		setMesh(skyBoxMesh);
		setPosition(0,0,0);
	}
}
