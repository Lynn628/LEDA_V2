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
 * 6/7/2017 ��������ʱ���ȡ������׼ȷ�����ٻ��� ���ԭʼ�ļ���Ŀ���ļ������� Jena��������RDF��Ԫ��ԭ�ļ��� ʱ���ȡ��������Ŀ���ļ�
 * 
 * @author Lynn
 *
 */
/**
 * 
 * ʱ���ȡ���ߣ� 
 * Open Spatial Extraction and Tagging (OpenSextant) software��https://github.com/OpenSextant/opensextant 
 *
 */
public class TimeExtractionToolEstimation {

	public static void main(String[] args) throws IOException {
		// ��ȡĿ¼·��
		long t1 = System.currentTimeMillis();
		Scanner scanner = new Scanner(System.in);
		// TDB������
		System.out.println("Please give TDB name ");
		String tdb = scanner.nextLine();
		// Ҫ��ȡ��Ŀ¼������
		System.out.println("Input the directory path:\n");
		String dirPath = scanner.nextLine();
		System.out.println("Please give Name to time extraction result file  ");
		String dstPath = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TimeExtractionResultFile//"
				+ scanner.nextLine() + ".txt";
		scanner.close();
		// ��ȡĿ¼·��
		String tdbName = "C://Users//Lynn//Desktop//Academic//LinkedDataProject//TDB//" + tdb + "TDB";
		Dataset ds = TDBFactory.createDataset(tdbName);
		Model model = ds.getDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		ArrayList<String> filePathList = ReadFilePath.readDir(dirPath);
		Iterator<String> iterator = filePathList.iterator();

		// ��ÿһ��rdf�ĵ����д���
		while (iterator.hasNext()) {
			// Դ�ļ�·��
			String filePath = iterator.next();
			//Ԥ�����ļ����滻Jena�޷�ʶ����ַ�
			//��ȡ��������ļ���
			int indexBegin = filePath.lastIndexOf("/");
			int indexEnd = filePath.lastIndexOf(".");
			String fileName = "processed_" +filePath.substring(indexBegin + 1, indexEnd);
			//System.out.println("*******New fileName ********" + fileName);
			String newFilePath = PreProcessRDF.PreProcessRDFFile(filePath, fileName);
		    //��ȡmodel
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
		// ��ȡmodel��statement
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
 
			// ��subject��URI�����ַ�������
			String newSub = URIUtil.processURI(resourceStr);
			//**************ʱ���ȡ����*********************
			// ����ԴURI����ʱ����Ϣ�ĳ�ȡ
			List<CoreMap> subURIList = SUTimeTool2.SUTimeJudgeFunc(pipeline, newSub);
			//************************************
			// �������URI����ʱ����Ϣ
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
			 * �ж�Predicate���޳����ŵ�ʱ������(type)
			 */
            
            
			// ��objectΪURIʱ������URI����ȡ��������е�ʱ���
			if (object instanceof Resource) {
				//**************ʱ���ȡ����*********************
				// ��Objectʱ����Ϣ�ĳ�ȡ������һ��������װ�������滻URIUtil.processURI,SUTimeJudge
				String newObj = URIUtil.processURI(object.toString());
				List<CoreMap> objURIList = SUTimeTool2.SUTimeJudgeFunc(pipeline, newObj);
				//************************************
				if (!objURIList.isEmpty()) {
					for (CoreMap cm : objURIList) {
					bufferedWriter.write(cm.toString() + " ");
					}
				}

			} else {
				// ��ObjectΪ�ַ���
				//**************ʱ���ȡ����*********************
				// �ж�ʱ����Ϣ����Ч�ԣ�ռ����literal�ı��أ���ο���ʱ����Ϣ���Ĺ淶��
				
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
