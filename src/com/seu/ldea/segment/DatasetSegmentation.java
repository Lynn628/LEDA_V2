package com.seu.ldea.segment;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.commonutil.BuildFromFile;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.time.InteractiveMatrix;
import com.seu.ldea.time.LabelClassWithTimeStatic;
import com.seu.ldea.timeutil.TimeUtil;

public class DatasetSegmentation {
	// ÿ����Դ��id�Լ�Я����ʱ����Ϣ
		public HashMap<Integer, ResourceInfo> resourceTimeInfo;
		// ÿ��class��id�Լ������ʱ�����Ժ�ʱ��
		public HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix;
		// �ǰ���ʲôʱ�����зֵ�
		public static String gapSign;

		
		private DatasetSegmentation(HashMap<Integer,ResourceInfo> resourceTimeInfo,
				HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix) {
			super();
			this.resourceTimeInfo = resourceTimeInfo;
			this.interactiveMatrix = interactiveMatrix;
		}


		static class GapInfo {
			private String gapSign;
			private int gapNum;
			// ��ֹ������ֵ
			private int begin;
			private int end;

			public GapInfo(String gapSign, int gapNum, int begin, int end) {
				super();
				this.gapSign = gapSign;
				this.gapNum = gapNum;
				this.begin = begin;
				this.end = end;
			}

			public int getBegin() {
				return begin;
			}

			public int getEnd() {
				return end;
			}

			public int getGapNum() {
				return gapNum;
			}

			public String getGapSign() {
				return gapSign;
			}
		}

