package com.seu.ldea.history;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.cluster.CentroidSelectionBigDecimal;
import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.entity.Dataset;
import com.seu.ldea.tau.RescalDistance;

public class ClusterImplTest {
	public static Dataset dataset;
	// ÿ��entity��������ʾ
	public static ArrayList<BigDecimal[]> entityVectors;

	/**
	 * 
	 * @param centroidList,���ĵ㼯��
	 * @param sliceNodes,������Ƭ�ϵ����е�
	 * @param sliceNeighborsMap,��Ƭ�ϵ���ھ�
	 * @return
	 */
	public static HashMap<Integer, int[]> labelPropagation(int[] centroidList,
			HashMap<Integer, HashSet<Integer>> sliceNeighborsMap) {
		/*
		 * ����Ⱥ��Ҳ���뵽������ �����ϴر�ǩ
		 * public static HashMap<Integer, int[]> labelPropagation(int[] centroidList,
				HashMap<Integer, HashSet<Integer>> sliceNeighborsMap, HashSet<Integer> nodes) {
		*/
		HashMap<Integer, int[]> labelMap = new HashMap<Integer, int[]>();
		Queue<Integer> nodeQueue = new LinkedList<Integer>();
		int[] labelArray;
		for (int i = 0; i < centroidList.length; i++) {
			labelArray = new int[2];
			labelArray[0] = centroidList[i];
			labelArray[1] = centroidList[i];
            if(centroidList[i] == 1471){
            	System.out.println("Do exist 1471 ");
            }
			labelMap.put(centroidList[i], labelArray);
			nodeQueue.offer(centroidList[i]);
		}

		int current;
		int tempNeighbor;
		int[] tempArray;
		Set<Integer> neighborSet;
		List<Integer> visitedNode = new ArrayList<Integer>();

		while (!nodeQueue.isEmpty()) {
			// ��ǰ�������
			current = nodeQueue.poll();
			//System.out.println("current is " + current +  "nodeQueue size" + nodeQueue.size());
				// �����е���Ϊ�ѷ���
				visitedNode.add(current);
				// �����ǰ�����е�û���ھӣ�������´�ѭ��
				if (!sliceNeighborsMap.containsKey(current)) {
					continue;
				}
				// ��ȡ�����еĵ���ڽӵ㼯��
				neighborSet = sliceNeighborsMap.get(current);
				Iterator<Integer> i = neighborSet.iterator();
				while (i.hasNext()) {
					tempNeighbor = (int) i.next();
					// �����ǰ�ڽӵ���ھӵ��ڴ���Ƭ��
						/*System.out.println("sliceNodes.contains tempNeighbor ?" 
								+ " temp neighbor is " + tempNeighbor);*/
						// �ѱ����ʵĶ��㲻�ٷ���
						if (visitedNode.contains(tempNeighbor))
							continue;
						// δ���ʵĵ���������
						if (!nodeQueue.contains(tempNeighbor)) {
							nodeQueue.offer(tempNeighbor);
						}
						if (!labelMap.containsKey(tempNeighbor)) {
							tempArray = new int[2];
							// ��ǰ�����еĵ�ı�ǩ����ɫ
							tempArray[0] = labelMap.get(current)[0];
							// ����ǰ�����еĵ㴫����
							tempArray[1] = current;
							labelMap.put(tempNeighbor, tempArray);
							//System.out.println("temp id is " + tempNeighbor);
							// ��ǰ�����еĵ��ڴ���Ƭ��
						} else {
							// �ó����ж�����ڽӵ��Ѿ���������ɫ���������ɫ�ж�
							if (labelMap.get(current)[0] == labelMap.get(tempNeighbor)[0])
								continue;
							else {
								// �Ƚϵ�ǰ����ɫ��A����ɫ���ж�֮ǰ������A�ĵ�͵�ǰ���������ĵ��A֮������ƶȱȽ�
								BigDecimal aBigDecimal = RescalDistance.calcVectorDistance(entityVectors,
										"Cosine-2", tempNeighbor, labelMap.get(tempNeighbor)[1]);
								BigDecimal bBigDecimal = RescalDistance.calcVectorDistance(entityVectors,
										"Cosine-2", tempNeighbor, current);
								int result = aBigDecimal.compareTo(bBigDecimal);
								if (result == 0) {
									continue;
								} else if (result == -1) {
									// ��ǰ���������ĵ��A�����ƶȴ�
									labelMap.get(tempNeighbor)[0] = labelMap.get(current)[0];
									labelMap.get(tempNeighbor)[1] = current;

								}
							}
						}
					}
				}
		
		visitedNode = null;
		nodeQueue = null;
		return labelMap;

	}
 
	

