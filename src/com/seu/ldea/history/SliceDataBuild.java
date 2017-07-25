package com.seu.ldea.history;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
import com.seu.ldea.segment.DatasetSegmentation;
import com.seu.ldea.time.InteractiveMatrix;
import com.seu.ldea.time.LabelClassWithTime;
import com.seu.ldea.util.BuildFromFile;

/**
 * 依据所选类别和时间属性切分链接数据构成时间片后， 构建每个时间片上的点 思路：以在此时间片上所选类别的顶点，找到其一部邻居，依据一步邻居找二步邻居
 * 
 * @author Lynn
 *
 */
public class SliceDataBuild {
	public static LinkedHashMap<Integer, HashSet<Integer>> slices;
	public static HashMap<Integer, Integer> resourceTypeMap;
	// 资源以及资源时间信息的映射
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo;
	// 资源的邻居与资源之间的映射
	public static HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;

	// 二维矩阵表示图中的链接关系
	public static int[][] connectionMatrix;

	/**
	 * 找到与该时间实体连接的点，可以自选点的类型
	 * 
	 * @param timeResource
	 * @param type,需要找哪一类型的点，-1表示全部类型
	 * @return 返回每个slice以及上面的点
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int typeId, int classId)
			throws IOException {
		// 时间序列编号以及上面所有的点的编号
		LinkedHashMap<Integer, HashSet<Integer>> result = new LinkedHashMap<>();
	    //从文件构建resourceTypeMap
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);
        HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			HashSet<Integer> nodes = new HashSet<>();
			int sliceNum = slice.getKey();
			for (Integer targetNodeId : slice.getValue()) {
				// 找寻以切分类型为顶点的二步邻居
				HashSet<Integer> connectedNodes = findConnectedNodes(targetNodeId, typeId, classId,2);
				if (connectedNodes != null)
					nodes.addAll(connectedNodes);
			}
			result.put(sliceNum, nodes);
		}

		// 打印输出
		FileWriter fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-SliceNodes2");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		String studiedClassType = "";
		if(resourceURI.containsKey(typeId)){
			studiedClassType = resourceURI.get(typeId);
		}else{
			studiedClassType = "all type";
		}
		bufferedWriter.write("SegmentTypeId is " + classId + ": " + resourceURI.get(classId) + "  ");
		bufferedWriter.write("studiedTypeId is " + typeId + ": " + studiedClassType);
		bufferedWriter.newLine();
		for (Entry<Integer, HashSet<Integer>> entry : result.entrySet()) {
			HashSet<Integer> nodes = entry.getValue();
			bufferedWriter.write("Slice num " +  entry.getKey() + " size is " + nodes.size());
			bufferedWriter.newLine();
		//	System.out.println("Slice num " + entry.getKey() + " size is " + nodes.size());
			for (Integer node : nodes) {
			//	System.out.print(node.toString() + " ");
				bufferedWriter.write(node + ": " + resourceURI.get(node));
                bufferedWriter.newLine();
			}
			bufferedWriter.write("----------------------------------------------------");
			bufferedWriter.newLine();
		//	System.out.println("\n");
		}
		bufferedWriter.close();
		return result;
	}

	/**
	 * 
	 * @param targetNodeId
	 * @param typeId
	 * @param classId
	 * @param recursionRound 迭代次数
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes(int targetNodeId, int typeId, int classId, int recursionRound) {
		if (recursionRound == 0)
			return null;

		recursionRound--;
		HashSet<Integer> result = new HashSet<>();

		// targetNode作主语与之相连的点
		if (resourceOutgoingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// 如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居
				if (typeId == classId) {
					result.add(targetNodeId);
					// 把与之相连接但不是切分类型的点加进来
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					} else {
						// 将无类型的邻接点加进来
						result.add(neighbor);
					}
				} else {
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
					if (typeId != -1) {
						// 如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == typeId) {
							result.add(neighbor);
							// 找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					} else {
						if (resourceTypeMap.containsKey(neighbor)) {
							if (resourceTypeMap.get(neighbor).intValue() != classId) {
								result.add(neighbor);
								HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId,
										recursionRound);
								if (neighbors != null)
									result.addAll(neighbors);
							}
						} else {
							// 无类型且相邻接的点
							result.add(neighbor);
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					}
				}
			}
		}

	
		if (resourceIncomingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceIncomingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// 如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居
				if (typeId == classId) {
					result.add(targetNodeId);
					// 把与之相连接但不是切分类型的点加进来
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					} else {
						// 将无类型的邻接点加进来
						result.add(neighbor);
					}
				} else {
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
					if (typeId != -1) {
						// 如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == typeId) {
							result.add(neighbor);
							// 找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					} else {
						if (resourceTypeMap.containsKey(neighbor)) {
							if (resourceTypeMap.get(neighbor).intValue() != classId) {
								result.add(neighbor);
								HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId,
										recursionRound);
								if (neighbors != null)
									result.addAll(neighbors);
							}
						} else {
							// 无类型且相邻接的点
							result.add(neighbor);
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					}
				}
			}
		}

		return result;
	}
	
	
	/**
	 * 
	 * @param slicesNodes
	 * @param nodesGlobalNeighbor
	 * @return构建每个切片上每个点的邻居映射
	 */
	public static HashMap<Integer, HashSet<Integer>> getSliceNodesNeighor(HashSet<Integer> slicesNodes, HashMap<Integer, HashSet<Integer>> nodesGlobalNeighbor){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		for(Entry<Integer, HashSet<Integer>> nodeGlobalNeighbor : nodesGlobalNeighbor.entrySet()){
			int nodeId = nodeGlobalNeighbor.getKey();
			if(slicesNodes.contains(nodeId)){
				//收集当前在切片上的点的邻居
				HashSet<Integer> nodeSliceNeighbors = new HashSet<>();
				for(int neighborId : nodeGlobalNeighbor.getValue()){
					if(slicesNodes.contains(neighborId)){
						nodeSliceNeighbors.add(neighborId);
					}
				}
				if(!nodeSliceNeighbors.isEmpty()){
					result.put(nodeId, nodeSliceNeighbors);
				//	System.out.println("nodeId " + nodeId + "local neigbor size " + nodeSliceNeighbors.size());
				}
			}
		}
		
		return result;
	}
	

	public static void main(String[] args) throws IOException, ParseException {
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";

		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		//此时的LabelClassWithTime的resourceTimeInfo已经是有类型的
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"");
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		// 依据类型切分数据集，以及每个数据集上面的时间
		slices = DatasetSegmentation.segmentDataSet(37, "http://swrc.ontoware.org/ontology#year");
		// 获取每个资源的类型
		resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);
		resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);
		// resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		getSliceLinkedNodes(rescalInputDir, 6539, 37);
	}


}
