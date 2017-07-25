package com.seu.ldea.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.util.SUTimeExtraction;
import com.seu.ldea.util.URIUtil;
import com.seu.ldea.virtuoso.SparqlQuery;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class LabelResourceWithTime2 {
	/**
	 * Resource map,�洢��Դ����Դ��id
	 */
	// public static HashMap<String, Integer> resourceMap = new HashMap<>();
	public HashMap<String, Integer> rMap;
	/**
	 * Predicate map,�洢ν���ν���id
	 */
	// public static HashMap<String, Integer> predicateMap = new HashMap<>();
	public HashMap<String, Integer> pMap;

	// �洢����id��ν��id��ʱ������set����ÿ��resource��ʱ����Ϣ
	public HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * ÿһ����Դ�������һ��������ʱ����Ϣ��������Ϣ
	 */
	public HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
    
	public HashMap<Integer, ResourceInfo> getResourceTimeInfo() {
		return resourceTimeInfo;
	}
	
	/**
	 * 
	 * @param dataset
	 * @param dstName
	 * @param dirPath
	 *            ���entity-id��words���ļ��е�ַ
	 * @throws IOException
	 */
	// public static HashMap<Integer, ResourceInfo> timeExtraction(Dataset
	// dataset)
	public HashMap<Integer, ResourceInfo> timeExtraction(Dataset dataset, String dstName, String dirPath) throws IOException {

		AnnotationPipeline pipeline = SUTimeExtraction.PipeInit();
		ResultSet resultSet = SparqlQuery.getAllTriplesResultSet(dataset);

		/*
		 * rMap = dataset.getrMap(); pMap = dataset.getpMap();
		 */
		rMap = getNodeIdMap(1, dirPath);
		pMap = getNodeIdMap(2, dirPath);
		int tripleNum = 0;
		while (resultSet.hasNext()) {
			tripleNum++;
			System.out.println(tripleNum);
			QuerySolution result = resultSet.nextSolution();
			RDFNode sub = result.get("s");
			String subStr = new String(sub.toString().getBytes(), "GBK");
			RDFNode pre = result.get("p");
			String preStr = new String(pre.toString().getBytes(), "GBK");
			RDFNode obj = result.get("o");
			String objStr = new String(obj.toString().getBytes(), "GBK");
			//System.out.println("subStr--- " + subStr);
			Integer subId = rMap.get(subStr);
			//System.out.println("subId--- " + subId);
			// �жϸ�URI�Ƿ���Ҫ����ʱ���ȡ���������www.org֮��ģ�������������ַ����ֵ����������ʱ���ȡ
			if (URIUtil.judgeURI(subStr)) {
				// ��subject��URI�����ַ���������ֿ�
				subStr = URIUtil.processURI(subStr);
				// **************ʱ���ȡ����*********************
				// ����ԴURI����ʱ����Ϣ�ĳ�ȡ
				List<CoreMap> subTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, subStr);
				// �������URI����ʱ����Ϣ
				if (!subTimeList.isEmpty()) {
					// ����ǰ�ڵ�û������ʱ���ǩ
					if (!resourceTimeInfo.containsKey(subId)) {
						// ����ǰ��Դ������ǩ
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					for (CoreMap cm : subTimeList) {
						// labelResource(currentRId, -1, cm);
						String time = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
						//�����֤ʱ���ʽ����Ч��
						labelResource(subId, "createdDate", time);
					}
				}
			}
		
			//System.out.println("preStr--- " + preStr);
			int preId = pMap.get(preStr);
			//���ν����label comment��Щ���򲻶Ժ���ı�����г�ȡ
			if(!preStr.equals("http://www.w3.org/2000/01/rdf-schema#label") &&
					!preStr.equals("http://www.w3.org/2000/01/rdf-schema#comment")){
			//System.out.println("preId--- " + preId);
			// System.out.println(preStr + " " + preId);
			// ��objectΪURIʱ������URI����ȡ��������е�ʱ���
			if (obj instanceof Resource) {
			//	System.out.println("objStr --- " + objStr + "rMap.containsKey(objStr)--- " + rMap.containsKey(objStr));
				int objId = rMap.get(objStr);
				if (URIUtil.judgeURI(objStr)) {
					// **************ʱ���ȡ����*********************
					objStr = URIUtil.processURI(objStr);
					List<CoreMap> objTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
					if (!objTimeList.isEmpty()) {
						if (!resourceTimeInfo.containsKey(objId)) {
							// ����ǰ��Դ������Ϣ��ǩ
							ResourceInfo resourceInfo = new ResourceInfo(objId);
							resourceTimeInfo.put(objId, resourceInfo);
						}
						for (CoreMap cm : objTimeList) {
							String time = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
							//�����֤ʱ���ʽ����Ч��
							labelResource(objId, "createdDate", time);
						}
					}
				}
			} else {
				// ��ObjectΪ�ַ���
				// **************ʱ���ȡ����*********************
			    List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(subId)) {
						// ����ǰ��Դ������Ϣ��ǩ
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					String time = getTimeInLiteral(list, objStr);
				    //�����֤ʱ����Ч�ԵĲ��� 
					if(time != ""){
						labelResource(subId, preStr, time);
				     }
				}
			}
		 }
		}
		//���д���ĵ�
		// ���ʵ���ʵ�������ʱ���ǩ
		Iterator<Entry<Integer, ResourceInfo>> iter = resourceTimeInfo.entrySet().iterator();
		// ��resourceMap��ת������rId ��resourceMap�����ҵ�ָ������ԴString
		HashMap<Integer, String> resourceReverseMap = reverseMap(rMap);
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//" + dstName
				+ ".txt";
		
		FileWriter fileWriter = new FileWriter(new File(dstPath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		// bufferedWriter.write(substr + " " + );
		while (iter.hasNext()) {
			Entry<Integer, ResourceInfo> entry = iter.next();
			Integer key = entry.getKey();
			System.out.println("current key " + key);
			String uri = resourceReverseMap.get(key);
			bufferedWriter.write(key + ": " + uri + " " + ((ResourceInfo) entry.getValue()).toString());
			bufferedWriter.newLine();
		
		}
		bufferedWriter.close();
		return resourceTimeInfo;
	}

	/**
	 * �ж�ʱ����Ϣ��ռ�ı���,����ռ������ʱ����Ϣ�ı�׼��ʾ��ʽ
	 * 
	 * @param uri
	 * @return
	 */
	public String getTimeInLiteral(List<CoreMap> list, String uri) {
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
	public void labelResource(Integer nodeId, String pString, String time) {
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
		LabelResourceWithTime2 labelResource = new LabelResourceWithTime2();
		labelResource.timeExtraction(dataset, "SWCCTimeTest4",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		long t2 = System.currentTimeMillis();
		System.out.println("time cost ----- " + (t2-t1)/1000);
		/*getNodeIdMap(1,
		 "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");*/
		//http://data.semanticweb.org/person/jose-luis-redondo-garc?a
	}
}
