package com.seu.ldea.cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.history.RescalDistanceTest;

public class CentroidSelection {
	public static Graph<Integer, DefaultEdge> graph;
	public static ArrayList<Entry<Integer, Double>> sortedList;
	// 记录输出日志
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;

	/**
	 * 依据k值以及中心度计算方法，获取候选质心集合
	 * 
	 * @param num
	 *            = k * 10
	 * @param type,
	 *            以什么计算方法选质心，pagerank...
	 * @return
	 * @throws IOException
	 */
	public static int[] getCandidateNodes(int num, int type) throws IOException {
		System.out.println("Candidate nodes set size is " + num);
		/*
		 * bufferedWriter.write("Candidate nodes set size is--> " + num);
		 * bufferedWriter.newLine();
		 */
		// bufferedWriter.close();
		int[] candidateNodes = new int[num];
		// 1 表示PageRank
		if (type == 1) {
			int j = 0;
			// 后期查询数据库，现阶段保存在内存
			for (int i = 0; i < num; i++) {
				candidateNodes[i]  = DegreeCalculation.getSortedDegree(graph, type).get(i).getKey();
				/*
				 * bufferedWriter.write("cadidate nodes " + i + " is--> " +
				 * candidateNodes[i]); bufferedWriter.newLine();
				 */
			}
		}
		return candidateNodes;

	}

	/**
	 * 选取最终生成簇的中心点， 中心度高，且相互之间距离较远,候选点与中心点集合中的点的距离都远
	 * 
	 * @param ingraph
	 * @param k
	 * @param vectorDistance
	 * @param type,pagerank...
	 * @return
	 * @throws IOException
	 */
	public static int[] getCentroidNodes(Graph<Integer, DefaultEdge> ingraph, ArrayList<Double[]> entityVectors,
			int k, int type) throws IOException {
		graph = ingraph;
		int candidateNum = k * 10;
		int[] candidateNodes;
		if(candidateNum <= ingraph.vertexSet().size()){
		candidateNodes = getCandidateNodes(k * 10, type);
		}else {
		candidateNodes = getCandidateNodes(ingraph.vertexSet().size(), type);	
		}
		//HashMap<Integer, String> datasetClass = Dataset.getDataSetClass("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo", "JamendoClassIndex");
		//ArrayList<Integer> candidateArr = new ArrayList<>();
		/*//清洗candidateNodes里面不包含class类别的点
		for(int i = 0; i < candidateNodes1.length; i++){
			if(!datasetClass.containsKey(candidateNodes1[i])){
				candidateArr.add(candidateNodes1[i]);
			}
		}
		int[] candidateNodes = new int[candidateArr.size()];
		for(Integer i = 0; i < candidateArr.size(); i++){
			candidateNodes[i] = candidateArr.get(i);
		}
		*/
		int[] centroidNodes = new int[k];
		// 中心点初始化
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// 将中心度最大的点加入质心集合，并以其作为初始点
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);

		candidateNodes[0] = -1;
		int selectedId = -1;
		// 用于表示短距离
		int stopFlag = 1;
		// 经历K-1次质心选取过程
		while (stopFlag < k) {
			Double globalMinimal = Double.MAX_VALUE;
			for (int i = 1; i < candidateNodes.length; i++) {
				Double localMinimal = Double.MAX_VALUE;;
				// 已被选入质心的点不会出现在candidate中
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// 已选入质心集合的顶点
						if (centroidNodes[j] != -1) {
							// 判断当前顶点与质心之间的距离
							Double distance = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
									candidateNodes[i], centroidNodes[j]);
							
                             // System.out.println("distance****" + distance );
							// 选取局部最小的距离
							if (distance.compareTo(localMinimal) < 0) {
								localMinimal = distance;
								//System.out.println("distance****" + localMinimal );
							}
						}
					}
                  //  System.out.println("localMinimal " + localMinimal + " globalMinimal " + globalMinimal);
					if (localMinimal.compareTo(globalMinimal) < 0) {
						globalMinimal = localMinimal;
						selectedId = i;

					}
				}
			}
			System.out.println("Selected id is ======= " + selectedId);
			System.out.println("#" + stopFlag + " centroid; # candidate set is  " + selectedId + " Real Node # is "
					+ candidateNodes[selectedId] + " mininal distance to other centroids is " + globalMinimal);

			// 将选出的点加入质心集合中，并将其从候选质心中排除
			centroidNodes[stopFlag] = candidateNodes[selectedId];
			candidateNodes[selectedId] = -1;
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
		graph = GraphUtil.buildGraph(dataset.getDatasetEmbedingPath());
		// sortedRank(calcPageRank(graph));
		HashMap<Integer, HashMap<Integer, Double>> vectorDistance = RescalDistanceForCluster
				.calcVectorDistance(dataset.getDatasetEmbedingPath(), method);
		int size = vectorDistance.size();
		// System.out.println("matrix size is ++++++++++++++++" + size);
		for (int i = 0; i < size; i++) {
			bufferedWriter.write(i + " : ");
			for (int j = 0; j < size; j++) {
				System.out.print(i + "-" + j + " : " + vectorDistance.get(i).get(j) + " ");
				bufferedWriter.write(i + "-" + j + " : " + vectorDistance.get(i).get(j).toString() + " ");
			}
			bufferedWriter.newLine();

		}
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(embedingPath);
		getCentroidNodes(graph,entityVectors, 5, 1);
		bufferedWriter.close();
		long end = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (end - begin) / 1000 + " s");
	}
}
