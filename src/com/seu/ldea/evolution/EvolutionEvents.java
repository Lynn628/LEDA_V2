package com.seu.ldea.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.seu.ldea.evolution.ClusterEvolution.ClusterInfo;
import com.seu.ldea.evolution.ClusterEvolution.ClusterMap;



/**
 * �����ݻ������еļ����¼������������������ѣ��ϲ������֣�����
 * һ������������ʱ���Ƭ�������¼�ֻ����һ�֣������ж�HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap��
 * �������¼���Ȼ�����ʱ��Ƭ�ϵĵ��sliceClusterMap�����Ƴ����ж�����Ϊ���������ɡ�������������Ȼ���ж����š�������������
 * ���жϷ��Ѻͺϲ����������
 * @author Lynn
 *
 */
public class EvolutionEvents {
   /* public static ArrayList<ClusterMap> findExpand(ArrayList<ClusterMap> clusterMapArr, LinkedHashMap<Integer, HashMap<Integer, HashSet<Integer>>> sliceClusters){
    	
    	
    	
    	return null;
    }*/
	
	//slice id , cluster id , event,  dstSlice id, dstCluster id
	class EvolutionTriple{
		ClusterInfo clusterBegin;
		String event;
		ClusterInfo clusterEnd;
		public EvolutionTriple(ClusterInfo clusterBegin, String event, ClusterInfo clusterEnd) {
			super();
			this.clusterBegin = clusterBegin;
			this.event = event;
			this.clusterEnd = clusterEnd;
			
		}
		
		
	}
	
	//�����߼����жϵ�ǰ������һʱ��Ƭ���Ƿ���ڹ�������Ψһ�أ������ڽϸ����ƶ��У�����һʱ��Ƭ�Ĵ�С����ǰ������
	public static ArrayList<EvolutionTriple> findGrowth(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	//�����߼����жϵ�ǰ������һʱ��Ƭ���Ƿ���ڹ�������Ψһ�أ����ڽϸ����ƶ�������ֻ��1���أ�����һʱ��Ƭ�Ĵ�С����ǰ������
	public static ArrayList<EvolutionTriple> findContraction(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		return null;
	}
	
	//�����߼����жϵ�ǰʱ��Ƭ�����м���������һʱ��Ƭ��ĳ�������й�����ϵ���ҵ�ǰ�⼸���صĹ�ģ���Ⱥ�һ����С
	public static ArrayList<EvolutionTriple> findMerging(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	
	
	//�����߼����жϵ�ǰ������һʱ��Ƭ���Ƿ���ڹ������ļ�����,��ֻ������������¼����ع����󣬼������ƶ���ֵ��������2�����ϸ��أ�����һʱ��Ƭ�ĴصĹ�ģ���ȵ�ǰ��С
	public static ArrayList<EvolutionTriple> findSplitting(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		return null;
	}
	//�����߼�����һʱ��Ƭ�ϵ�ĳ�أ�����ǰһʱ��Ƭ�Ĵص�ӳ�䣬�����Լ�û�б�ָ��
	public static ArrayList<EvolutionTriple> findBirth(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	
	//�����߼�����ǰ������һʱ��Ƭû��ӳ���ϵ
	public static ArrayList<EvolutionTriple> findDeath(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		return null;
	}
	
	
	public static void main(String[] args){
		ArrayList<EvolutionTriple> result = new ArrayList<>();
		
	}
}
