package engine.core.graph;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
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
	
	public Texture(int width, int height, int internalFormat, int pixelFormat) throws Exception{
		
		this.ID = glGenTextures();
		this.width = width;
		this.height = height;
		
		glBindTexture(GL_TEXTURE_2D, this.ID);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
		float[] borderColor = {1.0f, 1.0f, 1.0f, 1.0f};
		ByteBuffer bb = ByteBuffer.allocateDirect(borderColor.length * Float.BYTES);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(borderColor);
		fb.position(0);
		glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, fb);
	}
	
	public Texture(int id){
		this.ID = id;
		this.width = 0;
		this.height = 0;
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
