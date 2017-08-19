package com.seu.ldea.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class KMeansInput {
    //拿到每个时间片上的点，统计点的个数num然后生成sqrt（num）+1维的矩阵，存放这些点，然后生成每个点的坐标与原始点的映射

	//依据每个时间片上的点
   public static double[][] KmeansInputBuild(int[] sliceData, HashMap<Integer, HashSet<Integer>> resourceIncomingNeighborMap,
			HashMap<Integer, HashSet<Integer>> resourceOutgoingNeighborMap){
	   //依据时间片上的点
	   System.out.println("*****************This slice size " + sliceData.length);
	double[][] nodeVector = new double[sliceData.length][sliceData.length]; 
	   for(int i = 0; i < sliceData.length; i++){
		   for(int j = 0; j < sliceData.length; j++){
			   int currentNodeId = sliceData[i];
			   int matchNodeId = sliceData[j];
			   if(resourceIncomingNeighborMap.get(currentNodeId).contains(matchNodeId) || 
					   resourceOutgoingNeighborMap.get(currentNodeId).contains(matchNodeId)){
				   nodeVector[i][j] = 1.0;
				   nodeVector[j][i] = 1.0;
			   }else{
				   nodeVector[i][j] = 0.0;
			   }
		   }
	   }
	  /* for(int i = 0; i < sliceData.length; i++){
		   for(int j = 0; j < sliceData.length; j++)
	       System.out.print(nodeVector[i][j] + " ");
		   System.out.println("\n");
	   }*/
	   return nodeVector;
   }
	
	public static Double[][] KmeansInputBuild(int[] sliceData, ArrayList<Double[]> entityVectors){
		int m = sliceData.length;
		int n = entityVectors.get(0).length;
		Double[][] result = new Double[m][n];
		for(int i = 0; i< m; i++){
			result[i] = entityVectors.get(sliceData[i]);
		
		}
		/*for(int i = 0; i < m; i++){
		 	
		for(int j =0; j < n; j++)
		 System.out.print("DATA " + result[i][j] + " ");
		System.out.println("\n");
		}*/
		return result;
	}
	
	
}
