package com.seu.ldea.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//用于距离计算的向量是规格化之后的
public class RescalDistanceForCluster {
	

	/**
	 * 將embedding文件組成ArrayList
	 * @param filePath
	 * @return
	 */
	public static ArrayList<Double[]> getNodeVector(String filePath) {
		// 存储所有entity以及对应的分解的出的rank维的向量
		ArrayList<Double[]> entityVectors = new ArrayList<>();

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
				Double[] vectorArr = new Double[latentNum];
				for (int i = 0; i < latentNum; i++) {
					vectorArr[i] = Double.parseDouble(vectorStr[i]);
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

	/**
	 * 查找并计算两个实体之间的距离
	 * @param entityVectors
	 * @param method
	 * @param sourceId
	 * @param dstId
	 * @return
	 */
	public static double calcVectorDistance(ArrayList<Double[]> entityVectors, String method, int sourceId,
			int dstId) {

		int latentNum = entityVectors.get(0).length;
		Double[] outerVector = entityVectors.get(sourceId);
		double distance = 0;
		// System.out.println(outerVector.toString());
		Double[] innerVector = entityVectors.get(dstId);
		if (method.equals("Euclidean")) {
			double sum = 0;
			// 用平方代替距离
			for (int k = 0; k < latentNum; k++)
			sum += (outerVector[k]-innerVector[k]) * (outerVector[k]-innerVector[k]);
			distance = sum;

		} else {
			double numerator = 0;
			double f1 = 0;
			double f2 = 0;
			for (int k = 0; k < latentNum; k++) {
				numerator += outerVector[k] * innerVector[k];
				f1 += outerVector[k] * outerVector[k];
				f2 += innerVector[k] * innerVector[k];
			}
			// denominator-分母，此处分母为cosine公式分母的平方
			double denominator = f1 * f2;
			// 距离计算选用Cosine的平方
			if (method.equals("Cosine-square")) {
				// 此处分子为cosine公式分子的平方
				distance = (numerator * numerator) / denominator;
				// 距离计算选用cosine计算公式的分子除以分母的平方
			} else if (method.equals("Cosine-1")) {
				distance = numerator / denominator;

				// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
			} else if (method.equals("Cosine-2")) {
				distance = numerator / denominator;
				if(distance <= 0){
					distance = -distance;
				}
			}
		}
		return distance;
	}

	public static void main(String[] args) {
		String embeddingFilePath = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\SWCC2-latent10-lambda0.embeddings.txt";
		String normailizedFilePath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		
	
	}
}
