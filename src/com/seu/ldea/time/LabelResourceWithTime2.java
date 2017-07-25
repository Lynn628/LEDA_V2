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
	 * Resource map,存储资源和资源的id
	 */
	// public static HashMap<String, Integer> resourceMap = new HashMap<>();
	public HashMap<String, Integer> rMap;
	/**
	 * Predicate map,存储谓语和谓语的id
	 */
	// public static HashMap<String, Integer> predicateMap = new HashMap<>();
	public HashMap<String, Integer> pMap;

	// 存储主语id和谓语id和时间区间set，即每个resource的时间信息
	public HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * 每一个资源对象存在一个关于其时间信息的描述信息
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
	 *            存放entity-id和words的文件夹地址
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
			// 判断该URI是否需要进行时间抽取，比如包含www.org之类的，比如包含连续字符数字的则无需进行时间抽取
			if (URIUtil.judgeURI(subStr)) {
				// 将subject的URI进行字符串处理，拆分开
				subStr = URIUtil.processURI(subStr);
				// **************时间抽取区域*********************
				// 对资源URI进行时间信息的抽取
				List<CoreMap> subTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, subStr);
				// 如果主语URI包含时间信息
				if (!subTimeList.isEmpty()) {
					// 若当前节点没有贴上时间标签
					if (!resourceTimeInfo.containsKey(subId)) {
						// 给当前资源创建标签
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					for (CoreMap cm : subTimeList) {
						// labelResource(currentRId, -1, cm);
						String time = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
						//添加验证时间格式的有效性
						labelResource(subId, "createdDate", time);
					}
				}
			}
		
			//System.out.println("preStr--- " + preStr);
			int preId = pMap.get(preStr);
			//如果谓词是label comment这些，则不对后面的宾语进行抽取
			if(!preStr.equals("http://www.w3.org/2000/01/rdf-schema#label") &&
					!preStr.equals("http://www.w3.org/2000/01/rdf-schema#comment")){
			//System.out.println("preId--- " + preId);
			// System.out.println(preStr + " " + preId);
			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (obj instanceof Resource) {
			//	System.out.println("objStr --- " + objStr + "rMap.containsKey(objStr)--- " + rMap.containsKey(objStr));
				int objId = rMap.get(objStr);
				if (URIUtil.judgeURI(objStr)) {
					// **************时间抽取区域*********************
					objStr = URIUtil.processURI(objStr);
					List<CoreMap> objTimeList = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
					if (!objTimeList.isEmpty()) {
						if (!resourceTimeInfo.containsKey(objId)) {
							// 给当前资源创建信息标签
							ResourceInfo resourceInfo = new ResourceInfo(objId);
							resourceTimeInfo.put(objId, resourceInfo);
						}
						for (CoreMap cm : objTimeList) {
							String time = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
							//添加验证时间格式的有效性
							labelResource(objId, "createdDate", time);
						}
					}
				}
			} else {
				// 当Object为字符串
				// **************时间抽取区域*********************
			    List<CoreMap> list = SUTimeExtraction.SUTimeJudgeFunc(pipeline, objStr);
				if (!list.isEmpty()) {
					if (!resourceTimeInfo.containsKey(subId)) {
						// 给当前资源创建信息标签
						ResourceInfo resourceInfo = new ResourceInfo(subId);
						resourceTimeInfo.put(subId, resourceInfo);
					}
					String time = getTimeInLiteral(list, objStr);
				    //添加验证时间有效性的步骤 
					if(time != ""){
						labelResource(subId, preStr, time);
				     }
				}
			}
		 }
		}
		//结果写入文档
		// 输出实体和实体上面的时间标签
		Iterator<Entry<Integer, ResourceInfo>> iter = resourceTimeInfo.entrySet().iterator();
		// 将resourceMap翻转，依据rId 在resourceMap里面找到指定的资源String
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
	 * 判断时间信息所占的比重,返回占比最大的时间信息的标准表示形式
	 * 
	 * @param uri
	 * @return
	 */
	public String getTimeInLiteral(List<CoreMap> list, String uri) {
		    double maxPercentage = 0;
		    String timeInfo = "";
		    String result = "";
			// 将识别出来的时间信息与当前谓语以<p, timeSpan>与资源绑定
			for (CoreMap cm : list) {
				double percentage = cm.toString().length()/(uri.length()*1.0);
				if(maxPercentage < percentage){
					maxPercentage = percentage;
					timeInfo = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
				}
			}
			//设置阈值为0.5
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
	 * 依据virtuosofilebuild生成的entity-id文件和words文件来获取entity和predicate对应的ID
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
	 * 若从三元组中抽取到时间信息，则给当前资源贴上时间标签
	 * 
	 * @param nodeId
	 * @param pString
	 * @param isSelf:判断是否是当前资源自身携带的
	 */
	public void labelResource(Integer nodeId, String pString, String time) {
		// predicate和此predicate上的时间区间Map
		HashMap<String, HashSet<TimeSpan>> predicateTimePairs = resourceTimeInfo.get(nodeId).getPredicateTimeMap();
		TimeSpan span = new TimeSpan(time, time);
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
