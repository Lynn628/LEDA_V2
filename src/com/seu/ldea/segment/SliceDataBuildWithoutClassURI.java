package com.seu.ldea.segment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.seu.ldea.cluster.GraphUtil;
import com.seu.ldea.entity.Dataset;
import com.seu.ldea.entity.ResourceInfo;
import com.seu.ldea.history.SliceDataBuildOldVersion2;

/**
 * 在Slice data build2 的基础上，创建时间片的时候不把class uri加进来 
 * @author Lynn
 *
 */
public class SliceDataBuildWithoutClassURI {
	public LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices;
	public HashMap<Integer, Integer> resourceTypeMap;
	
	// 资源的邻居与资源之间的映射
	public HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;

	
	public SliceDataBuildWithoutClassURI(){
		
	}

	private SliceDataBuildWithoutClassURI(LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices,
			HashMap<Integer, Integer> resourceTypeMap,
			HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap,
			HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap) {
		super();
		this.timeEntitySlices = timeEntitySlices;
		this.resourceTypeMap = resourceTypeMap;
		this.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
		this.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
	}
     
	
	
	public void setResourceIncomingNeighborMap(HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap) {
		this.resourceIncomingNeighborMap = resourceIncomingNeighborMap;
	}

	public void setResourceOutgoingNeighborMap(HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap) {
		this.resourceOutgoingNeighborMap = resourceOutgoingNeighborMap;
	}
	
	

	public void setResourceTypeMap(HashMap<Integer, Integer> resourceTypeMap) {
		this.resourceTypeMap = resourceTypeMap;
	}
	
	public void setTimeEntitySlices(LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices) {
		this.timeEntitySlices = timeEntitySlices;
	}


	public static SliceDataBuildWithoutClassURI initSliceDataBuild(LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices, String rescalInputDir) throws IOException{
		HashMap<Integer, Integer>  resourceTypeMap =  ResourceInfo.getReourceTypeMap(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);
		SliceDataBuildWithoutClassURI sliceDataBuild = new SliceDataBuildWithoutClassURI(timeEntitySlices, resourceTypeMap, resourceOutgoingNeighborMap, resourceIncomingNeighborMap); 
		return sliceDataBuild;

	}
	
