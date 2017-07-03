package com.seu.ldea.rescal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class TopKPredicate {

	/**
	 * 处理三元组编号文档，获取使用最多的top k谓语
	 * 
	 * @param k
	 * @param filePath,triple文件
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, Integer> getTopKPredicate(int k, String filePath) throws IOException {
		File input = new File(filePath);
		// System.out.println("Is exist " + input.exists());
		FileReader fileReader = new FileReader(input);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		// 存储<oldId, frequency> 的map
		HashMap<Integer, Integer> predicateMap = new HashMap<>();

		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			String[] trilpleArr = line.split(" ");
			// 获取谓语的id
			int preNum = Integer.parseInt(trilpleArr[1]);
			if (predicateMap.containsKey(preNum)) {
				// 目前有多少个
				int freq = predicateMap.get(preNum);
				// 当前predicate频数加一
				predicateMap.put(preNum, ++freq);
			} else {
				// 未收录过此predicate，则将predicate插入map，频数置1
				predicateMap.put(preNum, 1);
			}
		}
		bufferedReader.close();
		for (Entry<Integer, Integer> entry : predicateMap.entrySet()) {
			System.out.println(" Predicate " + entry.getKey() + "frequence " + entry.getValue());
		}
		// hashMap 排序完成后获取前k个predicate 以ArrayList返回
		return sortedTopKPredicate(k, predicateMap);
	}

	/**
	 * 返回排好序的 predicate 
	 * @param k
	 * @param predicateMap
	 * @return
	 */
	public static HashMap<Integer, Integer> sortedTopKPredicate(Integer k, HashMap<Integer, Integer> predicateMap) {
		ArrayList<Entry<Integer, Integer>> list_data = new ArrayList<Entry<Integer, Integer>>(predicateMap.entrySet());
		// 存储top k个predicate <oldId, newId>
		HashMap<Integer, Integer> topPreMap = new HashMap<>();
		Collections.sort(list_data, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				// 降序排序
				return o2.getValue() - o1.getValue();
			}
		});

		for (int i = 1; i <= k; i++) {
			topPreMap.put(list_data.get(i - 1).getKey(), i);
			System.out.println("old " + list_data.get(i - 1).getKey() + " new " + i);
		}

		/*
		 * for(Entry<Integer, Integer> entry : topPreMap.entrySet()){
		 * System.out.println("ol " + entry.getKey() + "" + entry.getValue()); }
		 */
		return topPreMap;
	}

	/**
	 * 创建top K 个谓语的子文件
	 * 
	 * @throws IOException
	 * 
	 */
	public static void makeTopKInputFile(int k, String filePath) throws IOException {
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Top"+ String.valueOf(k);
		//自动创建TopK文件夹
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// 存储predicate <oldId, newId>的映射
		HashMap<Integer, Integer> predicateMap = getTopKPredicate(k, filePath);
		// 存放resource <oldId, newId>的Map
		HashMap<Integer, Integer> resourceMap = new HashMap<>();
		//存放新的三元组文件
		FileWriter tripleWriter = new FileWriter(path + "\\triple");
		BufferedWriter tripleBw = new BufferedWriter(tripleWriter);
		String rowFileName = "";
		String colFileName = "";
		FileReader fileReader = new FileReader(filePath);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		// resource id
		int newResourceId = 0;
		int lineNum = 0;
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(++lineNum);
			String[] lineArr = line.split(" ");

			int oldSubId = Integer.valueOf(lineArr[0]);
			int oldPreId = Integer.valueOf(lineArr[1]);
			int oldObjId = Integer.valueOf(lineArr[2]);
		   //写入triple文件的编码
			int subId = -1;
			//int preId = -1;
			int objId = -1;
			// 判断当前predicate是否满足条件
			if (predicateMap.containsKey(oldPreId)) {
				int newPreId = predicateMap.get(oldPreId);
				//preId = newPreId;
				colFileName = path + "\\" + newPreId + "-cols";
				rowFileName = path + "\\" + newPreId + "-rows";
				//tripleFile.write(subId + " " + preId + " " + objId + "\n");
				FileWriter fw1 = new FileWriter(colFileName, true);
				BufferedWriter bw1 = new BufferedWriter(fw1);
				// BufferedWriter bw1 = new BufferedWriter(fw1);
				FileWriter fw2 = new FileWriter(rowFileName, true);
				BufferedWriter bw2 = new BufferedWriter(fw2);
				// 写row文件，主语写入row文件中
				if (resourceMap.containsKey(oldSubId)) {
					/*
					 * fw1.write(resourceMap.get(oldSubId) + " "); fw1.close();
					 */
					subId = resourceMap.get(oldSubId);
					bw2.write(subId + " ");
					bw2.flush();
				} else {
					subId = newResourceId;
					resourceMap.put(oldSubId, newResourceId);
					
					bw2.write(subId + " ");
					bw2.flush();
					newResourceId++;
				}
				// 写col文件,宾语写入col文件中
				if (resourceMap.containsKey(oldObjId)) {
					/*
					 * fw2.write(resourceMap.get(oldObjId) + " "); fw2.close();
					 */
					objId = resourceMap.get(oldObjId);
					bw1.write(objId + " ");
					bw1.flush();
				} else {
					objId = newResourceId;
					resourceMap.put(oldObjId, newResourceId);
					bw1.write(objId + " ");
					bw1.flush();
					newResourceId++;
				}
				bw1.close();
				bw2.close();
				//将三元组写入文件
				tripleBw.write(subId + " " + newPreId + " " + objId);
				tripleBw.newLine();
			}
		}
		tripleBw.close();
		// 生成entity-id文件，存储Resource的映射
		FileWriter fileWriter1 = new FileWriter(path + "\\entity-ids", true);
		for (Entry<Integer, Integer> entry : resourceMap.entrySet()) {
			fileWriter1.write(entry.getValue() + " " + entry.getKey() + "\n");
			System.out.println("RNewId " + entry.getValue() + " ROldId " + entry.getKey());
		}
		fileWriter1.close();
		
		// 生成words文件，存储Predicate的映射
		FileWriter fileWriter2 = new FileWriter(path + "\\words", true);
		for (Entry<Integer, Integer> entry : predicateMap.entrySet()) {
			fileWriter2.write(entry.getValue() + " " + entry.getKey() + "\n");
			System.out.println("PNewId " + entry.getValue() + " POldId " + entry.getKey());
		}
		fileWriter2.close();
		bufferedReader.close();
	}

	public static void main(String[] args) {
		int i = 0;
		System.out.println(i++);
		int j = 0;
		System.out.println(++j);
	}

	/**
	 * 利用数据库 获取使用的最多的 top k 个Predicate,将top k的predicate的序列映射写入文件
	 * 
	 * @param k
	 * @return
	 * @throws IOException
	 */
	/*
	 * public static ArrayList<Integer> getTopPredicateDB(int k) throws
	 * IOException { int i = 0; HashMap<Integer, Integer> topPreMap = new
	 * HashMap<>(); // 存储前k个predicate ArrayList<Integer> topPreList = new
	 * ArrayList<>(); ResultSet resultSet = null; File outFile = new
	 * File("D://rescalInputFile//predicateMap"); FileWriter fw = new
	 * FileWriter(outFile); BufferedWriter bufferedWriter = new
	 * BufferedWriter(fw); String tableName = null; try (Statement stmt =
	 * conn.createStatement()) { resultSet =
	 * stmt.executeQuery("select preId,count(*) from" + tableName +
	 * "group by preId order by count(*) desc"); while (resultSet.next() && i <
	 * k) { topPreMap.put(resultSet.getInt(1), resultSet.getInt(2));
	 * topPreList.add(resultSet.getInt(1)); bufferedWriter.write(i + 1 + "   " +
	 * resultSet.getInt(1)); bufferedWriter.newLine(); i++;
	 * System.out.println("i num is " + i); } } catch (SQLException e) {
	 * e.printStackTrace(); } bufferedWriter.flush(); bufferedWriter.close();
	 * return topPreList; }
	 */

}
