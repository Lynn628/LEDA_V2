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
 * 要实现聚类需要提供的函数：
 *   1.获取每个节点的邻接顶点信息
 *   2.提供方法计算图中顶点的中心度，并排序
 *   3.获取图中每个顶点张量分解后的值,计算向量之间的距离,可以通过RescalDistance.java中的函数获得
 *   4.从候选质心中确定最终的质心，利用张量分解实体的排序结果(调用RescalDistance.java)，选取候选质心中离质心集合中所有点都远的点确定为质心
 *   5.标签传播时，需要生成虚拟文档，当出现冲突时利用文本相似性进行排除(冲突排除可否用向量的相似性进行替代,省去虚拟文档的生成)
 *   6.评估聚类结果的好坏
 * 6/21/2017
 * @author Lynn
 *
 */
public class clusterUtil {
	/**
	 * 利用triple文件生成Jgraph，计算图的中心度
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
		//添加顶点
		while((line = br1.readLine()) != null){
			String[] lineArr = line.split(":");
			graph.addVertex(Integer.parseInt(lineArr[0]));
			
		}
		//添加边
		while((line = br2.readLine()) != null){
			String[] lineArr = line.split(" ");
			graph.addEdge(Integer.parseInt(lineArr[0]), Integer.parseInt(lineArr[2]));
		}
		br1.close();
		br2.close();
		return graph;
	}
	
	/**
	 * 计算图中点的PageRank
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
	 * 依据score值升序排序entry
	 * @param socreMap
	 */
	public static void sortRank(Map<Integer, Double> socreMap){
		ArrayList<Entry<Integer, Double>> list_data = new ArrayList<>(socreMap.entrySet());
		System.out.println("Is original vecotr list is empty" + list_data.isEmpty());
		Collections.sort(list_data, new Comparator<Entry<Integer, Double>>(){
				@Override
				public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
					// 升序排序
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
