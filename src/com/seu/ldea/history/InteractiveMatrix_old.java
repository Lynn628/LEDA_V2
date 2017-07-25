package com.seu.ldea.history;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.seu.ldea.entity.TimeSpan;

/**
 * 把LabelClasswithtime 中的classPTMap转化为Interactive matrix
 * @author Lynn
 *
 */

public class InteractiveMatrix_old {
	//HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = new HashMap<>();
	
	public static HashMap<Integer, HashMap<String, TimeSpan>> getInteractiveMatrix(HashMap<Integer, HashMap<String, TimeSpan>> classPTMap){
		HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix = classPTMap;
		//矩阵中的时间 属性
		HashSet<String> propertySet =  new HashSet<>();
		//矩阵中的class id 和 String的map
		HashMap<Integer, String> classMap = new HashMap<>();
		//统计有多少有效的时间属性和时间区间
		Iterator<Entry<Integer, HashMap<String, TimeSpan>>> iteratorOuter = interactiveMatrix.entrySet().iterator();
		//for(Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()){
		 while(iteratorOuter.hasNext()){
		     Entry<Integer, HashMap<String, TimeSpan>> entry = iteratorOuter.next();
			 HashMap<String, TimeSpan> pts = entry.getValue();
			 System.out.println("pts size before  " + pts.size());
			 Iterator<Entry<String, TimeSpan>> iterator = pts.entrySet().iterator();
			 while(iterator.hasNext()){
				 Entry<String, TimeSpan> pt = iterator.next();
				 TimeSpan span = pt.getValue();
				 String property = pt.getKey();
				 //时间区间不为空
				 if(!span.getBegin().equals("") && !span.getEnd().equals("")){
					 propertySet.add(property);
				 }else{
					 //删除不合格时间属性以及时间区间键值对
					 iterator.remove();
			      }
		     }
			 //如果当前class经处理后pts为0，则从interactiveMatrix中删除此类
			 if(pts.size() == 0){
				 iteratorOuter.remove();
			  }
			 System.out.println("Pts after " + pts.size());
		}
		System.out.println("time property #  " + propertySet.size());
		//填充其它class不具备的时间属性
		for(Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()){
			 HashMap<String, TimeSpan> pts = entry.getValue();
			 //System.out.println("pts before " + pts.size()) ;
			 for(String property : propertySet){
				
				// System.out.println("property -- " + property);
				 if(!pts.containsKey(property)){
					 pts.put(property, new TimeSpan("", ""));
				 }
			 }
			 //System.out.println("pts after " + pts.size());
		}
		System.out.println("class # " + interactiveMatrix.size());
		System.out.println("time property #  " + propertySet.size());
		return interactiveMatrix;
	}
	
	/**
	 * 读取文件创建classPTMap
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, HashMap<String, TimeSpan>> getClassPTMap(String path) throws IOException{
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = new HashMap<>();
		FileReader fileReader = new FileReader(path);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		//class id
		Integer id = null;
		//此class对应的hashmap
		HashMap<String, TimeSpan> pts = null;
		while((line = bufferedReader.readLine()) != null){
			if(!line.equals("")){
				String[] itemArr = line.split(" ");
				if(itemArr.length == 3){
					//创建当前class的pts map
					pts = new HashMap<>();
					id = Integer.parseInt(itemArr[0]);
					String property = itemArr[1];
					String spanStr = itemArr[2];
					String[] spanArr = spanStr.split(",");
					int index1 = spanArr[0].indexOf("<");
					String begin = spanArr[0].substring(index1+1);
					int index2 = spanArr[1].indexOf(">");
					String end = spanArr[1].substring(0, index2);
				//	System.out.println("id-- " + id + " property-- " + property + " begin-- "+ begin + " end-- " + end);
					pts.put(property, new TimeSpan(begin, end));
				}else if(itemArr.length == 2){
					String property = itemArr[0];
					String spanStr = itemArr[1];
					String[] spanArr = spanStr.split(",");
					int index1 = spanArr[0].indexOf("<");
					String begin = spanArr[0].substring(index1+1);
					int index2 = spanArr[1].indexOf(">");
					String end = spanArr[1].substring(0, index2);
					//System.out.println(" property-- " + property + " begin-- "+ begin + " end-- " + end);
					pts.put(property, new TimeSpan(begin, end));
				}
			}else{
				//一旦遇到空白行则将前一三元组存入
				if(id != null && pts != null){
				classPTMap.put(id, pts);
				}
			}
		}
		System.out.println("-----------" + classPTMap.size() + "-----------");
		bufferedReader.close();
		return classPTMap;
	}
	
	public static void showInteractiveMatrix(HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix) throws IOException{
		       //结果写入文档
				String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-Interactive-Matrix.txt";
				FileWriter fileWriter = new FileWriter(dstFile);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				for (Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()) {
				   System.out.println(entry.getKey() + " ");
					bufferedWriter.write(entry.getKey() + " ");
					bufferedWriter.newLine();
					HashMap<String, TimeSpan> classInfo = entry.getValue();
					for(Entry<String, TimeSpan> entry2 : classInfo.entrySet()){
						System.out.print(entry2.getKey() + " -- " + entry2.getValue().toString() + "  ");
					bufferedWriter.write(entry2.getKey() + " -- " + entry2.getValue().toString());
					bufferedWriter.newLine();
				}
					System.out.println("\n----");
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
				bufferedWriter.close();
	}
	
    public static void main(String[] args) throws IOException{
    	HashMap<Integer, HashMap<String, TimeSpan>> classPTMap  = getClassPTMap("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-classPTMap.txt");
	    showInteractiveMatrix(getInteractiveMatrix(classPTMap));
    }
}
