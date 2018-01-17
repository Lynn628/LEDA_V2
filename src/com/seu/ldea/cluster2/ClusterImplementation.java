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

//�������ĵ��ǹ淶����ģ�����bigdecimal
/**
 * 
 * @author Lynn
 *
 */
public class ClusterImplementation {
	//public static Dataset dataset;
	// ÿ��entity��������ʾ
	public static ArrayList<Double[]> entityVectors;
	//�洢ÿ��entity��id�Լ�����type id
	public static HashMap<Integer, Integer> resourceTypeMap;
    public static int id;
	/**
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
			// ��ǰ�������
			current = nodeQueue.poll();
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
						Double aDouble = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, labelMap.get(tempNeighbor)[1]);
						Double bDouble = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
								tempNeighbor, current);
						int result = aDouble.compareTo(bDouble);
						if (result == 0) {
							continue;
						} else if (result < 0) {
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
			Double maxDistance = new Double(0);
			// ��centroid�������ƶ�������Ϊ��ǩ
			for (int i = 0; i < centroidList.length; i++) {
				Double distance = RescalDistanceForCluster.calcVectorDistance(entityVectors, "Cosine-2",
						isolatedNode.getKey(), centroidList[i]);
				if (distance.compareTo(maxDistance) > 0)
					maxDistance = distance;
				clusterBelongedTo = centroidList[i];

			}
		
		//	System.out.println("isolated node id-- " + isolatedNode.getKey() + " cluster label--" + clusterBelongedTo);
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
			if (!result.containsKey(clusterId)) {
				HashSet<Integer> clusterNodesSet = new HashSet<>();
				clusterNodesSet.add(node.getKey());
				result.put(clusterId, clusterNodesSet);
			} else {
				result.get(clusterId).add(node.getKey());
			}
		}
		//����ص������Լ�ÿ���صĳ�Ա��ID
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
	 * ��ȡÿ��ʱ��Ƭ�Լ�ʱ��Ƭ�ϵĴ�
	 * @param sliceNodes
	 * @param entityVectors
	 * @param outgoingNeighborsMap
	 * @param incomingNeighborsMap
	 * @param studiedClassId���û��о��Ķ�������
	 * @param dir:rescal�����ļ���ַ
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
        //�������е���ھ�Map������������ȵ�
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
			// �����ʱ��Ƭ���е�,��ʼѡȡ����
			long t1 = System.currentTimeMillis();
			if (slice.getValue().size() != 0) {
				/*Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph(slice.getValue(), outgoingNeighborsMap,
						incomingNeighborsMap);*/
				//System.out.println("\n ---------------- Begin graph building ------------ ");
				Graph<Integer, DefaultEdge> graph = GraphUtil.buildGraph2(slice.getValue(), allNeighborMap);
				// ����ܹ���ͼ��������pagerank������ѡ���Ĳ��ܾ���
				if(graph.vertexSet().size() > 0) {
					int[] centroidNodesList = CentroidSelection.getCentroidNodes(graph, 
							entityVectors, 5, 1, studiedClassId, rescalDir);
					/* HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor =
					 SliceDataBuild.getSliceNodesNeighor(slice.getValue(), incomingNeighborsMap);*/
                     //���б�ǩ���������Ƿ���
					HashMap<Integer, HashSet<Integer>> nodesSliceNeighbor =
							 SliceDataBuild.getNodesNeighborOnSlice(slice.getValue(), allNeighborMap);
					HashMap<Integer, int[]> nodeLabelMap = labelPropagation(centroidNodesList, 
							nodesSliceNeighbor, slice.getValue());
					HashMap<Integer, HashSet<Integer>> clusters = allocateNodestoCluster(nodeLabelMap,
							outgoingNeighborsMap, incomingNeighborsMap);
				    //�����ڼ���connectednessʱ���еĵ㲻�����ض�����
					if(!calcConnectedness){
				    	for(Entry<Integer, HashSet<Integer>> entry : clusters.entrySet()){
				    		Integer clusterId = entry.getKey();
				    		HashSet<Integer> clusterNodes = entry.getValue();
				    		//ֻ�������о����͵ĵ�Ĵ�
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
		//����timeEntity�Լ�ʱ�����Խ�����Ƭ
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
				.initSegment(resourcePTMapPath, classPTMapPath, rescalInputDir)
				.segmentDataSet(segmentEntityClassId, timeProperty);
        //�ɳ�����Ƭ�������յ���Ƭ
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild
				.initSliceDataBuild(timeEntitySlices, rescalInputDir)
				.getSliceNodes(rescalInputDir, studiedClassId, segmentEntityClassId);

		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** ÿ��ʱ��Ƭ�ϵĵ���о������ **/
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(normalizedEmbeddingFilePath);
		ClusterImplementation.entityVectors = entityVectors;
		// ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĴصĵ�
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
