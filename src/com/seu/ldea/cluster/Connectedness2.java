package com.seu.ldea.cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.segment.DatasetSegmentation2;
import com.seu.ldea.segment.SliceDataBuild2;

public class Connectedness2 {
	// 每个时间片上的点
	HashMap<Integer, HashSet<Integer>> sliceNodes;
	// 每个时间片上的簇的点,时间片编号, 簇编号,簇中的点的集合
	HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes;

	// 资源的邻居与资源之间的映射
	public static HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;

	// 获取每个时间切片每个簇的connectness
	/**
	 * 
	 * @param sliceClusters
	 *            时间片编号， 簇编号，每个簇的点
	 * @return 时间片编号，簇编号，连接度值
	 */
	public static HashMap<Integer, Double> getConnectedness(HashMap<Integer, HashSet<Integer>> sliceNodes,
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes) {
		// 每个时间片以及其connectedness值
		HashMap<Integer, Double> result = new HashMap<>();

		for (Entry<Integer, HashSet<Integer>> entry1 : sliceNodes.entrySet()) {
			// 当前时间片上的点不为空
			if (entry1.getValue().size() != 0) {
				for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry2 : sliceClusterNodes.entrySet()) {
					// 对应到同一个时间片
					if (entry1.getKey() == entry2.getKey()) {
						System.out.println("slice id " + entry2.getKey() + "slice size " + entry1.getValue().size());
						int sliceTotalEdge = getSliceEdges(entry1.getValue());
						// 簇标签以及每个簇中的点
						HashMap<Integer, HashSet<Integer>> clusterNodesOnASlilce = entry2.getValue();
						System.out.println("cluster number on the slice  " + entry2.getValue().size());
						// if(clusterNodesOnASlilce != null){
						int edgesAmongClusters = 0;
						System.out.println("before calc edges among cluster -----");
						ArrayList<HashSet<Integer>> sliceClusters = new ArrayList<>(clusterNodesOnASlilce.values());
						for (int i = 0; i < sliceClusters.size(); i++) {
							HashSet<Integer> nodesSet1 = sliceClusters.get(i);
							for (int j = i + 1; j < sliceClusters.size(); j++) {
								HashSet<Integer> nodesSet2 = sliceClusters.get(j);
								// 找任意两个簇之间的边的个数
								System.out.println(
										"nodeSet1 size " + nodesSet1.size() + " nodeSet2 size " + nodesSet2.size());
								edgesAmongClusters = edgesAmongClusters + getEdgesBetweenClusters(nodesSet1, nodesSet2);
								System.out.println("edgeAmong a pair of cluster " + edgesAmongClusters);
							}
							System.out.println("edgesAmongClusters -- " + edgesAmongClusters + " sliceTotalEdge -- "
									+ sliceTotalEdge);
							double connectedness = edgesAmongClusters / (sliceTotalEdge * 1.0);
							result.put(entry1.getKey(), connectedness);
						}
					}
				}
			} else {
				// 如果当前时间片无点
				result.put(entry1.getKey(), 0.0);
			}
		}

		for (Entry<Integer, Double> entry : result.entrySet()) {
			System.out.println("Connectness on slice " + entry.getKey() + " is " + entry.getValue());
		}

		return result;
	}

	/**
	 * 查找每个点的邻居点查找两个点之间是否存在边
	 * 
	 * @param nodes
	 * @return
	 */
	public static int getSliceEdges(HashSet<Integer> nodeSet) {
		int edgeNum = 0;
		Object[] nodeArr = nodeSet.toArray();
		for (int i = 0; i < nodeArr.length; i++) {
			int node1 = (int) nodeArr[i];
			for (int j = i + 1; j < nodeArr.length; j++) {
				int node2 = (int) nodeArr[j];
				if (resourceIncomingNeighborMap.get(node1).contains(node2)
						|| resourceOutgoingNeighborMap.get(node1).contains(node2)) {
					edgeNum++;
				}
			}
		}

		return edgeNum;
	}

	/**
	 * 找到两个社团之间连接的边的个数
	 * 
	 * @param nodeSet1
	 * @param nodeSet2
	 * @return
	 */
	public static int getEdgesBetweenClusters(HashSet<Integer> nodeSet1, HashSet<Integer> nodeSet2) {
		int edgeNum = 0;
		for (Integer node1 : nodeSet1) {
			for (Integer node2 : nodeSet2) {
				if (resourceIncomingNeighborMap.get(node1).contains(node2)
						|| resourceOutgoingNeighborMap.get(node1).contains(node2)) {
					edgeNum++;
				}
			}
		}
		return edgeNum;
	}

	public static void main(String[] args) throws IOException, ParseException {
		long t1 = System.currentTimeMillis();
/*		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";*/
		/*String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\SWCC2-latent10-lambda0.embeddings.txt";
	    String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ClassPTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";
		*/
		/*String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";*/
	
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
		
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2.initDataSegment(path, path2, rescalInputDir).segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");

		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild2.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, -1, 161771);
		 /*       
		for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
		        	 System.out.println(" Slice # " + entry.getKey() + " size " + entry.getValue().size());
		        	 for(Integer item : entry.getValue()){
		        		 System.out.print(item + " ");
		        	 }
		        	 System.out.println("\n");
		         }*/
		
		resourceIncomingNeighborMap = GraphUtil.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil.getNodeOutgoingNeighbors(rescalInputDir);
       
		/** 每个时间片上的点进行聚类操作 **/
        ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
 		ClusterImplementation.entityVectors = entityVectors;
		// 时间片以及每个时间片上的簇的点
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceClusterMap(sliceNodes, entityVectors, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);
		
		System.out.println("--------Test----------");		
		  for(Entry<Integer, HashSet<Integer>> entry :sliceNodes.entrySet()){ 
			  HashSet<Integer> aset = entry.getValue(); 
		       System.out.println("slice id " + aset.isEmpty()); 
		  for(Integer bset: aset)
		 {
			   System.out.print(" " + bset);
			   }
		  System.out.println("\n");
		  }
	
		  
		for(Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry :sliceClusterNodes.entrySet()){ 
			  HashMap<Integer, HashSet<Integer>> aset = entry.getValue(); 
		       System.out.println("aSet empty? " + aset.isEmpty()); 
		  for(Entry<Integer, HashSet<Integer>> bset: aset.entrySet())
		 {
			   System.out.println("bset cluster size -------" + bset.getValue().size());
			   } }
		System.out.println("Slice cluster nodes size " + sliceClusterNodes.size());
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost " + (t2 - t1) / 1000 + " s");
	
		getConnectedness(sliceNodes, sliceClusterNodes);
        long t3 = System.currentTimeMillis();
        System.out.println("Conntectness time cost " + (t3 - t2)/ 1000 + " s");
	}
}
