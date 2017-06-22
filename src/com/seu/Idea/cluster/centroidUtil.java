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
 * Ҫʵ�־�����Ҫ�ṩ�ĺ����� 1.��ȡÿ���ڵ���ڽӶ�����Ϣ 2.�ṩ��������ͼ�ж�������Ķȣ�������
 * 3.��ȡͼ��ÿ�����������ֽ���ֵ,��������֮��ľ���,����ͨ��RescalDistance.java�еĺ������
 * 4.�Ӻ�ѡ������ȷ�����յ����ģ����������ֽ�ʵ���������(����RescalDistance.java)��ѡȡ��ѡ�����������ļ��������е㶼Զ�ĵ�ȷ��Ϊ����
 * 5.��ǩ����ʱ����Ҫ���������ĵ��������ֳ�ͻʱ�����ı������Խ����ų�(��ͻ�ų��ɷ��������������Խ������,ʡȥ�����ĵ�������) 6.�����������ĺû�
 * 6/21/2017
 * 
 * @author Lynn
 *
 */
public class centroidUtil {
	public static ArrayList<Entry<Integer, Double>> sortedList;
	// ��¼�����־
	public static FileWriter fileWriter;
	public static BufferedWriter bufferedWriter;

	
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

	/**
	 * ����ͼ�е��PageRank
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
	 * ����scoreֵ��������entry
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
				// ��������
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
	 * ����kֵ�Լ����Ķȼ��㷽������ȡ��ѡ���ļ���
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
		// 1 ��ʾPageRank
		if (type == 1) {
			// ���ڲ�ѯ���ݿ⣬�ֽ׶α������ڴ�
			for (int i = 0; i < num; i++) {
				candidateNodes[i] = sortedList.get(i).getKey();
				bufferedWriter.write("cadidate nodes " + i + " is--> " + candidateNodes[i]);
				bufferedWriter.newLine();
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
	public static int[] getCentroidNodes(Graph<Integer, DefaultEdge> graph, int k, BigDecimal[][] vectorDistance)
			throws IOException {
		int[] candidateNodes = getCandidateNodes(k * 10, 1);
		int[] centroidNodes = new int[k];
		// ���ĵ��ʼ��
		for (int i = 0; i < k; i++)
			centroidNodes[i] = -1;

		// �����Ķ����ĵ�������ļ��ϣ���������Ϊ��ʼ��
		centroidNodes[0] = candidateNodes[0];
		System.out.println("first centroid node is " + candidateNodes[0]);
		bufferedWriter.newLine();
		bufferedWriter.write("first centroid node is---> " + candidateNodes[0]);
		bufferedWriter.newLine();
		candidateNodes[0] = -1;
		int selectedId = -1;
		// ���ڱ�ʾ�̾���
		int stopFlag = 1;
		// ����K-1������ѡȡ����
		while (stopFlag < k) {
			BigDecimal globalMinimalDistance = new BigDecimal(BigInteger.TEN);
			for (int i = 1; i < candidateNodes.length; i++) {
				BigDecimal localMinimalDistance = new BigDecimal(BigInteger.TEN);
				if (candidateNodes[i] != -1) {
					// System.out.println("CandidcateNodes[i] is in this round
					// ***** " + candidateNodes[i]);
					for (int j = 0; j < centroidNodes.length; j++) {
						// ��ѡ�����ļ��ϵĶ���
						if (centroidNodes[j] != -1) {
							// �жϵ�ǰ����������֮��ľ���
							BigDecimal distance = vectorDistance[candidateNodes[i]][centroidNodes[j]];
							// ѡȡ�ֲ���С�ľ���
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
		return candidateNodes;
	}

	

	public static void main(String[] args) throws IOException {
		long begin = System.currentTimeMillis();
		fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\log.txt");
		bufferedWriter = new BufferedWriter(fileWriter);
		Scanner scanner = new Scanner(System.in);
		// path �ļ�·��,rescal�����ļ���Ŀ¼��ַ
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
