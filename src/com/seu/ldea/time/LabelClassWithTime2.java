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
	// ÿ����Դ��id�Լ�Я����ʱ����Ϣ
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
		 * @param dir,rescal�����ļ�Ŀ¼
		 * @param resultFileName, classPTMap���ļ���
		 * @return
		 * @throws IOException
		 * @throws ParseException
		 */
		public HashMap<Integer, ResourceInfo> getClassTimeInformation(String dir) throws IOException, ParseException {
			// �洢����ʱ����Ϣ�����Լ���ʱ���ǩ
			HashMap<Integer, ResourceInfo> classTimeInfo = new HashMap<>();
			String wordsFile = dir + "\\words";
			FileReader fileReader = new FileReader(wordsFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			//��ȡ�����ݼ���������
			HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
			HashMap<Integer, String> datasetAllClass = new HashMap<>();
			// type��uri���
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
			// �����ļ�
			String colFile = dir + "\\" + num + "-cols";
			// �����ļ�
			String rowFile = dir + "\\" + num + "-rows";
			FileReader fr1 = new FileReader(colFile);
			FileReader fr2 = new FileReader(rowFile);
			BufferedReader br1 = new BufferedReader(fr1);
			BufferedReader br2 = new BufferedReader(fr2);
			String bString1 = br1.readLine();
			String bString2 = br2.readLine();
			br1.close();
			br2.close();
			// �洢��
			String[] bArr1 = bString1.split(" ");
			// �洢Entity
			String[] bArr2 = bString2.split(" ");
			bufferedReader.close();
			
			System.out.println("resourceTimeInfoSize ---" + resourceTimeInfo.size());
			for (int i = 0; i < bArr1.length; i++) {
				System.out.println("entity id is " + bArr2[i] + " class id is " + bArr1[i]);
				int entityId = Integer.parseInt(bArr2[i]);
				int classId = Integer.parseInt(bArr1[i]);
				//�ռ������ݼ�����
				datasetAllClass.put(classId, resourceURI.get(classId));
				// ������ʱ���ǩ��Entity set�в����Ƿ��д�entity id��ʱ���ǩ
				ResourceInfo resourceInfo = resourceTimeInfo.get(entityId);
				if (classTimeInfo.containsKey(classId)) {
					if (resourceInfo != null) {
						// ����Դ��Ϣ�������
						resourceInfo.setType(classId);
						ResourceInfo classInfo = classTimeInfo.get(classId);
						HashMap<String, HashSet<TimeSpan>> cPts = classInfo.getPredicateTimeMap();
						HashMap<String, HashSet<TimeSpan>> rPts = resourceInfo.getPredicateTimeMap();
						for(Entry<String, HashSet<TimeSpan>> pt : rPts.entrySet()){
							String property = pt.getKey(); 
							if(cPts.containsKey(property)){
								HashSet<TimeSpan> spans = pt.getValue();
								for(TimeSpan span : spans){
									//��class�ڴ�property�ϲ�����ʱ����Ϣ
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
					//��ʱ��ʵ���pts��
					HashMap<String, HashSet<TimeSpan>> rPts = resourceInfo.getPredicateTimeMap();
	               //��ʼ����class��resourceinfo��ֱ�ӽ�ʵ���pts����class��Pts
					classInfo.setPredicateTimeMap(rPts);    					
					classTimeInfo.put(classId, classInfo);
					}
				}
			}
		
		//getClassTimeSpanInfo(classTimeInfo , resultFileName);
			//д��
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
		 * ��ȡÿ��class�Լ�class ������ʱ�����������ʱ������
		 * @param classTimeLabels
		 * @param resultFileName
		 * @return
		 * @throws ParseException
		 * @throws IOException
		 */
		public HashMap<Integer, HashMap<String, TimeSpan>>  getClassTimeSpanInfo(HashMap<Integer, ResourceInfo> classTimeInfo , String resultFileName) throws ParseException, IOException{
			//�洢ÿ��class��ʱ�������Լ��������ϵ�ʱ������
			HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = new HashMap<>();
			//����ÿһ��class
			for(Entry<Integer, ResourceInfo> entry : classTimeInfo.entrySet()){
				Integer classId = entry.getKey();
				ResourceInfo resourceInfo = entry.getValue();
				HashMap<String, HashSet<TimeSpan>> ptPairs = resourceInfo.getPredicateTimeMap();
				//��ȡ��class��ÿһ��pt pair
				for(Entry<String, HashSet<TimeSpan>> entry2 : ptPairs.entrySet()){
					String classBeginTime = "";
					String classEndTime = "";
					String property = entry2.getKey();
					//�ϲ���p�����time
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
	              //�õ�����ֹʱ�䶼��Ϊ��
				if(!classBeginTime.equals("") && !classEndTime.equals("")){		
					//��class�ڴ�Property�����ʱ������
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
			//���д���ĵ�
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
