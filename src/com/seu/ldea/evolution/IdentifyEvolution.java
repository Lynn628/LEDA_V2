package com.seu.ldea.evolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
import com.seu.ldea.connectedness.Connectedness;
import com.seu.ldea.entity.Dataset;
import com.seu.ldea.evolution.Correlation.ClusterCorrelation;
import com.seu.ldea.evolution.Correlation.ClusterForwardBackwardCorr;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.segment.SliceDataBuild;

public class IdentifyEvolution {
	public static ArrayList<EvolutionEventTriple> growthEvolutionMaps = new ArrayList<>();
	public static ArrayList<EvolutionEventTriple> contractionEvolutionMaps = new ArrayList<>();
	public static ArrayList<EvolutionEventTriple> mergeEvolutionMaps = new ArrayList<>();
	public static ArrayList<EvolutionEventTriple> splitEvolutionMaps = new ArrayList<>();
	public static ArrayList<EvolutionEventTriple> birthEvolutionMaps = new ArrayList<>();
	public static ArrayList<EvolutionEventTriple> deathEvolutionMaps = new ArrayList<>();

	static class EvolutionEventTriple {
		Integer beginSliceId;
		Integer beginClusterId;
		Integer endSliceId;
		Integer endClusterId;
		String event;

		public EvolutionEventTriple(Integer beginSlice, Integer beginCluster, Integer endSlice, Integer endCluster,
				String event) {
			super();
			this.beginSliceId = beginSlice;
			this.beginClusterId = beginCluster;
			this.endSliceId = endSlice;
			this.endClusterId = endCluster;
			this.event = event;
		}

		public Integer getBeginClusterId() {
			return beginClusterId;
		}

		public Integer getBeginSliceId() {
			return beginSliceId;
		}

		public Integer getEndClusterId() {
			return endClusterId;
		}

		public Integer getEndSliceId() {
			return endSliceId;
		}

		public String getEvent() {
			return event;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "< sliceId: " + beginSliceId + " clusterId:" + beginClusterId + " , " + event + " , " + " SliceId:"
					+ endSliceId + " clusterId:" + endClusterId + " >";
		}
	}

