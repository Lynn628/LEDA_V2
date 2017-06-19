package com.seu.ldea.time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

import com.seu.ldea.util.PreProcessRDF;
import com.seu.ldea.util.ReadFilePath;
import com.seu.ldea.util.SUTimeTool2;
import com.seu.ldea.util.URIUtil;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.util.CoreMap;

/**
 * 6/7/2017 计算评估时间抽取函数的准确率与召回率 完成原始文件与目标文件的生成 Jena解析生成RDF三元组原文件， 时间抽取函数生成目标文件
 * 
 * @author Lynn
 *
 */
/**
 * 
 * 时间抽取工具： 
 * Open Spatial Extraction and Tagging (OpenSextant) software：https://github.com/OpenSextant/opensextant 
 *
 */
public class TimeExtractionToolEstimation {

	public static void main(String[] args) throws IOException {
		// 读取目录路径
		long t1 = System.currentTimeMillis();
		Scanner scanner = new Scanner(System.in);
		// TDB的名字
		System.out.println("Please give TDB name ");
		String tdb = scanner.nextLine();
		// 要读取的目录的名字
		System.out.println("Input the directory path:\n");
		String dirPath = scanner.nextLine();
		System.out.println("Please give Name to time extraction result file  ");
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//"
				+ scanner.nextLine() + ".txt";
		scanner.close();
		// 读取目录路径
		String tdbName = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TDB//" + tdb + "TDB";
		Dataset ds = TDBFactory.createDataset(tdbName);
		Model model = ds.getDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		ArrayList<String> filePathList = ReadFilePath.readDir(dirPath);
		Iterator<String> iterator = filePathList.iterator();

		// 对每一个rdf文档进行处理
		while (iterator.hasNext()) {
			// 源文件路径
			String filePath = iterator.next();
			//预处理文件，替换Jena无法识别的字符
			//截取处理过的文件名
			int indexBegin = filePath.lastIndexOf("/");
			int indexEnd = filePath.lastIndexOf(".");
			String fileName = "processed_" +filePath.substring(indexBegin + 1, indexEnd);
			//System.out.println("*******New fileName ********" + fileName);
			String newFilePath = PreProcessRDF.PreProcessRDFFile(filePath, fileName);
		    //读取model
           // model.read(newFilePath);
		    RDFDataMgr.read(ds,newFilePath, Lang.NQUADS);
			//FileManager.get().readModel(model, newFilePath, "N-Quads");
            System.out.println(ds.getDefaultModel().size());
		}
		//ds.addNamedModel("http://btc2014/data0.nq", model);
		
      //s  System.out.println("Model size is " + ds.getDefaultModel().size());
		//timeExtraction(model, dstPath);
			long t2 = System.currentTimeMillis();
			double timeCost = (t2 - t1) / 1000.0;
			System.out.println("End of main~~~~~~time cost " + timeCost + "s");
	}
	
	public static void  timeExtraction(Model model, String dstPath) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(dstPath));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		AnnotationPipeline pipeline = SUTimeTool2.PipeInit();
		// 获取model的statement
		StmtIterator iterator = model.listStatements();
		int id = 0;
		while (iterator.hasNext()) {
			id++;
			Statement statement = iterator.next();
			// System.out.println(statement.toString());
			Resource resource = statement.getSubject();
			String resourceStr = resource.toString();
			System.out.println(statement.toString());
		    bufferedWriter.write(statement.toString());
		    bufferedWriter.newLine();
 
			// 将subject的URI进行字符串处理
			String newSub = URIUtil.processURI(resourceStr);
			//**************时间抽取区域*********************
			// 对资源URI进行时间信息的抽取
			List<CoreMap> subURIList = SUTimeTool2.SUTimeJudgeFunc(pipeline, newSub);
			//************************************
			// 如果主语URI包含时间信息
			bufferedWriter.write(id + ":" + resourceStr + " ");
					for (CoreMap cm : subURIList) {
					//	labelResource(currentRId, -1, cm);
					bufferedWriter.write("<br> " + cm.toString() + "</br>   ");
					}
			
			Property property = statement.getPredicate();
			RDFNode object = statement.getObject();
			String propertyStr = property.toString();
			String objectStr = object.toString();
            bufferedWriter.write("; " + propertyStr + ";" + objectStr + " ");
			/**
			 * 判断Predicate，剔除干扰的时间属性(type)
			 */
            
            
			// 当object为URI时，处理URI，提取并输出其中的时间词
			if (object instanceof Resource) {
				//**************时间抽取区域*********************
				// 对Object时间信息的抽取可以用一个方法封装起来，替换URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(object.toString());
				List<CoreMap> objURIList = SUTimeTool2.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					for (CoreMap cm : objURIList) {
					bufferedWriter.write(cm.toString() + " ");
					}
				}

			} else {
				// 当Object为字符串
				//**************时间抽取区域*********************
				// 判断时间信息的有效性，占整个literal的比重，其次考虑时间信息表达的规范性
				
				//***************************
				List<CoreMap> list = SUTimeTool2.SUTimeJudgeFunc(pipeline, objectStr);
				
				if (!list.isEmpty()) {
					for (CoreMap cm : list) {
						bufferedWriter.write(cm.toString() + " ");
					}
				}
			}
			bufferedWriter.newLine();
			bufferedWriter.newLine();
		}
            bufferedWriter.close();
		
	// System.out.println("Statement Number: " + lineNum);
	}
	//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\BTChallenge2014\data0.nq
	//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
}
