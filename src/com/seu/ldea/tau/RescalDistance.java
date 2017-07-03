package com.seu.ldea.tau;

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
public class RescalDistance {
	
	/**
	 * 
	 * @param filePath��Rescal embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static BigDecimal[][] calcVectorDistance(String filePath, String method){
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
		//���һ��Ԫ�س�ʼ������Ĺ���
		for(int i = 0; i < dimension; i++){
			for(int j = 0; j < dimension; j++){
				distanceMatrix[i][j] = new BigDecimal(0);
			}
		}
	//	printMatrix(distanceMatrix);
	    Iterator<BigDecimal[]> outerIterator = entityVectors.iterator();
	    int iterNum = 0;
		while(outerIterator.hasNext()){
			 iterNum++;
			 
			 BigDecimal[] outerVector = outerIterator.next();
			// System.out.println(outerVector.toString());
			 int outerId = entityVectors.indexOf(outerVector);
			 //�ڲ��������λ��ָ����λ��
			 Iterator<BigDecimal[]> innerIterator = entityVectors.iterator();
			 for(int i = 0; i < iterNum - 1; i++){
				 innerIterator.next();
			 }
			//����ʣ������id�뵱ǰid֮��ľ���
		 while(innerIterator.hasNext()){
				 BigDecimal[] innerVector = innerIterator.next();
			//	 System.out.println(innerVector.toString());
				 int innerId = entityVectors.indexOf(innerVector);
				 if(method.equals("Euclidean")){
				 BigDecimal sum = new BigDecimal(0);
				 for(int i = 0; i < latentNum; i++){
					sum = sum.add((outerVector[i].subtract(innerVector[i])).pow(2));
				 }
				 //��ƽ���������
				 BigDecimal distance = sum;
				// System.out.println("outerId and InnerId is -- >" + outerId + " " + innerId);
				 distanceMatrix[outerId][innerId]= distance;
				 distanceMatrix[innerId][outerId] = distance;
				 }else {
					 BigDecimal numerator = new BigDecimal(0);
					 BigDecimal f1 = new BigDecimal(0);
					 BigDecimal f2 = new BigDecimal(0);
					 for(int i = 0; i < latentNum; i++){
						numerator = numerator.add(outerVector[i].multiply(innerVector[i]));
						f1 = f1.add(outerVector[i].pow(2));
						f2 = f2.add(innerVector[i].pow(2));
						 }
					//denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
					 BigDecimal denominator = f1.multiply(f2);
				     System.out.println(">>>>>>>>>>>>" + f1);
					 //�������ѡ��Cosine��ƽ��
					 if(method.equals("Cosine-square")){
					 //�˴�����Ϊcosine��ʽ���ӵ�ƽ��
					 BigDecimal distance = (numerator.multiply(numerator)).divide(denominator, 10,4);
					 distanceMatrix[outerId][innerId]= distance;
					 distanceMatrix[innerId][outerId] = distance;
					 //�������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
					 }else if(method.equals("Cosine-1")) {
						 BigDecimal distance = numerator.divide(denominator, 10, 4);
						 distanceMatrix[outerId][innerId]= distance;
						 distanceMatrix[innerId][outerId] = distance;
						// System.out.println(outerId + "-" + innerId + "-" + distance);
				     //�������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
					 }else if(method.equals("Cosine-2")){
						 BigDecimal distance = (numerator.abs()).divide(denominator, 10, 4);
						 distanceMatrix[outerId][innerId]= distance;
						 distanceMatrix[innerId][outerId] = distance;
					 }
				 }
			}
	
		}
		return distanceMatrix;
	}
	
	
	public static ArrayList<Entry<Integer, BigDecimal>> sortVectorDistance(BigDecimal[][] matrix, String type){
		//����ȡǰ100
	   int dimension = matrix.length;
	   //����linked hashmap��֤�������˳��һ��
	   LinkedHashMap<Integer, BigDecimal> valueMap = new LinkedHashMap<>();
		for(int i = 0; i < dimension; i++){
			for(int j = 0; j < dimension; j++){
				int id = i* dimension + j;
			    valueMap.put(id, matrix[i][j]);
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
					return o1.getValue().compareTo(o2.getValue());
		       }
		      });
		
		for (int i = 0; i < entryList.size(); i++) {
			System.out.println(entryList.get(i).getKey() + "---> " + entryList.get(i).getValue());
		}
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
	/*	String numberStr = "8.527210019739954837e-11";
		BigDecimal number = new BigDecimal(numberStr);
		String numberStr2 = "2.067192128537560858e-10";
		BigDecimal number2 = new BigDecimal(numberStr2);
		BigDecimal result = number.add(number2);
		System.out.println(result.doubleValue());
		String aString = "-1.096619396952590060e-15 -1.541779030098108941e-10 4.572340874016548707e-15 -1.228008179942374617e-13 -2.675978283291999450e-15 2.547156754840260746e-15 -1.206041782753271513e-13 -4.861489940044404239e-15 1.537488430952749440e-10 3.784480607420342211e-1";
		String bString = "-1.088166736137109356e-15 -1.541827367606518010e-10 4.490734916738140035e-15 -1.214540492732992064e-13 -2.678741201361051696e-15 2.568881522902228112e-15 -1.191991942833114481e-13 -4.814851848341933688e-15 1.538230857870067078e-10 3.809721655117075247e-15";
	   // String aString = "-1.096619396952590060e-15 -1.541779030098108941e-10 4.572340874016548707e-15 -1.228008179942374617e-13 -2.675978283291999450e-15 2.547156754840260746e-15 -1.206041782753271513e-13 -4.861489940044404239e-15 1.537488430952749440e-10 3.784480607420342211e-1";
		//String bString = "1.641413865918811616e-14 -2.288223633857064224e-09 -4.192921045496021932e-13 1.540012427796555686e-12 -5.276574086847124795e-14 9.708806922254631368e-14 1.905692845601200279e-12 4.041050861979755826e-13 2.713418384096298218e-09 1.334966766864147119e-13";
	    String[] aStrings = aString.split(" ");
	    String[] bStrings = bString.split(" ");
	    BigDecimal sum = new BigDecimal(0);
	    BigDecimal numerator = new BigDecimal(0);
		 BigDecimal f1 = new BigDecimal(0);
		 BigDecimal f2 = new BigDecimal(0);
		 for(int i = 0; i < aStrings.length; i++){
			 BigDecimal aBigDecimal = new BigDecimal(aStrings[i]);
			 BigDecimal bigDecimal = new BigDecimal(bStrings[i]);		 
			numerator = numerator.add(aBigDecimal.multiply(bigDecimal));
			f1 = f1.add(aBigDecimal.pow(2));
			f2 = f2.add(bigDecimal.pow(2));
			 }
		 BigDecimal numeratorAbs = numerator.abs();
		 numeratorAbs = numeratorAbs.multiply(numeratorAbs);
		// System.out.println(f1 + "   " +f2);
		 //BigDecimal denominator = new BigDecimal(Math.sqrt((f1.multiply(f2)).doubleValue()));
		//ȡ����ֵ
		 BigDecimal denominator = f1.multiply(f2);
		 // denominator = denominator.multiply(denominator);
		 BigDecimal distance = (numeratorAbs.abs()).divide(denominator, 10, 4);
		
	 //3.4018707463085896480379488691315091478716E-12 3.149679078939121713719100387914577980683437338182216804229605085752726066906107327525585E-19 10800690.04190

	    System.out.println( distance);
	    
		//BigDecimal[][] matrix = calcEuclideanDistance("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\ExperimentResultData\\icpw2009-latent10.embeddings.txt", 171);
		//printMatrix(matrix);
		//System.out.println(matrix[0][3].setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue());Cosine-square
		 * icpw2009-complete-latent150-lambda0.embeddings.txt
*/	 
		//printMatrix(calcVectorDistance("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\icpw2009complete-latent10-lambda0.embeddings.txt", "Cosine-2"));
		//printMatrix(calcVectorDistance("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\icpw2009-complete-latent150-lambda0.embeddings.txt", "Cosine-1"));
	   printMatrix(calcVectorDistance("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\vectorCalcTest.txt", "Cosine-2"));	
	}
}
