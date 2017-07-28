package com.seu.ldea.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.seu.ldea.evolution.ClusterEvolution.ClusterInfo;
import com.seu.ldea.evolution.ClusterEvolution.ClusterMap;



/**
 * 发现演化过程中的几种事件，增长，收缩，分裂，合并，出现，消亡
 * 一个社团在相邻时间的片发生的事件只能有一种，依次判断HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap，
 * 上面点的事件，然后将相关时间片上的点从sliceClusterMap上面移除，判定次序为，先找生成、消亡的社区，然后判定扩张、收缩的社区，
 * 再判断分裂和合并的社区情况
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
	
	//代码逻辑：判断当前簇与下一时间片上是否存在关联最大的唯一簇，即在在较高相似度阈，若下一时间片的簇小，则当前簇收缩
	public static ArrayList<EvolutionTriple> findGrowth(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	//代码逻辑：判断当前簇与下一时间片上是否存在关联最大的唯一簇，即在较高相似度阈里面只有1个簇，若下一时间片的簇小，则当前簇收缩
	public static ArrayList<EvolutionTriple> findContraction(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		return null;
	}
	
	//代码逻辑：判断当前时间片上面有几个簇与下一时间片的某几个簇有关联关系，且当前这几个簇的规模都比后一个簇小
	public static ArrayList<EvolutionTriple> findMerging(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	
	
	//代码逻辑：判断当前簇与下一时间片上是否存在关联最大的几个簇,且只有这个簇与这下几个簇关联大，即在相似度阈值区间里面2个以上个簇，且下一时间片的簇的规模都比当前簇小
	public static ArrayList<EvolutionTriple> findSplitting(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		return null;
	}
	//代码逻辑：下一时间片上的某簇，遍历前一时间片的簇的映射，发现自己没有被指向
	public static ArrayList<EvolutionTriple> findBirth(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		
		
		
		return null;
	}
	
	//代码逻辑：当前簇在下一时间片没有映射关系
	public static ArrayList<EvolutionTriple> findDeath(HashMap<Integer, ArrayList<ClusterMap>> sliceClusterMap){
		return null;
	}
	
	
	public static void main(String[] args){
		ArrayList<EvolutionTriple> result = new ArrayList<>();
		
	}
}
