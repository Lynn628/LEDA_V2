package com.seu.ldea.connectedness;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.cluster.ClusterImplementation;
import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.cluster.RescalDistanceForCluster;
import com.seu.ldea.entity.Dataset;
import com.seu.ldea.history.ClusterImplementationOldVersion;
import com.seu.ldea.history.SliceDataBuildOldVersion2;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.segment.SliceDataBuild;

public class Connectedness {
	// ÿ��ʱ��Ƭ�ϵĵ�
	HashMap<Integer, HashSet<Integer>> sliceNodes;
	// ÿ��ʱ��Ƭ�ϵĴصĵ�,ʱ��Ƭ���, �ر��,���еĵ�ļ���
	HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes;

	// ��Դ���ھ�����Դ֮���ӳ��
	public static HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;

	// ��ȡÿ��ʱ����Ƭÿ���ص�connectness
	/**
	 * 
	 * @param sliceClusters
	 *            ʱ��Ƭ��ţ� �ر�ţ�ÿ���صĵ�
	 * @return ʱ��Ƭ��ţ��ر�ţ����Ӷ�ֵ
	 */
	public static HashMap<Integer, Double> getConnectedness(HashMap<Integer, HashSet<Integer>> sliceNodes,
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes) {
		// ÿ��ʱ��Ƭ�Լ���connectednessֵ
		HashMap<Integer, Double> result = new HashMap<>();
		for (Entry<Integer, HashSet<Integer>> entry1 : sliceNodes.entrySet()) {
			// ��ǰʱ��Ƭ�ϵĵ㲻Ϊ��
			if (entry1.getValue().size() != 0) {
				for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry2 : sliceClusterNodes.entrySet()) {
					// ��Ӧ��ͬһ��ʱ��Ƭ
					if (entry1.getKey() == entry2.getKey()) {
						//System.out.println("slice id " + entry2.getKey() + "slice size " + entry1.getValue().size());
						int sliceTotalEdge = getSliceEdges(entry1.getValue());
						// �ر�ǩ�Լ�ÿ�����еĵ�
						HashMap<Integer, HashSet<Integer>> clusterNodesOnASlilce = entry2.getValue();
						//System.out.println("Cluster number on  slice  " + entry2.getKey() + " is " + entry2.getValue().size());
						// if(clusterNodesOnASlilce != null){
						int edgesAmongClusters = 0;
					//	System.out.println("before calc edges among cluster -----");
						ArrayList<HashSet<Integer>> sliceClusters = new ArrayList<>(clusterNodesOnASlilce.values());
						//һ��ʱ��Ƭ�ϵĴ�֮��ı���
						for (int i = 0; i < sliceClusters.size(); i++) {
							HashSet<Integer> nodesSet1 = sliceClusters.get(i);
							for (int j = i + 1; j < sliceClusters.size(); j++) {
								HashSet<Integer> nodesSet2 = sliceClusters.get(j);
								// ������������֮��ıߵĸ���
								//System.out.println(
								//		"nodeSet1 size " + nodesSet1.size() + " nodeSet2 size " + nodesSet2.size());
								edgesAmongClusters = edgesAmongClusters + getEdgesBetweenClusters(nodesSet1, nodesSet2);
							//	System.out.println("edgeAmong a pair of cluster " + edgesAmongClusters);
							}
						}
						System.out.println("edgesAmongClusters -- " + edgesAmongClusters + " sliceTotalEdge -- "
								+ sliceTotalEdge);
						double connectedness = edgesAmongClusters / (sliceTotalEdge * 1.0);
						result.put(entry1.getKey(), connectedness);
					}
				}
			} else {
				// �����ǰʱ��Ƭ�޵�
				result.put(entry1.getKey(), 0.0);
			}
		}

		for (Entry<Integer, Double> entry : result.entrySet()) {
			System.out.println("Connectness on slice " + entry.getKey() + " is " + entry.getValue());
		}

		return result;
	}

	/**
	 * ����ÿ������ھӵ����������֮���Ƿ���ڱ�
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
	 * �ҵ���������֮�����ӵıߵĸ���
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
	
		/*String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";*/

		String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ResourcePTMap0722.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
	/*	LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");*/
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(10367, "http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date");


		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild
				.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, 1, 10367);
		/*LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild2//129827

				.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir,129827, 161771);*/
		/*
		 * for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
		 * System.out.println(" Slice # " + entry.getKey() + " size " +
		 * entry.getValue().size()); for(Integer item : entry.getValue()){
		 * System.out.print(item + " "); } System.out.println("\n"); }
		 */

		 resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		 resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** ÿ��ʱ��Ƭ�ϵĵ���о������ **/
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(normalizedEmbeddingFilePath);
		ClusterImplementationOldVersion.entityVectors = entityVectors;
		HashMap<Integer, String> classTypeId = Dataset.getDataSetClass(rescalInputDir);
		// ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĴصĵ�
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceClusterMap(sliceNodes, entityVectors, classTypeId, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);

		System.out.println("--------Test----------");
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry : sliceClusterNodes.entrySet()) {
			HashMap<Integer, HashSet<Integer>> aset = entry.getValue();
			System.out.println("aSet empty? " + aset.isEmpty());
			for (Entry<Integer, HashSet<Integer>> bset : aset.entrySet()) {
				System.out.println("bset cluster size -------" + bset.getValue().size());
			}
		}
       
		getConnectedness(sliceNodes, sliceClusterNodes);
		
	
  }
}
