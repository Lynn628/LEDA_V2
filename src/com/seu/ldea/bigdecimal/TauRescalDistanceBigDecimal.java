package com.seu.ldea.bigdecimal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * 
 * @author Lynn
 *计算rescal生成的实体向量之间的距离
 *计算：Cosin值，欧式距离...
 */
//D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\ExperimentResultData\icpw2009-latent10.embeddings.txt
public class TauRescalDistanceBigDecimal {
	
	/**
	 * 
	 * @param filePath：Rescal embedding文件
	 *
	 * @param method:距离计算方法：Euclidean或者Cosine
	 * @return 相应实体向量之间的聚类
	 */
	public static BigDecimal[][] getVectorDistanceMatrix(String filePath, String method){
		// LinkedHashMap<Integer, BigDecimal[]> entityVectors = new LinkedHashMap<>();
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();
		//有多少个Entity就有多少维
		int dimension = 0;
		//隐变量的个数
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			//存储所有entity以及对应的分解的出的rank维的向量<id, int[]> 
			String currenttLine = "";
			//读取每一行分解出的向量
			while((currenttLine = bufferedReader.readLine()) != null){
				String[] vectorStr = currenttLine.split(" ");
			    latentNum = vectorStr.length;
			    BigDecimal [] vectorArr = new BigDecimal[latentNum];
			    for(int i = 0; i < latentNum; i++){
			    	vectorArr[i] =  new BigDecimal(vectorStr[i]);
			    	//System.out.println(vectorStr[i]);
			    }
			   entityVectors.add(vectorArr);
			   dimension++;
			  
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BigDecimal[][] distanceMatrix = new BigDecimal[dimension][dimension];
		
		for(int i = 0; i < dimension; i++){
			for(int j = 0; j < dimension; j++)
				distanceMatrix[i][j] = getTwoNodeVectorDistance(i, j, entityVectors, method);
			}
	
		return distanceMatrix;
	}
	
	
	
	public static BigDecimal getTwoNodeVectorDistance(int index1, int index2, ArrayList<BigDecimal[]> entityVectors, String method) {
		
       BigDecimal[] outerVector = entityVectors.get(index1);
       BigDecimal[] innerVector = entityVectors.get(index2);
        int latentNum = innerVector.length;
       BigDecimal distance = new BigDecimal(0);
		if (method.equals("Euclidean")) {
			BigDecimal sum = new BigDecimal(0);
			for (int i = 0; i < latentNum ; i++) {
			   sum = sum.add((outerVector[i].subtract(innerVector[i])).pow(2));
					 
			}
			// 用平方代替距离
			distance = sum;
		
		} else {
			 BigDecimal numerator = new BigDecimal(0);
			 BigDecimal f1 = new BigDecimal(0);
			 BigDecimal f2 = new BigDecimal(0);
			 for(int i = 0; i < latentNum; i++){
				numerator = numerator.add(outerVector[i].multiply(innerVector[i]));
				f1 = f1.add(outerVector[i].pow(2));
				f2 = f2.add(innerVector[i].pow(2));
				 }
			// denominator-分母，此处分母为cosine公式分母的平方
			 BigDecimal denominator = f1.multiply(f2);
				if(denominator.equals(0)){
					System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);
					
				}
				 //距离计算选用Cosine的平方
				 if(method.equals("Cosine-square")){
				 //此处分子为cosine公式分子的平方
				  distance = (numerator.multiply(numerator)).divide(denominator, 10,4);
				
				// 距离计算选用cosine计算公式的分子除以分母的平方
			}  else if(method.equals("Cosine-1")) {
				 distance = numerator.divide(denominator, 10, 4);
				
				// System.out.println(outerId + "-" + innerId + "-" +
				// distance);
				// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
			} else if(method.equals("Cosine-2")){
				  distance = (numerator.abs()).divide(denominator, 10, 4);
				
			 }
		}
	
              return distance;
}
	
	
	
	public static ArrayList<Entry<Integer, BigDecimal>> sortVectorDistance(BigDecimal[][] matrix, String type){
		//排序取前100
	   int dimension = matrix.length;
	   //利用linked hashmap保证读入读出顺序一致
	   LinkedHashMap<Integer, BigDecimal> valueMap = new LinkedHashMap<>();
		for(int i = 0; i < dimension; i++){
			for(int j = 0; j < dimension; j++){
			//	if( i!= j){
				int id = i* dimension + j;
			    valueMap.put(id, matrix[i][j]);
				//}
			   // System.out.println(matrix[i][j]);
			}
		}
		//hashmap排序
		ArrayList<Entry<Integer, BigDecimal>> entryList = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is original vecotr list is empty" + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, BigDecimal>>(){
				@Override
				public int compare(Entry<Integer, BigDecimal> o1, Entry<Integer, BigDecimal> o2) {
					// 升序排序
					//return o1.getValue().compareTo(o2.getValue());
					//余弦距离，数值越大越相似，因而需要降序排序
					return o2.getValue().compareTo(o1.getValue());
		       }
		      });
		
	/*	for (int i = 0; i < entryList.size(); i++) {
			System.out.println(entryList.get(i).getKey() + "---> " + entryList.get(i).getValue());
		}*/
		return entryList;
	}
	

	//打印输出矩阵
	public static void printMatrix(BigDecimal[][] matrix) {
		int length = matrix.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				System.out.print((i + "," + j + "-->" + matrix[i][j]) + " ");
			}
			System.out.println("\n");
		}
		//.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue()
	}
	
	
	public static void main(String[] args){
	
	   printMatrix(getVectorDistanceMatrix("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\vectorCalcTest.txt", "Cosine-2"));	
	}
}
