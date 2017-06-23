package com.seu.Idea.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;



/**
 * 1.依据triple文件和entity-id构建JGrapht图
 * 2.获取每个顶点的邻接顶点集合，利用triple文件，或者利用JGrapht获取邻接顶点
 * @author Lynn
 *
 */
public class ClusterUtil {
	
	/**
	 * 
	 * @param inputFileFoldPath,Rescal 输入文件夹所在位置
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
			graph.addVertex(Integer.parseInt(lineArr[0]));

		}
		// 添加边
		while ((line = br2.readLine()) != null) {
			String[] lineArr = line.split(" ");
			graph.addEdge(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		}
		br1.close();
		br2.close();
		return graph;
	}
	
	
	public static Map<Integer, Set<Integer>> getNodeNeighbors(String inputFileFoldPath) throws IOException{
		Map<Integer, Set<Integer>> nodeNeighborMap = new HashMap<Integer, Set<Integer>>();

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
	/*	for(Entry<Integer, Set<Integer>> entry: nodeNeighborMap.entrySet()){
			for(Integer node: entry.getValue()){
			System.out.println(entry.getKey() + "has neighbor---->" + node);
			}
			System.out.println("-----------");
		}*/
		
		return nodeNeighborMap;
	}
	
	public static void main(String[] args) throws IOException{
		getNodeNeighbors("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\icpw-2009-complete");
	}
}