	public static void identifyEvolutionEvents(
			LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> inputslicesClusters,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {
		// 指向当前时间片的iteration
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> currIter = inputslicesClusters.entrySet()
				.iterator();
		// 指向后一个时间片的itera
		Iterator<Entry<Integer, HashMap<Integer, HashSet<Integer>>>> laterIter = inputslicesClusters.entrySet()
				.iterator();
		// 后一个指针先往后面走一步
		laterIter.next();
		while (laterIter.hasNext()) {
			Entry<Integer, HashMap<Integer, HashSet<Integer>>> currentSlice = currIter.next();
			Entry<Integer, HashMap<Integer, HashSet<Integer>>> latterSlice = laterIter.next();
			Integer currentSliceId = currentSlice.getKey();
			Integer latterSliceId = latterSlice.getKey();
			HashMap<Integer, HashSet<Integer>> currentClusters = currentSlice.getValue();
			HashMap<Integer, HashSet<Integer>> latterClusters = latterSlice.getValue();
			for (Entry<Integer, HashSet<Integer>> currentCluster : currentClusters.entrySet()) {
				Integer currentClusterId = currentCluster.getKey();
				HashSet<Integer> currentClusterSet = currentCluster.getValue();

				for (Entry<Integer, HashSet<Integer>> latterCluster : latterClusters.entrySet()) {
					Integer latterClusterId = latterCluster.getKey();
					HashSet<Integer> latterClusterSet = latterCluster.getValue();

					findGrowth(currentSliceId, currentClusterId, currentClusterSet, latterSliceId, latterClusterId,
							latterClusterSet, correlations, threshold);

					findContraction(currentSliceId, currentClusterId, currentClusterSet, latterSliceId, latterClusterId,
							latterClusterSet, correlations, threshold);

					findMerge(currentSliceId, currentClusters, latterSliceId, latterClusterId, latterClusterSet,
							correlations, threshold);

					findSplit(currentSliceId, currentClusterId, currentClusterSet, latterSliceId, latterClusters,
							correlations, threshold);

					findBirth(currentSliceId, latterSliceId, latterClusterId, correlations, threshold);

					findDeath(currentSliceId, currentClusterId, latterSliceId, correlations, threshold);
				}
			}
		}
		System.out.println("--growthEvolutionMaps" + growthEvolutionMaps.size());
		printEventTriple(growthEvolutionMaps);
		/*
		 * HashSet<Integer> unique1 = new HashSet<>(); for(EvolutionEventTriple
		 * item: growthEvolutionMaps){
		 * if(!unique1.contains(item.getBeginClusterId())){
		 * unique1.add(item.getBeginClusterId()); System.out.println(item); } }
		 */
		System.out.println("--contractionEvolutionMaps" + contractionEvolutionMaps.size());
		/*
		 * HashSet<Integer> unique2 = new HashSet<>(); for(EvolutionEventTriple
		 * item: contractionEvolutionMaps){
		 * System.out.println(item);if(!unique2.contains(item.getBeginClusterId(
		 * ))){ unique2.add(item.getBeginClusterId()); System.out.println(item);
		 * } }
		 */
		printEventTriple2(contractionEvolutionMaps);

		System.out.println("--mergeEvolutionMaps" + mergeEvolutionMaps.size());
		printEventTriple(mergeEvolutionMaps);
		/*
		 * for(EvolutionEventTriple item: mergeEvolutionMaps)
		 * System.out.println(item);
		 */
		System.out.println("--splitEvolutionMaps" + splitEvolutionMaps.size());
		/*
		 * for(EvolutionEventTriple item: splitEvolutionMaps)
		 * System.out.println(item);
		 */
		printEventTriple2(splitEvolutionMaps);
		System.out.println("--birthEvolutionMaps" + birthEvolutionMaps.size());
		/*
		 * for(EvolutionEventTriple item: birthEvolutionMaps)
		 * System.out.println(item);
		 */
		printEventTriple2(birthEvolutionMaps);
		System.out.println("--deathEvolutionMaps size " + deathEvolutionMaps.size());
		/*
		 * for(EvolutionEventTriple item: deathEvolutionMaps)
		 * System.out.println(item);
		 */
		printEventTriple(deathEvolutionMaps);

	}

	public static void printEventTriple(ArrayList<EvolutionEventTriple> input) {
		HashSet<Integer> unique1 = new HashSet<>();
		for (EvolutionEventTriple item : input) {
			if (!unique1.contains(item.getBeginClusterId())) {
				unique1.add(item.getBeginClusterId());
				System.out.println(item);
			}
		}
	}

	public static void printEventTriple2(ArrayList<EvolutionEventTriple> input) {
		HashSet<Integer> unique1 = new HashSet<>();
		for (EvolutionEventTriple item : input) {
			if (!unique1.contains(item.getEndClusterId())) {
				unique1.add(item.getEndClusterId());
				System.out.println(item);
			}
		}
	}

	public static void findGrowth(Integer currentSliceId, Integer currentClusterId, HashSet<Integer> currentClusterSet,
			Integer latterSliceId, Integer latterClusterId, HashSet<Integer> latterClusterSet,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {

		ArrayList<ClusterCorrelation> forwardCorr = correlations.get(currentSliceId).get(currentClusterId)
				.getForwardClusterSimilarity();
		for (ClusterCorrelation item : forwardCorr) {
			if (item.getSliceId() == latterSliceId && item.getClusterId() == latterClusterId
					&& item.getSimilarity() > threshold) {
				int sameNodeNum = 0;
				for (Integer nodeId1 : currentClusterSet) {
					for (Integer nodeId2 : latterClusterSet) {
						if (nodeId1.intValue() == nodeId2.intValue()) {
							sameNodeNum++;
						}
					}
				}
				// 增加的大于减少的
				if ((latterClusterSet.size() - sameNodeNum) > (currentClusterSet.size() - sameNodeNum)) {
					EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, currentClusterId,
							latterSliceId, latterClusterId, "growth");
					growthEvolutionMaps.add(evolution);
					// return true;
				}
			}
		}
		// return false;
	}

	public static void findContraction(Integer currentSliceId, Integer currentClusterId,
			HashSet<Integer> currentClusterSet, Integer latterSliceId, Integer latterClusterId,
			HashSet<Integer> latterClusterSet,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {

		ArrayList<ClusterCorrelation> backwardArr = correlations.get(latterSliceId).get(latterClusterId)
				.getBackwardClusterSimilarity();
		for (ClusterCorrelation item : backwardArr) {
			if (item.getSliceId().intValue() == currentSliceId && item.getClusterId().intValue() == currentClusterId
					&& item.getSimilarity() > threshold) {
				// System.out.println("Correlation value " +
				// item.getSimilarity());
				int sameNodeNum = 0;
				for (Integer nodeId1 : currentClusterSet) {
					for (Integer nodeId2 : latterClusterSet) {
						if (nodeId1.intValue() == nodeId2.intValue()) {
							sameNodeNum++;
						}
					}
				}
				// 增加的比较少的少
				if ((latterClusterSet.size() - sameNodeNum) < (currentClusterSet.size() - sameNodeNum)) {
					EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, currentClusterId,
							latterSliceId, latterClusterId, "contraction");
					contractionEvolutionMaps.add(evolution);
					// return true;
				}
			}
		}
		// return false;
	}

	public static void findMerge(Integer currentSliceId, HashMap<Integer, HashSet<Integer>> currentClusters,
			Integer latterSliceId, Integer latterClusterId, HashSet<Integer> latterClusterSet,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {

		// 可能合并为后一时间片上某簇的集合
		ArrayList<Integer> clusterIds = new ArrayList<>();
		HashSet<Integer> unionNodeSet = new HashSet<>();
		for (Entry<Integer, HashSet<Integer>> currentCluster : currentClusters.entrySet()) {
			ArrayList<ClusterCorrelation> forwardArr = correlations.get(currentSliceId).get(currentCluster.getKey())
					.getForwardClusterSimilarity();
			for (ClusterCorrelation item : forwardArr) {
				if (item.getSliceId().intValue() == latterSliceId && item.getClusterId().intValue() == latterClusterId
						&& item.getSimilarity() > threshold) {
					clusterIds.add(currentCluster.getKey());
					unionNodeSet.addAll(currentCluster.getValue());
				}
			}
		}
		System.out.println("currSlice " + currentSliceId + " latterslice " + latterSliceId + "merge size" + clusterIds.size());
		// 有超过一个的簇合并成后一时间片的某簇
		if (clusterIds.size() > 1) {
			int sameNodesNum = 0;
			for (Integer node1 : latterClusterSet) {
				for (Integer node2 : unionNodeSet) {
					if (node1.intValue() == node2.intValue()) {
						sameNodesNum++;
					}
				}
			}
			System.out.println("currSlice " + currentSliceId + " latterslice " + latterSliceId + 
					"****Merge sim forward " + sameNodesNum / (latterClusterSet.size() * 1.0) + " backward " +
					sameNodesNum / (unionNodeSet.size() * 1.0));
			if (sameNodesNum / (latterClusterSet.size() * 1.0) > threshold
					&& sameNodesNum / (unionNodeSet.size() * 1.0) > threshold) {
				for (Integer id : clusterIds) {
					EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, id, latterSliceId,
							latterClusterId, "merge");
					mergeEvolutionMaps.add(evolution);

				}
				// return true;

			}
		} else {
			// return false;
		}

		// return false;
	}

	public static void findSplit(Integer currentSliceId, Integer currentClusterId, HashSet<Integer> currentClusterSet,
			Integer latterSliceId, HashMap<Integer, HashSet<Integer>> latterClusters,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {
		// 后一时间片几个簇由前一时间片上某簇分裂而成
		ArrayList<Integer> clusterIds = new ArrayList<>();
		HashSet<Integer> unionNodeSet = new HashSet<>();

		for (Entry<Integer, HashSet<Integer>> latterCluster : latterClusters.entrySet()) {
			ArrayList<ClusterCorrelation> backwardArr = correlations.get(latterSliceId).get(latterCluster.getKey())
					.getBackwardClusterSimilarity();
			for (ClusterCorrelation item : backwardArr) {
				if (item.getSliceId().intValue() == currentSliceId && item.getClusterId().intValue() == currentClusterId
						&& item.getSimilarity() > threshold) {
					clusterIds.add(latterCluster.getKey());
					unionNodeSet.addAll(latterCluster.getValue());
				}
			}
		}
		// 有超过一个的簇与前一时间片的某簇相对应
		if (clusterIds.size() > 1) {
			int sameNodesNum = 0;
			for (Integer node1 : currentClusterSet) {
				for (Integer node2 : unionNodeSet) {
					if (node1.intValue() == node2.intValue()) {
						sameNodesNum++;
					}
				}
			}
		
			if (sameNodesNum / (currentClusterSet.size() * 1.0) > threshold
					&& sameNodesNum / (unionNodeSet.size() * 1.0) > threshold) {
				for (Integer id : clusterIds) {
					EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, currentClusterId,
							latterSliceId, id, "split");
					splitEvolutionMaps.add(evolution);

				}
				// return true;

			}
		} else {
			// return false;
		}

		// return false;
	}

	public static void findBirth(Integer currentSliceId, Integer latterSliceId, Integer latterClusterId,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {
		ArrayList<ClusterCorrelation> backwardArr = correlations.get(latterSliceId).get(latterClusterId)
				.getBackwardClusterSimilarity();
		boolean flag1 = false;
		// 检查前一时间片上的每个簇与自己的Corr
		for (ClusterCorrelation item : backwardArr) {
			if (item.getSimilarity() > threshold)
				flag1 = true;
		}
		// 前一时间片没有与它相似度高的簇
		if (flag1 == false) {
			boolean flag2 = false;
			// 且前一时间片无簇合并成它
			for (EvolutionEventTriple item : mergeEvolutionMaps) {
				if (item.getBeginSliceId().intValue() == currentSliceId
						&& item.getEndSliceId().intValue() == latterSliceId
						&& item.getEndClusterId() == latterClusterId) {
					flag2 = true;
				}
			}
			if (flag2 == false) {
				EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, -1, latterSliceId,
						latterClusterId, "birth");
				birthEvolutionMaps.add(evolution);
				// return true;
			}
		}
		// return false;
	}

	public static void findDeath(Integer currentSliceId, Integer currentClusterId, Integer latterSliceId,
			LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations, double threshold) {
		ArrayList<ClusterCorrelation> forwardArr = correlations.get(currentSliceId).get(currentClusterId)
				.getForwardClusterSimilarity();
		boolean flag1 = false;
		for (ClusterCorrelation item : forwardArr) {
			if (item.getSimilarity() > threshold) {
				flag1 = true;
			}
		}
		if (flag1 == false) {
			boolean flag2 = false;
			for (EvolutionEventTriple item : splitEvolutionMaps) {
				if (item.getBeginSliceId().intValue() == currentSliceId
						&& item.getBeginClusterId().intValue() == currentClusterId
						&& item.getEndSliceId() == latterSliceId) {
					flag2 = true;
				}

			}
			if (flag2 == false) {
				EvolutionEventTriple evolution = new EvolutionEventTriple(currentSliceId, currentClusterId,
						latterSliceId, -1, "death");
				deathEvolutionMaps.add(evolution);
			}
		}
	}

	public static LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> removeVacantSlice(
			LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> input) {
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new LinkedHashMap<>();
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> slice : input.entrySet()) {
			if (slice.getValue() != null) {
				result.put(slice.getKey(), slice.getValue());
			}
		}
		return result;
	}

