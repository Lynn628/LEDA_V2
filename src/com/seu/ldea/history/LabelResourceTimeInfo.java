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
 * @author Lynn 6/5/2017 搭时间抽取的程序框架，读取三元组，确定是否是资源，确定此资源是否已存在，确定此资源是否有时间信息
 */
public class LabelResourceTimeInfo {
	/**
	 * Resource map,存储资源和资源的id
	 */
	public static HashMap<String, Integer> resourceMap = new HashMap<>();
	
	/**
	 * Predicate map,存储谓语和谓语的id
	 */
	public static HashMap<String, Integer> predicateMap = new HashMap<>();
	// 存储主语id和谓语id和时间区间set，即每个resource的时间信息
	public static HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * 每一个资源对象存在一个关于其时间信息的描述信息
	 */
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();

	public static void main(String[] args) throws IOException{
		// 读取目录路径
		long t1 = System.currentTimeMillis();
		Scanner scanner = new Scanner(System.in);
		// TDB的名字
		System.out.println("Please give TDB name ");
		String tdb = scanner.nextLine();
		// 要读取的目录的名字
		System.out.println("Input the directory path:\n");
		String dirPath = scanner.nextLine();
		System.out.println("Please give processed triple file Name ");
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//"
				+ scanner.nextLine() + ".txt";
		scanner.close();
		// 读取目录路径
		String tdbName = "D:\\rescalInputFile\\" + tdb + "TDB";
		Dataset ds = TDBFactory.createDataset(tdbName);

		Model model = ds.getDefaultModel();

		ArrayList<String> filePathList = ReadFilePath.readDir(dirPath);
		Iterator<String> iterator = filePathList.iterator();

		// 对每一个rdf文档进行处理
		while (iterator.hasNext()) {
			// 源文件路径
			String filePath = iterator.next();
			System.out.println(filePath);
			//预处理文件，替换Jena无法识别的字符
			//截取处理过的文件名
			int indexBegin = filePath.lastIndexOf("/");
			int indexEnd = filePath.lastIndexOf(".");
			String fileName = "processed_" +filePath.substring(indexBegin + 1, indexEnd);
			String newFilePath = PreProcessRDF.PreProcessRDFFile(filePath, fileName);
		    //读取model
			FileManager.get().readModel(model, newFilePath);
			//FileManager.get().readModel(model, filePath);
			// model.read(filePath, "RDF/XML");
		}
		
		//对Model中的数据进行抽取，返回时间资源编号以及标签的HashMap集合
		HashMap<Integer, ResourceInfo> labeledResources = timeExtraction(model);
		Iterator<Entry<Integer, ResourceInfo>> iter = labeledResources.entrySet().iterator();
		//将resourceMap翻转，依据rId 在resourceMap里面找到指定的资源String
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
	 * 用Jena处理文档，抽取并处理每一个Statement的主谓宾
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
		// 获取model的statement
		StmtIterator iterator = model.listStatements();
		// 给资源编号
		int rId = 0;
		// 给谓语编号
		int pId = 0;
		int currentRId;
		int currentPId;
		while (iterator.hasNext()) {
			Statement statement = iterator.next();
			// System.out.println(statement.toString());
			Resource resource = statement.getSubject();
			String resourceStr = resource.toString();
			// ResourceInfo resourceInfo;
			// 不包含此资源
			if (!resourceMap.containsKey(resourceStr)) {
				rId++;
				// 当前资源的Id
				currentRId = rId;
				resourceMap.put(resourceStr, currentRId);
			
			} else {
				currentRId = resourceMap.get(resourceStr);
			
			}

			// 将subject的URI进行字符串处理
			String newSub = URIUtil.processURI(resourceStr);
			//**************时间抽取区域*********************
			// 对资源URI进行时间信息的抽取
			List<CoreMap> subURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newSub);
			//************************************
			// 如果主语URI包含时间信息
			if (!subURIList.isEmpty()) {
				
				if (!resourceTimeInfo.containsKey(currentRId)) {
					// 给当前资源创建信息标签
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
			 * 判断Predicate，剔除干扰的时间属性(type)
			 */
			// 判断是否已存在此predicate
			if (!predicateMap.containsKey(propertyStr)) {
				pId++;
				currentPId = pId;
				predicateMap.put(propertyStr, pId);
			} else {
				currentPId = predicateMap.get(propertyStr);
			}

			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (object instanceof Resource) {
				// 判断是否已存在此资源
				if (!resourceMap.containsKey(objectStr)) {
					rId++;
					resourceMap.put(objectStr, rId);
				}
				//**************时间抽取区域*********************
				// 对Object时间信息的抽取可以用一个方法封装起来，替换URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(object.toString());
				List<CoreMap> objURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					if (!resourceTimeInfo.containsKey(currentRId)) {
						// 给当前资源创建信息标签
						ResourceInfo resourceInfo = new ResourceInfo(currentRId);
						resourceTimeInfo.put(currentRId, resourceInfo);
					}

					for (CoreMap cm : objURIList) {
						labelResource(currentRId, "createdDate", cm);
					}
				}

			} else {
				// 当Object为字符串
				//**************时间抽取区域*********************
				// 判断时间信息的有效性，占整个literal的比重，其次考虑时间信息表达的规范性
				
				
				//***************************
				List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objectStr);
				
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(currentRId)) {
						// 给当前资源创建信息标签
						ResourceInfo resourceInfo = new ResourceInfo(currentRId);
						resourceTimeInfo.put(currentRId, resourceInfo);
					}
					// 将识别出来的时间信息与当前谓语以<p, timeSpan>与资源绑定
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
	 * 若从三元组中抽取到时间信息，则给当前资源贴上时间标签
	 * 
	 * @param currentRId
	 * @param currentPid
	 * @param isSelf:判断是否是当前资源自身携带的
	 */
	public static void labelResource(Integer currentRId, String currentPId, CoreMap cm) {
		
		HashMap<String, HashSet<TimeSpan>> timePairMap = resourceTimeInfo.get(currentRId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(cm.toString(), cm.toString());
		// 判断当前resource是否有时间标签，如果有，则添加时间信息
		if (timePairMap.containsKey(currentPId)) {
			HashSet<TimeSpan> timeSpanSet = timePairMap.get(currentPId);
			timeSpanSet.add(span);
		} else {
			HashSet<TimeSpan> timeSpanSet = new HashSet<>();
			timeSpanSet.add(span);
			// 给resource贴上时间信息标签
			timePairMap.put(currentPId, timeSpanSet);
		}
	}
//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
}
