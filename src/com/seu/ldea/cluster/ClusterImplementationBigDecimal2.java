package com.seu.ldea.cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.entity.Dataset;
import com.seu.ldea.history.SliceDataBuild;
import com.seu.ldea.segment.DatasetSegmentation2;
import com.seu.ldea.segment.SliceDataBuild2;
import com.seu.ldea.tau.RescalDistance;

public class ClusterImplementationBigDecimal2 {
	public static Dataset dataset;
	// ÿ��entity��������ʾ
	public static ArrayList<BigDecimal[]> entityVectors;

	/**
	 * ����Ⱥ��Ҳ���뵽������ �����ϴر�ǩ
	 * 
	 * @param centroidList,���ĵ㼯��
	 * @param sliceNodes,������Ƭ�ϵ����е�
	 * @param sliceNeighborsMap,��Ƭ�ϵ���ھ�
	 * @return
	 */
	public static HashMap<Integer, int[]> labelPropagation(int[] centroidList,
			HashMap<Integer, HashSet<Integer>> sliceNeighborsMap, HashSet<Integer> sliceTotalNodes) {

		HashMap<Integer, int[]> labelMap = new HashMap<Integer, int[]>();
		Queue<Integer> nodeQueue = new LinkedList<Integer>();
		int[] labelArray;
		for (int i = 0; i < centroidList.length; i++) {
			labelArray = new int[2];
			labelArray[0] = centroidList[i];
			labelArray[1] = centroidList[i];

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
			// System.out.println("current is " + current + "nodeQueue size" +
			// nodeQueue.size());
			// �����е���Ϊ�ѷ���
			visitedNode.add(current);
			// �����ǰ�����е�û���ھӣ�������´�ѭ��
			if (!sliceNeighborsMap.containsKey(current)) {
				// System.out.println("Node without neighbor **** " + current);
				continue;
			}
			// ��ȡ�����еĵ���ڽӵ㼯��
			neighborSet = sliceNeighborsMap.get(current);
			Iterator<Integer> i = neighborSet.iterator();
			while (i.hasNext()) {
				tempNeighbor = (int) i.next();
				// �����ǰ�ڽӵ���ھӵ��ڴ���Ƭ��
				/*
				 * System.out.println("sliceNodes.contains tempNeighbor ?" +
				 * " temp neighbor is " + tempNeighbor);
				 */
				// �ѱ����ʵĶ��㲻�ٷ���
				if (visitedNode.contains(tempNeighbor))
					continue;
				// δ���ʵĵ���������
				if (!nodeQueue.contains(tempNeighbor)) {
					nodeQueue.offer(tempNeighbor);
				}
				// ����ǰ��δ����ɫ
				if (!labelMap.containsKey(tempNeighbor)) {
					tempArray = new int[2];
					// ��ǰ�����еĵ�ı�ǩ����ɫ
					tempArray[0] = labelMap.get(current)[0];
					// ����ǰ�����еĵ㴫����
					tempArray[1] = current;
					labelMap.put(tempNeighbor, tempArray);
					// System.out.println("temp id is " + tempNeighbor);
					// ��ǰ�����еĵ��ڴ���Ƭ��
				} else {
					// �ó����ж�����ڽӵ��Ѿ���������ɫ���������ɫ�ж�
					if (labelMap.get(current)[0] == labelMap.get(tempNeighbor)[0])
						continue;
					else {
						// �Ƚϵ�ǰ����ɫ��A����ɫ���ж�֮ǰ������A�ĵ�͵�ǰ���������ĵ��A֮������ƶȱȽ�
						BigDecimal aBigDecimal = RescalDistance.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, labelMap.get(tempNeighbor)[1]);
						BigDecimal bBigDecimal = RescalDistance.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, current);
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

		/*
		 * HashMap<Integer, int[]> isolatedNodesLabel =
		 * getIsolatedNodeCluster(sliceTotalNodes, labelMap, centroidList);
		 * for(Entry<Integer, int[]> nodeLabel : isolatedNodesLabel.entrySet()){
		 * labelMap.put(nodeLabel.getKey(), nodeLabel.getValue()); }
		 */
		return labelMap;

	}

	public static HashMap<Integer, int[]> getIsolatedNodeCluster(HashSet<Integer> sliceTotalNodes,
			HashMap<Integer, int[]> labeledNodes, int[] centroidList) {
		HashMap<Integer, int[]> isolatedNodesLabelSet = new HashMap<>();
		for (Integer nodeId : sliceTotalNodes) {
			// �ҵ�������
			if (!labeledNodes.containsKey(nodeId)) {
				int[] nodeLabelArr = new int[2];
				isolatedNodesLabelSet.put(nodeId, nodeLabelArr);
			}
		}

		for (Entry<Integer, int[]> isolatedNode : isolatedNodesLabelSet.entrySet()) {
			int clusterBelongedTo = 0;
			BigDecimal maxDistance = new BigDecimal(0);
			// ��centroid�������ƶ�������Ϊ��ǩ
			for (int i = 0; i < centroidList.length; i++) {
				BigDecimal distance = RescalDistance.calcVectorDistance(entityVectors, "Cosine-2",
						isolatedNode.getKey(), centroidList[i]);
				if (distance.compareTo(maxDistance) > 0)
					maxDistance = distance;
				clusterBelongedTo = centroidList[i];

			}
			// System.out.println("isolated node id-- " + isolatedNode.getKey()
			// + " cluster label-- " + centroidList[i] + " distance " +
			// distance);
			System.out.println("isolated node id-- " + isolatedNode.getKey() + " cluster label--" + clusterBelongedTo);
			// ����������ϴر�ǩ�Լ����������ı�ǩ
			isolatedNode.getValue()[0] = clusterBelongedTo;
			isolatedNode.getValue()[1] = clusterBelongedTo;
		}

		return isolatedNodesLabelSet;
	}

	/**
	 * ����ÿ����ı�ǩ�������������ͬ��cluster��
	 * 
	 * @param labelMap
	 * @return
	 */
	public static HashMap<Integer, HashSet<Integer>> allocateNodestoCluster(HashMap<Integer, int[]> labelMap,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> incomingNeighborsMap) {
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		for (Entry<Integer, int[]> node : labelMap.entrySet()) {
			int clusterId = node.getValue()[0];
			/*
			 * System.out.println(node.getKey() + " ---* " + " outgoing number "
			 * + outgoingNeighborsMap.get(node.getKey()).size() +
			 * " incoming number " +
			 * incomingNeighborsMap.get(node.getKey()).size() + " clusterId "+
			 * clusterId + " ----* " + " outgoing number " +
			 * outgoingNeighborsMap.get(clusterId).size() + " incoming number "
			 * + incomingNeighborsMap.get(clusterId).size());
			 */
			// System.out.println("nodeId is " + node.getKey() + " node cluster
			// id is " + clusterId);
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
	 * 
	 * @param sliceNodes,
	 *            ÿ��ʱ��Ƭ�϶���ļ���
	 * @param entityVectors
	 * @param outgoingNeighborsMap
	 * @param incomingNeighborsMap
	 * @return
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> getSliceClusterMap(
			LinkedHashMap<Integer, HashSet<Integer>> sliceNodes, ArrayList<BigDecimal[]> entityVectors,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> incomingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> nodeNeighborsMap) throws IOException {

		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new LinkedHashMap<>();
		for (Entry<Integer, HashSet<Integer>> slice : sliceNodes.entrySet()) {
			// �����ʱ��Ƭ���е�
			if (slice.getValue().size() != 0) {
				Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(slice.getValue(), outgoingNeighborsMap,
						incomingNeighborsMap);
				System.out.println("Graph size to select centroid**** " + graph.vertexSet().size() + "Slice nodes "
						+ slice.getValue().size());
				// ����ܹ���ͼ��������pagerank������ѡ���Ĳ��ܾ���
				if (graph.vertexSet().size() > 0) {
					int[] centroidNodesList = CentroidSelectionBigDecimal.getCentroidNodes(graph, entityVectors, 5, 1);
					// HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor =
					// SliceDataBuild.getSliceNodesNeighor(slice.getValue(),
					// incomingNeighborsMap);
					HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor = SliceDataBuild
							.getSliceNodesNeighor(slice.getValue(), nodeNeighborsMap);

					HashMap<Integer, int[]> nodeLabelMap = labelPropagation(centroidNodesList, nodesSliceNeighbor,
							slice.getValue());
					HashMap<Integer, HashSet<Integer>> clusters = allocateNodestoCluster(nodeLabelMap,
							outgoingNeighborsMap, incomingNeighborsMap);
					System.out.println(
							"Slice id ******* " + slice.getKey() + " slice size " + slice.getValue().size() + " ");
					System.out.println(" Labeled nodes amount ********  " + nodeLabelMap.size());
					System.out.println("Graph Nodes Num *********" + graph.vertexSet().size());
					System.out.println("Slice clusters number *******  " + clusters.size());
					result.put(slice.getKey(), clusters);
				}else {
					System.out.println("No connected nodes on slice ");
				}
			} else {
				result.put(slice.getKey(), null);
			}

		}
		return result;
	}

	public static void main(String[] args) throws IOException, ParseException {
		long t1 = System.currentTimeMillis();
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";

		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");

		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild2
				.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, -1, 161771);
		/*
		 * for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
		 * System.out.println(" Slice # " + entry.getKey() + " size " +
		 * entry.getValue().size()); for(Integer item : entry.getValue()){
		 * System.out.print(item + " "); } System.out.println("\n"); }
		 */

		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** ÿ��ʱ��Ƭ�ϵĵ���о������ **/
		ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
		ClusterImplementationBigDecimal.entityVectors = entityVectors;
		// ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĴصĵ�
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementationBigDecimal
				.getSliceClusterMap(sliceNodes, entityVectors, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);

		System.out.println("--------Test----------");
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry : sliceClusterNodes.entrySet()) {
			HashMap<Integer, HashSet<Integer>> aset = entry.getValue();
			System.out.println("aSet empty? " + aset.isEmpty());
			for (Entry<Integer, HashSet<Integer>> bset : aset.entrySet()) {
				System.out.println("bset cluster size -------" + bset.getValue().size());
			}
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost ****** " + (t2 - t1) / 1000);
	}

}
