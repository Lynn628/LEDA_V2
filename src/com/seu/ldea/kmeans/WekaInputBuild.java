package com.seu.ldea.kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.connectedness.KmeansConnectedness;
import com.seu.ldea.history.SliceDataBuildOldVersion2;
import com.seu.ldea.segment.DatasetSegmentation;

public class WekaInputBuild {
	public static void main(String[] args) throws Exception {
		long t1 = System.currentTimeMillis();
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";

		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
					.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
					.getNodeOutgoingNeighbors(rescalInputDir);
		
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
        //129827
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuildOldVersion2
				.initSliceDataBuild(timeEntitySlices, rescalInputDir)
				.getSliceLinkedNodes(rescalInputDir, 129827, 161771);
	/*	String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ResourcePTMap0722.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";*/
	
	
	
		/*LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation2
				.initDataSegment(path, path2, rescalInputDir)
				.segmentDataSet(10367, "http://lsdis.cs.uga.edu/projects/semdis/opus#last_modified_date");*/


		

		int i = 0;
		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceKMeansClusterNodes = new HashMap<>();
		for (Entry<Integer, HashSet<Integer>> slice : sliceNodes.entrySet()) {
			long clusBegin = 0 , clusEnd = 0;
			// 簇不为空
			if (slice.getValue() != null) {
				i++;
				System.out.println("BeginSlice " + slice.getValue().size());
				if(slice.getValue().size() <=15000){
					clusBegin = System.currentTimeMillis();
					HashMap<Integer, HashSet<Integer>> sliceClusterResult = KMeansCluster(slice.getValue(), resourceIncomingNeighborMap, resourceOutgoingNeighborMap, slice.getKey().toString());	
					//KMeansCluster(slice.getValue(), resourceIncomingNeighborMap, resourceOutgoingNeighborMap, slice.getKey().toString());	
					clusEnd = System.currentTimeMillis();
					sliceKMeansClusterNodes.put(slice.getKey(), sliceClusterResult);
					KmeansConnectedness.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
					KmeansConnectedness.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
					KmeansConnectedness.getConnectedness(sliceNodes, sliceKMeansClusterNodes);
					long t2 = System.currentTimeMillis();
					System.out.println((t2 - t1) / 1000);
					System.out.println("-----------KMeans Cluster cost----" + (clusEnd - clusBegin)/1000.0 + " s");
			}else{
				sliceKMeansClusterNodes.put(slice.getKey(), null);
			}
			System.out.println(i);
		}
	}
	}
		public static HashMap<Integer, HashSet<Integer>> KMeansCluster(HashSet<Integer> sliceDataSet,
				HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap,
				HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap, 
				String fileName) throws Exception {
			int[] sliceDataArr = new int[sliceDataSet.size()];
			Object[] toArrSet = sliceDataSet.toArray();
			for (int i = 0; i < sliceDataArr.length; i++) {
				sliceDataArr[i] = (int) toArrSet[i];
			}
			double[][] points = KMeansInput.KmeansInputBuild(sliceDataArr, resourceIncomingNeighborMap, resourceOutgoingNeighborMap);
			String filePath = "D:\\" + fileName + "-kmeansInput.txt";
			FileWriter fileWriter = new FileWriter(filePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		    bufferedWriter.write("@RELATION " + fileName + "-kmeansInput");
		    bufferedWriter.newLine();
			//声明属性
		    for(int i = 0; i < points.length; i++){
			bufferedWriter.write("@ATTRIBUTE " + i + " REAL");
			bufferedWriter.newLine();
			}
		    bufferedWriter.write("@DATA");
		    bufferedWriter.newLine();
			for(int i = 0; i < points.length; i++){
				for(int j = 0; j < points.length; j++){
					bufferedWriter.write(points[i][j] + " ");
				}
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		//	String filePath = "D:\\0-kmeansInput.txt";
		  int[] labels =  WekaKMeans.wekaKMeans(filePath);
		   return allocateNodeToCluster(labels, sliceDataArr);
		}

		
		/**
		 * 
		 * @param memberShipLabel,K-means
		 *            计算好的输出结果，数组每个位置上的点，以及该点的簇标签
		 * @param sliceDataArr,原始点id和新id，数组每个id存原始点的id
		 * @return 簇和簇中的点
		 */
		public static HashMap<Integer, HashSet<Integer>> allocateNodeToCluster(int[] memberShipLabel, int[] sliceDataArr) {

			HashMap<Integer, HashSet<Integer>> cluster = new HashMap<>();
			for(int i = 0; i < memberShipLabel.length; i++){
	        	if(!cluster.containsKey(memberShipLabel[i])){
	        		HashSet<Integer> nodes = new HashSet<>();
	        		int nodeId = sliceDataArr[i];
	        		nodes.add(nodeId);
	        		cluster.put(memberShipLabel[i], nodes);
	        	}else{
	        		int nodeId = sliceDataArr[i];
	        		cluster.get(memberShipLabel[i]).add(nodeId);
	        	}
	        }
			return cluster;
		}
		
}
