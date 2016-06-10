package engine.core;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class MouseInput {
	
	private final Vector2d previousPos;
	private final Vector2d currentPos;
	
	private final Vector2f displVec;
	
	private boolean inWindow = false;
	private boolean leftButtonPressed = false;
	private boolean rightButtonPressed = false;
	private boolean leftButtonReleased = false;
	private boolean rightButtonReleased = false;
	
	private GLFWCursorPosCallback cursorPosCallback;
	
	private GLFWCursorEnterCallback cursorEnterCallback;
	
	private GLFWMouseButtonCallback mouseButtonCallback;
	
	public MouseInput(){
		
		previousPos = new Vector2d(-1,-1);
		currentPos = new Vector2d(0,0);
		displVec = new Vector2f();
	}
	
	public void init(Display display){
		
		glfwSetCursorPosCallback(display.getDisplayID(),
				cursorPosCallback = new GLFWCursorPosCallback(){
			@Override
			public void invoke(long window, double xpos, double ypos){
				
				currentPos.x = xpos;
				currentPos.y = ypos;
			}
		});
		
		glfwSetCursorEnterCallback(display.getDisplayID(),
				cursorEnterCallback = new GLFWCursorEnterCallback(){
			@Override
			public void invoke(long window, int entered){
				inWindow = entered == 1;
			}
		});
		
		glfwSetMouseButtonCallback(display.getDisplayID(),
				mouseButtonCallback = new GLFWMouseButtonCallback(){
			@Override
			public void invoke(long window, int button, int action, int mods){
				leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
				rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
				
				leftButtonReleased = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE;
				rightButtonReleased = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_RELEASE;
			}
		});
	}
	
	public Vector2d getCursorPosition(){
		return new Vector2d(currentPos.x, currentPos.y);
	}
	
	public Vector2f getDisplVec(){
		return displVec;
	}
	
	public void input(Display display){
		
		displVec.x = 0;
		displVec.y = 0;
		
		if(previousPos.x > 0 && previousPos.y > 0 && inWindow){
			
			double deltaX = currentPos.x - previousPos.x;
			double deltaY = currentPos.y - previousPos.y;
			
			boolean rotateX = deltaX != 0;
			boolean rotateY = deltaY != 0;
			
			if(rotateX){
				displVec.y = (float) deltaX;
			}
			
			if(rotateY){
				displVec.x = (float) deltaY;
			}
		}
		
		previousPos .x = currentPos.x;
		previousPos.y = currentPos.y;
	}
	
	public boolean isLeftButtonPressed(){
		return leftButtonPressed;
	}
	
	public boolean isRightButtonPressed(){
		return rightButtonPressed;
	}
	
	public boolean isLeftButtonReleased(){
		if(leftButtonReleased){
			leftButtonReleased = false;
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isRightButtoReleased(){
		if(rightButtonReleased){
			rightButtonReleased = false;
			return true;
		}else{
			return false;
		}
	}
}
