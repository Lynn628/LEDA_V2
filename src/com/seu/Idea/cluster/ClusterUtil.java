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
 * 1.����triple�ļ���entity-id����JGraphtͼ
 * 2.��ȡÿ��������ڽӶ��㼯�ϣ�����triple�ļ�����������JGrapht��ȡ�ڽӶ���
 * @author Lynn
 *
 */
public class ClusterUtil {
	
	/**
	 * 
	 * @param inputFileFoldPath,Rescal �����ļ�������λ��
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
	
	
	public static Map<Integer, Set<Integer>> getNodeNeighbors(String inputFileFoldPath) throws IOException{
		Map<Integer, Set<Integer>> nodeNeighborMap = new HashMap<Integer, Set<Integer>>();

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
