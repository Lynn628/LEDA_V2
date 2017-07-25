package com.seu.ldea.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.util.CoreMap;

import com.seu.ldea.entity.*;
import com.seu.ldea.util.*;


/**
 * 
 * @author Lynn 6/5/2017 ��ʱ���ȡ�ĳ����ܣ���ȡ��Ԫ�飬ȷ���Ƿ�����Դ��ȷ������Դ�Ƿ��Ѵ��ڣ�ȷ������Դ�Ƿ���ʱ����Ϣ
 */
public class LabelResourceTimeInfo {
	/**
	 * Resource map,�洢��Դ����Դ��id
	 */
	public static HashMap<String, Integer> resourceMap = new HashMap<>();
	
	/**
	 * Predicate map,�洢ν���ν���id
	 */
	public static HashMap<String, Integer> predicateMap = new HashMap<>();
	// �洢����id��ν��id��ʱ������set����ÿ��resource��ʱ����Ϣ
	public static HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * ÿһ����Դ�������һ��������ʱ����Ϣ��������Ϣ
	 */
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();

	public static void main(String[] args) throws IOException{
		// ��ȡĿ¼·��
		long t1 = System.currentTimeMillis();
		Scanner scanner = new Scanner(System.in);
		// TDB������
		System.out.println("Please give TDB name ");
		String tdb = scanner.nextLine();
		// Ҫ��ȡ��Ŀ¼������
		System.out.println("Input the directory path:\n");
		String dirPath = scanner.nextLine();
		System.out.println("Please give processed triple file Name ");
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//"
				+ scanner.nextLine() + ".txt";
		scanner.close();
		// ��ȡĿ¼·��
		String tdbName = "D:\\rescalInputFile\\" + tdb + "TDB";
		Dataset ds = TDBFactory.createDataset(tdbName);

		Model model = ds.getDefaultModel();

		ArrayList<String> filePathList = ReadFilePath.readDir(dirPath);
		Iterator<String> iterator = filePathList.iterator();

		// ��ÿһ��rdf�ĵ����д���
		while (iterator.hasNext()) {
			// Դ�ļ�·��
			String filePath = iterator.next();
			System.out.println(filePath);
			//Ԥ�����ļ����滻Jena�޷�ʶ����ַ�
			//��ȡ��������ļ���
			int indexBegin = filePath.lastIndexOf("/");
			int indexEnd = filePath.lastIndexOf(".");
			String fileName = "processed_" +filePath.substring(indexBegin + 1, indexEnd);
			String newFilePath = PreProcessRDF.PreProcessRDFFile(filePath, fileName);
		    //��ȡmodel
			FileManager.get().readModel(model, newFilePath);
			//FileManager.get().readModel(model, filePath);
			// model.read(filePath, "RDF/XML");
		}
		
		//��Model�е����ݽ��г�ȡ������ʱ����Դ����Լ���ǩ��HashMap����
		HashMap<Integer, ResourceInfo> labeledResources = timeExtraction(model);
		Iterator<Entry<Integer, ResourceInfo>> iter = labeledResources.entrySet().iterator();
		//��resourceMap��ת������rId ��resourceMap�����ҵ�ָ������ԴString
		HashMap<Integer, String> resourceReverseMap = reverseMap(resourceMap);
		
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
		System.out.println("Model size is : " + model.size());
		System.out.println("Resource # has id " + resourceMap.size());
		System.out.println("Resource # has time information " + resourceTimeInfo.size());
		System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
	/**
	 * ��Jena�����ĵ�����ȡ������ÿһ��Statement����ν��
	 * 
	 * @param filePath
	 * @param dstPath
	 * @param bufferedWriter
	 * @throws IOException
	 */
	public static HashMap<Integer, ResourceInfo> timeExtraction(Model model) {
		// System.out.println("Begin process the triple ");
		// initialize the annotationPipeline
		AnnotationPipeline pipeline = SUTimeExtraction.PipeInit();
		// ��ȡmodel��statement
		StmtIterator iterator = model.listStatements();
		// ����Դ���
		int rId = 0;
		// ��ν����
		int pId = 0;
		int currentRId;
		int currentPId;
		while (iterator.hasNext()) {
			Statement statement = iterator.next();
			// System.out.println(statement.toString());
			Resource resource = statement.getSubject();
			String resourceStr = resource.toString();
			// ResourceInfo resourceInfo;
			// ����������Դ
			if (!resourceMap.containsKey(resourceStr)) {
				rId++;
				// ��ǰ��Դ��Id
				currentRId = rId;
				resourceMap.put(resourceStr, currentRId);
			
			} else {
				currentRId = resourceMap.get(resourceStr);
			
			}

			// ��subject��URI�����ַ�������
			String newSub = URIUtil.processURI(resourceStr);
			//**************ʱ���ȡ����*********************
			// ����ԴURI����ʱ����Ϣ�ĳ�ȡ
			List<CoreMap> subURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newSub);
			//************************************
			// �������URI����ʱ����Ϣ
			if (!subURIList.isEmpty()) {
				
				if (!resourceTimeInfo.containsKey(currentRId)) {
					// ����ǰ��Դ������Ϣ��ǩ
					ResourceInfo resourceInfo = new ResourceInfo(currentRId);					
					resourceTimeInfo.put(currentRId, resourceInfo);
				}
					for (CoreMap cm : subURIList) {
					//	labelResource(currentRId, -1, cm);
						labelResource(currentRId, "createdDate", cm);
					}
				
				
			}
			Property property = statement.getPredicate();
			RDFNode object = statement.getObject();
			String propertyStr = property.toString();
			String objectStr = object.toString();
			/**
			 * �ж�Predicate���޳����ŵ�ʱ������(type)
			 */
			// �ж��Ƿ��Ѵ��ڴ�predicate
			if (!predicateMap.containsKey(propertyStr)) {
				pId++;
				currentPId = pId;
				predicateMap.put(propertyStr, pId);
			} else {
				currentPId = predicateMap.get(propertyStr);
			}

			// ��objectΪURIʱ������URI����ȡ��������е�ʱ���
			if (object instanceof Resource) {
				// �ж��Ƿ��Ѵ��ڴ���Դ
				if (!resourceMap.containsKey(objectStr)) {
					rId++;
					resourceMap.put(objectStr, rId);
				}
				//**************ʱ���ȡ����*********************
				// ��Objectʱ����Ϣ�ĳ�ȡ������һ��������װ�������滻URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(object.toString());
				List<CoreMap> objURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					if (!resourceTimeInfo.containsKey(currentRId)) {
						// ����ǰ��Դ������Ϣ��ǩ
						ResourceInfo resourceInfo = new ResourceInfo(currentRId);
						resourceTimeInfo.put(currentRId, resourceInfo);
					}

					for (CoreMap cm : objURIList) {
						labelResource(currentRId, "createdDate", cm);
					}
				}

			} else {
				// ��ObjectΪ�ַ���
				//**************ʱ���ȡ����*********************
				// �ж�ʱ����Ϣ����Ч�ԣ�ռ����literal�ı��أ���ο���ʱ����Ϣ���Ĺ淶��
				
				
				//***************************
				List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objectStr);
				
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(currentRId)) {
						// ����ǰ��Դ������Ϣ��ǩ
						ResourceInfo resourceInfo = new ResourceInfo(currentRId);
						resourceTimeInfo.put(currentRId, resourceInfo);
					}
					// ��ʶ�������ʱ����Ϣ�뵱ǰν����<p, timeSpan>����Դ��
					for (CoreMap cm : list) {
						labelResource(currentRId, property.getLocalName(), cm);
					}
				}
			}
		}

		return resourceTimeInfo;
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
	
	public static <E> HashMap<E, E> reverseMap2(HashMap<E, E> resourceMap){
		HashMap<E, E> reversedMap = new HashMap<>();
		Iterator<Entry<E, E>> iterator = resourceMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<E, E> entry = iterator.next();
			E key = entry.getKey();
			E value = entry.getValue();
			reversedMap.put(value, key);
		}
		return reversedMap;
	}
	// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\conferences\eswc-2012-complete.rdf
	/**
	 * ������Ԫ���г�ȡ��ʱ����Ϣ�������ǰ��Դ����ʱ���ǩ
	 * 
	 * @param currentRId
	 * @param currentPid
	 * @param isSelf:�ж��Ƿ��ǵ�ǰ��Դ����Я����
	 */
	public static void labelResource(Integer currentRId, String currentPId, CoreMap cm) {
		
		HashMap<String, HashSet<TimeSpan>> timePairMap = resourceTimeInfo.get(currentRId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(cm.toString(), cm.toString());
		// �жϵ�ǰresource�Ƿ���ʱ���ǩ������У������ʱ����Ϣ
		if (timePairMap.containsKey(currentPId)) {
			HashSet<TimeSpan> timeSpanSet = timePairMap.get(currentPId);
			timeSpanSet.add(span);
		} else {
			HashSet<TimeSpan> timeSpanSet = new HashSet<>();
			timeSpanSet.add(span);
			// ��resource����ʱ����Ϣ��ǩ
			timePairMap.put(currentPId, timeSpanSet);
		}
	}
//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
}
