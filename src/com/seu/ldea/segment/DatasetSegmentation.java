package com.seu.ldea.segment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.oldVersion.InteractiveMatrix_old;
import com.seu.ldea.time.LabelClassWithTime;
import com.seu.ldea.util.TimeUtil;


public class DatasetSegmentation {
	// ÿ����Դ��id�Լ�Я����ʱ����Ϣ
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo;
	//ÿ��class��id�Լ������ʱ�����Ժ�ʱ��
	public static HashMap<Integer, HashMap<String, TimeSpan>> interactiveMatrix; 
	//�ǰ���ʲôʱ�����зֵ�
	public static String gapSign;

	static class GapInfo{
		private String gapSign;
		private int gapNum;
		//��ֹ������ֵ
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
	 * @param classSelected
	 * @param propertySelected
	 * @return
	 */
	public static HashMap<Integer, HashSet<Integer>>  segmentDataSet(int classSelected, String propertySelected){
		TimeSpan selectedTimeSpan = interactiveMatrix.get(classSelected).get(propertySelected);
		GapInfo gapInfo = getTimeGapInfo(selectedTimeSpan);
		//ʱ����Ƭ����Լ��ڴ���Ƭ����ѡclass�ĵ�ļ���
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		int gapNum = gapInfo.getGapNum();
		int begin = gapInfo.getBegin();
		//int end = gapInfo.getEnd();
		gapSign = gapInfo.getGapSign();
		HashMap<Integer, HashSet<Date>> candidateResources = searchResourceTimeInfo(classSelected, propertySelected);
         
		for(int i = 0; i<= gapNum; i++){
			//�ض�ʱ�����ε�ʱ���
			HashSet<Integer> rIds = new HashSet<>();
	     for(Entry<Integer, HashSet<Date>> candidate : candidateResources.entrySet()){
			 //һ����Դ�ڴ�ʱ�������ϵĶ��ʱ��ֵ
			 for(Date date : candidate.getValue()){
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
				default:
					break;
				}
				//�жϵ�ǰʱ�����ĸ�ʱ��������
				if((begin + i) <= current && current < (begin + i + 1))
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
	 * @return ��������������ʵ���id ��ʱ��
	 */
	public static HashMap<Integer, HashSet<Date>> searchResourceTimeInfo(int classSelected, String propertySelected){
		HashMap<Integer, HashSet<Date>> resultMap = new HashMap<>();
		for(Entry<Integer, ResourceInfo> entry : resourceTimeInfo.entrySet()){
        ResourceInfo resourceInfo =	entry.getValue();
        int rId = entry.getKey();
       System.out.println("before type");
        Integer type = resourceInfo.getType();
        System.out.println("Type ----" + type );
        if(type != null && type == classSelected){
        	HashMap<String, HashSet<TimeSpan>> pts = entry.getValue().getPredicateTimeMap();
        	HashSet<Date> resourceDates = new HashSet<>();
        	for(Entry<String, HashSet<TimeSpan>> pt : pts.entrySet()){
        		if(pt.getKey().equals(propertySelected)){
        			HashSet<TimeSpan> spans = pt.getValue();
        			for(TimeSpan span : spans){
        				String time = span.getBegin();
        				Date dateTime = TimeUtil.formatTime(time);
        				if(dateTime != null){
        					resourceDates.add(dateTime);
        				}
        			}
        		  if(!resourceDates.isEmpty()){
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
	 * @param classSelected
	 * @param propertySelected
	 * @return
	 */
	public static GapInfo getTimeGapInfo(TimeSpan span){
		String beginStr = span.getBegin();
		String endStr = span.getEnd();
	   
	    //���ڸ�ʽΪ//2011-03-31T10:30:00+0530
	    int yearGap = 0;
	    int monthGap = 0;
	    int dayGap = 0;
	    if(beginStr.contains("T")){
	    	int index1 = beginStr.indexOf("T");
	    	beginStr = beginStr.substring(0, index1);
	    	int index2 = endStr.indexOf("T");
	    	endStr = endStr.substring(0, index2);
	    }
	    	String[] beginArr = beginStr.split("-");
	    	String[] endArr = endStr.split("-");
	    	int beginYear = 0;
	    	int endYear = 0;
	    	if(!beginArr[0].equals("XX") && !endArr[0].equals("XX")){
	    		 beginYear = Integer.parseInt(beginArr[0]);
	    		 endYear = Integer.parseInt(endArr[0]);
                yearGap = endYear - beginYear;	    		
	    	}
	    	int beginMonth = 0;
	    	int endMonth = 0;
	    	if(!beginArr[1].equals("XX") && !endArr[1].equals("XX")){
	    		 beginMonth = Integer.parseInt(beginArr[1]);
	    		 endMonth = Integer.parseInt(endArr[1]);
	    		monthGap = endMonth - beginMonth;
	    	}
	    	int beginDay = 0;
	    	int endDay = 0;
	    	if(!beginArr[2].equals("XX") && !endArr[1].equals("XX")){
	    		 beginDay = Integer.parseInt(beginArr[2]);
	    		 endDay = Integer.parseInt(endArr[2]);
	    		dayGap = endDay - beginDay;
	    	} 	
	    	
	    	if(yearGap != 0){
	    		return new GapInfo("year", yearGap, beginYear, endYear );
	    	}else if(monthGap != 0){
	    		return new GapInfo("month", monthGap, beginMonth, endMonth);
	    	}else if(dayGap != 0){
	    		return new GapInfo("day", dayGap, beginDay, endDay);
	    	}else return null;
	    	
	}  	
	
	//���ĵ��й���resourceTimeInfo,resourceֻ��ʱ����Ϣ��û�б���type -->����type
	
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
	    	 //��ÿһ��rid�ҵ���type
	    	 
	    	// resourceInfo.setType(type);
	    	 resourceInfo.setPredicateTimeMap(pts);
	    	 result.put(rId, resourceInfo);
	    	// System.out.println("resourceInfo" + resourceInfo.toString());
	    }
		
		return result;
	}
	
	public static void main(String[] args) throws IOException, ParseException{
		//System.out.println(getTimeGapSign(new TimeSpan("2012-05-28T18:30:00","2013-10-12T10:00:00")));
		/*String line = "0: http://data.semanticweb.org/conference/dc/2010 < createdDate, <2010-XX-XX,2010-XX-XX>; > < http://www.w3.org/2002/12/cal/icaltzd#dtend, <2010-10-22,2010-10-22>; > < http://www.w3.org/2002/12/cal/icaltzd#dtstart, <2010-10-20,2010-10-20>; > ";
	  	 String[] testArr = line.split(" < ");
    	 int rId = Integer.parseInt(testArr[0].split(": ")[0]);
    	 HashMap<String, HashSet<TimeSpan>> pts = new HashMap<>();
    	 for(int i = 1; i < testArr.length; i++){
    		 String[] ptArr = testArr[i].split(", ");
    		 String property = ptArr[0];
    		 System.out.println("property -- " + property);
    		 String[] spans = ptArr[1].split("; ");
    		 System.out.println("spans size -- " + spans.length);
    		 HashSet<TimeSpan> spanSet = new HashSet<>();
    	
    		 for(int j = 0; j < spans.length -1; j++){
    			 System.out.println("spans[j] -- " + spans[j]);
    			 int index1 = spans[j].indexOf("<");
    			 int index2 = spans[j].indexOf(",");
    			 String timeStr = spans[j].substring(index1 +1 , index2);
    			 spanSet.add(new TimeSpan(timeStr, timeStr));
    			 System.out.println("timeStr -- " + timeStr);
    		 }
    	 }*/
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCCresult\\SWCCTimeTest3.txt";
		 HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = getResourceTimeInfo(path);
		 LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		 LabelClassWithTime.getClassTimeInformation("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2", " ");
		resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		interactiveMatrix = InteractiveMatrix_old.getInteractiveMatrix(InteractiveMatrix_old.getClassPTMap("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-Interactive-Matrix2.txt"));
	    HashMap<Integer,HashSet<Integer>> slices = segmentDataSet(7534 , "http://www.w3.org/2002/12/cal/ical#dtstart");
	    for(Entry<Integer, HashSet<Integer>> slice :slices.entrySet()){
	    	System.out.println(slice.getKey() + " -- ");
	    	for(Integer rId : slice.getValue()){
	    		System.out.print(rId + " ");
	    	}
	    	System.out.println();
	    }
	}
}
