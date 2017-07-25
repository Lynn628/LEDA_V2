package com.seu.ldea.history;

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
import edu.stanford.nlp.util.CoreMap;

public class LabelResourceWithTime {
	/**
	 * Resource map,�洢��Դ����Դ��id
	 */
	//public static HashMap<String, Integer> resourceMap = new HashMap<>();
	public static HashMap<String, Integer> rMap;
	/**
	 * Predicate map,�洢ν���ν���id
	 */
	//public static HashMap<String, Integer> predicateMap = new HashMap<>();
	public static HashMap<String, Integer> pMap;
	
	// �洢����id��ν��id��ʱ������set����ÿ��resource��ʱ����Ϣ
	public static HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * ÿһ����Դ�������һ��������ʱ����Ϣ��������Ϣ
	 */
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();

	public static void main(String[] args) throws IOException{
		/*Scanner scanner = new Scanner(System.in);
		String dstPath = scanner.nextLine();
		long t1 = System.currentTimeMillis();
		String url = "jdbc:virtuoso://localhost:1111";
		System.out.println("Please give the graph IRI ");
		
		String graphName = scanner.nextLine();
	    scanner.close();
		Dataset dataset = new Dataset(url, graphName, "dba", "dba");
		//��Model�е����ݽ��г�ȡ������ʱ����Դ����Լ���ǩ��HashMap����
		HashMap<Integer, ResourceInfo> labeledResources = timeExtraction(dataset);
		Iterator<Entry<Integer, ResourceInfo>> iter = labeledResources.entrySet().iterator();
		//��resourceMap��ת������rId ��resourceMap�����ҵ�ָ������ԴString
		HashMap<Integer, String> resourceReverseMap = reverseMap(rMap);
		
		FileWriter fileWriter = new FileWriter(new File(dstPath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		while (iter.hasNext()) {
			Entry<Integer, ResourceInfo> entry = iter.next();
			Integer key = entry.getKey();
			System.out.println("current key " + key);
		    String uri = resourceReverseMap.get(key);
		    bufferedWriter.write(key + ": "  + uri + " -- "+ ((ResourceInfo) entry.getValue()).toString());
		    bufferedWriter.newLine();
		//	System.out.println( key + ": "  + uri + " -- "+ ((ResourceInfo) entry.getValue()).toString());
			//System.out.println((Integer) entry.getKey() + "---" + ((ResourceInfo) entry.getValue()).toString());
		}
		bufferedWriter.close();
		long t2 = System.currentTimeMillis();
		double timeCost = (t2 - t1) / 1000.0;
		//System.out.println("Model size is : " + model.size());
		System.out.println("Resource # has id " + rMap.size());
		System.out.println("Resource # has time information " + resourceTimeInfo.size());
		System.out.println("End of main~~~~~~time cost " + timeCost + "s");*/
	}
//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
	/**
	 * 
	 * @param dataset
	 * @return dstName��������ʱ����Ϣ���ļ����ļ���
	 * @throws IOException 
	 */
	//public static HashMap<Integer, ResourceInfo> timeExtraction(Dataset dataset) 
	public static void timeExtraction(Dataset dataset , String dstName) throws IOException{
	
		AnnotationPipeline pipeline = SUTimeExtraction.PipeInit();
		ResultSet resultSet = SparqlQuery.getAllTriplesResultSet(dataset);
	
		 rMap = dataset.getrMap();
		 pMap = dataset.getpMap();
		 int tripleNum =0;
		 while(resultSet.hasNext()){
			    tripleNum++;
			     System.out.println(tripleNum);
				 QuerySolution result = resultSet.nextSolution();
				 RDFNode sub = result.get("s");
				 String subStr = sub.toString();
				 RDFNode pre = result.get("p"); 
				 String preStr = pre.toString();
				 RDFNode obj = result.get("o");
				 String objStr = obj.toString();
		     
			// ��subject��URI�����ַ�������
			String newSub = URIUtil.processURI(subStr);
			//**************ʱ���ȡ����*********************
			// ����ԴURI����ʱ����Ϣ�ĳ�ȡ
			List<CoreMap> subTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newSub);
			Integer subId = rMap.get(subStr);
			// �������URI����ʱ����Ϣ
			if (!subTimeList.isEmpty()) {
				//����ǰ�ڵ�û������ʱ���ǩ
				if (!resourceTimeInfo.containsKey(subId)) {
					// ����ǰ��Դ������ǩ
					ResourceInfo resourceInfo = new ResourceInfo(subId);
					resourceTimeInfo.put(subId, resourceInfo);
				}
					for (CoreMap cm : subTimeList) {
					//	labelResource(currentRId, -1, cm);
						labelResource(subId, "createdDate", cm);
				}
			}
			
			/**
			 * �ж�Predicate���޳����ŵ�ʱ������(type)
			 */
			//System.out.println("PreStr is " + preStr);
			int preId = pMap.get(preStr);
          //  System.out.println(preStr + " " + preId);
			// ��objectΪURIʱ������URI����ȡ��������е�ʱ���
			if (obj instanceof Resource) {
				// �ж��Ƿ��Ѵ��ڴ���Դ
				int objId = rMap.get(objStr);
				//**************ʱ���ȡ����*********************
				// ��Objectʱ����Ϣ�ĳ�ȡ������һ��������װ�������滻URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(obj.toString());
				List<CoreMap> objURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					if (!resourceTimeInfo.containsKey(objId)) {
						// ����ǰ��Դ������Ϣ��ǩ
						ResourceInfo resourceInfo = new ResourceInfo(objId);
						resourceTimeInfo.put(objId, resourceInfo);
					}
					
					for (CoreMap cm : objURIList) {
						labelResource(objId, "createdDate", cm);
					}
				}

			} else {
				// ��ObjectΪ�ַ���
				//**************ʱ���ȡ����*********************
				// �ж�ʱ����Ϣ����Ч�ԣ�ռ����literal�ı��أ���ο���ʱ����Ϣ���Ĺ淶��
				//***************************
				List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(subId)) {
						// ����ǰ��Դ������Ϣ��ǩ
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					// ��ʶ�������ʱ����Ϣ�뵱ǰν����<p, timeSpan>����Դ��
					for (CoreMap cm : list) {
						labelResource(subId, preStr, cm);
					}
				}
			}
		}
		 //���ʵ���ʵ�������ʱ���ǩ
		 Iterator<Entry<Integer, ResourceInfo>> iter = resourceTimeInfo.entrySet().iterator();
			//��resourceMap��ת������rId ��resourceMap�����ҵ�ָ������ԴString
			HashMap<Integer, String> resourceReverseMap = reverseMap(rMap);
			String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//"
					+ dstName + ".txt";
			FileWriter fileWriter = new FileWriter(new File(dstPath));
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			while (iter.hasNext()) {
				Entry<Integer, ResourceInfo> entry = iter.next();
				Integer key = entry.getKey();
				System.out.println("current key " + key);
			    String uri = resourceReverseMap.get(key);
			    bufferedWriter.write(key + ": "  + uri + " -- "+ ((ResourceInfo) entry.getValue()).toString());
			    bufferedWriter.newLine();
			//	System.out.println( key + ": "  + uri + " -- "+ ((ResourceInfo) entry.getValue()).toString());
				//System.out.println((Integer) entry.getKey() + "---" + ((ResourceInfo) entry.getValue()).toString());
			}
			bufferedWriter.close();
		//return resourceTimeInfo;
		// System.out.println("Statement Number: " + lineNum);
	}

	public static HashMap<Integer, String> reverseMap(HashMap<String,Integer> resourceMap){
		HashMap<Integer, String> reversedMap = new HashMap<>();
		Iterator<Entry<String, Integer>> iterator = resourceMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			Integer value = entry.getValue();
			reversedMap.put(value, key);
		}
		return reversedMap;
	}
	
	//����virtuosofilebuild���ɵ�entity-id�ļ���words�ļ�����ȡentity��predicate��Ӧ��ID
	public static HashMap<String, Integer> getNodeIdMap(int type, String dir) throws IOException{
		HashMap<String, Integer> nodeMap = new HashMap<>();
		String path = "";
		if(type == 1){
			path = dir + "entity-id";
		}else if(type == 2){
			path = dir + "words";
		}
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while((line = br.readLine()) != null){
			String[] lineArr = line.split(":");
			nodeMap.put(lineArr[1], Integer.parseInt(lineArr[0]));
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
	public static void labelResource(Integer nodeId, String pString, CoreMap cm) {
		//predicate�ʹ�predicate�ϵ�ʱ������Map
		HashMap<String, HashSet<TimeSpan>> predicateTimePairs = resourceTimeInfo.get(nodeId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(cm.toString(), cm.toString());
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
}