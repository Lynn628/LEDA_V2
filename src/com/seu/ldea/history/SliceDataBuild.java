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
 * ������ѡ����ʱ�������з��������ݹ���ʱ��Ƭ�� ����ÿ��ʱ��Ƭ�ϵĵ� ˼·�����ڴ�ʱ��Ƭ����ѡ���Ķ��㣬�ҵ���һ���ھӣ�����һ���ھ��Ҷ����ھ�
 * 
 * @author Lynn
 *
 */
public class SliceDataBuild {
	public static LinkedHashMap<Integer, HashSet<Integer>> slices;
	public static HashMap<Integer, Integer> resourceTypeMap;
	// ��Դ�Լ���Դʱ����Ϣ��ӳ��
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo;
	// ��Դ���ھ�����Դ֮���ӳ��
	public static HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap;

	// ��ά�����ʾͼ�е����ӹ�ϵ
	public static int[][] connectionMatrix;

	/**
	 * �ҵ����ʱ��ʵ�����ӵĵ㣬������ѡ�������
	 * 
	 * @param timeResource
	 * @param type,��Ҫ����һ���͵ĵ㣬-1��ʾȫ������
	 * @return ����ÿ��slice�Լ�����ĵ�
	 * @throws IOException
	 */
	public static LinkedHashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int typeId, int classId)
			throws IOException {
		// ʱ�����б���Լ��������еĵ�ı��
		LinkedHashMap<Integer, HashSet<Integer>> result = new LinkedHashMap<>();
	    //���ļ�����resourceTypeMap
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);
        HashMap<Integer, String> resourceURI = ResourceInfo.getReourceURIMap(dir);
		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			HashSet<Integer> nodes = new HashSet<>();
			int sliceNum = slice.getKey();
			for (Integer targetNodeId : slice.getValue()) {
				// ��Ѱ���з�����Ϊ����Ķ����ھ�
				HashSet<Integer> connectedNodes = findConnectedNodes(targetNodeId, typeId, classId,2);
				if (connectedNodes != null)
					nodes.addAll(connectedNodes);
			}
			result.put(sliceNum, nodes);
		}

		// ��ӡ���
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
	 * @param recursionRound ��������
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes(int targetNodeId, int typeId, int classId, int recursionRound) {
		if (recursionRound == 0)
			return null;

		recursionRound--;
		HashSet<Integer> result = new HashSet<>();

		// targetNode��������֮�����ĵ�
		if (resourceOutgoingNeighborMap.containsKey(targetNodeId)) {
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for (Integer neighbor : neighborSet) {
				// ���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ�
				if (typeId == classId) {
					result.add(targetNodeId);
					// ����֮�����ӵ������з����͵ĵ�ӽ���
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					} else {
						// �������͵��ڽӵ�ӽ���
						result.add(neighbor);
					}
				} else {
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
					if (typeId != -1) {
						// �����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == typeId) {
							result.add(neighbor);
							// �ҵ�ǰ���һ���ھ�
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
							// �����������ڽӵĵ�
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
				// ���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ�
				if (typeId == classId) {
					result.add(targetNodeId);
					// ����֮�����ӵ������з����͵ĵ�ӽ���
					if (resourceTypeMap.containsKey(neighbor)) {
						if (resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					} else {
						// �������͵��ڽӵ�ӽ���
						result.add(neighbor);
					}
				} else {
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
					if (typeId != -1) {
						// �����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor)
								&& resourceTypeMap.get(neighbor).intValue() == typeId) {
							result.add(neighbor);
							// �ҵ�ǰ���һ���ھ�
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
							// �����������ڽӵĵ�
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
		String path = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\SWCC-ResourcePTMap0706-1.txt";
		String rescalInputDir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2";

		HashMap<Integer, ResourceInfo> noTypeResourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		LabelClassWithTime.resourceTimeInfo = noTypeResourceTimeInfo;
		HashMap<Integer, ResourceInfo> classTimeInfo = LabelClassWithTime.getClassTimeInformation(rescalInputDir);
		//��ʱ��LabelClassWithTime��resourceTimeInfo�Ѿ��������͵�
		DatasetSegmentation.resourceTimeInfo = LabelClassWithTime.resourceTimeInfo;
		HashMap<Integer, HashMap<String, TimeSpan>> classPTMap = LabelClassWithTime.getClassTimeSpanInfo(classTimeInfo,
				"");
		DatasetSegmentation.interactiveMatrix = InteractiveMatrix.getInteractiveMatrix(classPTMap);
		// ���������з����ݼ����Լ�ÿ�����ݼ������ʱ��
		slices = DatasetSegmentation.segmentDataSet(37, "http://swrc.ontoware.org/ontology#year");
		// ��ȡÿ����Դ������
		resourceTypeMap = ResourceInfo.getReourceTypeMap(rescalInputDir);
		resourceIncomingNeighborMap = GraphUtil
				.getNodeIncomingNeighbors(rescalInputDir);
		resourceOutgoingNeighborMap = GraphUtil
				.getNodeOutgoingNeighbors(rescalInputDir);
		// resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
		getSliceLinkedNodes(rescalInputDir, 6539, 37);
	}


}
