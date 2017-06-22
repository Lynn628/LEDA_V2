package com.seu.Idea.cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

import com.seu.ldea.tau.RescalDistance;

/**
 * 要实现聚类需要提供的函数： 1.获取每个节点的邻接顶点信息 2.提供方法计算图中顶点的中心度，并排序
 * 3.获取图中每个顶点张量分解后的值,计算向量之间的距离,可以通过RescalDistance.java中的函数获得
 * 4.从候选质心中确定最终的质心，利用张量分解实体的排序结果(调用RescalDistance.java)，选取候选质心中离质心集合中所有点都远的点确定为质心
 * 5.标签传播时，需要生成虚拟文档，当出现冲突时利用文本相似性进行排除(冲突排除可否用向量的相似性进行替代,省去虚拟文档的生成) 6.评估聚类结果的好坏
 * 6/21/2017
 * 
 * @author Lynn
 *
 */
public class centroidUtil {
	public static ArrayList<Entry<Integer, Double>> sortedList;
	// 记录输出日志
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;

	
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

	/**
	 * 计算图中点的PageRank
	 * 
	 * @param graph
	 * @return
	 */
	public static Map<Integer, Double> calcPageRank(Graph<Integer, DefaultEdge> graph) {
		PageRank<Integer, DefaultEdge> pageRank = new PageRank<>(graph);
		for (Entry<Integer, Double> item : pageRank.getScores().entrySet()) {
			// System.out.println("vertex " + item.getKey() + "---> " + "score "
			// + item.getValue());
		}
		return pageRank.getScores();
	}

	/**
	 * 依据score值升序排序entry
	 * 
	 * @param socreMap
	 * @throws IOException
	 */
	public static void sortedRank(Map<Integer, Double> socreMap) throws IOException {
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(socreMap.entrySet());
		System.out.println("whether list is empty " + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// 降序排序
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		for (Entry<Integer, Double> item : entryList) {
			System.out.println("vertex " + item.getKey() + "---> " + item.getValue());
			bufferedWriter.write("vertex " + item.getKey() + "---> " + item.getValue());
			bufferedWriter.newLine();
		}
		sortedList = entryList;
	}

	
	/**
	 * 依据k值以及中心度计算方法，获取候选质心集合
	 * 
	 * @param k
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static int[] getCandidateNodes(int num, int type) throws IOException {
		System.out.println("Candidate nodes set size is " + num);
		bufferedWriter.write("Candidate nodes set size is--> " + num);
		bufferedWriter.newLine();
		// bufferedWriter.close();
		int[] candidateNodes = new int[num];
		// 1 表示PageRank
		if (type == 1) {
			// 后期查询数据库，现阶段保存在内存
			for (int i = 0; i < num; i++) {
				candidateNodes[i] = sortedList.get(i).getKey();
				bufferedWriter.write("cadidate nodes " + i + " is--> " + candidateNodes[i]);
				bufferedWriter.newLine();
			}
		}
		return candidateNodes;

	}
	
	/**
	 * 选取最终生成簇的中心点， 中心度高，且相互之间距离较远,候选点与中心点集合中的点的距离都远
	 * 
	 * @param graph
	 * @param k
	 * @param vectorDistance
	 * @return
	 * @throws IOException
	 */
	public static int[] getCentroidNodes(Graph<Integer, DefaultEdge> graph, int k, BigDecimal[][] vectorDistance)
			throws IOException {
		int[] candidateNodes = getCandidateNodes(k * 10, 1);
		int[] centroidNodes = new int[k];
		// 中心点初始化
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// 将中心度最大的点加入质心集合，并以其作为初始点
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);
		bufferedWriter.newLine();
		bufferedWriter.write("first centroid node is---> " + candidateNodes[0]);
		bufferedWriter.newLine();
		candidateNodes[0] = -1;
		int selectedId = -1;
		// 用于表示短距离
		int stopFlag = 1;
		// 经历K-1次质心选取过程
		while (stopFlag < k) {
			BigDecimal globalMinimalDistance = new BigDecimal(BigInteger.TEN);
			for (int i = 1; i < candidateNodes.length; i++) {
				BigDecimal localMinimalDistance = new BigDecimal(BigInteger.TEN);
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// 已选入质心集合的顶点
						if (centroidNodes[j] != -1) {
							// 判断当前顶点与质心之间的距离
							BigDecimal distance = vectorDistance[candidateNodes[i]][centroidNodes[j]];
							// 选取局部最小的距离
							if (distance.compareTo(localMinimalDistance) < 0) {
								localMinimalDistance = distance;
							}
						}
					}

					if (localMinimalDistance.compareTo(globalMinimalDistance) < 0) {
						globalMinimalDistance = localMinimalDistance;
						selectedId = i;

					}
				}
			}
			System.out.println("#" + stopFlag + 1 + "centroid - candidate# " + selectedId + " Node# is "
					+ candidateNodes[selectedId] + " mininal distance to other centroids is " + globalMinimalDistance);
			bufferedWriter.write("#" + stopFlag + 1 + "centroid - candidate# " + selectedId + " Node# is "
					+ candidateNodes[selectedId] + " mininal distance to other centroids is " + globalMinimalDistance);
			bufferedWriter.newLine();
			// 将选出的点加入质心集合中，并将其从候选质心中排除
			centroidNodes[stopFlag] = candidateNodes[selectedId];
			candidateNodes[selectedId] = -1;
			// globalMinimalDistance = new BigDecimal(BigInteger.TEN);
			/*
			 * System.out.println("*****************");
			 * System.out.println(centroidNodes[stopFlag] + " ** " +
			 * candidateNodes[selectedId]);
			 */
			stopFlag++;
		}
		candidateNodes = null;
		return candidateNodes;
	}

	

	public static void main(String[] args) throws IOException {
		long begin = System.currentTimeMillis();
		fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\log.txt");
		bufferedWriter = new BufferedWriter(fileWriter);
		Scanner scanner = new Scanner(System.in);
		// path 文件路径,rescal输入文件的目录地址
		System.out.println("Please give  file path ");
		String path = scanner.nextLine();
		System.out.println("Please give embedding file path ");
		String embedingPath = scanner.nextLine();
		System.out.println("Please give the vector calculation method ");
		String method = scanner.nextLine();
		scanner.close();
		Graph<Integer, DefaultEdge> graph = buildGraph(path);
		sortedRank(calcPageRank(graph));
		BigDecimal[][] vectorDistance = RescalDistance.calcVectorDistance(embedingPath, method);
		int size = vectorDistance.length;
		// System.out.println("matrix size is ++++++++++++++++" + size);
		for (int i = 0; i < size; i++) {
			bufferedWriter.write(i + " : ");
			for (int j = 0; j < size; j++) {
				System.out.print(i + "-" + j + " : " + vectorDistance[i][j] + " ");
				bufferedWriter.write(i + "-" + j + " : " + vectorDistance[i][j].toString() + " ");
			}
			bufferedWriter.newLine();

		}
		
		getCentroidNodes(graph, 5, vectorDistance);
		bufferedWriter.close();
		long end = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (end - begin) / 1000 + " s");
	}

	// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\rescalInput\icpw-2009-complete
	// D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\icpw2009complete-latent10-lambda0.embeddings.txt
	// Cosine-square Cosine-1
}// Euclidean