	/**
	 * 
	 * @param dir， rescal输入文件地址
	 * @param typeId， 需要研究的类型的id，需要找哪一类型的点，-1表示全部类型
	 * @param classId， 是依据哪个类对时间片进行切分的
	 * @return 时间片以及每个时间片上的点的集合
	 * @throws IOException
	 */
	public LinkedHashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int studiedClassId, int segmentClassId)
			throws IOException {
		// 时间片编号以及上面所有的点的集合
		LinkedHashMap<Integer, HashSet<Integer>> result = new LinkedHashMap<>();
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);
        HashMap<Integer, String> datasetclassURI = Dataset.getDataSetClass(dir, "JamendoClassURI");
		
		HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
		for (Entry<Integer, HashSet<Integer>> slice : timeEntitySlices.entrySet()) {
			//每个时间片上的点进行处理
			HashSet<Integer> nodes = new HashSet<>();
			//先将每个时间片上的时间实体加进来
			nodes.addAll(slice.getValue());
			int sliceId = slice.getKey();	
			//找具体时间实体的某一类型的邻居
			for (Integer timeEntityId : slice.getValue()) {
				// 找寻以切分类型为顶点的二步邻居
				HashSet<Integer> connectedNodes = findConnectedNodes(timeEntityId, studiedClassId, segmentClassId,2, datasetclassURI);
				if (connectedNodes != null)
					//找到每个点的邻居节点集合，加到nodes集合中
					nodes.addAll(connectedNodes);
			}
			result.put(sliceId, nodes);
		}

		// 打印输出
		FileWriter fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\DBLP-SliceNodes");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		String studiedClassType = "";
		if(resourceURI.containsKey(studiedClassId)){
			studiedClassType = resourceURI.get(studiedClassId);
		}else{
			studiedClassType = "all type";
		}
		bufferedWriter.write("SegmentTypeId is " + segmentClassId + ": " + resourceURI.get(segmentClassId) + "  ");
		bufferedWriter.write("studiedTypeId is " + studiedClassId + ": " + studiedClassType);
		bufferedWriter.newLine();
		for (Entry<Integer, HashSet<Integer>> entry : result.entrySet()) {
			HashSet<Integer> nodes = entry.getValue();
			bufferedWriter.write("Slice num " +  entry.getKey() + " size is " + nodes.size());
			bufferedWriter.newLine();
		//	System.out.println("Slice num " + entry.getKey() + " size is " + nodes.size());
			for (Integer node : nodes) {
				System.out.print(node.toString() + " ");
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
	 * 找到链接数据切分类型点的邻居类型
	 * @return
	 */
    public static HashSet<Integer> getSegmentedClassRelatedClass(Integer segementId){
    	return null;
    	
    }

	/**
	 * 
	 * @param targetNodeId
	 * @param studiedClassId
	 * @param segementClassId
	 * @param recursionRound 迭代次数
	 * @return
	 */
	public HashSet<Integer> findConnectedNodes(int targetNodeId, int studiedClassId, int segementClassId, int recursionRound, HashMap<Integer, String> classURI) {
		if (recursionRound == 0)
			return null;
		
		recursionRound--;
		HashSet<Integer> result = new HashSet<>();

		// targetNode作主语与之相连的点
		if (resourceOutgoingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// 如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居
				if (studiedClassId == segementClassId) {
					result.add(targetNodeId);
					// 把与之相连接但不是切分类型的点加进来
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor))
							result.add(neighbor);
					} else {
						// 将无类型的邻接点加进来
						if(!classURI.containsKey(neighbor))
						result.add(neighbor);
					}
				} else {
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
					if (studiedClassId != -1) {
						// 如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == studiedClassId && !classURI.containsKey(neighbor)) {
							result.add(neighbor);
							// 依据要研究的点找一步邻居，一步邻居不考虑顶点类型,但不包含切分类型的点
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId, recursionRound,classURI);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					} else {
						//不限定相连接点的类型，但不能是切分类型
						if (resourceTypeMap.containsKey(neighbor)) {
							if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor)) {
								result.add(neighbor);
								HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId,
										recursionRound, classURI);
								if (neighbors != null)
									result.addAll(neighbors);
							}
						} else {
							// 无类型且相邻接的点
							if(!classURI.containsKey(neighbor))
							   result.add(neighbor);
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId, recursionRound,classURI);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					}
				}
			}
		}

	    //目标时间实体做宾语时
		if (resourceIncomingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceIncomingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// 如果选取的研究类型和切分时间片的类型相同，时间片上的点为一步邻居
				if (studiedClassId == segementClassId) {
					result.add(targetNodeId);
					// 把与之相连接但不是切分类型的点加进来
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor))
							result.add(neighbor);
					} else {
						// 将无类型的邻接点加进来
						if(!classURI.containsKey(neighbor))
						result.add(neighbor);
					}
				} else {
					// 判断i的类型，看是否放入result中,-1表示不限定相连接的点的类型
					if (studiedClassId != -1) {
						// 如果此实体有类型且是要研究的
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == studiedClassId) {
							result.add(neighbor);
							// 依据要研究的点找一步邻居，一步邻居不考虑顶点类型
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId, recursionRound, classURI);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					} else {
						if (resourceTypeMap.containsKey(neighbor)) {
							if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor)) {
								result.add(neighbor);
								HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId,
										recursionRound, classURI);
								if (neighbors != null)
									result.addAll(neighbors);
							}
						} else {
							// 无类型且相邻接的点
							if(!classURI.containsKey(neighbor))
							result.add(neighbor);
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId, recursionRound, classURI);
							
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
	/*	String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";

		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"");
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		SliceDataBuild2 sliceBuild = new SliceDataBuild2();
		// 依据类型切分数据集，以及每个数据集上面的时间实体的集合
		sliceBuild.setTimeEntitySlices(DatasetSegmentation.segmentDataSet(37, "http://swrc.ontoware.org/ontology#year"));;
		System.out.println("time entity slice size " + timeEntitySlices.size());
		for(Entry<Integer, HashSet<Integer>> aEntry : timeEntitySlices.entrySet()){
			System.out.println("Slice num " + aEntry.getKey());
			for(Integer aInteger : aEntry.getValue()){
				System.out.print(" " + aInteger);
			}
			System.out.println("\n");
		}
		// 获取每个资源的类型
		sliceBuild.setResourceTypeMap(ResourceInfo.getReourceTypeMap(rescalInputDir));
		
		sliceBuild.setResourceIncomingNeighborMap(GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir));
		sliceBuild.setResourceOutgoingNeighborMap(GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir));
		// resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		//getSliceLinkedNodes(rescalInputDir, 6539, 37);
		  LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = sliceBuild.getSliceLinkedNodes(rescalInputDir, 1, 37);
	         for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
	        	 System.out.println(" Slice # " + entry.getKey() + " size " + entry.getValue().size());
	        	 for(Integer item : entry.getValue()){
	        		 System.out.print(item + " ");
	        	 }
	        	 System.out.println("\n");
	         }*/
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ResourcePTMap0724.txt";
		String path2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\Jamendo-ClassPTMap-0724.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\Jamendo";
		
		LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices = DatasetSegmentation.initDataSegment(path, path2, rescalInputDir).segmentDataSet(161771, "http://purl.org/dc/elements/1.1/date");
         for(Entry<Integer, HashSet<Integer>> entry : timeEntitySlices.entrySet()){
        	 System.out.println(" Slice #-- " + entry.getKey() + " size " + entry.getValue().size());
        	 for(Integer item : entry.getValue()){
        		 System.out.print(item + " ");
        	 }
        	 System.out.println("\n");
           }
         
		
		LinkedHashMap<Integer, HashSet<Integer>> sliceNodes = SliceDataBuildOldVersion2.initSliceDataBuild(timeEntitySlices, rescalInputDir).getSliceLinkedNodes(rescalInputDir, -1, 161771);
		        
		for(Entry<Integer, HashSet<Integer>> entry : sliceNodes.entrySet()){
		        	 System.out.println(" Slice # " + entry.getKey() + " size " + entry.getValue().size());
		        	 for(Integer item : entry.getValue()){
		        		 System.out.print(item + " ");
		        	 }
		        	 System.out.println("\n");
		         }
	}
}
