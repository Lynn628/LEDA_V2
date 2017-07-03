package com.seu.ldea.cluster;

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

import com.mysql.fabric.xmlrpc.base.Data;
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
public class CentroidUtil {
	
	public static Graph<Integer, DefaultEdge> graph;
	public static ArrayList<Entry<Integer, Double>> sortedList;
	// 记录输出日志
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;
	
	/**
	 * 依据k值以及中心度计算方法，获取候选质心集合
	 * @param num = k * 10
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static int[] getCandidateNodes(int num, int type) throws IOException {
		System.out.println("Candidate nodes set size is " + num);
		/*bufferedWriter.write("Candidate nodes set size is--> " + num);
		bufferedWriter.newLine();*/
		// bufferedWriter.close();
		int[] candidateNodes = new int[num];
		// 1 表示PageRank
		if (type == 1) {
			// 后期查询数据库，现阶段保存在内存
			for (int i = 0; i < num; i++) {
				candidateNodes[i] = DegreeCalculation.getSortedDegree(graph, type).get(i).getKey();
			/*	bufferedWriter.write("cadidate nodes " + i + " is--> " + candidateNodes[i]);
				bufferedWriter.newLine();*/
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
	public static int[] getCentroidNodes(Graph<Integer, DefaultEdge> ingraph, int k, BigDecimal[][] vectorDistance, int type)
			throws IOException {
		graph = ingraph; 
		int[] candidateNodes = getCandidateNodes(k * 10, type);
		int[] centroidNodes = new int[k];
		// 中心点初始化
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// 将中心度最大的点加入质心集合，并以其作为初始点
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);
		/*bufferedWriter.newLine();
		bufferedWriter.write("first centroid node is---> " + candidateNodes[0]);
		bufferedWriter.newLine();*/
		candidateNodes[0] = -1;
		int selectedId = -1;
		// 用于表示短距离
		int stopFlag = 1;
		// 经历K-1次质心选取过程
		while (stopFlag < k) {
			BigDecimal globalMinimal = new BigDecimal(BigInteger.TEN);
			for (int i = 1; i < candidateNodes.length; i++) {
				BigDecimal localMinimal = new BigDecimal(BigInteger.TEN);
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// 已选入质心集合的顶点
						if (centroidNodes[j] != -1) {
							// 判断当前顶点与质心之间的距离
							BigDecimal distance = vectorDistance[candidateNodes[i]][centroidNodes[j]];
							// 选取局部最小的距离
							if (distance.compareTo(localMinimal) < 0) {
								localMinimal = distance;
							}
						}
					}

					if (localMinimal.compareTo(globalMinimal) < 0) {
						globalMinimal = localMinimal;
						selectedId = i;

					}
				}
			}
			System.out.println("#" + stopFlag + 1 + "centroid - candidate# " + selectedId + " Node# is "
					+ candidateNodes[selectedId] + " mininal distance to other centroids is " + globalMinimal);
			//bufferedWriter.write("#" + stopFlag + 1 + "centroid - candidate# " + selectedId + " Node# is "
					//+ candidateNodes[selectedId] + " mininal distance to other centroids is " + globalMinimal);
			//bufferedWriter.newLine();
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
		return centroidNodes;
	}


	public static void main(String[] args) throws IOException {
		long begin = System.currentTimeMillis();
		fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\log.txt");
		bufferedWriter = new BufferedWriter(fileWriter);
		Scanner scanner = new Scanner(System.in);
		// path 文件路径,rescal输入文件的目录地址
		System.out.println("Please give  file path ");
		String directoryPath = scanner.nextLine();
		System.out.println("Please give embedding file path ");
		String embedingPath = scanner.nextLine();
		System.out.println("Please give the vector calculation method ");
		String method = scanner.nextLine();
		scanner.close();
		Dataset dataset = new Dataset(directoryPath, embedingPath);
		graph = ClusterUtil.buildGraph(dataset.getDatasetEmbedingPath());
		//sortedRank(calcPageRank(graph));
		BigDecimal[][] vectorDistance = RescalDistance.calcVectorDistance(dataset.getDatasetEmbedingPath(), method);
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
		getCentroidNodes(graph, 5, vectorDistance, 1);
		bufferedWriter.close();
		long end = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (end - begin) / 1000 + " s");
	}

	// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\rescalInput\icpw-2009-complete
	// D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\icpw2009complete-latent10-lambda0.embeddings.txt
	// Cosine-square Cosine-1
}// Euclidean
