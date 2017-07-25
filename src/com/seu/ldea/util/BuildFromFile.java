package com.seu.ldea.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;

/**
 * �����࣬���м����ļ�����һЩʹ�õ�����
 * @author Lynn
 *
 */
public class BuildFromFile {
	
	/**
	 * ���ĵ��й���resourceTimeInfo,resourceֻ��ʱ����Ϣ����Դû�б���type��Ϣ
	 * @param path
	 * @return
	 * @throws IOException
	 */
		public static HashMap<Integer, ResourceInfo> getResourceTimeInfo(String path) throws IOException{
			HashMap<Integer, ResourceInfo> result = new HashMap<>();
			FileReader fReader = new FileReader(path);
		    BufferedReader bufferedReader = new BufferedReader(fReader);
		    String line = "";
		    while((line = bufferedReader.readLine()) != null){
		    	//String[] 
		    	 String[] testArr = line.split(" < ");
		    	 int rId = Integer.parseInt(testArr[0].split(": ")[0]);
		    	 ResourceInfo resourceInfo = new ResourceInfo(rId);
		    	 HashMap<String, HashSet<TimeSpan>> pts = new HashMap<>();
		    	 for(int i = 1; i < testArr.length; i++){
		    		 String[] ptArr = testArr[i].split(", ");
		    		 String property = ptArr[0];
		    		 String[] spans = ptArr[1].split("; ");
		    		 HashSet<TimeSpan> spanSet = new HashSet<>();
		    		 for(int j = 0; j < spans.length-1; j++){
		    			 int index1 = spans[j].indexOf("<");
		    			 int index2 = spans[j].indexOf(",");
		    			 String timeStr = spans[j].substring(index1 +1 , index2);
		    			 spanSet.add(new TimeSpan(timeStr, timeStr));
		    			// System.out.println("--rid--" + rId +"--property--" + property + "-- timeStr --" + timeStr);
		    		 }
		    		 
		    		 pts.put(property, spanSet);
		    		// System.out.println("spanSet size **** " + spanSet.size());
		    	 }
		    	// resourceInfo.setType(type);
		    	 resourceInfo.setPredicateTimeMap(pts);
		    	 result.put(rId, resourceInfo);
		    }
		    bufferedReader.close();
			
			return result;
		}
		
		
		/**
		 * ��ȡ�ļ�����classPTMap, PTMap��classʱ����Ϣ
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
			//��class��Ӧ��hashmap
			HashMap<String, TimeSpan> pts = null;
			while((line = bufferedReader.readLine()) != null){
				if(!line.equals("")){
					String[] itemArr = line.split(" ");
					if(itemArr.length == 3){
						//������ǰclass��pts map
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
					//һ�������հ�����ǰһ��Ԫ�����
					if(id != null && pts != null){
					classPTMap.put(id, pts);
					}
				}
			}
		//	System.out.println("-----------" + classPTMap.size() + "-----------");
			bufferedReader.close();
			return classPTMap;
		}
	
		//���ļ���ȡtypedResourceTimeInfo���ο�LabelClassWithTimeд����ļ���ʽ
		
		
		
		/**
		 * ���ļ��л�ȡ����ʵ��֮�����������
		 */
		
		public HashMap<Integer, HashMap<Integer, BigDecimal>> getEntityVectorDistanceFromFile(String filePath){
			HashMap<Integer, HashMap<Integer, BigDecimal>> result = new HashMap<>();
			
			
			
			return result;
		}
}
