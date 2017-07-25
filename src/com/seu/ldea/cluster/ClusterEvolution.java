package com.seu.ldea.cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.history.SliceDataBuild;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.time.InteractiveMatrix;
import com.seu.ldea.time.LabelClassWithTime;
import com.seu.ldea.util.BuildFromFile;

public class ClusterEvolution {
	// 每个时间片上的点
	HashMap<Integer, HashSet<Integer>> sliceNodes;
	// 每个时间片上的簇的点,时间片编号, 簇编号,簇中的点的集合
	HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes;

	// 资源的邻居与资源之间的映射
	public static HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;
	
	class ClusterMap{
		int sliceId;
		int clusterId;
		ArrayList<Integer> incomingClusterId;
		ArrayList<Integer> outgoingClusterId;
	   public ArrayList<Integer> getIncomingClusterId() {
		return incomingClusterId;
	}
	   public ArrayList<Integer> getOutgoingClusterId() {
		return outgoingClusterId;
	}
	
	
	}
	
	/**
	 * 
	 * @param slicesClusters,
	 *            每个时间片以及时间片上的簇以及簇中点的集合
	 * @return 时间片编号，社团编号，与该社团相关联的下一时间片上社团的编号
	 */
	public static ArrayList<ClusterMap> getCluterEvolution(
			LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> inputslicesClusters) {
	    //每个时间片以及每个时间片上的簇，以及簇的
	    ArrayList<ClusterMap> result = new ArrayList<>();
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> slicesClusters = new LinkedHashMap<>();
		//剔除上面没有点的时间片
		for(Entry<Integer, HashMap<Integer, HashSet<Integer>>> slice : inputslicesClusters.entrySet()){
        	if(slice.getValue() != null){
        		slicesClusters.put(slice.getKey(), slice.getValue());
        	}
        }
		
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> iter1 = slicesClusters.entrySet().iterator();
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> iter2 = slicesClusters.entrySet().iterator();
		Entry<Integer, HashMap<Integer, HashSet<Integer>>> currentSlice;
		Entry<Integer, HashMap<Integer, HashSet<Integer>>> latterSlice = iter2.next();
		if (latterSlice != null)
			while (iter2.hasNext()) {
				currentSlice = iter1.next();
				latterSlice = iter2.next();
				
				System.out.println(" current slice id " + currentSlice.getKey() + " clusters amount "
						+ currentSlice.getValue().size());
				System.out.println("latter slice id " + latterSlice.getKey() + " clusters amount "
						+ latterSlice.getValue().size());
				HashMap<Integer, HashSet<Integer>> currentClusters = currentSlice.getValue();
				HashMap<Integer, HashSet<Integer>> latterClusters = latterSlice.getValue();

				for (Entry<Integer, HashSet<Integer>> cluster1 : currentClusters.entrySet()) {
					HashSet<Integer> clusterNodeSet1 = cluster1.getValue();

					for (Entry<Integer, HashSet<Integer>> cluster2 : latterClusters.entrySet()) {
						HashSet<Integer> clusterNodeSet2 = cluster2.getValue();
						int totalNodes = clusterNodeSet1.size() + clusterNodeSet2.size();

						double percentage = getClusterOverlapAmount(clusterNodeSet1, clusterNodeSet2)
								/ (totalNodes * 1.0);
						if(percentage >= 0.005){
							
						}
						System.out.println("****************Two cluster similar percentage is****  " + percentage);
					}
				}
			}
		return null;
	}
	
	
	

	public static int getClusterOverlapAmount(HashSet<Integer> currentCluster, HashSet<Integer> latterCluster) {
		int sameNodes = 0;
		for (Integer outterNum : currentCluster) {
			for (Integer innerNum : latterCluster) {
				if (outterNum == innerNum)
					sameNodes++;
			}
		}
		return sameNodes;
	}

	public static void main(String[] args) throws IOException, ParseException {


		long t1 = System.currentTimeMillis();
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\SWCC2-latent10-lambda0.embeddings.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";

		resourceIncomingNeighborMap = GraphUtil.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil.getNodeOutgoingNeighbors(rescalInputDir);

		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"");
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		// 依据类型切分数据集，以及每个数据集上面的时间
		SliceDataBuild.slices = DatasetSegmentation.segmentDataSet(2049, "http://www.w3.org/2002/12/cal/ical#dtstart");
		// 获取每个资源的类型
		SliceDataBuild.resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);
		SliceDataBuild.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
		SliceDataBuild.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
		// resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		ArrayList<BigDecimal[]> entityVectors = RescalDistance.getNodeVector(embeddingFilePath);
		ClusterImplementation.entityVectors = entityVectors;

		// 时间片以及时间片上的点的映射
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild.getSliceLinkedNodes(rescalInputDir, -1, 37);
		// 时间片以及每个时间片上的簇的点
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation.getSliceClusterMap(
				sliceNodes, entityVectors, resourceOutgoingNeighborMap, resourceIncomingNeighborMap);
		System.out.println("--------Test----------");
		
	/*for(Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry : sliceClusterNodes.entrySet()){
		  HashMap<Integer, HashSet<Integer>> aset = entry.getValue();
		 // System.out.println("aSet empty? " + aset.isEmpty());
		  for(Entry<Integer, HashSet<Integer>> bset: aset.entrySet()){
			 // System.out.println("bset cluster size -------" + bset.getValue().size());
		  }
		}*/
		System.out.println("Slice cluster nodes size(time slice amount)  " + sliceClusterNodes.size());
		getCluterEvolution(sliceClusterNodes);
	}
}
