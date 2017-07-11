package com.seu.ldea.segment;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.seu.ldea.cluster.ClusterUtil;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.entity.TimeSpan;
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
	public static HashMap<Integer, HashSet<Integer>> slices ;
	public static HashMap<Integer, Integer> resourceTypeMap;
	//资源以及资源时间信息的映射
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo;
	//资源的邻居与资源之间的映射
	public static HashMap<Integer, Set<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, Set<Integer>> resourceIncomingNeighborMap;
	
	//二维矩阵表示图中的链接关系
	public static int[][] connectionMatrix;

	/**
	 * 找到与该时间实体连接的点，可以自选点的类型
	 * 
	 * @param timeResource
	 * @param type
	 * @return 返回每个slice以及上面的点
	 * @throws IOException
	 */
	public static HashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int typeId, int classId) throws IOException {
		// 时间序列编号以及上面所有的点的编号
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);

		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			HashSet<Integer> nodes = new HashSet<>();
			int sliceNum = slice.getKey();
			for (Integer targetNodeId : slice.getValue()) {
				//找寻以切分类型为顶点的二步邻居
				HashSet<Integer> connectedNodes = findConnectedNodes(targetNodeId, typeId, classId, 2);
				if(connectedNodes != null)
				nodes.addAll(connectedNodes);
			}
			result.put(sliceNum, nodes);
		}
	
		//打印输出
		for(Entry<Integer, HashSet<Integer>> entry : result.entrySet()){
			HashSet<Integer> nodes = entry.getValue();
			System.out.println("Slice num " + entry.getKey() + " size is " + nodes.size());
			for(Integer node : nodes){
				
				System.out.println(node.toString() + " ");
			}
			System.out.println("\n");
		}
		return result;
	}

	/**
	 * 查询与此特定点直接相连的邻居点的集合
	 * 
	 * @param targetNodeId
	 * @param typeId
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes(int targetNodeId, int typeId) {
		HashSet<Integer> result = new HashSet<>();

		// targetNode作宾语与之相连的点
		for (int i = 0; i < connectionMatrix.length; i++) {
			if (connectionMatrix[i][targetNodeId] == 1) {
				// 判断i的类型，看是否放入result中,-1表示抽取特定类型相连接的点
				if (typeId != -1) {
					if (resourceTypeMap.containsKey(i) && resourceTypeMap.get(i).intValue() == typeId) {
						// 还要添加查看此node是否具有时间信息，且时间信息在所选定的范围内
						result.add(i);
					}
				} else {
					// 还要添加查看此node是否具有时间信息，且时间信息在所选定的范围内
					result.add(i);
				}
			}
		}

		// targetNode作主语与之相连的点
		for (int j = 0; j < connectionMatrix[targetNodeId].length; j++) {
			if (connectionMatrix[targetNodeId][j] == 1) {
				// 判断j的类型，看是否放入result中,-1表示抽取特定类型相连接的点
				if (typeId != -1) {
					if (resourceTypeMap.containsKey(j) && resourceTypeMap.get(j).intValue() == typeId) {
						// 还要添加查看此node是否具有时间信息，且时间信息在所选定的范围内
						result.add(j);
					}
				} else {
					// 还要添加查看此node是否具有时间信息，且时间信息在所选定的范围内
					result.add(j);
				}
			}
		}

		return result;
	}



	public static HashSet<Integer> findConnectedNodes(int targetNodeId, int typeId,int classId, int recursionRound) {
		if(recursionRound == 0)
			return null;
		
		recursionRound--;
		HashSet<Integer> result = new HashSet<>();
        
		// targetNode作主语与之相连的点
		if(resourceOutgoingNeighborMap.containsKey(targetNodeId)){
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for(Integer neighbor : neighborSet){
				//如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居 
				if(typeId == classId){
					result.add(targetNodeId);
					//把与之相连接但不是切分类型的点加进来
					if(resourceTypeMap.containsKey(neighbor)){
						if(resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					}else{
					//将无类型的邻接点加进来
					result.add(neighbor);
					}
				}else{
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
				    if(typeId != -1) {
						//如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor) && resourceTypeMap.get(neighbor).intValue() == typeId){
						    result.add(neighbor);
							//找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if(neighbors != null) 
							   result.addAll(neighbors);
					}
				} else {
				  if(resourceTypeMap.containsKey(neighbor)){
					if(resourceTypeMap.get(neighbor).intValue() != classId){
					  result.add(neighbor);
					  HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
					  if(neighbors != null)
					  result.addAll(neighbors);
				    }
				  }else {
					//无类型且相邻接的点
					result.add(neighbor);
					HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
				    if(neighbors != null)
					result.addAll(neighbors);
				}
				}
			}
		 }
		}
		
		// targetNode作宾语与之相连的点
		System.out.println("resourceIncomingneighborMap is empty " + resourceIncomingNeighborMap.isEmpty());
		if(resourceIncomingNeighborMap.containsKey(targetNodeId)){
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceIncomingNeighborMap.get(targetNodeId);
			for(Integer neighbor : neighborSet){
				//如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居 
				if(typeId == classId){
					result.add(targetNodeId);
					//把与之相连接但不是切分类型的点加进来
					if(resourceTypeMap.containsKey(neighbor)){
						if(resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					}else{
					//将无类型的邻接点加进来
					result.add(neighbor);
					}
				}else{
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
				    if(typeId != -1) {
						//如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor) && resourceTypeMap.get(neighbor).intValue() == typeId){
						    result.add(neighbor);
							//找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
							if(neighbors != null)
							result.addAll(neighbors);
					}
				} else {
				  if(resourceTypeMap.containsKey(neighbor)){
					if(resourceTypeMap.get(neighbor).intValue() != classId){
					  result.add(neighbor);
					  HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
					  if(neighbors != null)
					  result.addAll(neighbors);
				    }
				  }else {
					//无类型且相邻接的点
					result.add(neighbor);
					HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
					if(neighbors != null)
					result.addAll(neighbors);
				}
				}
			}
		 }
		}

		return result;
	}

	
	
	public static void main(String[] args) throws IOException, ParseException{
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";

		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(
				"C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2", " ");
		DatasetSegmentationTest.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"");
		DatasetSegmentationTest.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
	    //依据类型切分数据集，以及每个数据集上面的时间
		slices = DatasetSegmentationTest.segmentDataSet(11, "http://swrc.ontoware.org/ontology#year");
	    //获取每个资源的类型
	    resourceTypeMap = ResourceInfo.getReourceTypeMap("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    resourceIncomingNeighborMap = (HashMap<Integer, Set<Integer>>) ClusterUtil.getNodeIncomingNeighbors("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    resourceOutgoingNeighborMap = (HashMap<Integer, Set<Integer>>) ClusterUtil.getNodeOutgoingNeighbors("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    //resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
	   getSliceLinkedNodes("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2", -1, 11);
	}
	
	/**
	 * 利用二维矩阵查看顶点之间的链接关系
	 * @param targetNodeId
	 * @param typeId 研究的实体类型
	 * @param classId 切分数据集依据类型
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes3(int targetNodeId, int typeId,int classId, int recursionRound) {
		if(recursionRound == 0){
			return null;
		}
		recursionRound--;
		HashSet<Integer> result = new HashSet<>();
     
		// targetNode作宾语与之相连的点
		for (int i = 0; i < connectionMatrix.length; i++) {
			if (connectionMatrix[i][targetNodeId] == 1) {
				//如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居 
				if(typeId == classId){
					result.add(targetNodeId);
					//把与之相连接但不是切分类型的点加进来
					if(resourceTypeMap.containsKey(i)){
						if(resourceTypeMap.get(i).intValue() != classId)
							result.add(i);
					}else{
					//将无类型的邻接点加进来
					result.add(i);
					}
				}else{
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
				    if(typeId != -1) {
						//如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(i) && resourceTypeMap.get(i).intValue() == typeId){
						    result.add(i);
							//找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(i, typeId, classId, recursionRound);
							result.addAll(neighbors);
					}
				} else {
				  if(resourceTypeMap.containsKey(i)){
					if(resourceTypeMap.get(i).intValue() != classId){
					  result.add(i);
					  HashSet<Integer> neighbors = findConnectedNodes(i, typeId, classId, recursionRound);
					  result.addAll(neighbors);
				    }
				  }else {
					//无类型且相邻接的点
					result.add(i);
					HashSet<Integer> neighbors = findConnectedNodes(i, typeId, classId, recursionRound);
					result.addAll(neighbors);
				}
				}
			}
		  }
		}

		// targetNode作主语与之相连的点
		for (int j = 0; j < connectionMatrix[targetNodeId].length; j++) {
			if (connectionMatrix[targetNodeId][j] == 1) {
				//如果每个时间片上选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居 
				if(typeId == classId){
					result.add(targetNodeId);
					//把与之相连接但不是切分类型的点加进来
					if(resourceTypeMap.containsKey(j)){
						if(resourceTypeMap.get(j).intValue() != classId)
							result.add(j);
					}else{
					//将无类型的邻接点加进来
					result.add(j);
					}
				}else{
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
				    if(typeId != -1) {
						//如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(j) && resourceTypeMap.get(j).intValue() == typeId){
						    result.add(j);
							//找当前点的一步邻居
							HashSet<Integer> neighbors = findConnectedNodes(j, typeId, classId, recursionRound);
							result.addAll(neighbors);
					}
				} else {
				  if(resourceTypeMap.containsKey(j)){
					if(resourceTypeMap.get(j).intValue() != classId){
					  result.add(j);
					  HashSet<Integer> neighbors = findConnectedNodes(j, typeId, classId, recursionRound);
					  result.addAll(neighbors);
				    }
				  }else {
					//无类型且相邻接的点
					result.add(j);
					HashSet<Integer> neighbors = findConnectedNodes(j, typeId, classId, recursionRound);
					result.addAll(neighbors);
				}
				}
			}
		 }
		}
		return result;
	}
	
}
