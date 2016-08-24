package engine.core;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Utils {
	
	public static String loadResource(String fileName) throws Exception{
		
		return new String(Files.readAllBytes(Paths.get(Utils.class.getResource(fileName).toURI())));
	}
	
	public static String loadResourceII(String fileName) throws Exception{
		
		String result = "";
		
		try(InputStream in = Utils.class.getClass().getResourceAsStream(fileName)){
			result = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
		}
		
		return result;
	}
	
	public static List<String> readAllLines(String fileName) throws Exception{
		
		List<String> list = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(Utils.class.getClass().getResourceAsStream(fileName)))){
			String line;
			while((line = br.readLine()) != null){
				list.add(line);
			}
		}
		
		return list;
	}
	
	public static float[] floatListToArray(List<Float> list){
		float[] array = new float[list.size()];
		
		int i=0;
		for(Float number: list){
			array[i] = number.floatValue();
			i++;
		}
		
		return array;
	}
	
	public static int[] intListToArray(List<Integer> list){
		int[] array = new int[list.size()];
		
		int i=0;
		for(Integer number: list){
			array[i] = number.intValue();
			i++;
		}
		return array;
	}
	
	public static boolean existsResourceFile(String fileName){
		boolean result;
		
		try(InputStream is = Utils.class.getResourceAsStream(fileName)){
			result = is != null;
		}catch (Exception error){
			result = false;
		}
		
		return result;
	}

}
