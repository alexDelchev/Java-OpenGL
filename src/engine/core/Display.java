package engine.core;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Display {
	
	private final String title;
	
	private int width, height;
	
	private boolean resized, vSync;

	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;
	private GLFWWindowSizeCallback windowSizeCallback;
	
	private long windowID;
	
	public Display(String title, int width, int height, boolean vSync){
		
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.resized = false;
	}
	
	public void init(){
		
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		if(glfwInit() != GLFW_TRUE)
			throw new IllegalStateException("Unnabe to initialize GLFW");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		

		
		windowID = glfwCreateWindow(width, height, title, NULL, NULL);
		if(windowID == NULL)
			throw new RuntimeException("Failed to create GLFW window");
		
		glfwSetWindowSizeCallback(windowID, windowSizeCallback = new GLFWWindowSizeCallback(){
			public void invoke(long window, int width, int height){
				Display.this.width = width;
				Display.this.height = height;
				Display.this.setResized(true);
			}
		});
		
		glfwSetKeyCallback(windowID, keyCallback = new GLFWKeyCallback(){
			public void invoke(long window, int key, int scancode, int action, int mods){
				if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
					glfwSetWindowShouldClose(window, GLFW_TRUE);
			}
				
		});
		
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(windowID, (vidmode.width()-width)/2, (vidmode.height()-height)/2);
		
		glfwMakeContextCurrent(windowID);
		if(isvSync()){
			glfwSwapInterval(1);
		}
		
		glfwShowWindow(windowID);
		
		GL.createCapabilities();
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}
	
	public long getDisplayID(){
		return windowID;
	}
	
	public void setClearColor(float r, float g, float b, float alpha){
		glClearColor(r,g,b,alpha);
	}
	
	public boolean windowShouldClose(){
		return glfwWindowShouldClose(windowID) == GL_TRUE;
	}
	
	public boolean isKeyPressed(int KeyCode){
		return glfwGetKey(windowID, KeyCode) == GLFW_PRESS;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public boolean isResized(){
		return resized;
	}
	
	public void setResized(boolean resized){
		this.resized = resized;
	}
	
	public boolean isvSync(){
		return vSync;
	}
	
	public void setvSync(boolean vSync){
		this.vSync = vSync;
	}
	
	public void update(){
		glfwSwapBuffers(windowID);
		glfwPollEvents();
	}
}
