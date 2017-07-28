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
	// 每个entity的向量表示
	public static ArrayList<BigDecimal[]> entityVectors;

	/**
	 * 
	 * @param centroidList,质心点集合
	 * @param sliceNodes,这张切片上的所有点
	 * @param sliceNeighborsMap,切片上点的邻居
	 * @return
	 */
	public static HashMap<Integer, int[]> labelPropagation(int[] centroidList,
			HashMap<Integer, HashSet<Integer>> sliceNeighborsMap) {
		/*
		 * 把离群点也加入到集合中 ，打上簇标签
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
			// 当前点出队列
			current = nodeQueue.poll();
			//System.out.println("current is " + current +  "nodeQueue size" + nodeQueue.size());
				// 出队列点标记为已访问
				visitedNode.add(current);
				// 如果当前出队列点没有邻居，则进入下次循环
				if (!sliceNeighborsMap.containsKey(current)) {
					continue;
				}
				// 获取出队列的点的邻接点集合
				neighborSet = sliceNeighborsMap.get(current);
				Iterator<Integer> i = neighborSet.iterator();
				while (i.hasNext()) {
					tempNeighbor = (int) i.next();
					// 如果当前邻接点的邻居点在此切片上
						/*System.out.println("sliceNodes.contains tempNeighbor ?" 
								+ " temp neighbor is " + tempNeighbor);*/
						// 已被访问的顶点不再访问
						if (visitedNode.contains(tempNeighbor))
							continue;
						// 未访问的点加入队列中
						if (!nodeQueue.contains(tempNeighbor)) {
							nodeQueue.offer(tempNeighbor);
						}
						if (!labelMap.containsKey(tempNeighbor)) {
							tempArray = new int[2];
							// 当前出队列的点的标签的颜色
							tempArray[0] = labelMap.get(current)[0];
							// 被当前出队列的点传播到
							tempArray[1] = current;
							labelMap.put(tempNeighbor, tempArray);
							//System.out.println("temp id is " + tempNeighbor);
							// 当前出队列的点在此切片上
						} else {
							// 该出队列顶点的邻接点已经被标上颜色，则进行着色判定
							if (labelMap.get(current)[0] == labelMap.get(tempNeighbor)[0])
								continue;
							else {
								// 比较当前被着色点A的颜色，判断之前传播给A的点和当前传播过来的点和A之间的相似度比较
								BigDecimal aBigDecimal = RescalDistance.calcVectorDistance(entityVectors,
										"Cosine-2", tempNeighbor, labelMap.get(tempNeighbor)[1]);
								BigDecimal bBigDecimal = RescalDistance.calcVectorDistance(entityVectors,
										"Cosine-2", tempNeighbor, current);
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
		return labelMap;

	}
 
	

	/**
	 * 依据每个点的标签，将其分配至不同的cluster中
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
	 * 返回每个时间片以及时间片上的簇
	 * 
	 * @param slices
	 * @return 时间序列以及每个时间序列，簇标签，簇的顶点集合
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> getSliceClusterMap(
			LinkedHashMap<Integer, HashSet<Integer>> slices, ArrayList<BigDecimal[]> entityVectors,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> incomingNeighborsMap) throws IOException {

		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new LinkedHashMap<>();
		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			// 如果此时间片上有点
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
		// 每个顶点以及其所属标签以及标签是由谁传播过来的信息的map
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