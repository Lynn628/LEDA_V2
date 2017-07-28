package com.seu.ldea.cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.entity.Dataset;

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
	public static int[] getCandidateNodes(int num, int type, HashMap<Integer, String> classTypeId) throws IOException {
		System.out.println("Candidate nodes set size is " + num);
		/*
		 * bufferedWriter.write("Candidate nodes set size is--> " + num);
		 * bufferedWriter.newLine();
		 */
		// bufferedWriter.close();
		int[] candidateNodes = new int[num];
		if(type == 1){
			//用来走pagerank全体数据的指针
			int j = 0;
			ArrayList<Entry<Integer, Double>> pagerankList = DegreeCalculation.getSortedDegree(graph, type);
			System.out.println("pagerank list == null ? " + pagerankList.size());
			int totalNum = pagerankList.size();
			int candidateRealSize = 0;
			for(int i = 0; i < num; i++){
				while(j >= 0 && totalNum >j){
					Integer currentId = pagerankList.get(j).getKey();
					if(classTypeId.containsKey(currentId)){
						j++;
					}else{
						j++;
						candidateRealSize++;
						candidateNodes[i] = currentId;
						//出while循环
						break;
					}
				}
				System.out.println("-- j is " + j + " real size is " + candidateRealSize);
				//已到pagerank末尾，没有值赋给canddidateNodes
				if(j == totalNum){
					break;//出for循环
				}
			}
			
			if(candidateRealSize < num){
				int[] newCandidateNodes = new int[candidateRealSize];
				for(int i = 0; i < candidateRealSize; i++)
					newCandidateNodes[i] = candidateNodes[i];
				
				System.out.println("************ newCandidate arr length " + newCandidateNodes.length);
				return newCandidateNodes;
			}else{
				//取得num个大小的candidate nodes
				return candidateNodes;
			}
		}
		
		// 1 表示PageRank
		/*不剔除class点的候选质心选择策略
		 * if (type == 1) {
			int j = 0;
			// 后期查询数据库，现阶段保存在内存
			for (int i = 0; i < num; i++) {
				candidateNodes[i]  = DegreeCalculation.getSortedDegree(graph, type).get(i).getKey();
				
				 * bufferedWriter.write("cadidate nodes " + i + " is--> " +
				 * candidateNodes[i]); bufferedWriter.newLine();
				 
			}
		}*/
		return candidateNodes;

	}


	/**
	 * 选取最终生成簇的中心点， 中心度高，且相互之间距离较远,候选点与中心点集合中的点的距离都远
	 * @param ingraph
	 * @param entityVectors
	 * @param k
	 * @param type,,pagerank...
	 * @param classTypeId, 给出class id的集合，用于查找候选点时进行排除
	 * @return
	 * @throws IOException
	 */
	public static int[] getCentroidNodes(Graph<Integer, DefaultEdge> ingraph, ArrayList<Double[]> entityVectors,
			int k, int type, HashMap<Integer, String> classTypeId) throws IOException {
		graph = ingraph;
		int candidateNum = k * 10;
		int[] candidateNodes;
		if(candidateNum <= ingraph.vertexSet().size()){
		candidateNodes = getCandidateNodes(k * 10, type, classTypeId );
		}else {
		candidateNodes = getCandidateNodes(ingraph.vertexSet().size(), type, classTypeId);	
		}
		
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
							// 选取局部最小的距离,distance越小，越不相似，离中心点越远
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
		
		String directoryPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
		String embedingPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedJamendo-latent10.txt";
		String method = "Cosine-2";
		Dataset dataset = new Dataset(directoryPath, embedingPath);
		graph = GraphUtil.buildGraph(dataset.getDatasetEmbedingPath());
	
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(embedingPath);
		//给予class id
		HashMap<Integer, String> classTypeId = Dataset.getDataSetClass(directoryPath, "");
	    System.out.println("entity vector is null ? " + entityVectors.size());
		getCentroidNodes(graph,entityVectors, 5, 1, classTypeId);
		bufferedWriter.close();
		long end = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (end - begin) / 1000 + " s");
	}
}
