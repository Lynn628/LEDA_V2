package com.seu.ldea.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;


import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.github.jsonldjava.core.RDFDataset.Node;

import riotcmd.infer;

/**
 * 1.依据triple文件和entity-id构建JGrapht图 2.获取每个顶点的邻接顶点集合，利用triple文件，或者利用JGrapht获取邻接顶点
 * 
 * @author Lynn
 *
 */
public class GraphUtil {

	/**
	 * 
	 * @param inputFileFoldPath,Rescal
	 *            输入文件夹所在位置
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
		// 添加顶点,获取顶点编号
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			// top60文件获取
			// String[] lineArr = line.split(" ");
			graph.addVertex(Integer.parseInt(lineArr[0]));

		}
		// 添加边
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			graph.addEdge(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		}
		br1.close();
		br2.close();
         // System.out.println("Graph size" + graph.vertexSet().size());
		return graph;
	}

	/**
	 * 构建顶点集合的图
	 * @param nodes
	 * @return
	 */
	/**
	 * 
	 * @param nodes, 每个时间片上的顶点集合
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
				System.out.println("Graph size>>>>> " + graph.vertexSet().size());
			}
		}
		return graph;
		
	}
	
	
	public static  Graph<Integer, DefaultEdge> buildGraph2(HashSet<Integer> nodes,
			HashMap<Integer, HashSet<Integer>> allNeighborMap ){
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
	    long t1 = System.currentTimeMillis();
		//Object[] nodesArr = nodes.toArray();
		/*for(int i = 0; i < nodesArr.length; i++){
			for(int j = i+1; j < nodesArr.length; j++){
				if(allNeighborMap.get(nodesArr[i]).contains(nodesArr[j])){
					graph.addVertex((Integer)nodesArr[i]);
					graph.addVertex((Integer)nodesArr[j]);
					graph.addEdge((Integer)nodesArr[i], (Integer)nodesArr[j]);
				    
				}*/
	    int j = 0;
		    for(int node : nodes){
		    	j++;
		    	graph.addVertex(node);
		    	if(allNeighborMap.containsKey(node)){
			    HashSet<Integer> neighborSet = allNeighborMap.get(node);
			    Object[] nodeNeighborArr = neighborSet.toArray();
			    for(int i = 0; i < nodeNeighborArr.length; i++){
			    	int neighbor = (Integer)nodeNeighborArr[i];
			    	if(nodes.contains(neighbor)){
						graph.addVertex((Integer)nodeNeighborArr[i]);
						if(node != neighbor)
						graph.addEdge(node, (Integer)nodeNeighborArr[i]);
			    	}
			    }
				//System.out.println(j + " : Graph size >>>>" + graph.vertexSet().size() );
			/*	if(incomingNeighborsMap.get(nodesArr[i]).contains(nodesArr[j])){
					graph.addVertex((Integer)nodesArr[i]);
					graph.addVertex((Integer)nodesArr[j]);
					graph.addEdge((Integer)nodesArr[j], (Integer)nodesArr[i]);
				}
			}*/
		}
	}
		long t2 = System.currentTimeMillis(); 
		System.out.println("Graph Building time cost " + (t2-t1)/1000.0);
		return graph;
		
	}
	

	/**
	 * 每个点以及每个点的邻居的map,该点作为起点
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
		// 添加顶点进入
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);

		}
		// 添加边
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
	 * 每个点以及每个点的邻居的map，该点作为终点
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
		// 添加顶点进入
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);

		}
		// 添加边
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			// 宾语作key，主语作value，收集有多少点指向宾语
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
	 * 每个点以及每个点的邻居的map，该点作为即可作终点，也可做起点
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
		// 添加顶点进入
		while ((line = br1.readLine()) != null) {
			String[] lineArr = line.split(":");
			Integer key = Integer.parseInt(lineArr[0]);
			HashSet<Integer> neighborSet = new HashSet<>();
			nodeNeighborMap.put(key, neighborSet);
			
           
		}
		// 添加边
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			// 宾语作key，主语作value，收集有多少点指向宾语
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
