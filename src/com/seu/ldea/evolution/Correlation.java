package com.seu.ldea.evolution;
/**
 * ���㲢�洢����ʱ��Ƭ����������֮������ƶ�
 * @author Lynn
 *
 */

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.cluster.ClusterImplementation;
import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.cluster.RescalDistanceForCluster;
import com.seu.ldea.entity.Dataset;
import com.seu.ldea.history.SliceDataBuildOldVersion2;
import com.seu.ldea.segment.DatasetSegmentation;

public class Correlation {

	static class ClusterCorrelation {
		Integer sliceId;
		Integer clusterId;
		Double similarity;

		public ClusterCorrelation(Integer sliceId, Integer clusterId, double similarity) {
			this.sliceId = sliceId;
			this.clusterId = clusterId;
			this.similarity = similarity;
		}

		public Integer getClusterId() {
			return clusterId;
		}

		public Integer getSliceId() {
			return sliceId;
		}

		public void setSimilarity(Double similarity) {
			this.similarity = similarity;
		}

		public Double getSimilarity() {
			return similarity;
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "sliceId: " + sliceId + " clusterId: " + clusterId + " Corr: " + similarity + " ; ";
		}
	}

	// ˳��ʱ����ķ���
	static class ClusterForwardBackwardCorr {
		Integer sliceId;
		Integer clusterId;
		ArrayList<ClusterCorrelation> forwardClusterSimilarity;
		ArrayList<ClusterCorrelation> backwardClusterSimilarity;

		public ClusterForwardBackwardCorr(Integer sliceId, Integer clusterId,
				ArrayList<ClusterCorrelation> forwardClusterSimilarity,
				ArrayList<ClusterCorrelation> backwardClusterSimilarity) {
			super();
			this.sliceId = sliceId;
			this.clusterId = clusterId;
			this.forwardClusterSimilarity = forwardClusterSimilarity;
			this.backwardClusterSimilarity = backwardClusterSimilarity;
		}

		public Integer getSliceId() {
			return sliceId;
		}

		public void setSliceId(Integer sliceId) {
			this.sliceId = sliceId;
		}

		public Integer getClusterId() {
			return clusterId;
		}

		public void setClusterId(Integer clusterId) {
			this.clusterId = clusterId;
		}

		public ArrayList<ClusterCorrelation> getForwardClusterSimilarity() {
			return forwardClusterSimilarity;
		}

		public void setForwardClusterSimilarity(ArrayList<ClusterCorrelation> forwardClusterSimilarity) {
			this.forwardClusterSimilarity = forwardClusterSimilarity;
		}

		public ArrayList<ClusterCorrelation> getBackwardClusterSimilarity() {
			return backwardClusterSimilarity;
		}

