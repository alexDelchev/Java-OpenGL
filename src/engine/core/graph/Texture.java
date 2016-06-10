package engine.core.graph;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {
	
	private final int ID;
	
	private final int width;
	
	private final int height;
	
	public Texture(String fileName) throws Exception{
		this(Texture.class.getResourceAsStream(fileName));
	}
	
	public Texture(InputStream is) throws Exception{
		
		PNGDecoder decoder = new PNGDecoder(is);
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buffer,  decoder.getWidth()*4, Format.RGBA);
		buffer.flip();
		
		int textureID = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, textureID);
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0,
				GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glGenerateMipmap(GL_TEXTURE_2D);

		this.ID = textureID;
		this.width = decoder.getWidth();
		this.height = decoder.getHeight();
	}
	
	public void bind(){
		glBindTexture(GL_TEXTURE_2D, ID);
	}
	
	public int getID(){
		return ID;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public void cleanup(){
		glDeleteTextures(ID);
	}
}
