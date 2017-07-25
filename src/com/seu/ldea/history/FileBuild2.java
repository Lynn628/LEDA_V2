package com.seu.ldea.history;

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
 * ��FileBuild�����϶Զ�ȡ��RDF�ļ�����Ԥ������ʹJena�ܽ����ļ�
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

		// ԭʼδ��������RDF�ļ���·��
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
			model.read(cleanedFilePath, "RDf/XML");
		} else if (cleanedFilePath.endsWith("ttl")) {
			model.read(cleanedFilePath, "TTL");
		}
		proecssFile(ds.getDefaultModel(), fileName);
		scanner.close();
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
		String directoryPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\";
		File dir = new File(directoryPath + fileName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// Model model = TDBUtil.getTDBModel();
		  FileWriter entityFile = new FileWriter(new File(directoryPath + fileName + "\\entity-ids"), true);		
		  FileWriter wordsFile = new FileWriter(new File(directoryPath + fileName + "\\words"), true);
		  FileWriter tripleFile = new FileWriter(new File(directoryPath + fileName + "\\triple"), true);
	
		// �洢resource id��map
		HashMap<String, Integer> rMap = new HashMap<>();
		// �洢predicate id��map
		HashMap<String, Integer> pMap = new HashMap<>();
		int rNum = 0;
		int pNum = 1;
		StmtIterator stmtIterator = model.listStatements();
		System.out.println("Model Size " + model.size());
		String rowFilePath = null;
		String colFilePath = null;
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
          
           //�������������������Ҫ�޸ģ�ʹ��predicate�ܹ�����
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
				colFilePath = directoryPath + fileName + "\\" + preId + "-cols";
				rowFilePath = directoryPath + fileName + "\\" + preId + "-rows";
				tripleFile.write(subId + " " + preId + " " + objId + "\n");

				FileWriter fw1 = new FileWriter(new File(colFilePath), true);
				// BufferedWriter bw1 = new BufferedWriter(fw1);
				FileWriter fw2 = new FileWriter(new File(rowFilePath), true);
				/*
				 * BufferedWriter bw2 = new BufferedWriter(fw2); bw1.write(subId
				 * + " "); bw2.write(objId + " ");
				 */
				//col file�洢���Ǳ��row�ļ��洢��������
				fw1.write(objId + " ");
				fw2.write(subId + " ");
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
				// �߽���ǰ��Ԫ��������ݿ��triple�����߽���Ԫ��ı��д���Ӧ�ļ���
				MySqlUtil.insertTriple2(subStr, preStr, objStr);
			}
		} // C:\Users\Lynn\Desktop\Academic\LinkedDataProject\DataSet\BTChallenge2014\data0.nq
	}// data0modified.nq
}