		public void setBackwardClusterSimilarity(ArrayList<ClusterCorrelation> backwardClusterSimilarity) {
			this.backwardClusterSimilarity = backwardClusterSimilarity;
		}

		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			
			StringBuilder result = new StringBuilder("SliceId: " + sliceId + " ClusterId:" + clusterId);
			result.append("\nForwardArr\n");
			for(ClusterCorrelation item : forwardClusterSimilarity){
				result.append(item.toString());
			}
			result.append("\nBackward\n");
			for(ClusterCorrelation item : backwardClusterSimilarity){
				result.append(item.toString());
			}
			return result.toString();
		}
	}

	// ��ȡ�������ݼ���ÿ��ʱ��Ƭ������������ʱ��Ƭ���ŵ�Correlationֵ
	/**
	 * 
	 * @param inputslicesClusters
	 * @param threshold
	 * @return sliceId, clusterId, clusterForward&Backward
	 */
	public static LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> getClusterCorrelationAmongSlices(
			LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> inputslicesClusters) {
		LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations = new LinkedHashMap<>();
		// ��ʼ��Correlation
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> slice : inputslicesClusters.entrySet()) {
			Integer sliceId = slice.getKey();
			HashMap<Integer, ClusterForwardBackwardCorr> sliceClustersCorr = new HashMap<>();
			for (Entry<Integer, HashSet<Integer>> cluster : slice.getValue().entrySet()) {
				Integer clusterId = cluster.getKey();
				System.out.println("Slice id " + sliceId + " Cluster id " + clusterId);
				ArrayList<ClusterCorrelation> forwardCorr = new ArrayList<>();
				ArrayList<ClusterCorrelation> backwardCorr = new ArrayList<>();
				ClusterForwardBackwardCorr clusterFBCorr = new ClusterForwardBackwardCorr(sliceId, clusterId,
						forwardCorr, backwardCorr);
				sliceClustersCorr.put(clusterId, clusterFBCorr);
			}
			correlations.put(sliceId, sliceClustersCorr);
		}
	
		// ָ��ǰʱ��Ƭ��iteration
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> currIter = inputslicesClusters.entrySet()
				.iterator();
		// ָ���һ��ʱ��Ƭ��itera
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> laterIter = inputslicesClusters.entrySet()
				.iterator();
		// ����������һ��
		laterIter.next();
		while (laterIter.hasNext()) {
			Entry<Integer, HashMap<Integer, HashSet<Integer>>> latterSlice = laterIter.next();
			Entry<Integer, HashMap<Integer, HashSet<Integer>>> currentSlice = currIter.next();
			Integer currentSliceId = currentSlice.getKey();
			Integer latterSliceId = latterSlice.getKey();
			HashMap<Integer, HashSet<Integer>> currentClusters = currentSlice.getValue();
			HashMap<Integer, HashSet<Integer>> latterClusters = latterSlice.getValue();
			
			// ĳʱ��Ƭ�ϴص�ǰ��ʱ��Ƭ�����ƶ���Ϣ
			for (Entry<Integer, HashSet<Integer>> currentCluster : currentClusters.entrySet()) {
				Integer currentClusterId = currentCluster.getKey();

				System.out.println("Current slice id " + currentSliceId + " latterSlice id " + latterSliceId);
				for (Entry<Integer, HashSet<Integer>> latterCluster : latterClusters.entrySet()) {
					// ��һ��ʱ��Ƭ��ĳһ����
					Integer latterClusterId = latterCluster.getKey();
                System.out.println("Current cluster id " + currentClusterId + " latter cluster id " + latterClusterId);
					double similarityForward = calcClusterCorrelation(currentCluster.getValue(),
							latterCluster.getValue());
					double similarityBackward = calcClusterCorrelation(latterCluster.getValue(),
							currentCluster.getValue());
					ClusterCorrelation forwardClusterCorr = new ClusterCorrelation(latterSliceId, latterClusterId,
							similarityForward);
					ClusterCorrelation backwardClusterCorr = new ClusterCorrelation(currentSliceId, currentClusterId,
							similarityBackward);
					// Ϊ��ǰʱ��Ƭ�ĵ�ǰ�����forward�Ϊ��һʱ��Ƭ�ĵ�ǰ�����backward��
					//correlations.get(currentSlice);
				 correlations.get(currentSliceId).get(currentClusterId).getForwardClusterSimilarity().add(forwardClusterCorr);
							//sforwardArr.add(forwardClusterCorr);
					
				 correlations.get(latterSliceId).get(latterClusterId).getBackwardClusterSimilarity().add(backwardClusterCorr);
						 //	backwardArr.add(backwardClusterCorr);
				}
			}
		}

		for(Entry<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> item : correlations.entrySet()){
			System.out.println("SliceId " + item.getKey());
			for(Entry<Integer, ClusterForwardBackwardCorr> item2 : item.getValue().entrySet()){
				System.out.println("ClusterId" + item2.getKey() + " forwardSize " + item2.getValue().getForwardClusterSimilarity().size() +
						" backwardSize " + item2.getValue().getBackwardClusterSimilarity().size());
			}
		}
		return correlations;
	}

	public static double calcClusterCorrelation(HashSet<Integer> cluster1, HashSet<Integer> cluster2) {
		int sameNodes = 0;
		for (Integer outterNum : cluster1) {
			for (Integer innerNum : cluster2) {
				//Integer �ȺűȽ�������
				if (outterNum.intValue() == innerNum.intValue()){
					sameNodes++;
				}
			}
		}
		//int totalNodes = cluster1.size() + cluster2.size();
		System.out.println("SameNodes---------" + sameNodes);
		double result = sameNodes / (cluster1.size() * 1.0);
		return result;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException{
		String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedJamendo-latent10.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";

		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");

		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuildOldVersion2
				.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, 129827, 161771);
		

		 HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		 HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** ÿ��ʱ��Ƭ�ϵĵ���о������ **/
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(normalizedEmbeddingFilePath);
		ClusterImplementation.entityVectors = entityVectors;
		HashMap<Integer, String> classTypeId = Dataset.getDataSetClass(rescalInputDir, "");
		// ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĴصĵ�
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceClusterMap(sliceNodes, entityVectors, classTypeId, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);
	  
	   getClusterCorrelationAmongSlices(sliceClusterNodes);
	}


}
