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
 * Ҫʵ�־�����Ҫ�ṩ�ĺ����� 1.��ȡÿ���ڵ���ڽӶ�����Ϣ 2.�ṩ��������ͼ�ж�������Ķȣ�������
 * 3.��ȡͼ��ÿ�����������ֽ���ֵ,��������֮��ľ���,����ͨ��RescalDistance.java�еĺ������
 * 4.�Ӻ�ѡ������ȷ�����յ����ģ����������ֽ�ʵ���������(����RescalDistance.java)��ѡȡ��ѡ�����������ļ��������е㶼Զ�ĵ�ȷ��Ϊ����
 * 5.��ǩ����ʱ����Ҫ���������ĵ��������ֳ�ͻʱ�����ı������Խ����ų�(��ͻ�ų��ɷ��������������Խ������,ʡȥ�����ĵ�������) 6.�����������ĺû�
 * 6/21/2017
 * 
 * @author Lynn
 *
 */
public class CentroidUtil {
	
	public static Graph<Integer, DefaultEdge> graph;
	public static ArrayList<Entry<Integer, Double>> sortedList;
	// ��¼�����־
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;
	
	/**
	 * ����kֵ�Լ����Ķȼ��㷽������ȡ��ѡ���ļ���
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
		// 1 ��ʾPageRank
		if (type == 1) {
			// ���ڲ�ѯ���ݿ⣬�ֽ׶α������ڴ�
			for (int i = 0; i < num; i++) {
				candidateNodes[i] = DegreeCalculation.getSortedDegree(graph, type).get(i).getKey();
			/*	bufferedWriter.write("cadidate nodes " + i + " is--> " + candidateNodes[i]);
				bufferedWriter.newLine();*/
			}
		}
		return candidateNodes;

	}
	
	/**
	 * ѡȡ�������ɴص����ĵ㣬 ���Ķȸߣ����໥֮������Զ,��ѡ�������ĵ㼯���еĵ�ľ��붼Զ
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
		// ���ĵ��ʼ��
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// �����Ķ����ĵ�������ļ��ϣ���������Ϊ��ʼ��
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);
		/*bufferedWriter.newLine();
		bufferedWriter.write("first centroid node is---> " + candidateNodes[0]);
		bufferedWriter.newLine();*/
		candidateNodes[0] = -1;
		int selectedId = -1;
		// ���ڱ�ʾ�̾���
		int stopFlag = 1;
		// ����K-1������ѡȡ����
		while (stopFlag < k) {
			BigDecimal globalMinimal = new BigDecimal(BigInteger.TEN);
			for (int i = 1; i < candidateNodes.length; i++) {
				BigDecimal localMinimal = new BigDecimal(BigInteger.TEN);
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// ��ѡ�����ļ��ϵĶ���
						if (centroidNodes[j] != -1) {
							// �жϵ�ǰ����������֮��ľ���
							BigDecimal distance = vectorDistance[candidateNodes[i]][centroidNodes[j]];
							// ѡȡ�ֲ���С�ľ���
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
			// ��ѡ���ĵ�������ļ����У�������Ӻ�ѡ�������ų�
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
		// path �ļ�·��,rescal�����ļ���Ŀ¼��ַ
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
