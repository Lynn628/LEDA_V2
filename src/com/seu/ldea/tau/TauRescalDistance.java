package com.seu.ldea.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * ����Tau�����Rescal������㺯����
 * @author Lynn
 *
 */
public class TauRescalDistance {

	/**
	 * 
	 * @param filePath��Rescal
	 *            embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static Double[][] getVectorDistanceMatrix(String filePath, String method) {
		// LinkedHashMap<Integer, BigDecimal[]> entityVectors = new
		// LinkedHashMap<>();
		ArrayList<Double[]> entityVectors = new ArrayList<>();
		// �ж��ٸ�Entity���ж���ά
		int dimension = 0;
		// �������ĸ���
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������<id, int[]>
			String currenttLine = "";
			// ��ȡÿһ�зֽ��������
			while ((currenttLine = bufferedReader.readLine()) != null) {
				String[] vectorStr = currenttLine.split(" ");
				latentNum = vectorStr.length;
				Double[] vectorArr = new Double[latentNum];
				for (int i = 0; i < latentNum; i++) {
					vectorArr[i] = new Double(vectorStr[i]);
					// System.out.println(vectorStr[i]);
				}
				entityVectors.add(vectorArr);
				dimension++;
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double[][] distanceMatrix = new Double[dimension][dimension];
		// ���һ��Ԫ�س�ʼ������Ĺ���
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				distanceMatrix[i][j] = new Double(0);
			}
		}
		// printMatrix(distanceMatrix);
		Iterator<Double[]> outerIterator = entityVectors.iterator();
		int iterNum = 0;
		while (outerIterator.hasNext()) {
			iterNum++;
			Double[] outerVector = outerIterator.next();
			// System.out.println(outerVector.toString());
			int outerId = entityVectors.indexOf(outerVector);
			// �ڲ��������λ��ָ����λ��
			Iterator<Double[]> innerIterator = entityVectors.iterator();
			for (int i = 0; i < iterNum - 1; i++) {
				innerIterator.next();
			}
			// ����ʣ������id�뵱ǰid֮��ľ���
			while (innerIterator.hasNext()) {
				Double[] innerVector = innerIterator.next();
				// System.out.println(innerVector.toString());
				int innerId = entityVectors.indexOf(innerVector);
				if (method.equals("Euclidean")) {
					Double sum = new Double(0);
					for (int i = 0; i < latentNum; i++) {
						sum += (outerVector[i] - innerVector[i]) * (outerVector[i] - innerVector[i]);
					}
					// ��ƽ���������
					Double distance = sum;
					// System.out.println("outerId and InnerId is -- >" +
					// outerId + " " + innerId);
					distanceMatrix[outerId][innerId] = distance;
					distanceMatrix[innerId][outerId] = distance;
				} else {
					Double numerator = new Double(0);
					Double f1 = new Double(0);
					Double f2 = new Double(0);
					for (int i = 0; i < latentNum; i++) {
						numerator += outerVector[i] * innerVector[i];
						f1 += outerVector[i] * outerVector[i];
						f2 += innerVector[i] * innerVector[i];
					}
					// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
					Double denominator = f1 * f2;
					if(denominator == 0){
						System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);
						
					}
					// �������ѡ��Cosine��ƽ��
					if (method.equals("Cosine-square")) {
						// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
						Double distance = (numerator * numerator) / denominator;
						distanceMatrix[outerId][innerId] = distance;
						distanceMatrix[innerId][outerId] = distance;
						// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
					} else if (method.equals("Cosine-1")) {
						Double distance = numerator / denominator;
						distanceMatrix[outerId][innerId] = distance;
						distanceMatrix[innerId][outerId] = distance;
						// System.out.println(outerId + "-" + innerId + "-" +
						// distance);
						// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
					} else if (method.equals("Cosine-2")) {
						Double distance = numerator / denominator;
						if (distance <= 0)
							distance = -distance;
						distanceMatrix[outerId][innerId] = distance;
						distanceMatrix[innerId][outerId] = distance;
					}
				}
			}

		}
		return distanceMatrix;
	}

	public static ArrayList<Entry<Integer, Double>> sortVectorDistance(Double[][] matrix) {
		int dimension = matrix.length;
		// ����linked hashmap��֤�������˳��һ��
		LinkedHashMap<Integer, Double> valueMap = new LinkedHashMap<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int id = i * dimension + j;
				valueMap.put(id, matrix[i][j]);
				// System.out.println(matrix[i][j]);
			}
		}
		// hashmap����
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is original vecotr list is empty" + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// ��������
				//return o1.getValue().compareTo(o2.getValue());
				//��������CosineֵԽ����������֮��ľ���Խ����
				return o2.getValue().compareTo(o1.getValue());
			}
		});

	/*	for (int i = 0; i < entryList.size(); i++) {
			System.out.println(entryList.get(i).getKey() +
					       "---> " + entryList.get(i).getValue());
		}*/
		return entryList;
	}

	// ��ӡ�������
	public static void printMatrix(Double[][] matrix) {
		int length = matrix.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				System.out.print((i + "," + j + "-->" + matrix[i][j]) + " ");
			}
			System.out.println("\n");
		}
		// .setScale(5, Double.ROUND_HALF_UP).doubleValue()
	}

	public static void main(String[] args) {
		
		// printMatrix(calcVectorDistance("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\icpw2009complete-latent10-lambda0.embeddings.txt",
		// "Cosine-2"));
		// printMatrix(calcVectorDistance("D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\icpw2009-complete-latent150-lambda0.embeddings.txt",
		// "Cosine-1"));
		printMatrix(getVectorDistanceMatrix("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\vectorCalcTest2.txt",
				"Cosine-2"));
	}
}
