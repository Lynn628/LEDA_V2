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
	// 每个entity的向量表示
	public static ArrayList<BigDecimal[]> entityVectors;

	/**
	 * 把离群点也加入到集合中 ，打上簇标签
	 * 
	 * @param centroidList,质心点集合
	 * @param sliceNodes,这张切片上的所有点
	 * @param sliceNeighborsMap,切片上点的邻居
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
			// 当前点出队列
			current = nodeQueue.poll();
			// System.out.println("current is " + current + "nodeQueue size" +
			// nodeQueue.size());
			// 出队列点标记为已访问
			visitedNode.add(current);
			// 如果当前出队列点没有邻居，则进入下次循环
			if (!sliceNeighborsMap.containsKey(current)) {
				// System.out.println("Node without neighbor **** " + current);
				continue;
			}
			// 获取出队列的点的邻接点集合
			neighborSet = sliceNeighborsMap.get(current);
			Iterator<Integer> i = neighborSet.iterator();
			while (i.hasNext()) {
				tempNeighbor = (int) i.next();
				// 如果当前邻接点的邻居点在此切片上
				/*
				 * System.out.println("sliceNodes.contains tempNeighbor ?" +
				 * " temp neighbor is " + tempNeighbor);
				 */
				// 已被访问的顶点不再访问
				if (visitedNode.contains(tempNeighbor))
					continue;
				// 未访问的点加入队列中
				if (!nodeQueue.contains(tempNeighbor)) {
					nodeQueue.offer(tempNeighbor);
				}
				// 若当前点未被着色
				if (!labelMap.containsKey(tempNeighbor)) {
					tempArray = new int[2];
					// 当前出队列的点的标签的颜色
					tempArray[0] = labelMap.get(current)[0];
					// 被当前出队列的点传播到
					tempArray[1] = current;
					labelMap.put(tempNeighbor, tempArray);
					// System.out.println("temp id is " + tempNeighbor);
					// 当前出队列的点在此切片上
				} else {
					// 该出队列顶点的邻接点已经被标上颜色，则进行着色判定
					if (labelMap.get(current)[0] == labelMap.get(tempNeighbor)[0])
						continue;
					else {
						// 比较当前被着色点A的颜色，判断之前传播给A的点和当前传播过来的点和A之间的相似度比较
						BigDecimal aBigDecimal = RescalDistance.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, labelMap.get(tempNeighbor)[1]);
						BigDecimal bBigDecimal = RescalDistance.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, current);
						int result = aBigDecimal.compareTo(bBigDecimal);
						if (result == 0) {
							continue;
						} else if (result == -1) {
							// 当前传播过来的点和A的相似度大
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
			// 找到孤立点
			if (!labeledNodes.containsKey(nodeId)) {
				int[] nodeLabelArr = new int[2];
				isolatedNodesLabelSet.put(nodeId, nodeLabelArr);
			}
		}

		for (Entry<Integer, int[]> isolatedNode : isolatedNodesLabelSet.entrySet()) {
			int clusterBelongedTo = 0;
			BigDecimal maxDistance = new BigDecimal(0);
			// 在centroid中找相似度最大的作为标签
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
			// 将孤立点加上簇标签以及传播过来的标签
			isolatedNode.getValue()[0] = clusterBelongedTo;
			isolatedNode.getValue()[1] = clusterBelongedTo;
		}

		return isolatedNodesLabelSet;
	}

	/**
	 * 依据每个点的标签，将其分配至不同的cluster中
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
	 *            每个时间片上顶点的集合
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
			// 如果此时间片上有点
			if (slice.getValue().size() != 0) {
				Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(slice.getValue(), outgoingNeighborsMap,
						incomingNeighborsMap);
				System.out.println("Graph size to select centroid**** " + graph.vertexSet().size() + "Slice nodes "
						+ slice.getValue().size());
				// 如果能构建图，才能算pagerank，才能选质心才能聚类
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

		/** 每个时间片上的点进行聚类操作 **/
		ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
		ClusterImplementationBigDecimal.entityVectors = entityVectors;
		// 时间片以及每个时间片上的簇的点
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
