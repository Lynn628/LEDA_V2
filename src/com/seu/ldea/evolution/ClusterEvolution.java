package com.seu.ldea.evolution;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.cluster.ClusterImplementationBigDecimal;
import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.evolution.ClusterEvolution.ClusterMap;
import com.seu.ldea.history.SliceDataBuild;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.tau.RescalDistance;
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
	
	static class  ClusterInfo{
		int sliceId;
		int clusterId;
		public ClusterInfo(int sliceId, int clusterId) {
			super();
			this.sliceId = sliceId;
			this.clusterId = clusterId;
		}
		
	}
    
	static class CorrelationInfo{
		ClusterInfo clusterInfo;
		double percentage;
		public CorrelationInfo(ClusterInfo clusterInfo, double percentage) {
			super();
			this.clusterInfo = clusterInfo;
			this.percentage = percentage;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return clusterInfo.toString() + " :" + percentage + " %";
		}
	}
	//收集与当前簇相关联的下一时间片的簇
	static class ClusterMap{	
	ClusterInfo currentClass;
	ArrayList<CorrelationInfo> relatedCluster;
	public ClusterMap(ClusterInfo currentClusterInfo, ArrayList<CorrelationInfo> relatedCluster) {
		super();
		this.currentClass = currentClusterInfo;
		this.relatedCluster = relatedCluster;
	}
	   
	   @Override
		public String toString() {
			// TODO Auto-generated method stub
		   String out = currentClass.sliceId  + " - " + currentClass.clusterId + ">>>";
			for(CorrelationInfo item : relatedCluster){
				out += item.toString() + "  ";
			}
		   
		   return out;
		}
	    
	}
	
	/**
	 * 
	 * @param slicesClusters,
	 *            每个时间片以及时间片上的簇以及簇中点的集合
	 * @return 时间片编号，社团编号，与该社团相关联的下一时间片上社团的编号
	 */
	public static HashMap<Integer, ArrayList<ClusterMap>> getCluterEvolution(
			LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> inputslicesClusters, double threshold) {
	    //每个时间片以及每个时间片上的簇，以及簇与相邻时间片上簇的关联
	    //时间片id， 簇的对应关系
		HashMap<Integer, ArrayList<ClusterMap>> result = new HashMap<Integer, ArrayList<ClusterMap>>();
		//剔除上面没有点的时间片,重新组成演化时间片
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> slicesClusters = new LinkedHashMap<>();
		for(Entry<Integer, HashMap<Integer, HashSet<Integer>>> slice : inputslicesClusters.entrySet()){
        	if(slice.getValue() != null){
        		slicesClusters.put(slice.getKey(), slice.getValue());
        	}
        }
		//时间片id,簇id,簇上点的集合
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> iter1 = slicesClusters.entrySet().iterator();
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> iter2 = slicesClusters.entrySet().iterator();
		Entry<Integer, HashMap<Integer, HashSet<Integer>>> currentSlice;
		Entry<Integer, HashMap<Integer, HashSet<Integer>>> latterSlice = iter2.next();
		//先指向后一个时间片  
		if(latterSlice != null)
			while (iter2.hasNext()) {
				currentSlice = iter1.next();
				latterSlice = iter2.next();	
				System.out.println(" current slice id " + currentSlice.getKey() + " clusters amount "
						+ currentSlice.getValue().size());
				System.out.println("latter slice id " + latterSlice.getKey() + " clusters amount "
						+ latterSlice.getValue().size());
				Integer currentSliceId = currentSlice.getKey();
				Integer latterSliceId = latterSlice.getKey();
				ArrayList<ClusterMap> currentSliceClusterMap = new ArrayList<>();
				HashMap<Integer, HashSet<Integer>> currentSliceClusters = currentSlice.getValue();
				HashMap<Integer, HashSet<Integer>> latterSliceClusters = latterSlice.getValue();
                   
				for (Entry<Integer, HashSet<Integer>> cluster1 : currentSliceClusters.entrySet()) {
					Integer currentClusterId = cluster1.getKey();
					ClusterInfo currentClusterInfo = new ClusterInfo(currentSliceId, currentClusterId);
					//收集下一时间片与他相关联的簇
					ArrayList<CorrelationInfo> relatedCluster = new ArrayList<>();
					HashSet<Integer> clusterNodeSet1 = cluster1.getValue();
					//遍历下一时间片的所有簇，找相关联的
					for (Entry<Integer, HashSet<Integer>> cluster2 : latterSliceClusters.entrySet()) {
						Integer latterClusterId = cluster2.getKey();
						HashSet<Integer> clusterNodeSet2 = cluster2.getValue();
						int totalNodes = clusterNodeSet1.size() + clusterNodeSet2.size();
						double percentage = getClusterOverlapNodesNumber(clusterNodeSet1, clusterNodeSet2)
								/ (totalNodes * 1.0);
						System.out.println("<<<<<<<Percentage >>>>>>" + percentage );
						if(percentage >= threshold){
						  ClusterInfo latterClusterInfo = new ClusterInfo(latterSliceId, latterClusterId);
						  CorrelationInfo correlationInfo = new CorrelationInfo(latterClusterInfo, percentage);
						  relatedCluster.add(correlationInfo);
						}
						System.out.println("****************Two cluster similar percentage is****  " + percentage);
					}
					ClusterMap correlationInfo = new ClusterMap(currentClusterInfo, relatedCluster);
				    currentSliceClusterMap.add(correlationInfo);
				}
				result.put(currentSliceId, currentSliceClusterMap);
			}
		return result;
	}
	
	
	

	public static int getClusterOverlapNodesNumber(HashSet<Integer> currentCluster, HashSet<Integer> latterCluster) {
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
		ClusterImplementationBigDecimal.entityVectors = entityVectors;

		// 时间片以及时间片上的点的映射
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuild.getSliceLinkedNodes(rescalInputDir, -1, 37);
		// 时间片以及每个时间片上的簇的点
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementationBigDecimal.getSliceClusterMap(
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
		HashMap<Integer, ArrayList<ClusterMap>> result = getCluterEvolution(sliceClusterNodes, 0);
	    for(Entry<Integer, ArrayList<ClusterMap>> item : result.entrySet()){
	    	System.out.println("Slice id " + item + " : " );
	    	for(ClusterMap map : item.getValue()){
	    		System.out.println(map.toString());
	    	}
	    }
	}
}
