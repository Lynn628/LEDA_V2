package com.seu.ldea.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.seu.ldea.cluster.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.util.TimeUtil;

/**
 * 汇总实体的时间信息，构建class的时间标签
 * 
 * @author Lynn 思路，words中找到type的id，依据id，读取对应的col和row文件，找到具有时间信息的资源所属的类别，
 *         汇总同一时间属性下面的时间
 */
public class LabelClassWithTime {
	// 每个资源的id以及携带的时间信息
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
    
	public static HashMap<Integer, ResourceInfo> getClassTimeInformation(String dir) throws IOException, ParseException {
		// 存储具有时间信息的类以及其时间标签
		HashMap<Integer, ArrayList<ResourceInfo>> classTimeInfo = new HashMap<>();
		String wordsFile = dir + "\\words";
		FileReader fileReader = new FileReader(wordsFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		// type的uri编号
		int num = 0;
		while ((line = bufferedReader.readLine()) != null) {
			int index = line.indexOf(":");
			String subStr1 = line.substring(0, index);
			String subStr2 = line.substring(index + 1);
			if (subStr2.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				num = Integer.parseInt(subStr1);
				break;
			}
		}
		// 宾语文件
		String colFile = dir + "\\" + num + "-cols";
		// 主语文件
		String rowFile = dir + "\\" + num + "-rows";
		FileReader fr1 = new FileReader(colFile);
		FileReader fr2 = new FileReader(rowFile);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String bString1 = br1.readLine();
		String bString2 = br2.readLine();
		br1.close();
		br2.close();
		// 存储类
		String[] bArr1 = bString1.split(" ");
		// 存储Entity
		String[] bArr2 = bString2.split(" ");
		bufferedReader.close();
		
		System.out.println("resourceTimeInfoSize ---" + resourceTimeInfo.size());
		for (int i = 0; i < bArr1.length; i++) {
			System.out.println("entity id is " + bArr2[i] + " class id is " + bArr1[i]);
			int entityId = Integer.parseInt(bArr2[i]);
			int classId = Integer.parseInt(bArr1[i]);
			// 在贴有时间标签的Entity set中查找是否有此entity id的时间标签
			ResourceInfo resourceInfo = resourceTimeInfo.get(entityId);
			if (classTimeInfo.containsKey(classId)) {
				/* System.out.println(resourceInfo.); */
				if (resourceInfo != null) {
					// 给资源信息标上类别
					resourceInfo.setType(classId);
					classTimeInfo.get(classId).add(resourceInfo);
				}
			} else {
				ArrayList<ResourceInfo> list = new ArrayList<ResourceInfo>();
				if (resourceInfo != null) {
					list.add(resourceInfo);
					classTimeInfo.put(classId, list);
				}
				// 若此类所有成员都没有时间标签，则该类不该放入classTimeInfo中
				/*
				 * else { classTimeInfo.put(classId, list); }
				 */
			}
		}
		// 存储合并后的<class, <p1 , timeSpan1>, <p2, timeSpan2>>
		HashMap<Integer, ResourceInfo> classTimeLabels = new HashMap<>();
		for (Entry<Integer, ArrayList<ResourceInfo>> entry : classTimeInfo.entrySet()) {
			int classId = entry.getKey();
			// 给class创建时间信息标签
			ResourceInfo aclassInfo = new ResourceInfo(classId);
			HashMap<String, HashSet<TimeSpan>> classtimePair = new HashMap<>();
			Iterator<ResourceInfo> iterator = entry.getValue().iterator();
			// 遍历此class所有的时间标签
			while (iterator.hasNext()) {
				// 此class收录的其中一个时间标签
				ResourceInfo resourceInfo = iterator.next();
				HashMap<String, HashSet<TimeSpan>> pTPairs = resourceInfo.getPredicateTimePair();
				// 遍历此时间标签的所有pt对
				for (Entry<String, HashSet<TimeSpan>> entry2 : pTPairs.entrySet()) {
					String property = entry2.getKey();
					HashSet<TimeSpan> resourceTimeSpans = entry2.getValue();
					if (classtimePair.containsKey(property)) {
						for (TimeSpan span : resourceTimeSpans) {
							classtimePair.get(property).add(span);
						}
					
					} else {
						classtimePair.put(entry2.getKey(), entry2.getValue());
					}
				}
			}
			aclassInfo.setTimeInfoPair(classtimePair);
			classTimeLabels.put(classId, aclassInfo);
		}
		
		rearrangeClassTimeLabels(classTimeLabels);
	/*	String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\labelclasstest3.txt";
		FileWriter fileWriter = new FileWriter(dstFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<Integer, ResourceInfo> entry : classTimeLabels.entrySet()) {
			bufferedWriter.write(entry.getKey() + " ");
			ResourceInfo classInfo = entry.getValue();
			bufferedWriter.write(classInfo.toString());
			bufferedWriter.newLine();
			bufferedWriter.flush();
		}
		bufferedWriter.close();*/
		return classTimeLabels;
	}
	
	public static HashMap<Integer, HashMap<String, TimeSpan>>  rearrangeClassTimeLabels(HashMap<Integer, ResourceInfo> classTimeLabels) throws ParseException, IOException{
		HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix = new HashMap<>();
		//遍历每一个class
		for(Entry<Integer, ResourceInfo> entry : classTimeLabels.entrySet()){
			Integer classId = entry.getKey();
			ResourceInfo resourceInfo = entry.getValue();
			HashMap<String, HashSet<TimeSpan>> ptPairs = resourceInfo.getPredicateTimePair();
			//获取此class的每一个pt pair
			for(Entry<String, HashSet<TimeSpan>> entry2 : ptPairs.entrySet()){
				String classBeginTime = "";
				String classEndTime = "";
				String property = entry2.getKey();
				//合并此p上面的time
				    HashSet<TimeSpan> timeSpans = entry2.getValue();
				    String minStr = "3000";
				    String maxStr = "1900";
				    DateFormat format = new SimpleDateFormat("yyyy");
				    Date timeMin = format.parse(minStr);
                    Date timeMax = format.parse(maxStr);	
                    
					for(TimeSpan span : timeSpans){
						String beginStr = span.getBegin();
						String endStr = span.getEnd();
						Date dateBegin = TimeUtil.formatTime(beginStr);
						Date dateEnd = TimeUtil.formatTime(endStr);
						if(dateBegin != null){
						 if(dateBegin.compareTo(timeMin) == -1){
							timeMin = dateBegin;
							System.out.println("timeMin is --- " + timeMin);
							classBeginTime = beginStr;
						 }
						}
						if(dateEnd != null){
						 if(dateEnd.compareTo(timeMax) == 1){
							timeMax = dateEnd;
							classEndTime = endStr;
							System.out.println("timeMax is --- " + timeMax);
						 }
						}
					}
					//此class在此Property上面的时间区间
			TimeSpan classTimeSpanOnAProperty = new TimeSpan(classBeginTime, classEndTime);
			if(interactiveMatrix.containsKey(classId)){
				HashMap<String, TimeSpan> pt = interactiveMatrix.get(classId);
				if(!pt.containsKey(property)){
					pt.put(property, classTimeSpanOnAProperty);
		 	 }
		  }else{
			  HashMap<String, TimeSpan> pt = new HashMap<>();
			  pt.put(property, classTimeSpanOnAProperty);
			  interactiveMatrix.put(classId, pt);
		  }
		}
	  }
		//结果写入文档
		String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-Interactive-Matrix2.txt";
		FileWriter fileWriter = new FileWriter(dstFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<Integer, HashMap<String, TimeSpan>> entry : interactiveMatrix.entrySet()) {
			bufferedWriter.write(entry.getKey() + " ");
			HashMap<String, TimeSpan> classInfo = entry.getValue();
			for(Entry<String, TimeSpan> entry2 : classInfo.entrySet()){
			bufferedWriter.write(entry2.getKey() + " -- " + entry2.getValue().toString());
			bufferedWriter.newLine();
		}
			bufferedWriter.newLine();
			bufferedWriter.flush();
		}
		bufferedWriter.close();
		return interactiveMatrix;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/SWCC.org", "dba", "dba");
		resourceTimeInfo = LabelResourceWithTimeTest.timeExtraction(dataset, "SWCC-Class-ExtractionEstimation4",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		System.out.println("resourceTimeInfoSize in Main ---" + resourceTimeInfo.size());
		getClassTimeInformation("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		long t2 = System.currentTimeMillis();
		double timeCost = (t2 - t1) / 1000.0;
		System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
}