	/**
	 * ����ÿ����ı�ǩ�������������ͬ��cluster��
	 * 
	 * @param nodeLabelMap
	 * @return
	 */
	public static HashMap<Integer, HashSet<Integer>> allocateNodestoCluster(HashMap<Integer, int[]> nodeLabelMap) {
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		for (Entry<Integer, int[]> node : nodeLabelMap.entrySet()) {
			int clusterId = node.getValue()[0];
		
			//System.out.println("nodeId is " + node.getKey() + "  node cluster id is " + clusterId);
			if (!result.containsKey(clusterId)) {
				HashSet<Integer> clusterNodesSet = new HashSet<>();
				clusterNodesSet.add(node.getKey());
				result.put(clusterId, clusterNodesSet);
			} else {
				result.get(clusterId).add(node.getKey());
			}
		}
		for (Entry<Integer, HashSet<Integer>> entry : result.entrySet()) {
			System.out.println("Cluster id " + entry.getKey() + " Cluster size " + entry.getValue().size());
		}
		return result;
	}

	
	
	/**
	 * ����ÿ��ʱ��Ƭ�Լ�ʱ��Ƭ�ϵĴ�
	 * 
	 * @param slices
	 * @return ʱ�������Լ�ÿ��ʱ�����У��ر�ǩ���صĶ��㼯��
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> getSliceClusterMap(
			LinkedHashMap<Integer, HashSet<Integer>> slices, ArrayList<BigDecimal[]> entityVectors,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> incomingNeighborsMap) throws IOException {

		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new LinkedHashMap<>();
		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			// �����ʱ��Ƭ���е�
			if (slice.getValue().size() != 0) {
				Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(slice.getValue(), outgoingNeighborsMap,
						incomingNeighborsMap);
				System.out.println("Graph Nodes Num ************" + graph.vertexSet().size());
				int[] centroidNodesList = CentroidSelectionBigDecimal.getCentroidNodes(graph, entityVectors, 5, 1);
				HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor = SliceDataBuild.getSliceNodesNeighor(slice.getValue(), outgoingNeighborsMap);
				HashMap<Integer, int[]> nodeLabelMap = labelPropagation(centroidNodesList, 
						nodesSliceNeighbor);
				System.out.println("Slice id-- " + slice.getKey() + " Node num on slice " + slice.getValue().size() +  " "
						+ " Labeled nodes amount is  " + nodeLabelMap.size());
				HashMap<Integer, HashSet<Integer>> clusters = allocateNodestoCluster(nodeLabelMap);
				System.out.println("The number of cluster on a slice  " + clusters.size());
				result.put(slice.getKey(), clusters);
			} else {
				result.put(slice.getKey(), null);
			}

		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Dataset directory --->");
		String datasetDirectory = scanner.nextLine();
		System.out.println("Dataset embedingpath-->");
		String datasetEmbedingPath = scanner.nextLine();
		dataset = new Dataset(datasetDirectory, datasetEmbedingPath);
		System.out.println(datasetDirectory + "\n" + datasetEmbedingPath);
		scanner.close();

		HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap = GraphUtil
				.getNodeOutgoingNeighbors(dataset.getDatasetDirectory());
		Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(dataset.getDatasetDirectory());
		entityVectors = RescalDistance.getNodeVector(datasetEmbedingPath);
		int[] centroidList = CentroidSelectionBigDecimal.getCentroidNodes(graph, entityVectors, 5, 1);
		// ÿ�������Լ���������ǩ�Լ���ǩ����˭������������Ϣ��map
		HashMap<Integer, int[]> labelMap = labelPropagation(centroidList, outgoingNeighborsMap);
		HashMap<Integer, ArrayList<Integer>> clusters = new HashMap<>();
		for (Entry<Integer, int[]> entry : labelMap.entrySet()) {
			int key = entry.getKey();
			/*
			 * for(int i = 0; i < entry.getValue().length; i++){
			 * 
			 * System.out.println(key + " --> " + entry.getValue()[i]); }
			 * System.out.println("-----------");
			 */
			int label = entry.getValue()[0];

			if (clusters.containsKey(label)) {
				clusters.get(label).add(key);
			} else {
				ArrayList<Integer> clusterNodes = new ArrayList<>();
				clusterNodes.add(key);
				clusters.put(label, clusterNodes);
			}
		}
		System.out.println("cluster map size is " + clusters.size());
		for (Entry<Integer, ArrayList<Integer>> entry : clusters.entrySet()) {
			System.out.println(" Nodes in cluster where ** " + entry.getKey() + "** as centroid ");
			for (Integer node : entry.getValue()) {
				System.out.println(node);
			}
			System.out.println("---------");
		}
	}
}
// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\rescalInput\SWCC2
// D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\SWCC2-latent10-lambda0.embeddings.txt