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
 * 辅助Tau计算的Rescal距离计算函数类
 * @author Lynn
 *
 */
public class TauRescalDistance {

	/**
	 * 
	 * @param filePath：Rescal
	 *            embedding文件
	 *
	 * @param method:距离计算方法：Euclidean或者Cosine
	 * @return 相应实体向量之间的聚类
	 */
	public static Double[][] getVectorDistanceMatrix(String filePath, String method) {
		// LinkedHashMap<Integer, BigDecimal[]> entityVectors = new
		// LinkedHashMap<>();
		ArrayList<Double[]> entityVectors = new ArrayList<>();
		// 有多少个Entity就有多少维
		int dimension = 0;
		// 隐变量的个数
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// 存储所有entity以及对应的分解的出的rank维的向量<id, int[]>
			String currenttLine = "";
			// 读取每一行分解出的向量
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
		// 添加一个元素初始化矩阵的过程
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
			// 内层迭代器定位到指定的位置
			Iterator<Double[]> innerIterator = entityVectors.iterator();
			for (int i = 0; i < iterNum - 1; i++) {
				innerIterator.next();
			}
			// 计算剩余所有id与当前id之间的距离
			while (innerIterator.hasNext()) {
				Double[] innerVector = innerIterator.next();
				// System.out.println(innerVector.toString());
				int innerId = entityVectors.indexOf(innerVector);
				if (method.equals("Euclidean")) {
					Double sum = new Double(0);
					for (int i = 0; i < latentNum; i++) {
						sum += (outerVector[i] - innerVector[i]) * (outerVector[i] - innerVector[i]);
					}
					// 用平方代替距离
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
					// denominator-分母，此处分母为cosine公式分母的平方
					Double denominator = f1 * f2;
					if(denominator == 0){
						System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);
						
					}
					// 距离计算选用Cosine的平方
					if (method.equals("Cosine-square")) {
						// 此处分子为cosine公式分子的平方
						Double distance = (numerator * numerator) / denominator;
						distanceMatrix[outerId][innerId] = distance;
						distanceMatrix[innerId][outerId] = distance;
						// 距离计算选用cosine计算公式的分子除以分母的平方
					} else if (method.equals("Cosine-1")) {
						Double distance = numerator / denominator;
						distanceMatrix[outerId][innerId] = distance;
						distanceMatrix[innerId][outerId] = distance;
						// System.out.println(outerId + "-" + innerId + "-" +
						// distance);
						// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
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
		// 利用linked hashmap保证读入读出顺序一致
		LinkedHashMap<Integer, Double> valueMap = new LinkedHashMap<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int id = i * dimension + j;
				valueMap.put(id, matrix[i][j]);
				// System.out.println(matrix[i][j]);
			}
		}
		// hashmap排序
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is original vecotr list is empty" + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// 升序排序
				//return o1.getValue().compareTo(o2.getValue());
				//降序排序，Cosine值越大，两个向量之间的距离越相似
				return o2.getValue().compareTo(o1.getValue());
			}
		});

	/*	for (int i = 0; i < entryList.size(); i++) {
			System.out.println(entryList.get(i).getKey() +
					       "---> " + entryList.get(i).getValue());
		}*/
		return entryList;
	}

	// 打印输出矩阵
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
