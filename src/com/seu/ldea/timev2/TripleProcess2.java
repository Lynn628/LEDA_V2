package com.seu.ldea.timev2;

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
import com.seu.ldea.timev2.TripleProcess3.SubAndObj;
import com.seu.ldea.virtuoso.SparqlQuery;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.process.WordToSentenceProcessor.NewlineIsSentenceBreak;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

/**
 * ����tripleProcess�Ļ����Ľ�һ�£���һ�����ݽṹ���洢��Ӧ��prdicate pid������ͱ���
 * @author Lynn
 *
 */
public class TripleProcess2 {
	static String rescalDirPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\";
	static String timeExtractionPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\" ;
	//�洢resource��url��id��ӳ��
	HashMap<String, Integer> rMap = new HashMap<>();
	//�洢predicate��url��id��ӳ��
	HashMap<String, Integer> pMap = new HashMap<>();
	//id��resource��url��ӳ��
	HashMap<Integer,String> rRMap = new HashMap<>();
	//�洢ÿ����Դ ����ʱ����Ϣ
	public HashMap<Integer, ResourceInfo> resourceTimeInfo = new HashMap<>();
	
	public HashMap<Integer, LinkedList<SubAndObj>> subAndObjMap = new HashMap<>();
	//��Ԫ���ļ�
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
	 * @param dataset---���ݼ��ļ���virtuoso������Ϣ
	 * @param rescalFolder--- row col�ļ���ŵ�ַ
	 * @param timeExtractionFileName --- ʱ���ȡ�ļ����λ��
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
	    tripleFile = new FileWriter(new File(rescalDirPath + rescalFolder + "\\triple"), true);
	    
		int rNum = 0;//rNum���ڱ��resource�ı��
		int pNum = 1;//pNum���ڱ��words�ļ��е�predicate
		//�����ش�ʧ��֮��wordsҲҪ��0��ʼ����pNum = 0
		long tripleNum = 0;
		 while(resultSet.hasNext()){
			 System.out.println(++tripleNum);
				// System.out.println("test3");
				 int subId = -1;
				 int objId = -1;
				 int preId = -1;
        //��ȡ��һ����Ԫ��
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
		    	   if(!rMap.containsKey(subStr)){
		    		   rMap.put(subStr, rNum);
		    		   rRMap.put(rNum, subStr);
		    		   subId = rNum;
		    		   entityFile.write(rNum + ":" + subStr + "\n");
		    		   rNum++;
		    	   }else{
		    		   subId = rMap.get(subStr);
		    	   }
		    	   if(obj instanceof Resource){
		    		   if (!rMap.containsKey(objStr)) {
							rMap.put(objStr, rNum);
							rRMap.put(rNum, objStr);
							objId = rNum;
							entityFile.write(rNum + ":" + objStr + "\n");
							rNum++;
						} else {
							objId = rMap.get(objStr);
						}
				//�洢col row��ӳ���ţ�����subAndObjMap
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
			//��ȡ��ǰ��Ԫ���ʱ����Ϣ
			tripleTimeExtraction(timeExtractionFileName, subStr, preStr, obj);
		 }		
     }
		 entityFile.close();
		 wordsFile.close();
		 tripleFile.close();
		 writeToFile(rescalFolder);
		 //���������е���Ԫ�飬��resourceTimeInfo����Ϣд���ĵ���
	     writeToFile2(timeExtractionFileName);	 
    }
    
   /**
    * ���ݵ�ǰ��Ԫ���id����rescal�������ļ�col row
    * @param subId
    * @param preId
    * @param objId
    * @param rescalFolder
    * @throws IOException
    */
	public void makeRescalInput(int subId, int preId, int objId, String rescalFolder) throws IOException {
		// д����Ӧ��row col�ļ���
		/*String colFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-cols";
		String rowFilePath = rescalDirPath + rescalFolder + "\\" + preId + "-rows";
		tripleFile.write(subId + " " + preId + " " + objId + "\n");

		FileWriter fw1 = new FileWriter(new File(colFilePath), true);
		FileWriter fw2 = new FileWriter(new File(rowFilePath), true);
		// col file�洢���Ǳ��row�ļ��洢��������
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
	 * ��ȡ����ǰ��Ԫ���ʱ����Ϣ
	 * @param timeExtractionFolder
	 * @param subStr
	 * @param preStr
	 * @param obj
	 */
	public void tripleTimeExtraction(String timeExtractionFileName, String subStr, String preStr, RDFNode obj) {
		int subId = rMap.get(subStr);
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
					// �����֤ʱ���ʽ����Ч��
					TimeExtractionUtil.labelResource(subId, "createdDate", time, resourceTimeInfo);
				}
			}
		}

		// ���ν����label comment��Щ���򲻶Ժ���ı�����г�ȡ
		if (!preStr.equals("http://www.w3.org/2000/01/rdf-schema#label")
				&& !preStr.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
			String objStr = obj.toString();
			// ��objectΪURIʱ������URI����ȡ��������е�ʱ���
			if (obj instanceof Resource) {// ����resource
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
							// �����֤ʱ���ʽ����Ч��
							TimeExtractionUtil.labelResource(objId, "createdDate", time, resourceTimeInfo);
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
					String time = TimeExtractionUtil.getTimeInLiteral(list, objStr);
					// �����֤ʱ����Ч�ԵĲ���
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
	 * ��ʱ���ȡ���д���ĵ�
	 * @param timeExtractionFileName
	 * @throws IOException
	 */
    public void writeToFile2(String timeExtractionFileName ) throws IOException{
    	//���д���ĵ�
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
		// ��ȡĿ¼·��
		long t1 = System.currentTimeMillis();
		Dataset dataset = new Dataset("jdbc:virtuoso://localhost:1111", "http://LDEA/DBLP.org", "dba", "dba");
		new TripleProcess2().processTripleFromVirtuoso(dataset, "DBLP12M18", "DBLP12M18");
		long t2 = System.currentTimeMillis();
			double timeCost = (t2 - t1) / 1000.0;
			System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
}