	public static void main(String[] args) throws IOException, ParseException {
		long t1 = System.currentTimeMillis();
		/*String normalizedEmbeddingFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";

		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuildTest// 129827
				.initSliceDataBuild(timeEntitySlices, rescalInputDir)
				.getSliceLinkedNodes(rescalInputDir, 129827, 161771);*/

		
		  String normalizedEmbeddingFilePath ="C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		  String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ResourcePTMap0722.txt";
		  String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt";
		  String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
		  //608035 Book 10367 Article in proced Article 45075 Article
		  LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation .initDataSegment(path, path2, rescalInputDir)
		                                                                                    .segmentDataSet(10367,
		  "http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date");
		  LinkedHashMap<Integer, HashSet<Integer>> sliceNodes =
		                                          SliceDataBuild .initSliceDataBuild(timeEntitySlices,rescalInputDir).
		                                          getSliceLinkedNodes(rescalInputDir, 1, 10367);
		

		
		  FileWriter fileWriter2 = new
		  FileWriter("E:\\SliceNode\\EverySliceNodes.txt");
		  BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2); 
		  for(Entry<Integer,HashSet<Integer>> item : sliceNodes.entrySet()){
		  
		  bufferedWriter2.write("Slice id " + item.getKey() + " Slice size " +
		  item.getValue().size());
		  bufferedWriter2.newLine();
		  for(Integer node: item.getValue()){ 
			  bufferedWriter2.write(node + " "); 
		  }
		  bufferedWriter2.newLine(); } 
		  bufferedWriter2.close();
		
		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);

		/** 每个时间片上的点进行聚类操作 **/
		ArrayList<Double[]> entityVectors = RescalDistanceForCluster.getNodeVector(normalizedEmbeddingFilePath);
		ClusterImplementation.entityVectors = entityVectors;
		HashMap<Integer, String> classTypeId = Dataset.getDataSetClass(rescalInputDir, "");
		System.out.println("Begin clustering method ");
		// 时间片以及每个时间片上的簇的点
		LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusterNodes = ClusterImplementation
				.getSliceClusterMap(sliceNodes, entityVectors, classTypeId, resourceOutgoingNeighborMap,
						resourceIncomingNeighborMap);

		// 将没点的时间片排除
		sliceClusterNodes = removeVacantSlice(sliceClusterNodes);
		LinkedHashMap<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> correlations = Correlation
				.getClusterCorrelationAmongSlices(sliceClusterNodes);
		FileWriter fileWriter = new FileWriter("E:\\SliceNode\\EveryClusterNodes.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> item : sliceClusterNodes.entrySet()) {
			bufferedWriter.write("Slice id is " + item.getKey() + " " + " slice size is " + item.getValue().size());
			bufferedWriter.newLine();
			for (Entry<Integer, HashSet<Integer>> item2 : item.getValue().entrySet()) {
				bufferedWriter.write("Cluster id is " + item2.getKey());
				bufferedWriter.newLine();
				for (Integer node : item2.getValue()) {
					bufferedWriter.write(node + " ");
				}
				bufferedWriter.write("***********************************");
				bufferedWriter.newLine();
			}
			bufferedWriter.write("***********************************");
			bufferedWriter.newLine();
		}
		bufferedWriter.close();
	/*	for (Entry<Integer, HashMap<Integer, ClusterForwardBackwardCorr>> item : correlations.entrySet()) {
			System.out.println("****Slice is " + item.getKey());
			for (Entry<Integer, ClusterForwardBackwardCorr> item2 : item.getValue().entrySet()) {
				System.out.println("****Cluster is " + item2.getKey());
				System.out.println(item2.getValue().toString());
			}
		}*/
	
		identifyEvolutionEvents(sliceClusterNodes, correlations, 0.3);
		System.out.println("**********Time entity size " + timeEntitySlices.size() + " sliceNodes size "
				+ sliceNodes.size() + " sliceNodesClusterSize " + timeEntitySlices.size());

		long t2 = System.currentTimeMillis();
		System.out.println("Time cost " + (t2 - t1) / 1000.0);
	}
}
