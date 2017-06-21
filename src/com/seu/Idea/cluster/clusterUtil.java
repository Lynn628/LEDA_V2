package com.seu.Idea.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Ҫʵ�־�����Ҫ�ṩ�ĺ�����
 *   1.��ȡÿ���ڵ���ڽӶ�����Ϣ
 *   2.�ṩ��������ͼ�ж�������Ķȣ�������
 *   3.��ȡͼ��ÿ�����������ֽ���ֵ,��������֮��ľ���,����ͨ��RescalDistance.java�еĺ������
 *   4.�Ӻ�ѡ������ȷ�����յ����ģ����������ֽ�ʵ���������(����RescalDistance.java)��ѡȡ��ѡ�����������ļ��������е㶼Զ�ĵ�ȷ��Ϊ����
 *   5.��ǩ����ʱ����Ҫ���������ĵ��������ֳ�ͻʱ�����ı������Խ����ų�(��ͻ�ų��ɷ��������������Խ������,ʡȥ�����ĵ�������)
 *   6.�����������ĺû�
 * 6/21/2017
 * @author Lynn
 *
 */
public class clusterUtil {
	/**
	 * ����triple�ļ�����Jgraph������ͼ�����Ķ�
	 * @param tripleFilePath
	 * @return
	 * @throws IOException 
	 */
	public static Graph<Integer, DefaultEdge> buildGraph(String inputFileFoldPath) throws IOException{
		String tripleFilePath = inputFileFoldPath + "\\entity-ids";
		String entityFilePath = inputFileFoldPath + "\\triple";
		FileReader fr1 = new FileReader(tripleFilePath);
		FileReader fr2 = new FileReader(entityFilePath);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		Graph<Integer, DefaultEdge> graph =  new DefaultDirectedGraph<>(DefaultEdge.class); 
		String line = "";
		//��Ӷ���
		while((line = br1.readLine()) != null){
			String[] lineArr = line.split(":");
			graph.addVertex(Integer.parseInt(lineArr[0]));
			
		}
		//��ӱ�
		while((line = br2.readLine()) != null){
			String[] lineArr = line.split(" ");
			graph.addEdge(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		}
		br1.close();
		br2.close();
		return graph;
	}
	
	/**
	 * ����ͼ�е��PageRank
	 * @param graph
	 * @return
	 */
	public static Map<Integer, Double> calcPageRank(Graph<Integer, DefaultEdge> graph){
		PageRank<Integer, DefaultEdge> pageRank = new PageRank<>(graph);
		for(Entry<Integer, Double> item : pageRank.getScores().entrySet()){
		//	System.out.println("vertex " + item.getKey() + "---> " + "score " + item.getValue());
		}
		return pageRank.getScores();
	}
	
	/**
	 * ����scoreֵ��������entry
	 * @param socreMap
	 */
	public static void sortRank(Map<Integer, Double> socreMap){
		ArrayList<Entry<Integer, Double>> list_data = new ArrayList<>(socreMap.entrySet());
		System.out.println("Is original vecotr list is empty" + list_data.isEmpty());
		Collections.sort(list_data, new Comparator<Entry<Integer, Double>>(){
				@Override
				public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
					// ��������
					return o1.getValue().compareTo(o2.getValue());
		       }
		      });
		for(Entry<Integer, Double> item : list_data){
			System.out.println("vertex " + item.getKey() + "---> " + item.getValue());
		}
	}
	
	public static void findcCentroid(){
		
	}
	
	public static void main(String[] args) throws IOException{
	    long begin = System.currentTimeMillis();
		Scanner scanner = new Scanner(System.in);
	    String path = scanner.nextLine();
	    scanner.close();
	    Graph<Integer, DefaultEdge> graph = buildGraph(path);
	    sortRank(calcPageRank(graph));
	    long end = System.currentTimeMillis();
	    System.out.println("Time cost -----> " + (end - begin)/1000 + " s");
	}
	
}
