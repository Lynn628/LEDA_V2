package com.seu.ldea.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//���ھ������������ǹ��֮���
public class RescalDistanceForCluster {
	/**
	 * 
	 * @param filePath��Rescal
	 *            embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static HashMap<Integer, HashMap<Integer, Double>> calcVectorDistance(String filePath, String method) {
		System.out.println("-------begin of vector distance calculation-------");
		long t1 = System.currentTimeMillis();

		// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������
		ArrayList<Double[]> entityVectors = new ArrayList<>();

		// �������ĸ���
		int latentNum = 0;
		// ÿ��ʵ���Լ�������ʵ��֮��ľ��룬�����Ǵ洢
		HashMap<Integer, HashMap<Integer, Double>> result = new HashMap<>();
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
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
				// dimension++;

			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < entityVectors.size(); i++) {

			System.out.println("----round---- " + i);
			Double[] outerVector = entityVectors.get(i);
			Double distance;
			// System.out.println(outerVector.toString());
			HashMap<Integer, Double> outerToinner = new HashMap<>();
			for (int j = i + 1; j < entityVectors.size(); j++) {
				// ����ʣ������id�뵱ǰid֮��ľ���
				Double[] innerVector = entityVectors.get(j);

				if (method.equals("Euclidean")) {
					double sum = 0;
					// ��ƽ���������
					for (int k = 0; k < latentNum; k++)
						sum += (outerVector[k]-innerVector[k])*(outerVector[k]-innerVector[k]);
					distance = sum;
					outerToinner.put(j, distance);

				} else {
					//����
					double numerator = 0;
					double f1 = 0;
					double f2 = 0;
					for (int k = 0; k < latentNum; k++) {
						numerator += outerVector[k] *innerVector[k];
						f1 += outerVector[k] * outerVector[k];
						f2 += innerVector[k] * innerVector[k];
					}
					// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
					double denominator = f1 * f2;
					// System.out.println(">>>>>>>>>>>>" + f1);
					// �������ѡ��Cosine��ƽ��
					if (method.equals("Cosine-square")) {
						// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
						distance = (numerator * numerator)/denominator;
						outerToinner.put(j, distance);
						// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
					} else if (method.equals("Cosine-1")) {
						distance = numerator/denominator;
						outerToinner.put(j, distance);

						// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
					} else if (method.equals("Cosine-2")) {
						distance = numerator/ denominator;
						if(distance <= 0)
							distance = -distance;
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

	/**
	 * ��embedding�ļ��M��ArrayList
	 * @param filePath
	 * @return
	 */
	public static ArrayList<Double[]> getNodeVector(String filePath) {
		// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������
		ArrayList<Double[]> entityVectors = new ArrayList<>();

		// �������ĸ���
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String currenttLine = "";
			// ��ȡÿһ�зֽ��������
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
	 * ���Ҳ���������ʵ��֮��ľ���
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
			// ��ƽ���������
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
			// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
			double denominator = f1 * f2;
			// �������ѡ��Cosine��ƽ��
			if (method.equals("Cosine-square")) {
				// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
				distance = (numerator * numerator) / denominator;
				// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
			} else if (method.equals("Cosine-1")) {
				distance = numerator / denominator;

				// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
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
		calcVectorDistance(normailizedFilePath, "Cosine-2");
	}
}
