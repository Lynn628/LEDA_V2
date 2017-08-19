package com.seu.ldea.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * ��ȡrescal����֮��ľ���
 * 
 * @author Lynn
 *
 */
public class RescalDistance {

	/**
	 * 
	 * @param filePath��Rescal
	 *            embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 *//*
	public static HashMap<Integer, HashMap<Integer, BigDecimal>> calcVectorDistance(String filePath, String method) {
		System.out.println("-------begin of vector distance calculation-------");
		long t1 = System.currentTimeMillis();

		// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();

		// �������ĸ���
		int latentNum = 0;
		// ÿ��ʵ���Լ�������ʵ��֮��ľ��룬�����Ǵ洢
		HashMap<Integer, HashMap<Integer, BigDecimal>> result = new HashMap<>();
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String currenttLine = "";
			// ��ȡÿһ�зֽ��������
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
				// ����ʣ������id�뵱ǰid֮��ľ���
				BigDecimal[] innerVector = entityVectors.get(j);

				if (method.equals("Euclidean")) {
					BigDecimal sum = new BigDecimal(0);
					// ��ƽ���������
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
					// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
					BigDecimal denominator = f1.multiply(f2);
					// System.out.println(">>>>>>>>>>>>" + f1);
					// �������ѡ��Cosine��ƽ��
					if (method.equals("Cosine-square")) {
						// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
						distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
						outerToinner.put(j, distance);
						// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
					} else if (method.equals("Cosine-1")) {
						distance = numerator.divide(denominator, 10, 4);
						outerToinner.put(j, distance);

						// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
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
		// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();

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
			// ��ƽ���������
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
			// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
			BigDecimal denominator = f1.multiply(f2);

			// �������ѡ��Cosine��ƽ��
			if (method.equals("Cosine-square")) {
				// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
				distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
				// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
			} else if (method.equals("Cosine-1")) {
				distance = numerator.divide(denominator, 10, 4);

				// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
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
