package com.seu.ldea.cluster2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.segment.SliceDataBuild;

//距离计算的点是规范化后的，不是bigdecimal
/**
 * 
 * @author Lynn
 *
 */
public class ClusterImplementation {
	//public static Dataset dataset;
	// 每个entity的向量表示
	public static ArrayList<Double[]> entityVectors;
	//存储每个entity的id以及它的type id
	public static HashMap<Integer, Integer> resourceTypeMap;
    public static int id;
	/**
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
		//List<Integer> visitedNode = new ArrayList<Integer>();
		HashSet<Integer> visitedNode = new HashSet<Integer>();
		int[] labelArray;
		for (int i = 0; i < centroidList.length; i++) {
			labelArray = new int[2];
			labelArray[0] = centroidList[i];
			labelArray[1] = centroidList[i];

			labelMap.put(centroidList[i], labelArray);
			nodeQueue.offer(centroidList[i]);
			visitedNode.add(centroidList[i]);
		}

		int current;
		int tempNeighbor;
		int[] tempArray;
		Set<Integer> neighborSet;

		while (!nodeQueue.isEmpty()) {
			// 当前点出队列
			current = nodeQueue.poll();
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
						Double aDouble = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, labelMap.get(tempNeighbor)[1]);
						Double bDouble = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, current);
						int result = aDouble.compareTo(bDouble);
						if (result == 0) {
							continue;
						} else if (result < 0) {
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
			Double maxDistance = new Double(0);
			// 在centroid中找相似度最大的作为标签
			for (int i = 0; i < centroidList.length; i++) {
				Double distance = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
						isolatedNode.getKey(), centroidList[i]);
				if (distance.compareTo(maxDistance) > 0)
					maxDistance = distance;
				clusterBelongedTo = centroidList[i];

			}
		
		//	System.out.println("isolated node id-- " + isolatedNode.getKey() + " cluster label--" + clusterBelongedTo);
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
			if (!result.containsKey(clusterId)) {
				HashSet<Integer> clusterNodesSet = new HashSet<>();
				clusterNodesSet.add(node.getKey());
				result.put(clusterId, clusterNodesSet);
			} else {
				result.get(clusterId).add(node.getKey());
			}
		}
		//输出簇的中心以及每个簇的成员的ID
		for (Entry<Integer, HashSet<Integer>> entry : result.entrySet()) {
			System.out.println("Cluster centroid id " + entry.getKey() 
			    + " Cluster size " + entry.getValue().size());
		    for(int memberId: entry.getValue()){
		    	if(resourceTypeMap.containsKey(memberId) 
		    			&& resourceTypeMap.get(memberId).equals(id))
		    	 System.out.print(memberId + " ");
		    }
		    System.out.println("\n");
		}
		System.out.println("\n");
		return result;
	}

	/**
	 * 获取每个时间片以及时间片上的簇
	 * @param sliceNodes
	 * @param entityVectors
	 * @param outgoingNeighborsMap
	 * @param incomingNeighborsMap
	 * @param studiedClassId：用户研究的对象类型
	 * @param dir:rescal输入文件地址
	 * @return
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> getSliceAndClusters(
			LinkedHashMap<Integer, HashSet<Integer>> sliceNodes, ArrayList<Double[]> entityVectors,
			HashMap<Integer, HashSet<Integer>> outgoingNeighborsMap,
			HashMap<Integer, HashSet<Integer>> incomingNeighborsMap,
			int studiedClassId, String rescalDir,
			boolean calcConnectedness
		) throws IOException {
        //创建所有点的邻居Map，包括出度入度点
		HashMap<Integer, HashSet<Integer>> allNeighborMap = outgoingNeighborsMap;
         for(Entry<Integer, HashSet<Integer>> node : incomingNeighborsMap.entrySet()){
        	 Integer nodeId = node.getKey();
        	 HashSet<Integer> nodeIncomingNeighbor = node.getValue();
        	 if(allNeighborMap.containsKey(nodeId)){
        		 allNeighborMap.get(nodeId).addAll(nodeIncomingNeighbor);
        	 }else{
        		 allNeighborMap.put(nodeId, nodeIncomingNeighbor);
        	 }
         }
		
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new LinkedHashMap<>();
		for (Entry<Integer, HashSet<Integer>> slice : sliceNodes.entrySet()) {
			// 如果此时间片上有点,开始选取质心
			long t1 = System.currentTimeMillis();
			if (slice.getValue().size() != 0) {
				/*Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(slice.getValue(), outgoingNeighborsMap,
						incomingNeighborsMap);*/
				//System.out.println("\n ---------------- Begin graph building ------------ ");
				Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph2(slice.getValue(), allNeighborMap);
				// 如果能构建图，才能算pagerank，才能选质心才能聚类
				if(graph.vertexSet().size() > 0) {
					int[] centroidNodesList = CentroidSelection.getCentroidNodes(graph, 
							entityVectors, 5, 1, studiedClassId, rescalDir);
					/* HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor =
					 SliceDataBuild.getSliceNodesNeighor(slice.getValue(), incomingNeighborsMap);*/
                     //进行标签传播不考虑方向
					HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor =
							 SliceDataBuild.getNodesNeighborOnSlice(slice.getValue(), allNeighborMap);
					HashMap<Integer, int[]> nodeLabelMap = labelPropagation(centroidNodesList, 
							nodesSliceNeighbor, slice.getValue());
					HashMap<Integer, HashSet<Integer>> clusters = allocateNodestoCluster(nodeLabelMap,
							outgoingNeighborsMap, incomingNeighborsMap);
				    //当用于计算connectedness时簇中的点不考虑特定类型
					if(!calcConnectedness){
				    	for(Entry<Integer, HashSet<Integer>> entry : clusters.entrySet()){
				    		Integer clusterId = entry.getKey();
				    		HashSet<Integer> clusterNodes = entry.getValue();
				    		//只包含待研究类型的点的簇
				            HashSet<Integer> newClusterNodes = new HashSet<>();
				    		for(Integer node : clusterNodes){
				    			if(resourceTypeMap.containsKey(node) 
				    					&& resourceTypeMap.get(node).equals(studiedClassId)){
				     			newClusterNodes.add(node);
				    			}
				    		}
				    		if(!newClusterNodes.isEmpty()){
				    			clusters.put(clusterId, newClusterNodes);
				    		}
				    	}
				    }
					/*	System.out.println(
							"\nSlice id: " + slice.getKey() + " slice size: " + slice.getValue().size() + " ");
					System.out.println("Labeled nodes amount ********" + nodeLabelMap.size());
					System.out.println("Graph Nodes amount *********" + graph.vertexSet().size());
					System.out.println("Cluster number on slice " + clusters.size());*/
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
	String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		String resourcePTMapPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String classPTMapPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
         int segmentEntityClassId = 161771; 
		 int studiedClassId = 129827; 
		 String timeProperty = "http://purl.org/dc/elements/1.1/date";
		
		/*String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		String resourcePTMapPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ResourcePTMap0722.txt";
		String classPTMapPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
	    //10367 article in proceedings 
		int segmentEntityClassId = 45075;
		int studiedClassId = 1;
		String timeProperty = "http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date";*/
		
		FileWriter fWriter = new FileWriter(new File("C:\\Users\\Lynn\\Desktop\\allSub2.txt"), true); 
 	    
		 id = studiedClassId;
		 resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);
		 System.out.println("Is resource type map empty ----- " + resourceTypeMap.isEmpty());
		//依据timeEntity以及时间属性进行切片
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
				.initSegment(resourcePTMapPath, classPTMapPath, rescalInputDir)
				.segmentDataSet(segmentEntityClassId, timeProperty);
        //由初步切片生成最终的切片
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild
				.initSliceDataBuild(timeEntitySlices, rescalInputDir)
				.getSliceNodes(rescalInputDir, studiedClassId, segmentEntityClassId);

		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** 每个时间片上的点进行聚类操作 **/
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(normalizedEmbeddingFilePath);
		ClusterImplementation.entityVectors = entityVectors;
		// 时间片以及每个时间片上的簇的点
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceAndClusters(sliceNodes, entityVectors, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap, studiedClassId, rescalInputDir, false);
	   
		HashMap<Integer, String> idURIMap =	ResourceInfo.getReourceURIMap(rescalInputDir);
		System.out.println("\n--------Test----------");
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry : sliceClusterNodes.entrySet()) {
			System.out.println("Slice id " + entry.getKey());
			HashMap<Integer, HashSet<Integer>> aset = entry.getValue();
			System.out.println("Slice cluster empty? " + aset.isEmpty());
			for (Entry<Integer, HashSet<Integer>> bset : aset.entrySet()) {
				System.out.println("cluster size is  -------" + bset.getValue().size() 
						+ "\n Cluster id is " + bset.getKey() );
			    HashSet<Integer> nodes = bset.getValue();
			    int count = 0;
			    for(Integer node : nodes){
			    	if(resourceTypeMap.containsKey(node) 
			    			&& resourceTypeMap.get(node).equals(studiedClassId)){
			    		fWriter.write(idURIMap.get(node)+"\n");
			    		count++;
			    	}
			    }
			    System.out.println("number of same type of nodes on this cluster is " + count);
			}
		}
		fWriter.close();
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost ****** " + (t2 - t1) / 1000);
	}
//161770
}
