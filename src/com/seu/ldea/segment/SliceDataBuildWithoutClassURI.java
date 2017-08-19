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
 * ��Slice data build2 �Ļ����ϣ�����ʱ��Ƭ��ʱ�򲻰�class uri�ӽ��� 
 * @author Lynn
 *
 */
public class SliceDataBuildWithoutClassURI {
	public LinkedHashMap<Integer, HashSet<Integer>> timeEntitySlices;
	public HashMap<Integer, Integer> resourceTypeMap;
	
	// ��Դ���ھ�����Դ֮���ӳ��
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
	 * @param dir�� rescal�����ļ���ַ
	 * @param typeId�� ��Ҫ�о������͵�id����Ҫ����һ���͵ĵ㣬-1��ʾȫ������
	 * @param classId�� �������ĸ����ʱ��Ƭ�����зֵ�
	 * @return ʱ��Ƭ�Լ�ÿ��ʱ��Ƭ�ϵĵ�ļ���
	 * @throws IOException
	 */
	public LinkedHashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int studiedClassId, int segmentClassId)
			throws IOException {
		// ʱ��Ƭ����Լ��������еĵ�ļ���
		LinkedHashMap<Integer, HashSet<Integer>> result = new LinkedHashMap<>();
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);
        HashMap<Integer, String> datasetclassURI = Dataset.getDataSetClass(dir, "JamendoClassURI");
		
		HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
		for (Entry<Integer, HashSet<Integer>> slice : timeEntitySlices.entrySet()) {
			//ÿ��ʱ��Ƭ�ϵĵ���д���
			HashSet<Integer> nodes = new HashSet<>();
			//�Ƚ�ÿ��ʱ��Ƭ�ϵ�ʱ��ʵ��ӽ���
			nodes.addAll(slice.getValue());
			int sliceId = slice.getKey();	
			//�Ҿ���ʱ��ʵ���ĳһ���͵��ھ�
			for (Integer timeEntityId : slice.getValue()) {
				// ��Ѱ���з�����Ϊ����Ķ����ھ�
				HashSet<Integer> connectedNodes = findConnectedNodes(timeEntityId, studiedClassId, segmentClassId,2, datasetclassURI);
				if (connectedNodes != null)
					//�ҵ�ÿ������ھӽڵ㼯�ϣ��ӵ�nodes������
					nodes.addAll(connectedNodes);
			}
			result.put(sliceId, nodes);
		}

		// ��ӡ���
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
	 * �ҵ����������з����͵���ھ�����
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
	 * @param recursionRound ��������
	 * @return
	 */
	public HashSet<Integer> findConnectedNodes(int targetNodeId, int studiedClassId, int segementClassId, int recursionRound, HashMap<Integer, String> classURI) {
		if (recursionRound == 0)
			return null;
		
		recursionRound--;
		HashSet<Integer> result = new HashSet<>();

		// targetNode��������֮�����ĵ�
		if (resourceOutgoingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// ���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ�
				if (studiedClassId == segementClassId) {
					result.add(targetNodeId);
					// ����֮�����ӵ������з����͵ĵ�ӽ���
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor))
							result.add(neighbor);
					} else {
						// �������͵��ڽӵ�ӽ���
						if(!classURI.containsKey(neighbor))
						result.add(neighbor);
					}
				} else {
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
					if (studiedClassId != -1) {
						// �����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == studiedClassId && !classURI.containsKey(neighbor)) {
							result.add(neighbor);
							// ����Ҫ�о��ĵ���һ���ھӣ�һ���ھӲ����Ƕ�������,���������з����͵ĵ�
							HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId, recursionRound,classURI);
							if (neighbors != null)
								result.addAll(neighbors);
						}
					} else {
						//���޶������ӵ�����ͣ����������з�����
						if (resourceTypeMap.containsKey(neighbor)) {
							if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor)) {
								result.add(neighbor);
								HashSet<Integer> neighbors = findConnectedNodes(neighbor, -1, segementClassId,
										recursionRound, classURI);
								if (neighbors != null)
									result.addAll(neighbors);
							}
						} else {
							// �����������ڽӵĵ�
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

	    //Ŀ��ʱ��ʵ��������ʱ
		if (resourceIncomingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceIncomingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// ���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ�
				if (studiedClassId == segementClassId) {
					result.add(targetNodeId);
					// ����֮�����ӵ������з����͵ĵ�ӽ���
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != segementClassId && !classURI.containsKey(neighbor))
							result.add(neighbor);
					} else {
						// �������͵��ڽӵ�ӽ���
						if(!classURI.containsKey(neighbor))
						result.add(neighbor);
					}
				} else {
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
					if (studiedClassId != -1) {
						// �����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == studiedClassId) {
							result.add(neighbor);
							// ����Ҫ�о��ĵ���һ���ھӣ�һ���ھӲ����Ƕ�������
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
							// �����������ڽӵĵ�
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
	 * @return����ÿ����Ƭ��ÿ������ھ�ӳ��
	 */
	public static HashMap<Integer, HashSet<Integer>> getSliceNodesNeighor(HashSet<Integer> slicesNodes, HashMap<Integer, HashSet<Integer>> nodesGlobalNeighbor){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		for(Entry<Integer, HashSet<Integer>> nodeGlobalNeighbor : nodesGlobalNeighbor.entrySet()){
			int nodeId = nodeGlobalNeighbor.getKey();
			if(slicesNodes.contains(nodeId)){
				//�ռ���ǰ����Ƭ�ϵĵ���ھ�
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
		// ���������з����ݼ����Լ�ÿ�����ݼ������ʱ��ʵ��ļ���
		sliceBuild.setTimeEntitySlices(DatasetSegmentation.segmentDataSet(37, "http://swrc.ontoware.org/ontology#year"));;
		System.out.println("time entity slice size " + timeEntitySlices.size());
		for(Entry<Integer, HashSet<Integer>> aEntry : timeEntitySlices.entrySet()){
			System.out.println("Slice num " + aEntry.getKey());
			for(Integer aInteger : aEntry.getValue()){
				System.out.print(" " + aInteger);
			}
			System.out.println("\n");
		}
		// ��ȡÿ����Դ������
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
