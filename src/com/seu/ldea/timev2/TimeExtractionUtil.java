package com.seu.ldea.timev2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.time.LabelResourceWithTime;

import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class TimeExtractionUtil {
	/**
	 * �ж�ʱ����Ϣ��ռ�ı���,����ռ������ʱ����Ϣ�ı�׼��ʾ��ʽ
	 * 
	 * @param uri
	 * @return
	 */
	public static String getTimeInLiteral(List<CoreMap> list, String uri) {
		    double maxPercentage = 0;
		    String timeInfo = "";
		    String result = "";
			// ��ʶ�������ʱ����Ϣ�뵱ǰν����<p, timeSpan>����Դ��
			for (CoreMap cm : list) {
				double percentage = cm.toString().length()/(uri.length()*1.0);
				if(maxPercentage < percentage){
					maxPercentage = percentage;
					timeInfo = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
				}
			}
			//������ֵΪ0.5
			if(maxPercentage > 0.5){
				result = timeInfo;
			}else if(uri.contains("http://www.w3.org/2001/XMLSchema#date")){
				result = timeInfo;
				//���DBLP��δʶ����������ӵ��޸�
			}else if(uri.contains("http://www.w3.org/2001/XMLSchema#gYear")){
				result = timeInfo;
			}
			System.out.println("%----- " + maxPercentage);
			return result;
	}

	public HashMap<Integer, String> reverseMap(HashMap<String, Integer> resourceMap) {
		HashMap<Integer, String> reversedMap = new HashMap<>();
		Iterator<Entry<String, Integer>> iterator = resourceMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			Integer value = entry.getValue();
			reversedMap.put(value, key);
		}
		return reversedMap;
	}

	/**
	 * ����virtuosofilebuild���ɵ�entity-id�ļ���words�ļ�����ȡentity��predicate��Ӧ��ID
	 * 
	 * @param type
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, Integer> getNodeIdMap(int type, String dir) throws IOException {
		HashMap<String, Integer> nodeMap = new HashMap<>();
		String path = "";
		if (type == 1) {
			path = dir + "\\entity-ids";
		} else if (type == 2) {
			path = dir + "\\words";
		}
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ((line = br.readLine()) != null) {
			// String[] lineArr = line.split(":");
			int index = line.indexOf(":");
			String subStr1 = line.substring(0, index);
			String subStr2 = line.substring(index + 1);
			nodeMap.put(subStr2, Integer.parseInt(subStr1));
			// System.out.println(subStr2 + "---" + subStr1);
		}
		br.close();
		return nodeMap;
	}

	// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\conferences\eswc-2012-complete.rdf
	/**
	 * ������Ԫ���г�ȡ��ʱ����Ϣ�������ǰ��Դ����ʱ���ǩ
	 * 
	 * @param nodeId
	 * @param pString
	 * @param isSelf:�ж��Ƿ��ǵ�ǰ��Դ����Я����
	 */
	public static void labelResource(Integer nodeId, String pString, String time,HashMap<Integer, ResourceInfo> resourceTimeInfo) {
		// predicate�ʹ�predicate�ϵ�ʱ������Map
		HashMap<String, HashSet<TimeSpan>> predicateTimePairs = resourceTimeInfo.get(nodeId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(time, time);
		// �жϵ�ǰresource�Ƿ����ڸ�ʱ���������б�ǩ������У������ʱ����Ϣ
		if (predicateTimePairs.containsKey(pString)) {
			HashSet<TimeSpan> timeSpanSet = predicateTimePairs.get(pString);
			timeSpanSet.add(span);
		} else {
			HashSet<TimeSpan> timeSpanSet = new HashSet<>();
			timeSpanSet.add(span);
			// ��resource����ʱ����Ϣ��ǩ
			predicateTimePairs.put(pString, timeSpanSet);
		}
	}

	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/SWCC.org", "dba", "dba");
		LabelResourceWithTime labelResource = new LabelResourceWithTime();
		labelResource.timeExtraction(dataset, "SWCCTimeTest4",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		long t2 = System.currentTimeMillis();
		System.out.println("time cost ----- " + (t2-t1)/1000);
		/*getNodeIdMap(1,
		 "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");*/
		//http://data.semanticweb.org/person/jose-luis-redondo-garc?a
	}
}
