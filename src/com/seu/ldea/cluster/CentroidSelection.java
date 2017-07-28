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
	// ��¼�����־
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;

	/**
	 * ����kֵ�Լ����Ķȼ��㷽������ȡ��ѡ���ļ���
	 * 
	 * @param num
	 *            = k * 10
	 * @param type,
	 *            ��ʲô���㷽��ѡ���ģ�pagerank...
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
		// 1 ��ʾPageRank
		if (type == 1) {
			int j = 0;
			// ���ڲ�ѯ���ݿ⣬�ֽ׶α������ڴ�
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
	 * ѡȡ�������ɴص����ĵ㣬 ���Ķȸߣ����໥֮������Զ,��ѡ�������ĵ㼯���еĵ�ľ��붼Զ
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
		/*//��ϴcandidateNodes���治����class���ĵ�
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
		// ���ĵ��ʼ��
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// �����Ķ����ĵ�������ļ��ϣ���������Ϊ��ʼ��
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);

		candidateNodes[0] = -1;
		int selectedId = -1;
		// ���ڱ�ʾ�̾���
		int stopFlag = 1;
		// ����K-1������ѡȡ����
		while (stopFlag < k) {
			Double globalMinimal = Double.MAX_VALUE;
			for (int i = 1; i < candidateNodes.length; i++) {
				Double localMinimal = Double.MAX_VALUE;;
				// �ѱ�ѡ�����ĵĵ㲻�������candidate��
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// ��ѡ�����ļ��ϵĶ���
						if (centroidNodes[j] != -1) {
							// �жϵ�ǰ����������֮��ľ���
							Double distance = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
									candidateNodes[i], centroidNodes[j]);
							
                             // System.out.println("distance****" + distance );
							// ѡȡ�ֲ���С�ľ���
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

			// ��ѡ���ĵ�������ļ����У�������Ӻ�ѡ�������ų�
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
		// path �ļ�·��,rescal�����ļ���Ŀ¼��ַ
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
