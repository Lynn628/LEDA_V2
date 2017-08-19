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
 *����rescal���ɵ�ʵ������֮��ľ���
 *���㣺Cosinֵ��ŷʽ����...
 */
//D:\RESCAL\Ext-RESCAL-master\Ext-RESCAL-master\ExperimentResultData\icpw2009-latent10.embeddings.txt
public class TauRescalDistanceBigDecimal {
	
	/**
	 * 
	 * @param filePath��Rescal embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static BigDecimal[][] getVectorDistanceMatrix(String filePath, String method){
		// LinkedHashMap<Integer, BigDecimal[]> entityVectors = new LinkedHashMap<>();
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();
		//�ж��ٸ�Entity���ж���ά
		int dimension = 0;
		//�������ĸ���
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			//�洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������<id, int[]> 
			String currenttLine = "";
			//��ȡÿһ�зֽ��������
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
			// ��ƽ���������
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
			// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
			 BigDecimal denominator = f1.multiply(f2);
				if(denominator.equals(0)){
					System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);
					
				}
				 //�������ѡ��Cosine��ƽ��
				 if(method.equals("Cosine-square")){
				 //�˴�����Ϊcosine��ʽ���ӵ�ƽ��
				  distance = (numerator.multiply(numerator)).divide(denominator, 10,4);
				
				// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
			}  else if(method.equals("Cosine-1")) {
				 distance = numerator.divide(denominator, 10, 4);
				
				// System.out.println(outerId + "-" + innerId + "-" +
				// distance);
				// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
			} else if(method.equals("Cosine-2")){
				  distance = (numerator.abs()).divide(denominator, 10, 4);
				
			 }
		}
	
              return distance;
}
	
	
	
	public static ArrayList<Entry<Integer, BigDecimal>> sortVectorDistance(BigDecimal[][] matrix, String type){
		//����ȡǰ100
	   int dimension = matrix.length;
	   //����linked hashmap��֤�������˳��һ��
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
		//hashmap����
		ArrayList<Entry<Integer, BigDecimal>> entryList = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is original vecotr list is empty" + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, BigDecimal>>(){
				@Override
				public int compare(Entry<Integer, BigDecimal> o1, Entry<Integer, BigDecimal> o2) {
					// ��������
					//return o1.getValue().compareTo(o2.getValue());
					//���Ҿ��룬��ֵԽ��Խ���ƣ������Ҫ��������
					return o2.getValue().compareTo(o1.getValue());
		       }
		      });
		
	/*	for (int i = 0; i < entryList.size(); i++) {
			System.out.println(entryList.get(i).getKey() + "---> " + entryList.get(i).getValue());
		}*/
		return entryList;
	}
	

	//��ӡ�������
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
