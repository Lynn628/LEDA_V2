package com.seu.ldea.timev2;
/**
 * Author:Lynn
 * Date: 1/3/2018
 * 百万级别的hashmap字符串比较耗时长，换成MD5和id的映射，MD5和string的映射
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.timeutil.SUTimeExtraction;
import com.seu.ldea.timeutil.URIUtil;
import com.seu.ldea.virtuoso.SparqlQuery;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class TripleProcess3 {
	static String rescalDirPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\";
	static String timeExtractionPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\" ;
	//存储resource的url的MD5和id的映射
	HashMap<String, Integer> md5IdMap = new HashMap<>();
//	//md5和url的map
//	HashMap<String, String> md5StrMap = new HashMap<>();
	//存储predicate的url和id的映射
	HashMap<String, Integer> pMap = new HashMap<>();
	//id和resource的url的映射
	HashMap<Integer,String> rRMap = new HashMap<>();
	//存储每个资源 及其时间信息
	public HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	
	public HashMap<Integer, LinkedList<SubAndObj>> subAndObjMap = new HashMap<>();
	//三元组文件
	FileWriter tripleFile;
	AnnotationPipeline pipeline;
	
	class SubAndObj{
		int subId;
		int objId;
		public SubAndObj(int subId, int objId) {
			this.subId = subId;
			this.objId = objId;
		}

		public int getObjId() {
			return objId;
		}
		
		public int getSubId() {
			return subId;
		}
	}

	/**
	 * 
	 * @param dataset---数据集文件在virtuoso包含信息
	 * @param rescalFolder--- row col文件存放地址
	 * @param timeExtractionFileName --- 时间抽取文件存放位置
	 * @throws IOException 
	 */
    public void processTripleFromVirtuoso(Dataset dataset, String rescalFolder, String timeExtractionFileName) throws IOException{
    	pipeline = SUTimeExtraction.PipeInit();
    	ResultSet resultSet = SparqlQuery.getAllTriplesResultSet(dataset);
		File dir = new File(rescalDirPath + rescalFolder);
		if (!dir.exists()) {
			dir.mkdir();
		}
		FileWriter entityFile = new FileWriter(new File(rescalDirPath + rescalFolder + "\\entity-ids"), true);		
		FileWriter wordsFile = new FileWriter(new File(rescalDirPath + rescalFolder + "\\words"), true);
	   // tripleFile = new FileWriter(new File(rescalDirPath + rescalFolder + "\\triple"), true);
	    
		int rNum = 0;//rNum用于标记resource的标号
		int pNum = 1;//pNum用于标号words文件中的predicate
		//发现重大失误之将words也要从0开始计数pNum = 0
		long tripleNum = 0;
		 while(resultSet.hasNext()){
			 System.out.println(++tripleNum);
				// System.out.println("test3");
				 int subId = -1;
				 int objId = -1;
				 int preId = -1;
        //读取出一条三元组
    //	QuerySolution triple = resultSet.next();
		QuerySolution result = resultSet.nextSolution();
	//	System.out.println(result.toString());
		RDFNode sub = result.get("s");
		String subStr = new String(sub.toString().getBytes(), "GBK");
		RDFNode pre = result.get("p");
		String preStr = new String(pre.toString().getBytes(), "GBK");
		RDFNode obj = result.get("o");
		String objStr = new String(obj.toString().getBytes(), "GBK");
    
			 if (!pMap.containsKey(preStr)) {
					pMap.put(preStr, pNum);
					preId = pNum;
					pNum++;
					System.out.println(preId + ":" + preStr + "\n");
					wordsFile.write(preId + ":" + preStr + "\n");
				} else {
					preId = pMap.get(preStr);
				}
		       if(sub instanceof Resource){
		    	   String md5Str = MD5Enc.getMD5(subStr);
		    	   if(!md5IdMap.containsKey(md5Str)){
		    		   md5IdMap.put(md5Str, rNum);
		    		   rRMap.put(rNum, subStr);
		    		   subId = rNum;
		    		   entityFile.write(rNum + ":" + subStr + "\n");
		    		   rNum++;
		    	   }else{
		    		   subId = md5IdMap.get(md5Str);
		    	   }
		    	   if(obj instanceof Resource){
		    		   String md5Str2 = MD5Enc.getMD5(objStr);
		    		   if (!md5IdMap.containsKey(md5Str2)) {
							md5IdMap.put(md5Str2, rNum);
							rRMap.put(rNum, objStr);
							objId = rNum;
							entityFile.write(rNum + ":" + objStr + "\n");
							rNum++;
						} else {
							objId = md5IdMap.get(md5Str2);
						}
				//存储col row的映射标号，生成subAndObjMap
			   //  makeRescalInput(subId, preId, objId, rescalFolder);
		    			if(subAndObjMap.containsKey(preId)){
		    				LinkedList<SubAndObj> list = subAndObjMap.get(preId);
		    				list.add(new SubAndObj(subId, objId));
		    			}else{
		    				LinkedList<SubAndObj> list = new LinkedList<>();
		    				list.add(new SubAndObj(subId, objId));
		    				subAndObjMap.put(preId, list);
		    			}		
		    	   }
			//抽取当前三元组的时间信息
			tripleTimeExtraction(timeExtractionFileName, subStr, preStr, obj);
		 }		
     }
		 entityFile.close();
		 wordsFile.close();
		// tripleFile.close();
		 writeToFile(rescalFolder);
		 //遍历完所有的三元组，将resourceTimeInfo的信息写到文档中
	     writeToFile2(timeExtractionFileName);	 
    }
    
   /**
    * 依据当前三元组的id生成rescal的输入文件col row
    * @param subId
    * @param preId
    * @param objId
    * @param rescalFolder
    * @throws IOException
    */
	public void makeRescalInput(int subId, int preId, int objId, String rescalFolder) throws IOException {
		// 写到对应的row col文件中
		/*String colFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-cols";
		String rowFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-rows";
		tripleFile.write(subId + " " + preId + " " + objId + "\n");

		FileWriter fw1 = new FileWriter(new File(colFilePath), true);
		FileWriter fw2 = new FileWriter(new File(rowFilePath), true);
		// col file存储的是宾语，row文件存储的是主语
		fw1.write(objId + " ");
		fw2.write(subId + " ");
		fw1.close();
		fw2.close();*/
		if(subAndObjMap.containsKey(preId)){
			LinkedList<SubAndObj> list = subAndObjMap.get(preId);
			list.add(new SubAndObj(subId, objId));
		}else{
			LinkedList<SubAndObj> list = new LinkedList<>();
			list.add(new SubAndObj(subId, objId));
			subAndObjMap.put(preId, list);
		}
	}
    
	/**
	 * 抽取出当前三元组的时间信息
	 * @param timeExtractionFolder
	 * @param subStr
	 * @param preStr
	 * @param obj
	 */
	public void tripleTimeExtraction(String timeExtractionFileName, String subStr, String preStr, RDFNode obj) {
		
		int subId = md5IdMap.get(MD5Enc.getMD5(subStr));
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
					// 添加验证时间格式的有效性
					TimeExtractionUtil.labelResource(subId, "createdDate", time, resourceTimeInfo);
				}
			}
		}

		// 如果谓词是label comment这些，则不对后面的宾语进行抽取
		if (!preStr.equals("http://www.w3.org/2000/01/rdf-schema#label")
				&& !preStr.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
			String objStr = obj.toString();
			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (obj instanceof Resource) {// 不是resource
				int objId = md5IdMap.get(MD5Enc.getMD5(objStr));
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
							// 添加验证时间格式的有效性
							TimeExtractionUtil.labelResource(objId, "createdDate", time, resourceTimeInfo);
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
					String time = TimeExtractionUtil.getTimeInLiteral(list, objStr);
					// 添加验证时间有效性的步骤
					if (time != "") {
						TimeExtractionUtil.labelResource(subId, preStr, time, resourceTimeInfo);
					}
				}
			}
		}
	}
	
	public void writeToFile(String rescalFolder) throws IOException{
		Iterator<Entry<Integer, LinkedList<SubAndObj>>> iterator = subAndObjMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer, LinkedList<SubAndObj>> entry = iterator.next();
			int preId = entry.getKey();
			String colFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-cols";
			String rowFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-rows";
			FileWriter fw1 = new FileWriter(new File(colFilePath), true);
			FileWriter fw2 = new FileWriter(new File(rowFilePath), true);
		    LinkedList<SubAndObj> list = iterator.next().getValue();
		    for(SubAndObj item:list){
		    	fw1.write(item.getObjId()+ " ");
		    	fw2.write(item.getSubId()+ " ");
		    //	tripleFile.write(subId + " " + preId + " " + objId + "\n");
		    }
		    fw1.close();
		    fw2.close();
		}
	}
	
	/**
	 * 将时间抽取结果写入文档
	 * @param timeExtractionFileName
	 * @throws IOException
	 */
    public void writeToFile2(String timeExtractionFileName ) throws IOException{
    	//结果写入文档
	    String dstPath = timeExtractionPath + timeExtractionFileName + ".txt";
		FileWriter fileWriter = new FileWriter(new File(dstPath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		// bufferedWriter.write(substr + " " + );
		 Iterator<Entry<Integer, ResourceInfo>> iter = resourceTimeInfo.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, ResourceInfo> entry = iter.next();
			Integer key = entry.getKey();
			System.out.println("current key " + key);
			String uri = rRMap.get(key);
			bufferedWriter.write(key + ": " + uri + " " + ((ResourceInfo) entry.getValue()).toString());
			bufferedWriter.newLine();
		}
		bufferedWriter.close();
		//return resourceTimeInfo;
  }
    
    public static void main(String[] args) throws IOException {
		// 读取目录路径
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/DBLP.org", "dba", "dba");
		new TripleProcess3().processTripleFromVirtuoso(dataset, "DBLP12M18", "DBLP12M18");
		long t2 = System.currentTimeMillis();
			double timeCost = (t2 - t1) / 1000.0;
			System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
}
