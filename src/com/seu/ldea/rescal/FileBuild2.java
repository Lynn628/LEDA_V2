package com.seu.ldea.rescal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
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

import com.seu.ldea.util.MySqlUtil;
import com.seu.ldea.util.PreProcessRDF;
import com.seu.ldea.util.TDBUtil;

/**
 * ��FileBuild�����϶Զ�ȡ��RDF�ļ�����Ԥ����ʹJena�ܽ����ļ�
 * ����Ҫ���������nq�ļ������model sizeΪ0
 * @author Lynn
 *
 */
public class FileBuild2 {
	public static void main(String[] args) throws IOException, SQLException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please give a name to TDB");
		String tdbPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TDB\\" + scanner.nextLine() + "TDB";
		System.out.println("Please give the file path to process");

		// ԭʼδ�������RDF�ļ���·��
		String rawFilePath = scanner.nextLine();
		int indexBegin = rawFilePath.lastIndexOf("\\");
		int indexEnd = rawFilePath.lastIndexOf(".");
       //�����ļ�·����ȡ�ļ���
		String fileName = rawFilePath.substring(indexBegin + 1, indexEnd);
		long t1 = System.currentTimeMillis();
		System.out.println("*******fileName ********" + fileName);
		// ��ϴRDF�ļ��������µ��ļ�,�޳�Jena����ʶ���|�������<
		String cleanedFilePath = PreProcessRDF.PreProcessRDFFile(rawFilePath, fileName);
		System.out.println(cleanedFilePath);
		Dataset ds = TDBFactory.createDataset(tdbPath);
		//TDB.getContext().set(TDB.symUnionDefaultGraph, true);
		//ds.addNamedModel(arg0, arg1);
		//Model model = ds.getNamedModel("");
		Model model = ds.getDefaultModel();
		if (rawFilePath.endsWith("nq")) {
		    model.read(cleanedFilePath, "N-Quads");
		} else if (cleanedFilePath.endsWith("rdf")) {
			//model.read(cleanedFilePath, "RDF/XML");
			 FileManager.get().loadModel(cleanedFilePath, "RDf/XML");
		} else if (cleanedFilePath.endsWith("ttl")) {
			 FileManager.get().loadModel(cleanedFilePath, "TTL");
		}
		proecssFile(ds.getDefaultModel(), fileName);
		scanner.close();
		/*
		 * proecssFile(); long t2 = System.currentTimeMillis();
		 * System.out.println("process total file time: "+ (t2-t1)/3600000 +
		 * "h");
		 */
		//C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\SWCC\eswc-2013-15-complete.rdf
		// TopKPredicate.getTopKPredicateFile(60,
		// "D:\\rescalInputFile\\Dbpedia2016-2017-5-11\\triple");
		// TopKPredicate.makeTopKInputFile(60,
		// "D:\\rescalInputFile\\Dbpedia2016-2017-5-11\\triple");
		model.close();
		ds.close();
		long t2 = System.currentTimeMillis();
		System.out.println("create sub input file time: " + (t2 - t1) / 3600000.0 + "h");
	}

	/**
	 * �����ļ�������setȥ��
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void proecssFile(Model model, String fileName) throws IOException, SQLException {
		File dir = new File("D:\\rescalInputFile\\" + fileName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// Model model = TDBUtil.getTDBModel();
		  FileWriter entityFile = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\" + fileName + "\\entity-ids", true);		
		  FileWriter wordsFile = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\" + fileName + "\\words", true);
		  FileWriter tripleFile = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\" + fileName + "\\triple", true);
			/*FileWriter entityFile = new FileWriter("D:\\rescalInputFile\\" + fileName + "\\entity-ids", true);		
		/*FileWriter entityFile = new FileWriter("D:\\rescalInputFile\\" + path + "\\entity-ids", true);
		FileWriter wordsFile = new FileWriter("D:\\rescalInputFile\\" + path + "\\words", true);
		FileWriter tripleFile = new FileWriter("D:\\rescalInputFile\\" + path + "\\triple", true);*/
		// �洢resource id��map
		HashMap<String, Integer> rMap = new HashMap<>();
		// �洢predicate id��map
		HashMap<String, Integer> pMap = new HashMap<>();
		int rNum = 0;
		int pNum = 1;
		StmtIterator stmtIterator = model.listStatements();
		System.out.println("Model Size " + model.size());
		String rowFileName = null;
		String colFileName = null;
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			RDFNode sub = statement.getSubject();
			String subStr = sub.toString();
			int subId = -1;
			int objId = -1;
			int preId = -1;
			Property pre = statement.getPredicate();
			String preStr = pre.toString();

			RDFNode obj = statement.getObject();
			String objStr = obj.toString();

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
				if (!pMap.containsKey(preStr)) {
					pMap.put(preStr, pNum);
					preId = pNum;
					pNum++;
					wordsFile.write(preId + ":" + preStr + "\n");
				} else {
					preId = pMap.get(preStr);
				}
				colFileName = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\" + fileName + "\\" + preId + "-cols";
				rowFileName = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\" + fileName + "\\" + preId + "-rows";
				tripleFile.write(subId + " " + preId + " " + objId + "\n");

				FileWriter fw1 = new FileWriter(colFileName, true);
				// BufferedWriter bw1 = new BufferedWriter(fw1);
				FileWriter fw2 = new FileWriter(rowFileName, true);
				/*
				 * BufferedWriter bw2 = new BufferedWriter(fw2); bw1.write(subId
				 * + " "); bw2.write(objId + " ");
				 */
				fw1.write(subId + " ");
				fw2.write(objId + " ");
				fw1.close();
				fw2.close();
			}
		}
		wordsFile.close();
		entityFile.close();
		tripleFile.close();
	}

	/**
	 * �����ļ����������ݿ�ȥ��
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void proecssFileDB() throws IOException, SQLException {

		Model model = TDBUtil.getTDBModel();
		StmtIterator stmtIterator = model.listStatements();
		System.out.println("Model Size " + model.size());

		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			// System.out.println(statement.toString());
			RDFNode sub = statement.getSubject();
			String subStr = sub.toString();

			Property pre = statement.getPredicate();
			String preStr = pre.toString();

			RDFNode obj = statement.getObject();
			String objStr = obj.toString();

			if (sub instanceof Resource && obj instanceof Resource) {
				MySqlUtil.insertMap(subStr, "resource");
				// ����������resource��predicate�������
				MySqlUtil.insertMap(preStr, "predicate");
				MySqlUtil.insertMap(objStr, "resource");
				// �߽���ǰ��Ԫ��������ݿ��triple���߽���Ԫ��ı��д���Ӧ�ļ���
				MySqlUtil.insertTriple2(subStr, preStr, objStr);
			}
		} // C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\BTChallenge2014\data0.nq
	}// data0modified.nq
}
