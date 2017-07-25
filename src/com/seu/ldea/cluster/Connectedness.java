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
 * 连接度作为聚类效果好坏的评估
 * 
 * @author Lynn
 * 
 *         connectedness = Ex / E , Ex指簇间相连边的个数，E表示图中总的边数
 *
 */
public class Connectedness {
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
						System.out.println("slice id " + entry2.getKey());
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
		*//** 给实体标记时间信息，统计class的时间区间 **//*
		// 依据rescal输入文件来获取资源的邻居
		resourceIncomingNeighborMap = GraphUtil.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil.getNodeOutgoingNeighbors(rescalInputDir);
		// 先对实体的时间信息进行抽取，从生成的文件创建resourcePTMap数据结构
		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
       // noTypeResourceTimeInfo = null;
		// 生成class的PTMap
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"DBLP-ClassPTMap0724");
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = BuildFromFile.getClassPTMap(path2);
		*//** 生成用户交互矩阵 **//*
		// 时间实体被表上了类别信息
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		// 生成用户交互矩阵
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		//classPTMap = null;
        //DBLP 45075:http://lsdis.cs.uga.edu/projects/semdis/opus#Article
		System.out.println("--- Please give the class id ");  
		Scanner scanner = new Scanner(System.in);
        Integer classId = Integer.parseInt(scanner.nextLine());
        //DBLP http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date
         System.out.println("--- Please give the time property ");
        String propertyStr = scanner.nextLine();
		
		*//** 依据时间类型以及时间属性将数据集划分成若干时间片，依据选取研究的类别构建每个时间片上的点 **//*
		SliceDataBuild.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
		SliceDataBuild.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
		// 依据选取的切分类型和时间属性切分数据集，每个时间片上保留在此时间片上的时间实体
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(161771,
				"http://purl.org/dc/elements/1.1/date");
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(37,
				"http://swrc.ontoware.org/ontology#year");
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(classId,
				propertyStr);
		// 获取每个资源的类型
		SliceDataBuild.resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);

		
		// 时间片以及时间片上的点的映射
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
		// 依据类型切分数据集，以及每个数据集上面的时间实体的集合
		SliceDataBuild.timeEntitySlices = DatasetSegmentation.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
		// 获取每个资源的类型
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
         //释放资源
         noTypeResourceTimeInfo = null;
         classPTMap = null;
         DatasetSegmentation.interactiveMatrix = null;
         DatasetSegmentation.resourceTimeInfo = null;
       
		/** 每个时间片上的点进行聚类操作 **/
         ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
 		ClusterImplementation.entityVectors = entityVectors;
		// 时间片以及每个时间片上的簇的点
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
