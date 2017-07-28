package com.seu.ldea.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.util.BuildFromFile;
import com.seu.ldea.util.TimeUtil;


public class LabelClassWithTime2 {
	// 每个资源的id以及携带的时间信息
		public HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	   
		
		public LabelClassWithTime2(HashMap<Integer, ResourceInfo> resourceTimeInfo) {
		super();
		this.resourceTimeInfo = resourceTimeInfo;
	}

		public void setResourceTimeInfo(HashMap<Integer, ResourceInfo> resourceTimeInfo) {
			this.resourceTimeInfo = resourceTimeInfo;
		}
		
		public HashMap<Integer, ResourceInfo> getResourceTimeInfo() {
			return resourceTimeInfo;
		}
		
		/**
		 * 
		 * @param dir,rescal输入文件目录
		 * @param resultFileName, classPTMap的文件名
		 * @return
		 * @throws IOException
		 * @throws ParseException
		 */
		public HashMap<Integer, ResourceInfo> getClassTimeInformation(String dir) throws IOException, ParseException {
			// 存储具有时间信息的类以及其时间标签
			HashMap<Integer, ResourceInfo> classTimeInfo = new HashMap<>();
			String wordsFile = dir + "\\words";
			FileReader fileReader = new FileReader(wordsFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			//获取本数据集的所有类
			HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
			HashMap<Integer, String> datasetAllClass = new HashMap<>();
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
				//收集本数据集的类
				datasetAllClass.put(classId, resourceURI.get(classId));
				// 在贴有时间标签的Entity set中查找是否有此entity id的时间标签
				ResourceInfo resourceInfo = resourceTimeInfo.get(entityId);
				if (classTimeInfo.containsKey(classId)) {
					if (resourceInfo != null) {
						// 给资源信息标上类别
						resourceInfo.setType(classId);
						ResourceInfo classInfo = classTimeInfo.get(classId);
						HashMap<String, HashSet<TimeSpan>> cPts = classInfo.getPredicateTimeMap();
						HashMap<String, HashSet<TimeSpan>> rPts = resourceInfo.getPredicateTimeMap();
						for(Entry<String, HashSet<TimeSpan>> pt : rPts.entrySet()){
							String property = pt.getKey(); 
							if(cPts.containsKey(property)){
								HashSet<TimeSpan> spans = pt.getValue();
								for(TimeSpan span : spans){
									//若class在此property上不包含时间信息
								  if(!cPts.get(property).contains(span)){
									cPts.get(property).add(span);
								}
							}
						}
					  }	//classTimeInfo.get(classId).add(resourceInfo);
					}
				} else {
					if (resourceInfo != null) {
					ResourceInfo classInfo = new ResourceInfo(classId);
					//此时间实体的pts对
					HashMap<String, HashSet<TimeSpan>> rPts = resourceInfo.getPredicateTimeMap();
	               //初始创建class的resourceinfo则直接将实体的pts当做class的Pts
					classInfo.setPredicateTimeMap(rPts);    					
					classTimeInfo.put(classId, classInfo);
					}
				}
			}
		
		//getClassTimeSpanInfo(classTimeInfo , resultFileName);
			//写类
			String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\classURI.txt";
			FileWriter fileWriter = new FileWriter(dstFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (Entry<Integer, String> entry : datasetAllClass.entrySet()) {
				bufferedWriter.write(entry.getKey() + " ");
				String uri = entry.getValue();
				bufferedWriter.write(uri);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		/*String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-class-time-set.txt";
			FileWriter fileWriter = new FileWriter(dstFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (Entry<Integer, ResourceInfo> entry : classTimeInfo.entrySet()) {
				bufferedWriter.write(entry.getKey() + " ");
				ResourceInfo classInfo = entry.getValue();
				bufferedWriter.write(classInfo.toString());
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.close();*/
			//return classTimeLabels;
			return  classTimeInfo;
		}
		
		/**
		 * 获取每个class以及class 包含的时间属性上面的时间区间
		 * @param classTimeLabels
		 * @param resultFileName
		 * @return
		 * @throws ParseException
		 * @throws IOException
		 */
		public HashMap<Integer, HashMap<String, TimeSpan>>  getClassTimeSpanInfo(HashMap<Integer, ResourceInfo> classTimeInfo , String resultFileName) throws ParseException, IOException{
			//存储每个class的时间属性以及该属性上的时间区间
			HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = new HashMap<>();
			//遍历每一个class
			for(Entry<Integer, ResourceInfo> entry : classTimeInfo.entrySet()){
				Integer classId = entry.getKey();
				ResourceInfo resourceInfo = entry.getValue();
				HashMap<String, HashSet<TimeSpan>> ptPairs = resourceInfo.getPredicateTimeMap();
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
	              //得到的起止时间都不为空
				if(!classBeginTime.equals("") && !classEndTime.equals("")){		
					//此class在此Property上面的时间区间
				TimeSpan classTimeSpanOnAProperty = new TimeSpan(classBeginTime, classEndTime);
				if(classPTMap.containsKey(classId)){
					HashMap<String, TimeSpan> pt = classPTMap.get(classId);
					if(!pt.containsKey(property)){
						pt.put(property, classTimeSpanOnAProperty);
			 	 }
			  }else{
				  HashMap<String, TimeSpan> pt = new HashMap<>();
				  pt.put(property, classTimeSpanOnAProperty);
				  classPTMap.put(classId, pt);
			   }
			 }
		   }
		}
			//结果写入文档
			String dstFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\" + resultFileName + ".txt";
			FileWriter fileWriter = new FileWriter(dstFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (Entry<Integer, HashMap<String, TimeSpan>> entry : classPTMap.entrySet()) {
				bufferedWriter.write(entry.getKey() + " ");
				HashMap<String, TimeSpan> classInfo = entry.getValue();
				for(Entry<String, TimeSpan> entry2 : classInfo.entrySet()){
				bufferedWriter.write(entry2.getKey() + " " + entry2.getValue().toString());
				bufferedWriter.newLine();
			}
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.close();
			return classPTMap;
		}
		
       
		public static void main(String[] args) throws IOException, ParseException {
			long t1 = System.currentTimeMillis();
		/*	Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/SWCC.org", "dba", "dba");
			resourceTimeInfo = LabelResourceWithTimeTest2.timeExtraction(dataset, "LinkedMDB2-ResourcePTMap0709-1",
					"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\LinkedMDB2");*/
			String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
			HashMap<Integer, ResourceInfo> resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
			LabelClassWithTime2 labelClass = new LabelClassWithTime2(resourceTimeInfo);
			System.out.println("resourceTimeInfoSize in Main ---" + resourceTimeInfo.size());
			HashMap<Integer, ResourceInfo> classTimeInfo = labelClass.getClassTimeInformation("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo");
			labelClass.getClassTimeSpanInfo(classTimeInfo,"Jamendo-ClassPTMap-0724");
			long t2 = System.currentTimeMillis();
			double timeCost = (t2 - t1) / 1000.0;
			System.out.println("End of main~~~~~~time cost " + timeCost + "s");
		}
}
