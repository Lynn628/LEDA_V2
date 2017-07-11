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
 * ������ѡ����ʱ�������з��������ݹ���ʱ��Ƭ�� ����ÿ��ʱ��Ƭ�ϵĵ� ˼·�����ڴ�ʱ��Ƭ����ѡ���Ķ��㣬�ҵ���һ���ھӣ�����һ���ھ��Ҷ����ھ�
 * 
 * @author Lynn
 *
 */
public class SliceDataBuild {
	public static HashMap<Integer, HashSet<Integer>> slices ;
	public static HashMap<Integer, Integer> resourceTypeMap;
	//��Դ�Լ���Դʱ����Ϣ��ӳ��
	public static HashMap<Integer, ResourceInfo> resourceTimeInfo;
	//��Դ���ھ�����Դ֮���ӳ��
	public static HashMap<Integer, Set<Integer>> resourceOutgoingNeighborMap;
	public static HashMap<Integer, Set<Integer>> resourceIncomingNeighborMap;
	
	//��ά�����ʾͼ�е����ӹ�ϵ
	public static int[][] connectionMatrix;

	/**
	 * �ҵ����ʱ��ʵ�����ӵĵ㣬������ѡ�������
	 * 
	 * @param timeResource
	 * @param type
	 * @return ����ÿ��slice�Լ�����ĵ�
	 * @throws IOException
	 */
	public static HashMap<Integer, HashSet<Integer>> getSliceLinkedNodes(String dir, int typeId, int classId) throws IOException {
		// ʱ�����б���Լ��������еĵ�ı��
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
		resourceTypeMap = ResourceInfo.getReourceTypeMap(dir);

		for (Entry<Integer, HashSet<Integer>> slice : slices.entrySet()) {
			HashSet<Integer> nodes = new HashSet<>();
			int sliceNum = slice.getKey();
			for (Integer targetNodeId : slice.getValue()) {
				//��Ѱ���з�����Ϊ����Ķ����ھ�
				HashSet<Integer> connectedNodes = findConnectedNodes(targetNodeId, typeId, classId, 2);
				if(connectedNodes != null)
				nodes.addAll(connectedNodes);
			}
			result.put(sliceNum, nodes);
		}
	
		//��ӡ���
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
	 * ��ѯ����ض���ֱ���������ھӵ�ļ���
	 * 
	 * @param targetNodeId
	 * @param typeId
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes(int targetNodeId, int typeId) {
		HashSet<Integer> result = new HashSet<>();

		// targetNode��������֮�����ĵ�
		for (int i = 0; i < connectionMatrix.length; i++) {
			if (connectionMatrix[i][targetNodeId] == 1) {
				// �ж�i�����ͣ����Ƿ����result��,-1��ʾ��ȡ�ض����������ӵĵ�
				if (typeId != -1) {
					if (resourceTypeMap.containsKey(i) && resourceTypeMap.get(i).intValue() == typeId) {
						// ��Ҫ��Ӳ鿴��node�Ƿ����ʱ����Ϣ����ʱ����Ϣ����ѡ���ķ�Χ��
						result.add(i);
					}
				} else {
					// ��Ҫ��Ӳ鿴��node�Ƿ����ʱ����Ϣ����ʱ����Ϣ����ѡ���ķ�Χ��
					result.add(i);
				}
			}
		}

		// targetNode��������֮�����ĵ�
		for (int j = 0; j < connectionMatrix[targetNodeId].length; j++) {
			if (connectionMatrix[targetNodeId][j] == 1) {
				// �ж�j�����ͣ����Ƿ����result��,-1��ʾ��ȡ�ض����������ӵĵ�
				if (typeId != -1) {
					if (resourceTypeMap.containsKey(j) && resourceTypeMap.get(j).intValue() == typeId) {
						// ��Ҫ��Ӳ鿴��node�Ƿ����ʱ����Ϣ����ʱ����Ϣ����ѡ���ķ�Χ��
						result.add(j);
					}
				} else {
					// ��Ҫ��Ӳ鿴��node�Ƿ����ʱ����Ϣ����ʱ����Ϣ����ѡ���ķ�Χ��
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
        
		// targetNode��������֮�����ĵ�
		if(resourceOutgoingNeighborMap.containsKey(targetNodeId)){
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceOutgoingNeighborMap.get(targetNodeId);
			for(Integer neighbor : neighborSet){
				//���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ� 
				if(typeId == classId){
					result.add(targetNodeId);
					//����֮�����ӵ������з����͵ĵ�ӽ���
					if(resourceTypeMap.containsKey(neighbor)){
						if(resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					}else{
					//�������͵��ڽӵ�ӽ���
					result.add(neighbor);
					}
				}else{
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
				    if(typeId != -1) {
						//�����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor) && resourceTypeMap.get(neighbor).intValue() == typeId){
						    result.add(neighbor);
							//�ҵ�ǰ���һ���ھ�
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
					//�����������ڽӵĵ�
					result.add(neighbor);
					HashSet<Integer> neighbors = findConnectedNodes(neighbor, typeId, classId, recursionRound);
				    if(neighbors != null)
					result.addAll(neighbors);
				}
				}
			}
		 }
		}
		
		// targetNode��������֮�����ĵ�
		System.out.println("resourceIncomingneighborMap is empty " + resourceIncomingNeighborMap.isEmpty());
		if(resourceIncomingNeighborMap.containsKey(targetNodeId)){
			HashSet<Integer> neighborSet = (HashSet<Integer>) resourceIncomingNeighborMap.get(targetNodeId);
			for(Integer neighbor : neighborSet){
				//���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ� 
				if(typeId == classId){
					result.add(targetNodeId);
					//����֮�����ӵ������з����͵ĵ�ӽ���
					if(resourceTypeMap.containsKey(neighbor)){
						if(resourceTypeMap.get(neighbor).intValue() != classId)
							result.add(neighbor);
					}else{
					//�������͵��ڽӵ�ӽ���
					result.add(neighbor);
					}
				}else{
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
				    if(typeId != -1) {
						//�����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(neighbor) && resourceTypeMap.get(neighbor).intValue() == typeId){
						    result.add(neighbor);
							//�ҵ�ǰ���һ���ھ�
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
					//�����������ڽӵĵ�
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
	    //���������з����ݼ����Լ�ÿ�����ݼ������ʱ��
		slices = DatasetSegmentationTest.segmentDataSet(11, "http://swrc.ontoware.org/ontology#year");
	    //��ȡÿ����Դ������
	    resourceTypeMap = ResourceInfo.getReourceTypeMap("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    resourceIncomingNeighborMap = (HashMap<Integer, Set<Integer>>) ClusterUtil.getNodeIncomingNeighbors("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    resourceOutgoingNeighborMap = (HashMap<Integer, Set<Integer>>) ClusterUtil.getNodeOutgoingNeighbors("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2");
	    //resourceTimeInfo = BuildFromFile.getResourceTimeInfo(path);
	   getSliceLinkedNodes("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\SWCC2", -1, 11);
	}
	
	/**
	 * ���ö�ά����鿴����֮������ӹ�ϵ
	 * @param targetNodeId
	 * @param typeId �о���ʵ������
	 * @param classId �з����ݼ���������
	 * @return
	 */
	public static HashSet<Integer> findConnectedNodes3(int targetNodeId, int typeId,int classId, int recursionRound) {
		if(recursionRound == 0){
			return null;
		}
		recursionRound--;
		HashSet<Integer> result = new HashSet<>();
     
		// targetNode��������֮�����ĵ�
		for (int i = 0; i < connectionMatrix.length; i++) {
			if (connectionMatrix[i][targetNodeId] == 1) {
				//���ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ� 
				if(typeId == classId){
					result.add(targetNodeId);
					//����֮�����ӵ������з����͵ĵ�ӽ���
					if(resourceTypeMap.containsKey(i)){
						if(resourceTypeMap.get(i).intValue() != classId)
							result.add(i);
					}else{
					//�������͵��ڽӵ�ӽ���
					result.add(i);
					}
				}else{
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
				    if(typeId != -1) {
						//�����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(i) && resourceTypeMap.get(i).intValue() == typeId){
						    result.add(i);
							//�ҵ�ǰ���һ���ھ�
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
					//�����������ڽӵĵ�
					result.add(i);
					HashSet<Integer> neighbors = findConnectedNodes(i, typeId, classId, recursionRound);
					result.addAll(neighbors);
				}
				}
			}
		  }
		}

		// targetNode��������֮�����ĵ�
		for (int j = 0; j < connectionMatrix[targetNodeId].length; j++) {
			if (connectionMatrix[targetNodeId][j] == 1) {
				//���ÿ��ʱ��Ƭ��ѡȡ���о����ͺ��з�ʱ��Ƭ��������ͬ��ʱ��Ƭ�ϵĵ�Ϊһ���ھ� 
				if(typeId == classId){
					result.add(targetNodeId);
					//����֮�����ӵ������з����͵ĵ�ӽ���
					if(resourceTypeMap.containsKey(j)){
						if(resourceTypeMap.get(j).intValue() != classId)
							result.add(j);
					}else{
					//�������͵��ڽӵ�ӽ���
					result.add(j);
					}
				}else{
					// �ж�i�����ͣ����Ƿ����result��,-1��ʾ���޶������ӵĵ������
				    if(typeId != -1) {
						//�����ʵ������������Ҫ�о���
						if (resourceTypeMap.containsKey(j) && resourceTypeMap.get(j).intValue() == typeId){
						    result.add(j);
							//�ҵ�ǰ���һ���ھ�
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
					//�����������ڽӵĵ�
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
