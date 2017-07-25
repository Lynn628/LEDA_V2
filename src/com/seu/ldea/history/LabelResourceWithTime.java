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
	 * Resource map,存储资源和资源的id
	 */
	//public static HashMap<String, Integer> resourceMap = new HashMap<>();
	public static HashMap<String, Integer> rMap;
	/**
	 * Predicate map,存储谓语和谓语的id
	 */
	//public static HashMap<String, Integer> predicateMap = new HashMap<>();
	public static HashMap<String, Integer> pMap;
	
	// 存储主语id和谓语id和时间区间set，即每个resource的时间信息
	public static HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * 每一个资源对象存在一个关于其时间信息的描述信息
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
		//对Model中的数据进行抽取，返回时间资源编号以及标签的HashMap集合
		HashMap<Integer, ResourceInfo> labeledResources = timeExtraction(dataset);
		Iterator<Entry<Integer, ResourceInfo>> iter = labeledResources.entrySet().iterator();
		//将resourceMap翻转，依据rId 在resourceMap里面找到指定的资源String
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
	 * @return dstName处理后包含时间信息的文件的文件名
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
		     
			// 将subject的URI进行字符串处理
			String newSub = URIUtil.processURI(subStr);
			//**************时间抽取区域*********************
			// 对资源URI进行时间信息的抽取
			List<CoreMap> subTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newSub);
			Integer subId = rMap.get(subStr);
			// 如果主语URI包含时间信息
			if (!subTimeList.isEmpty()) {
				//若当前节点没有贴上时间标签
				if (!resourceTimeInfo.containsKey(subId)) {
					// 给当前资源创建标签
					ResourceInfo resourceInfo = new ResourceInfo(subId);
					resourceTimeInfo.put(subId, resourceInfo);
				}
					for (CoreMap cm : subTimeList) {
					//	labelResource(currentRId, -1, cm);
						labelResource(subId, "createdDate", cm);
				}
			}
			
			/**
			 * 判断Predicate，剔除干扰的时间属性(type)
			 */
			//System.out.println("PreStr is " + preStr);
			int preId = pMap.get(preStr);
          //  System.out.println(preStr + " " + preId);
			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (obj instanceof Resource) {
				// 判断是否已存在此资源
				int objId = rMap.get(objStr);
				//**************时间抽取区域*********************
				// 对Object时间信息的抽取可以用一个方法封装起来，替换URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(obj.toString());
				List<CoreMap> objURIList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					if (!resourceTimeInfo.containsKey(objId)) {
						// 给当前资源创建信息标签
						ResourceInfo resourceInfo = new ResourceInfo(objId);
						resourceTimeInfo.put(objId, resourceInfo);
					}
					
					for (CoreMap cm : objURIList) {
						labelResource(objId, "createdDate", cm);
					}
				}

			} else {
				// 当Object为字符串
				//**************时间抽取区域*********************
				// 判断时间信息的有效性，占整个literal的比重，其次考虑时间信息表达的规范性
				//***************************
				List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(subId)) {
						// 给当前资源创建信息标签
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					// 将识别出来的时间信息与当前谓语以<p, timeSpan>与资源绑定
					for (CoreMap cm : list) {
						labelResource(subId, preStr, cm);
					}
				}
			}
		}
		 //输出实体和实体上面的时间标签
		 Iterator<Entry<Integer, ResourceInfo>> iter = resourceTimeInfo.entrySet().iterator();
			//将resourceMap翻转，依据rId 在resourceMap里面找到指定的资源String
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
	
	//依据virtuosofilebuild生成的entity-id文件和words文件来获取entity和predicate对应的ID
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
	 * 若从三元组中抽取到时间信息，则给当前资源贴上时间标签
	 * 
	 * @param nodeId
	 * @param pString
	 * @param isSelf:判断是否是当前资源自身携带的
	 */
	public static void labelResource(Integer nodeId, String pString, CoreMap cm) {
		//predicate和此predicate上的时间区间Map
		HashMap<String, HashSet<TimeSpan>> predicateTimePairs = resourceTimeInfo.get(nodeId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(cm.toString(), cm.toString());
		// 判断当前resource是否有在该时间属性上有标签，如果有，则添加时间信息
		if (predicateTimePairs.containsKey(pString)) {
			HashSet<TimeSpan> timeSpanSet = predicateTimePairs.get(pString);
			timeSpanSet.add(span);
		} else {
			HashSet<TimeSpan> timeSpanSet = new HashSet<>();
			timeSpanSet.add(span);
			// 给resource贴上时间信息标签
			predicateTimePairs.put(pString, timeSpanSet);
		}
   }
}