		/**
		 * ���ص���ʱ��Ƭ����Լ��ڴ˱�ŵĶ���set����
		 * 
		 * @param classSelected
		 * @param propertySelected
		 * @return
		 */
		public  LinkedHashMap<Integer, HashSet<Integer>> segmentDataSet(int classSelected, String propertySelected) {
			TimeSpan selectedTimeSpan = interactiveMatrix.get(classSelected).get(propertySelected);
			GapInfo gapInfo = getTimeGapInfo(selectedTimeSpan);
			System.out.println("selected time span " + selectedTimeSpan.getBegin() + " " + selectedTimeSpan.getEnd());
			// ʱ����Ƭ����Լ��ڴ���Ƭ����ѡclass�ĵ�ļ���
			//HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
	        LinkedHashMap<Integer, HashSet<Integer>> result = new LinkedHashMap<>();
			int gapNum = gapInfo.getGapNum();
			int begin = gapInfo.getBegin();
			// int end = gapInfo.getEnd();
			gapSign = gapInfo.getGapSign();
			HashMap<Integer, HashSet<Date>> candidateResources = searchResourceTimeInfo(classSelected, propertySelected);

			for (int i = 0; i <= gapNum; i++) {
				// �ض�ʱ�����ε�ʱ���
				HashSet<Integer> rIds = new HashSet<>();
				for (Entry<Integer, HashSet<Date>> candidate : candidateResources.entrySet()) {
					// һ����Դ�ڴ�ʱ�������ϵĶ��ʱ��ֵ
					for (Date date : candidate.getValue()) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						int current = 0;
						switch (gapSign) {
						case "year":
							current = calendar.get(Calendar.YEAR);
							break;
						case "month":
							current = calendar.get(Calendar.MONTH);
							break;
						case "day":
							current = calendar.get(Calendar.DAY_OF_MONTH);
							break;
						case "hour":
							current = calendar.get(Calendar.HOUR_OF_DAY);
						default:
							break;
						}
						// �жϵ�ǰʱ�����ĸ�ʱ��������
						if ((begin + i) <= current && current < (begin + i + 1))
							rIds.add(candidate.getKey());
					}
				}
				result.put(i, rIds);
			}
			System.out.println("--- GapSign ---" + gapSign);
			return result;
		}

		/**
		 * ��resourcetimeInfo�в�������ָ�����ָ��ʱ�����Ե�ʵ�壬��ʱ����Ϣ��Ч
		 * 
		 * @return ��������������ʵ���id ��ʱ��
		 */
		public HashMap<Integer, HashSet<Date>> searchResourceTimeInfo(int classSelected, String propertySelected) {
			HashMap<Integer, HashSet<Date>> resultMap = new HashMap<>();
			for (Entry<Integer, ResourceInfo> entry : resourceTimeInfo.entrySet()) {
				ResourceInfo resourceInfo = entry.getValue();
				int rId = entry.getKey();
				//System.out.println("before type");
				Integer type = resourceInfo.getType();
				//System.out.println("Type ----" + type);
				if (type != null && type == classSelected) {
					HashMap<String, HashSet<TimeSpan>> pts = entry.getValue().getPredicateTimeMap();
					HashSet<Date> resourceDates = new HashSet<>();
					for (Entry<String, HashSet<TimeSpan>> pt : pts.entrySet()) {
						if (pt.getKey().equals(propertySelected)) {
							HashSet<TimeSpan> spans = pt.getValue();
							for (TimeSpan span : spans) {
								String time = span.getBegin();
								Date dateTime = TimeUtil.formatTime(time);
								if (dateTime != null) {
									resourceDates.add(dateTime);
								}
							}
							if (!resourceDates.isEmpty()) {
								resultMap.put(rId, resourceDates);
							}
						}
					}
				}
			}
			return resultMap;
		}

		/**
		 * ��ȡ�з�ʱ����,�ж�������Ϊ�зֻ������»������з�
		 * 
		 * @param classSelected
		 * @param propertySelected
		 * @return
		 */
		public static GapInfo getTimeGapInfo(TimeSpan span) {
			String beginStr = span.getBegin();
			String endStr = span.getEnd();
			String beginHourStr = "";
			String endHourStr = "";
			// ���ڸ�ʽΪ//2011-03-31T10:30:00+0530
			int yearGap = 0;
			int monthGap = 0;
			int dayGap = 0;
			int hourGap = 0;
			System.out.println("begin Str " + beginStr);
			System.out.println("end str " + endStr);
			if (beginStr.contains("T")) {
				int index1 = beginStr.indexOf("T");
				// ��ȡСʱ��λ��
				int index2 = beginStr.indexOf(":");
				beginHourStr = beginStr.substring(index1 + 1, index2);
			    beginStr = beginStr.substring(0, index1);
				//System.out.println("beginStr 1 ===" + beginStr);
				//System.out.println("beginHourStr === " + beginHourStr);
			} 
			if(endStr.contains("T")){
				int index3 = endStr.indexOf("T");
				int index4 = endStr.indexOf(":");
				endHourStr = endStr.substring(index3 + 1, index4);
				endStr = endStr.substring(0, index3);
			}
			
			String[] beginArr = beginStr.split("-");
			String[] endArr = endStr.split("-");
			int beginYear = 0;
			int endYear = 0;
			if (!beginArr[0].equals("XX") && !endArr[0].equals("XX")) {
				beginYear = Integer.parseInt(beginArr[0]);
				endYear = Integer.parseInt(endArr[0]);
				yearGap = endYear - beginYear;
			}
			int beginMonth = 0;
			int endMonth = 0;
			if (!beginArr[1].equals("XX") && !endArr[1].equals("XX")) {
				beginMonth = Integer.parseInt(beginArr[1]);
				endMonth = Integer.parseInt(endArr[1]);
				monthGap = endMonth - beginMonth;
			}
			int beginDay = 0;
			int endDay = 0;
			if (!beginArr[2].equals("XX") && !endArr[1].equals("XX")) {
				beginDay = Integer.parseInt(beginArr[2]);
				endDay = Integer.parseInt(endArr[2]);
				dayGap = endDay - beginDay;
			}

			int beginHour = 0;
			int endHour = 0;
			if (beginHourStr != "" && endHourStr != "") {
				beginHour = Integer.parseInt(beginHourStr);
				endHour = Integer.parseInt(endHourStr);
				hourGap = endHour - beginDay;
			}
			if (yearGap != 0) {
				return new GapInfo("year", yearGap, beginYear, endYear);
			} else if (monthGap != 0) {
				return new GapInfo("month", monthGap, beginMonth, endMonth);
			} else if (dayGap != 0) {
				return new GapInfo("day", dayGap, beginDay, endDay);
			} else if (hourGap != 0) {
				return new GapInfo("hour", hourGap, beginHour, endHour);
			} else
				return null;

		}

		public static DatasetSegmentation initDataSegment(String path, String classPTName, String rescalInputDir) throws IOException, ParseException{
			HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
			LabelClassWithTimeStatic.resourceTimeInfo = noTypeResourceTimeInfo;
			HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTimeStatic.getClassTimeInformation(rescalInputDir);
			//��һ������ʡ��resource������������Ϣ
			HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTimeStatic.getClassTimeSpanInfo(classTimeInfo,
					"");
			//����������ϢresourceTimeInfo����dataset segment
			HashMap<Integer, ResourceInfo> typedresourceTimeInfo = LabelClassWithTimeStatic.resourceTimeInfo;
			System.out.println("typed resourceTimeInfo size>>> " + typedresourceTimeInfo.size() );
			HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
			DatasetSegmentation datasetSegmentation2 = new DatasetSegmentation(typedresourceTimeInfo, interactiveMatrix);
		    return datasetSegmentation2;
		}
		
		
		/**
		 * �����ֳ������ɵ�
		 * @param path,resource PT map�ļ�
		 * @param resourceTimeInfo,ʱ��ʵ�屻��������͵�timeInfo����Ҫ���� LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
					classPTName);
			   //����������ϢresourceTimeInfo����dataset segment
			     HashMap<Integer, ResourceInfo> resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		 * @param path2,classPTMap �ļ�
		 * @param rescalInputDir
		 * @return
		 * @throws IOException
		 * @throws ParseException
		 */
		public static DatasetSegmentation initDataSegment(String path,HashMap<Integer, ResourceInfo> typedresourceTimeInfo, String path2, String rescalInputDir) throws IOException, ParseException{
			HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
			LabelClassWithTimeStatic.resourceTimeInfo = noTypeResourceTimeInfo;
			//��һ������ʡ��resource������������Ϣ
			HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = BuildFromFile.getClassPTMap(path2);
			//����������ϢresourceTimeInfo����dataset segment
			HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
			DatasetSegmentation datasetSegmentation2 = new DatasetSegmentation(typedresourceTimeInfo, interactiveMatrix);
		    return datasetSegmentation2;
		}

		public static void main(String[] args) throws IOException, ParseException {
	
			String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
			String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
			String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
			String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
			
			LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation.initDataSegment(path, " ", rescalInputDir).segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
	         for(Entry<Integer, HashSet<Integer>> entry : timeEntitySlices.entrySet()){
	        	// System.out.println(" Slice #-- " + entry.getKey() + " size " + entry.getValue().size());
	        	 for(Integer item : entry.getValue()){
	        		 System.out.print(item + " ");
	        	 }
	        	 System.out.println("\n");
	           }
		}
}
