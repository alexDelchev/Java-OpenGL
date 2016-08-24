package engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import engine.core.Utils;

public class MD5Frame {
	
	private int ID;
	
	private float[] frameData;

	public int getID(){
		return ID;
	}
	
	public void setID(int ID){
		this.ID = ID;
	}
	
	public float[] getFrameData(){
		return frameData;
	}
	
	public void setFrameData(float[] frameData){
		this.frameData = frameData;
	}
	
	@Override
	public String toString(){
		StringBuilder str = new StringBuilder("frame " + ID + " [data: " + System.lineSeparator());
		for(float frameData: frameData){
			str.append(frameData).append(System.lineSeparator());
		}
		str.append("]").append(System.lineSeparator());
		
		return str.toString();
	}
	
	public static MD5Frame parse(String blockID, List<String> blockBody) throws Exception{
		
		MD5Frame result = new MD5Frame();
		String[] tokens = blockID.trim().split("\\s+");
		if(tokens != null && tokens.length >= 2){
			result.setID(Integer.parseInt(tokens[1]));
		}else{
			throw new Exception("Wrong frame definition: " + blockID);
		}
		
		List<Float> data = new ArrayList<>();
		for(String line: blockBody){
			List<Float> lineData = parseLine(line);
			if(lineData != null){
				data.addAll(lineData);
			}
		}
		
		float[] dataArray = Utils.floatListToArray(data);
		result.setFrameData(dataArray);
		
		return result;
	}
	
	private static List<Float> parseLine(String line){
		String[] tokens = line.trim().split("\\s+");
		List<Float> data = new ArrayList<>();
		for(String token: tokens){
			data.add(Float.parseFloat(token));
		}
		
		return data;
	}
}
