package com.seu.ldea.rescal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.time.LabelResourceWithTimeStatic;
import com.seu.ldea.virtuoso.SparqlQuery;
/**
 * 从Virtuoso读取三元组，创建RESCAL的输入文件
 * @author Lynn
 *
 */
public class RescalInputBulid {
    
	
	/**
	 * 处理文件，利用set去重
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void rescalInputBuild(Dataset dataset, String fileName) throws IOException, SQLException {
	   ResultSet resultSet = SparqlQuery.getAllTriplesResultSet(dataset);
		String directoryPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\";
		
		File dir = new File(directoryPath + fileName);
		if (!dir.exists()) {
			dir.mkdir();
		}
	
		  FileWriter entityFile = new FileWriter(new File(directoryPath + fileName + "\\entity-ids"), true);		
		  FileWriter wordsFile = new FileWriter(new File(directoryPath + fileName + "\\words"), true);
		  FileWriter tripleFile = new FileWriter(new File(directoryPath + fileName + "\\triple"), true);
	
		// 存储resource-id的map
		HashMap<String, Integer> rMap = new HashMap<>();
		// 存储predicate-id的map
		HashMap<String, Integer> pMap = new HashMap<>();
	
		int rNum = 0;
		int pNum = 1;
		long tripleNum = 0;
		String rowFilePath = null;
		String colFilePath = null;
		 while(resultSet.hasNext()){
			  tripleNum++;
			    System.out.println(tripleNum);
				// System.out.println("test3");
				 int subId = -1;
				 int objId = -1;
				 int preId = -1;
				 QuerySolution result = resultSet.nextSolution();
				 RDFNode sub = result.get("s");
				 String subStr = sub.toString();
				 RDFNode pre = result.get("p"); 
				 String preStr = pre.toString();
				 RDFNode obj = result.get("o");
				 String objStr = obj.toString();
 
				 System.out.println("----");
				 //构造谓语词映射
				 if (!pMap.containsKey(preStr)) {
						pMap.put(preStr, pNum);
						preId = pNum;
						pNum++;
						wordsFile.write(preId + ":" + preStr + "\n");
					} else {
						preId = pMap.get(preStr);
					}
				 
				if (sub instanceof Resource && obj instanceof Resource) {
					if (!rMap.containsKey(subStr)) {
						rMap.put(subStr, rNum);
						subId = rNum;
						entityFile.write(rNum + ":" + subStr + "\n");
						rNum++;
					} else {
						subId = rMap.get(subStr);
					}
					if (!rMap.containsKey(objStr)) {
						rMap.put(objStr, rNum);
						objId = rNum;
						entityFile.write(rNum + ":" + objStr + "\n");
						rNum++;
					} else {
						objId = rMap.get(objStr);
					}
					
					colFilePath = directoryPath + fileName + "\\" + preId + "-cols";
					rowFilePath = directoryPath + fileName + "\\" + preId + "-rows";
					tripleFile.write(subId + " " + preId + " " + objId + "\n");

					FileWriter fw1 = new FileWriter(new File(colFilePath), true);
					// BufferedWriter bw1 = new BufferedWriter(fw1);
					FileWriter fw2 = new FileWriter(new File(rowFilePath), true);
					
					//col file存储的是宾语，row文件存储的是主语
					fw1.write(objId + " ");
					fw2.write(subId + " ");
					fw1.close();
					fw2.close();
				}
			}
			wordsFile.close();
			entityFile.close();
			tripleFile.close();
			System.out.println(rMap.size() + "size ");
            dataset.setrMap(rMap);			
			dataset.setpMap(pMap);
		 }
	
	
	public static void main(String[] args) throws IOException, SQLException{
		long t1 = System.currentTimeMillis();
		 String url = "jdbc:virtuoso://localhost:1111";
		/* System.out.println("Please give the graph Name ");
		 Scanner sc = new Scanner(System.in);
		 String graphName = sc.nextLine();
		 System.out.println("Please give the directory name ");
		 //fileName处理后的文件文件夹的名字
		 String dirName = sc.nextLine();
		 System.out.println("Please give fileName of Time info");
		 String fileName = sc.nextLine();
		 sc.close();*/
		 String graphName = "http://LDEA/Jamendo.org";
		 String rescalFileName ="Jamendo";
		 String rescalFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\"+ rescalFileName;
		 String timeFileName ="Jamendo-ResourcePTMap0724";
		 Dataset dataset = new Dataset(url, graphName, "dba", "dba");
		 
		/* VirtGraph graph = new VirtGraph(graphName, url, "dba", "dba");
		 
		 Query sparql = QueryFactory.create("select ?s ?p ?o where {?s ?p ?o}");
		 VirtuosoQueryExecution virtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql,graph);
		// System.out.println("test2");
		 ResultSet resultSet = virtuosoQueryExecution.execSelect();*/
		 rescalInputBuild(dataset , rescalFileName);
		 System.out.println(dataset.getpMap().size());
		 LabelResourceWithTimeStatic.timeExtraction(dataset, timeFileName, rescalFilePath);
		 long t2 = System.currentTimeMillis();
		 System.out.println(" time cost " + (t2-t1)/1000);
	  }
	}
	//"http://LDEA/SWCC.org"

