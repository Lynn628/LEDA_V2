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
	public static int[] getCandidateNodes(int num, int type, HashMap<Integer, String> classTypeId) throws IOException {
		System.out.println("Candidate nodes set size is " + num);
		/*
		 * bufferedWriter.write("Candidate nodes set size is--> " + num);
		 * bufferedWriter.newLine();
		 */
		// bufferedWriter.close();
		int[] candidateNodes = new int[num];
		if(type == 1){
			//������pagerankȫ�����ݵ�ָ��
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
						//��whileѭ��
						break;
					}
				}
				System.out.println("-- j is " + j + " real size is " + candidateRealSize);
				//�ѵ�pagerankĩβ��û��ֵ����canddidateNodes
				if(j == totalNum){
					break;//��forѭ��
				}
			}
			
			if(candidateRealSize < num){
				int[] newCandidateNodes = new int[candidateRealSize];
				for(int i = 0; i < candidateRealSize; i++)
					newCandidateNodes[i] = candidateNodes[i];
				
				System.out.println("************ newCandidate arr length " + newCandidateNodes.length);
				return newCandidateNodes;
			}else{
				//ȡ��num����С��candidate nodes
				return candidateNodes;
			}
		}
		
		// 1 ��ʾPageRank
		/*���޳�class��ĺ�ѡ����ѡ�����
		 * if (type == 1) {
			int j = 0;
			// ���ڲ�ѯ���ݿ⣬�ֽ׶α������ڴ�
			for (int i = 0; i < num; i++) {
				candidateNodes[i]  = DegreeCalculation.getSortedDegree(graph, type).get(i).getKey();
				
				 * bufferedWriter.write("cadidate nodes " + i + " is--> " +
				 * candidateNodes[i]); bufferedWriter.newLine();
				 
			}
		}*/
		return candidateNodes;

	}


	/**
	 * ѡȡ�������ɴص����ĵ㣬 ���Ķȸߣ����໥֮������Զ,��ѡ�������ĵ㼯���еĵ�ľ��붼Զ
	 * @param ingraph
	 * @param entityVectors
	 * @param k
	 * @param type,,pagerank...
	 * @param classTypeId, ����class id�ļ��ϣ����ڲ��Һ�ѡ��ʱ�����ų�
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
							// ѡȡ�ֲ���С�ľ���,distanceԽС��Խ�����ƣ������ĵ�ԽԶ
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
		
		String directoryPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
		String embedingPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedJamendo-latent10.txt";
		String method = "Cosine-2";
		Dataset dataset = new Dataset(directoryPath, embedingPath);
		graph = GraphUtil.buildGraph(dataset.getDatasetEmbedingPath());
	
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(embedingPath);
		//����class id
		HashMap<Integer, String> classTypeId = Dataset.getDataSetClass(directoryPath, "");
	    System.out.println("entity vector is null ? " + entityVectors.size());
		getCentroidNodes(graph,entityVectors, 5, 1, classTypeId);
		bufferedWriter.close();
		long end = System.currentTimeMillis();
		System.out.println("Time cost -----> " + (end - begin) / 1000 + " s");
	}
}
