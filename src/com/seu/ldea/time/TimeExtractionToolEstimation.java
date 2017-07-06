package com.seu.ldea.time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.seu.ldea.cluster.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.query.SparqlQuery;
import com.seu.ldea.util.SUTimeTool2;
import com.seu.ldea.util.URIUtil;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

/**
 * 6/7/2017 计算评估时间抽取函数的准确率与召回率 完成原始文件与目标文件的生成 Jena解析生成RDF三元组原文件， 时间抽取函数生成目标文件
 * 
 * @author Lynn
 *
 */
/**
 * 6/29/2017 逐条检查三元组抽取出的时间信息
 * 时间抽取工具： 
 * Open Spatial Extraction and Tagging (OpenSextant) software：https://github.com/OpenSextant/opensextant 
 *
 */
public class TimeExtractionToolEstimation {
	/**
	 * Resource map,存储资源和资源的id
	 */
	// public static HashMap<String, Integer> resourceMap = new HashMap<>();
	public static HashMap<String, Integer> rMap;
	/**
	 * Predicate map,存储谓语和谓语的id
	 */
	// public static HashMap<String, Integer> predicateMap = new HashMap<>();
	public static HashMap<String, Integer> pMap;

	// 存储主语id和谓语id和时间区间set，即每个resource的时间信息
	public static HashMap<Integer, HashMap<Integer, HashSet<TimeSpan>>> timeInfoMap = new HashMap<>();
	/**
	 * 每一个资源对象存在一个关于其时间信息的描述信息
	 */
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	
	/**
	 * 
	 * @param dataset
	 * @param dstName
	 * @param dirPath
	 *            存放entity-id和words的文件夹地址
	 * @throws IOException
	 */
	public static void timeExtraction(Dataset dataset, String dstName, String dirPath) throws IOException {

		AnnotationPipeline pipeline = SUTimeTool2.PipeInit();
		ResultSet resultSet = SparqlQuery.getAllTriplesResultSet(dataset);
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//timetoolEstimation//" + dstName
				+ ".txt";
		FileWriter fileWriter = new FileWriter(new File(dstPath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		/*
		 * rMap = dataset.getrMap(); pMap = dataset.getpMap();
		 */
		rMap = LabelResourceWithTimeTest.getNodeIdMap(1, dirPath);
		pMap = LabelResourceWithTimeTest.getNodeIdMap(2, dirPath);
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
		
			bufferedWriter.write(tripleNum + "   " + subStr + "   " + preStr + "   " +objStr);
			bufferedWriter.newLine();
			// 判断该URI是否需要进行时间抽取，比如包含www.org之类的，比如包含连续字符数字的则无需进行时间抽取
			if (URIUtil.judgeURI(subStr)) {
				// 将subject的URI进行字符串处理，拆分开
				subStr = URIUtil.processURI(subStr);
				// **************时间抽取区域*********************
				// 对资源URI进行时间信息的抽取
				List<CoreMap> subTimeList = SUTimeTool2.SUTimeJudgeFunc(pipeline, subStr);
				// 如果主语URI包含时间信息
				System.out.println("subTimeList size " + subTimeList.size());
				if (!subTimeList.isEmpty()) {
					// 若当前节点没有贴上时间标签
					System.out.println("-------IN sub judge-----");
					for (CoreMap cm : subTimeList) {
						// labelResource(currentRId, -1, cm);
						String time2 = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
						//LabelResourceWithTimeTest.labelResource(subId, "createdDate", time);
						bufferedWriter.write("subStr---" + time2);
						bufferedWriter.newLine();
						System.out.println("SubURI " + time2);
					}
					bufferedWriter.flush();
				}
			}
			if(!preStr.equals("http://www.w3.org/2000/01/rdf-schema#label") &&
					!preStr.equals("http://www.w3.org/2000/01/rdf-schema#comment")){
			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (obj instanceof Resource) {
				if (URIUtil.judgeURI(objStr)) {
					// **************时间抽取区域*********************
					objStr = URIUtil.processURI(objStr);
					List<CoreMap> objTimeList = SUTimeTool2.SUTimeJudgeFunc(pipeline, objStr);
						for (CoreMap cm : objTimeList) {
							String time1 = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
							//LabelResourceWithTimeTest.labelResource(objId, "createdDate", time);
							 bufferedWriter.write("objStr1---" + time1);
							 bufferedWriter.newLine();
							 System.out.println("ObjURI " + time1);
						}
						bufferedWriter.flush();
				}
			} else {
				// 当Object为字符串
			    List<CoreMap> list = SUTimeTool2.SUTimeJudgeFunc(pipeline, objStr);
				if (!list.isEmpty()) {				
					String time = LabelResourceWithTimeTest.getTimeInLiteral(list, objStr);
				      if(!time.equals("")){
						//LabelResourceWithTimeTest.labelResource(subId, preStr, time);
				    	  bufferedWriter.write("objStr2---" + time);
						  bufferedWriter.newLine();
				     }
				}
			}
			bufferedWriter.write("---");
			bufferedWriter.newLine();
			bufferedWriter.flush();
		 }
		}
		bufferedWriter.close();
		// return resourceTimeInfo;
		// System.out.println("Statement Number: " + lineNum);
	}

	public static void main(String[] args) throws IOException {
		// 读取目录路径
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/SWCC.org", "dba", "dba");
		timeExtraction(dataset, "SWCCTimeExtractionEstimation4",
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
		long t2 = System.currentTimeMillis();
			double timeCost = (t2 - t1) / 1000.0;
			System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
	//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\BTChallenge2014\data0.nq
	//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
}
