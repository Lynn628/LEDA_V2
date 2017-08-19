package com.seu.ldea.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * 获取rescal两点之间的距离
 * 
 * @author Lynn
 *
 */
public class RescalDistance {

	/**
	 * 
	 * @param filePath：Rescal
	 *            embedding文件
	 *
	 * @param method:距离计算方法：Euclidean或者Cosine
	 * @return 相应实体向量之间的距离
	 *//*
	public static HashMap<Integer, HashMap<Integer, BigDecimal>> calcVectorDistance(String filePath, String method) {
		System.out.println("-------begin of vector distance calculation-------");
		long t1 = System.currentTimeMillis();

		// 存储所有entity以及对应的分解的出的rank维的向量
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();

		// 隐变量的个数
		int latentNum = 0;
		// 每个实体以及与其他实体之间的距离，上三角存储
		HashMap<Integer, HashMap<Integer, BigDecimal>> result = new HashMap<>();
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String currenttLine = "";
			// 读取每一行分解出的向量
			while ((currenttLine = bufferedReader.readLine()) != null) {
				String[] vectorStr = currenttLine.split(" ");
				latentNum = vectorStr.length;
				BigDecimal[] vectorArr = new BigDecimal[latentNum];
				for (int i = 0; i < latentNum; i++) {
					vectorArr[i] = new BigDecimal(vectorStr[i]);
					// System.out.println(vectorStr[i]);
				}
				entityVectors.add(vectorArr);
				// dimension++;

			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < entityVectors.size(); i++) {

			System.out.println("----round---- " + i);
			BigDecimal[] outerVector = entityVectors.get(i);
			BigDecimal distance;
			// System.out.println(outerVector.toString());
			HashMap<Integer, BigDecimal> outerToinner = new HashMap<>();
			for (int j = i + 1; j < entityVectors.size(); j++) {
				// 计算剩余所有id与当前id之间的距离
				BigDecimal[] innerVector = entityVectors.get(j);

				if (method.equals("Euclidean")) {
					BigDecimal sum = new BigDecimal(0);
					// 用平方代替距离
					for (int k = 0; k < latentNum; k++)
						sum = sum.add((outerVector[k].subtract(innerVector[k])).pow(2));
					distance = sum;
					outerToinner.put(j, distance);

				} else {
					BigDecimal numerator = new BigDecimal(0);
					BigDecimal f1 = new BigDecimal(0);
					BigDecimal f2 = new BigDecimal(0);
					for (int k = 0; k < latentNum; k++) {
						numerator = numerator.add(outerVector[k].multiply(innerVector[k]));
						f1 = f1.add(outerVector[k].pow(2));
						f2 = f2.add(innerVector[k].pow(2));
					}
					// denominator-分母，此处分母为cosine公式分母的平方
					BigDecimal denominator = f1.multiply(f2);
					// System.out.println(">>>>>>>>>>>>" + f1);
					// 距离计算选用Cosine的平方
					if (method.equals("Cosine-square")) {
						// 此处分子为cosine公式分子的平方
						distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
						outerToinner.put(j, distance);
						// 距离计算选用cosine计算公式的分子除以分母的平方
					} else if (method.equals("Cosine-1")) {
						distance = numerator.divide(denominator, 10, 4);
						outerToinner.put(j, distance);

						// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
					} else if (method.equals("Cosine-2")) {
						distance = (numerator.abs()).divide(denominator, 10, 4);
						outerToinner.put(j, distance);
					}
				}
				distance = null;
			}
			result.put(i, outerToinner);
		}
		long t2 = System.currentTimeMillis();
		System.out.println("End of vector calculation------time cost -------" + (t2 - t1) / 1000 + " s");
		return result;
	}
*/
	public static ArrayList<BigDecimal[]> getNodeVector(String filePath) {
		// 存储所有entity以及对应的分解的出的rank维的向量
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();

		// 隐变量的个数
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String currenttLine = "";
			// 读取每一行分解出的向量
			while ((currenttLine = bufferedReader.readLine()) != null) {
				String[] vectorStr = currenttLine.split(" ");
				latentNum = vectorStr.length;
				BigDecimal[] vectorArr = new BigDecimal[latentNum];
				for (int i = 0; i < latentNum; i++) {
					vectorArr[i] = new BigDecimal(vectorStr[i]);
					// System.out.println(vectorStr[i]);
				}
				entityVectors.add(vectorArr);
				// dimension++;

			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entityVectors;
	}

	
	
	public static BigDecimal calcVectorDistance(ArrayList<BigDecimal[]> entityVectors, String method, int sourceId,
			int dstId) {

		int latentNum = entityVectors.get(0).length;
		BigDecimal[] outerVector = entityVectors.get(sourceId);
		BigDecimal distance = new BigDecimal(0);
		// System.out.println(outerVector.toString());
		BigDecimal[] innerVector = entityVectors.get(dstId);
		if (method.equals("Euclidean")) {
			BigDecimal sum = new BigDecimal(0);
			// 用平方代替距离
			for (int k = 0; k < latentNum; k++)
				sum = sum.add((outerVector[k].subtract(innerVector[k])).pow(2));
			distance = sum;

		} else {
			BigDecimal numerator = new BigDecimal(0);
			BigDecimal f1 = new BigDecimal(0);
			BigDecimal f2 = new BigDecimal(0);
			for (int k = 0; k < latentNum; k++) {
				numerator = numerator.add(outerVector[k].multiply(innerVector[k]));
				f1 = f1.add(outerVector[k].pow(2));
				f2 = f2.add(innerVector[k].pow(2));
			}
			// denominator-分母，此处分母为cosine公式分母的平方
			BigDecimal denominator = f1.multiply(f2);

			// 距离计算选用Cosine的平方
			if (method.equals("Cosine-square")) {
				// 此处分子为cosine公式分子的平方
				distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
				// 距离计算选用cosine计算公式的分子除以分母的平方
			} else if (method.equals("Cosine-1")) {
				distance = numerator.divide(denominator, 10, 4);

				// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
			} else if (method.equals("Cosine-2")) {
				distance = (numerator.abs()).divide(denominator, 10, 4);
			}
		}
		return distance;
	}

	public static void main(String[] args) {
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\SWCC2-latent10-lambda0.embeddings.txt";
	//	calcVectorDistance(embeddingFilePath, "Cosine-2");
	}
}
