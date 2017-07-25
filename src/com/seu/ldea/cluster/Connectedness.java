package com.seu.ldea.cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.jena.sparql.sse.builders.BuilderExpr.Build;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.segment.SliceDataBuild;
import com.seu.ldea.time.InteractiveMatrix;
import com.seu.ldea.time.LabelClassWithTime;
import com.seu.ldea.util.BuildFromFile;

/**
 * ���Ӷ���Ϊ����Ч���û�������
 * 
 * @author Lynn
 * 
 *         connectedness = Ex / E , Exָ�ؼ������ߵĸ�����E��ʾͼ���ܵı���
 *
 */
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
						System.out.println("slice id " + entry2.getKey());
						int sliceTotalEdge = getSliceEdges(entry1.getValue());
						// �ر�ǩ�Լ�ÿ�����еĵ�
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
								// ������������֮��ıߵĸ���
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
/*		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";*/
		/*String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\SWCC2-latent10-lambda0.embeddings.txt";
	    String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ClassPTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";
		*//** ��ʵ����ʱ����Ϣ��ͳ��class��ʱ������ **//*
		// ����rescal�����ļ�����ȡ��Դ���ھ�
		resourceIncomingNeighborMap = GraphUtil.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil.getNodeOutgoingNeighbors(rescalInputDir);
		// �ȶ�ʵ���ʱ����Ϣ���г�ȡ�������ɵ��ļ�����resourcePTMap���ݽṹ
		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
       // noTypeResourceTimeInfo = null;
		// ����class��PTMap
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"DBLP-ClassPTMap0724");
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = BuildFromFile.getClassPTMap(path2);
		*//** �����û��������� **//*
		// ʱ��ʵ�屻�����������Ϣ
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		// �����û���������
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		//classPTMap = null;
        //DBLP 45075:http://lsdis.cs.uga.edu/projects/semdis/opus#Article
		System.out.println("--- Please give the class id ");  
		Scanner scanner = new Scanner(System.in);
        Integer classId = Integer.parseInt(scanner.nextLine());
        //DBLP http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date
         System.out.println("--- Please give the time property ");
        String propertyStr = scanner.nextLine();
		
		*//** ����ʱ�������Լ�ʱ�����Խ����ݼ����ֳ�����ʱ��Ƭ������ѡȡ�о�����𹹽�ÿ��ʱ��Ƭ�ϵĵ� **//*
		SliceDataBuild.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
		SliceDataBuild.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
		// ����ѡȡ���з����ͺ�ʱ�������з����ݼ���ÿ��ʱ��Ƭ�ϱ����ڴ�ʱ��Ƭ�ϵ�ʱ��ʵ��
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(161771,
				"http://purl.org/dc/elements/1.1/date");
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(37,
				"http://swrc.ontoware.org/ontology#year");
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(classId,
				propertyStr);
		// ��ȡÿ����Դ������
		SliceDataBuild.resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);

		
		// ʱ��Ƭ�Լ�ʱ��Ƭ�ϵĵ��ӳ��
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild.getSliceLinkedNodes(rescalInputDir, -1,
				classId);
         //System.out.println("--- Please give the class id to study ");
         //Integer classId2 = Integer.parseInt(scanner.nextLine());
         LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild.getSliceLinkedNodes(rescalInputDir, 6539, 37);
         for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
        	 System.out.println(" Slice # " + entry.getKey() + " size " + entry.getValue().size());
        	 for(Integer item : entry.getValue()){
        		 System.out.print(item + " ");
        	 }
        	 System.out.println("\n");
         }*/
		/*String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";*/
	
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\Jamendo-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		//HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,"");
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = BuildFromFile.getClassPTMap(path2);		
		SliceDataBuild.resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		SliceDataBuild.resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		// ���������з����ݼ����Լ�ÿ�����ݼ������ʱ��ʵ��ļ���
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
		// ��ȡÿ����Դ������
				SliceDataBuild.resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);

		         //System.out.println("--- Please give the class id to study ");
		         //Integer classId2 = Integer.parseInt(scanner.nextLine());
		         LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild.getSliceLinkedNodes(rescalInputDir, -1, 161771);
		         for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
		        	 System.out.println(" Slice # " + entry.getKey() + " size " + entry.getValue().size());
		        	 for(Integer item : entry.getValue()){
		        		 System.out.print(item + " ");
		        	 }
		        	 System.out.println("\n");
		         }
		         resourceIncomingNeighborMap = GraphUtil.getNodeIncomingNeighbors(rescalInputDir);
		 		resourceOutgoingNeighborMap = GraphUtil.getNodeOutgoingNeighbors(rescalInputDir);
         //�ͷ���Դ
         noTypeResourceTimeInfo = null;
         classPTMap = null;
         DatasetSegmentation.interactiveMatrix = null;
         DatasetSegmentation.resourceTimeInfo = null;
       
		/** ÿ��ʱ��Ƭ�ϵĵ���о������ **/
         ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
 		ClusterImplementation.entityVectors = entityVectors;
		// ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĴصĵ�
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceClusterMap(sliceNodes, entityVectors, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);
		System.out.println("--------Test----------");
        
		
		  for(Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry :sliceClusterNodes.entrySet()){ 
			  HashMap<Integer, HashSet<Integer>> aset = entry.getValue(); 
		       System.out.println("aSet empty? " + aset.isEmpty()); 
		  for(Entry<Integer, HashSet<Integer>> bset: aset.entrySet())
		 {
			   System.out.println("bset cluster size -------" + bset.getValue().size());
			   } }
		 
		

		System.out.println("Slice cluster nodes size " + sliceClusterNodes.size());
		getConnectedness(sliceNodes, sliceClusterNodes);
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost " + (t2 - t1) / 1000 + " s");
	}

}
