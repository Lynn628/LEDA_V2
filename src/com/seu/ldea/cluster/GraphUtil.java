package com.seu.ldea.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * 1.����triple�ļ���entity-id����JGraphtͼ 2.��ȡÿ��������ڽӶ��㼯�ϣ�����triple�ļ�����������JGrapht��ȡ�ڽӶ���
 * 
 * @author Lynn
 *
 */
public class GraphUtil {

	/**
	 * 
	 * @param inputFileFoldPath,Rescal
	 *            �����ļ�������λ��
	 * @return
	 * @throws IOException
	 */
	public static Graph<Integer, DefaultEdge> buildGraph(String inputFileFoldPath) throws IOException {
		String tripleFilePath = inputFileFoldPath + "\\entity-ids";
		String entityFilePath = inputFileFoldPath + "\\triple";
		FileReader fr1 = new FileReader(tripleFilePath);
		FileReader fr2 = new FileReader(entityFilePath);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

		String line = "";
		// ��Ӷ���,��ȡ������
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			// top60�ļ���ȡ
			// String[] lineArr = line.split(" ");
			graph.addVertex(Integer.parseInt(lineArr[0]));

		}
		// ��ӱ�
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			graph.addEdge(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		}
		br1.close();
		br2.close();

		return graph;
	}

	/**
	 * �������㼯�ϵ�ͼ
	 * @param nodes
	 * @return
	 */
	/**
	 * 
	 * @param nodes, ÿ��ʱ��Ƭ�ϵĶ��㼯��
	 * @param outgoingNeighborsMap
	 * @param incomingNeighborsMap
	 * @return
	 */
	public static  Graph<Integer, DefaultEdge> buildGraph(HashSet<Integer> nodes,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap, HashMap<Integer, HashSet<Integer>> incomingNeighborsMap){
		Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		Object[] nodesArr = nodes.toArray();
		for(int i = 0; i < nodesArr.length; i++){
			for(int j = i+1; j < nodesArr.length; j++){
				if(outgoingNeighborsMap.get(nodesArr[i]).contains(nodesArr[j])){
					graph.addVertex((Integer)nodesArr[i]);
					graph.addVertex((Integer)nodesArr[j]);
					graph.addEdge((Integer)nodesArr[i], (Integer)nodesArr[j]);
				}
				if(incomingNeighborsMap.get(nodesArr[i]).contains(nodesArr[j])){
					graph.addVertex((Integer)nodesArr[i]);
					graph.addVertex((Integer)nodesArr[j]);
					graph.addEdge((Integer)nodesArr[j], (Integer)nodesArr[i]);
				}
			}
		}
		return graph;
		
	}

	/**
	 * ÿ�����Լ�ÿ������ھӵ�map,�õ���Ϊ���
	 * 
	 * @param inputFileFoldPath
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, HashSet<Integer>> getNodeOutgoingNeighbors(String inputFileFoldPath) throws IOException {
		HashMap<Integer, HashSet<Integer>> nodeNeighborMap = new HashMap<Integer, HashSet<Integer>>();

		String tripleFilePath = inputFileFoldPath + "\\entity-ids";
		String entityFilePath = inputFileFoldPath + "\\triple";
		FileReader fr1 = new FileReader(tripleFilePath);
		FileReader fr2 = new FileReader(entityFilePath);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String line = "";
		// ��Ӷ������
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);

		}
		// ��ӱ�
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			Integer key = Integer.parseInt(lineArr[0]);
			Integer neighbor = Integer.parseInt(lineArr[2]);
			nodeNeighborMap.get(key).add(neighbor);
		}
		br1.close();
		br2.close();
		/*
		 * for(Entry<Integer, Set<Integer>> entry: nodeNeighborMap.entrySet()){
		 * for(Integer node: entry.getValue()){
		 * System.out.println(entry.getKey() + "has neighbor---->" + node); }
		 * System.out.println("-----------"); }
		 */

		return nodeNeighborMap;
	}

	/**
	 * ÿ�����Լ�ÿ������ھӵ�map���õ���Ϊ�յ�
	 * 
	 * @param inputFileFoldPath
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, HashSet<Integer>> getNodeIncomingNeighbors(String inputFileFoldPath) throws IOException {
		HashMap<Integer, HashSet<Integer>> nodeNeighborMap = new HashMap<Integer, HashSet<Integer>>();

		String tripleFilePath = inputFileFoldPath + "\\entity-ids";
		String entityFilePath = inputFileFoldPath + "\\triple";
		FileReader fr1 = new FileReader(tripleFilePath);
		FileReader fr2 = new FileReader(entityFilePath);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String line = "";
		// ��Ӷ������
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);

		}
		// ��ӱ�
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			// ������key��������value���ռ��ж��ٵ�ָ�����
			Integer key = Integer.parseInt(lineArr[2]);
			Integer neighbor = Integer.parseInt(lineArr[0]);
			nodeNeighborMap.get(key).add(neighbor);
		}
		br1.close();
		br2.close();
		/*
		 * for(Entry<Integer, Set<Integer>> entry: nodeNeighborMap.entrySet()){
		 * for(Integer node: entry.getValue()){
		 * System.out.println(entry.getKey() + "has neighbor---->" + node); }
		 * System.out.println("-----------"); }
		 */
		return nodeNeighborMap;
	}

	
	/**
	 * ÿ�����Լ�ÿ������ھӵ�map���õ���Ϊ�������յ㣬Ҳ�������
	 * 
	 * @param inputFileFoldPath
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer, HashSet<Integer>> getNodeNeighbors(String inputFileFoldPath) throws IOException {
		HashMap<Integer, HashSet<Integer>> nodeNeighborMap = new HashMap<Integer, HashSet<Integer>>();

		String tripleFilePath = inputFileFoldPath + "\\entity-ids";
		String entityFilePath = inputFileFoldPath + "\\triple";
		FileReader fr1 = new FileReader(tripleFilePath);
		FileReader fr2 = new FileReader(entityFilePath);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		String line = "";
		// ��Ӷ������
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);
			
           
		}
		// ��ӱ�
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			// ������key��������value���ռ��ж��ٵ�ָ�����
			Integer subKey = Integer.parseInt(lineArr[0]);
			Integer objKey = Integer.parseInt(lineArr[2]);
			//Integer neighbor = Integer.parseInt(lineArr[0]);
			nodeNeighborMap.get(subKey).add(objKey);
			nodeNeighborMap.get(objKey).add(subKey);
		}
		br1.close();
		br2.close();
		/*
		 * for(Entry<Integer, Set<Integer>> entry: nodeNeighborMap.entrySet()){
		 * for(Integer node: entry.getValue()){
		 * System.out.println(entry.getKey() + "has neighbor---->" + node); }
		 * System.out.println("-----------"); }
		 */
		return nodeNeighborMap;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		HashMap<Integer, HashSet<Integer>> nodeNeigborsMap = (HashMap<Integer, HashSet<Integer>>) getNodeOutgoingNeighbors(
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\LinkedMDB2");
		for (Entry<Integer, HashSet<Integer>> entry : nodeNeigborsMap.entrySet()) {
			System.out.println(entry.getKey() + " -- ");
			for (Integer item : entry.getValue()) {
				System.out.print(item.intValue() + " \n");
			}
			System.out.println("-------");
		}

	}
}
