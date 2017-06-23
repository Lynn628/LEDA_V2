package com.seu.Idea.cluster;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import com.seu.ldea.tau.RescalDistance;


/**
 * 由质心向外传播，标签着色，冲突解决
 * @author Lynn
 *
 */
public class ClusterImpl {

	public static Dataset dataset;
	public static BigDecimal[][] vectorDistance;
	public static  Map<Integer, int[]> labelPropagation(int[] centroidList, Map<Integer, Set<Integer>>neighborsMap){
         Map<Integer, int[]> labelMap = new HashMap<Integer, int[]>();
         Queue<Integer> nodeQueue = new LinkedList<Integer>();
         int[] labelArray;
         for(int i = 0; i < centroidList.length; i++){
        	 labelArray = new int[2];
        	 
        	 labelArray[0] = centroidList[i];
        	 labelArray[1] =centroidList[i];
        			 
        	 labelMap.put(centroidList[i], labelArray);
        	 nodeQueue.offer(centroidList[i]);
         }
         labelArray = null;
         
         int current;
         int tempNeighbor;
         int[] tempArray;
         BigDecimal[][] confilctResult;
         Set<Integer> neighborSet;
         
         List<Integer> visitedNode =  new ArrayList();
         
         while(!nodeQueue.isEmpty()){
        	 current = nodeQueue.poll();
        	 visitedNode.add(current);
        	 
        	 if(!neighborsMap.containsKey(current)){
        		 continue;
        	 }
        	 //获取出队列的点的邻接点
        	 neighborSet = neighborsMap.get(current);
        	Iterator<Integer> i = neighborSet.iterator();
        	while(i.hasNext()){
        		tempNeighbor = (int) i.next();
        		//已被访问的顶点不再访问
        		if(visitedNode.contains(tempNeighbor))
        			continue;
        		//未访问的点加入队列中
        		if(!nodeQueue.contains(tempNeighbor)){
        			nodeQueue.offer(tempNeighbor);
        		}
        		
        		if(!labelMap.containsKey(tempNeighbor)){
        			tempArray = new int[2];
        			//当前出队列的点的标签的颜色
        			tempArray[0] = labelMap.get(current)[0];
        			//被当前出队列的点传播到
        			tempArray[1] = current;
        			labelMap.put(tempNeighbor, tempArray);
        		}else{
        			//该出队列顶点的邻接点已经被标上颜色，则进行着色判定
        			if(labelMap.get(current)[0] == labelMap.get(tempNeighbor)[0])
        				continue;
        			else{
        				confilctResult = vectorDistance;
        				int result = confilctResult[tempNeighbor][labelMap.get(tempNeighbor)[1]].compareTo(confilctResult[current][labelMap.get(tempNeighbor)[1]]);
        				if(result == 0){
        					continue;
        				}else if(result < 1){
        					labelMap.get(tempNeighbor)[0] = labelMap.get(current)[0];
        					labelMap.get(tempNeighbor)[1] = current;
        					
        				}
        			}
        		}
        	}
         }        
         visitedNode = null;
         nodeQueue = null;
         return labelMap;
		
	}
	
	public static void main(String[] args) throws IOException{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Dataset directory --->");
		String datasetDirectory = scanner.nextLine();
		System.out.println("Dataset embedingpath-->");
		String datasetEmbedingPath = scanner.nextLine();
		dataset = new Dataset(datasetDirectory, datasetEmbedingPath);
		System.out.println(datasetDirectory + "\n" + datasetEmbedingPath);
		scanner.close();
		Map<Integer, Set<Integer>>neighborsMap = ClusterUtil.getNodeNeighbors(dataset.getDatasetDirectory());
	    Graph<Integer, DefaultEdge> graph = ClusterUtil.buildGraph(dataset.getDatasetDirectory());
	    vectorDistance = RescalDistance.calcVectorDistance(datasetEmbedingPath,"Cosine-2");
	   int[] centroidList = CentroidUtil.getCentroidNodes(graph, 5, vectorDistance);
	   
	   Map<Integer, int[]> labelMap = labelPropagation(centroidList, neighborsMap);
	   for(Entry<Integer, int[]> entry: labelMap.entrySet()){
		   int key = entry.getKey();
		   for(int i = 0; i < entry.getValue().length; i++){
			   System.out.println(key + " --> " + entry.getValue()[i]);
		   }
		   System.out.println("-----------");
	   }
	}
	
}//// C:\Users\Lynn\Desktop\Academic\LinkedDataProject\rescalInput\icpw-2009-complete
// D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\icpw2009complete-latent10-lambda0.embeddings.txt
