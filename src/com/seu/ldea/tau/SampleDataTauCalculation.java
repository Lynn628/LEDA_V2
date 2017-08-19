package com.seu.ldea.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

public class SampleDataTauCalculation {
	/**
	 * �����ݼ��в������ֵ���У�ѡȡ���ֵ㹹���ɾ��󣬼����׼���룬Ȼ������ArrayList<Double[]> entityVectors��
	 * ʵʱ���������֮��ľ����Ȼ�� Ҫ����ѡ���ĵ�;�������ĵ��ӳ���ϵ��Ȼ�����þ�������ĵ���о�����㣬����˳��
	 * vector����ʵ�ʾ��������������򣬸���һ��˳��Ȼ���������˳��֮��������
	 */
	public static final int NodeNum = 100;
	// �����ѡȡ��Χ�������entity �ĸ���,DBLPΪ 2395434 JamendoΪ412576
	public static final int BOUND = 2395434;
	// ԭʼid����id��ӳ��
	public static HashMap<Integer, Integer> sampledDataMap = new HashMap<>();
	// ��id��ԭʼidӳ��
	public static HashMap<Integer, Integer> reversedSampledDataMap = new HashMap<>();

	public static void initSampleDataMap() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		Random random = new Random();
		while (list.size() < NodeNum) {
			int num = random.nextInt(BOUND);
			if (!list.contains(num)) {
				list.add(num);
			}
		}
		System.out.println("Random list size " + list.size());
		for (int i = 0; i < list.size(); i++) {
			System.out.println("sample data--" + i + ": " + list.get(i));
			sampledDataMap.put(list.get(i), i);
			reversedSampledDataMap.put(i, list.get(i));
		}

	}

	/**
	 * 
	 * @param dir
	 * @param sampledDataMap,ԭʼid����id��ӳ��
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static int[][] sampledDataMatrix(String dir) throws NumberFormatException, IOException {
		// ��ʼ������
		int[][] matrix = new int[NodeNum][NodeNum];
		// ��ʼ����׼�������
		String triplePath = dir + "\\triple";
		FileReader fileReader = new FileReader(triplePath);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			String[] item = line.split(" ");
			int origin = Integer.valueOf(item[0]);
			int end = Integer.valueOf(item[2]);
			if (sampledDataMap.containsKey(origin) && sampledDataMap.containsKey(end)) {
				int startId = sampledDataMap.get(origin);
				int endId = sampledDataMap.get(end);
				matrix[startId][endId] = 1;
			}
		}
		for (int i = 0; i < NodeNum; i++) {
			for (int j = 0; j < NodeNum; j++) {
				if (matrix[i][j] != 1 && i != j)
					matrix[i][j] = Integer.MAX_VALUE;
				if (i == j) {
					matrix[i][j] = 0;
				}
			}
		}
		bufferedReader.close();
		return matrix;
	}

	/**
	 * 
	 * @param dir,rescalinput
	 *            dir
	 * @param normalizedrescalEmbeddingPath
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */

	/*
	 * public static double sampledDataTau(String dir, String
	 * normalizedrescalEmbeddingPath) throws NumberFormatException, IOException{
	 * //��ʼ��map initSampleDataMap(); int[][] sampledDataConnctionMatrix =
	 * sampledDataMatrix(dir); int[][] shortF =
	 * StandardDistance.floydDistance(sampledDataConnctionMatrix); // ��׼��������
	 * ArrayList<Entry<Integer, Integer>> standardDistanceList =
	 * StandardDistance.sortStandard(shortF); Double[][]
	 * sampledDataVectorDistanceMatrix =
	 * getVectorDistanceMatrix(normalizedrescalEmbeddingPath, "Cosine-2");
	 * ArrayList<Entry<Integer, Double>> vectorDistanceList =
	 * sortVectorDistance(sampledDataVectorDistanceMatrix); int[] standard = new
	 * int[standardDistanceList.size()]; int[] comparison = new
	 * int[standardDistanceList.size()];
	 * 
	 * for(int i = 0; i < standardDistanceList.size(); i++){ //��׼���������
	 * standard[i] = standardDistanceList.get(i).getKey(); //�������������
	 * comparison[i] = vectorDistanceList.get(i).getKey();
	 * System.out.println("st id " + standard[i] + " cmp id " + comparison[i]);
	 * }
	 * 
	 * double tauValue = new TauCalculation().calculateTau(standard,
	 * comparison); return tauValue; }
	 */

	public static ArrayList<String[]> sampledDataTau(String dir, String normalizedrescalEmbeddingDir)
			throws NumberFormatException, IOException {
		// ��ʼ��map
		initSampleDataMap();
		double tauValue = 0;
		ArrayList<String[]> result = new ArrayList<>();
		int[][] sampledDataConnctionMatrix = sampledDataMatrix(dir);
		int[][] shortF = StandardDistance.floydDistance(sampledDataConnctionMatrix);
		// ��׼��������
		ArrayList<Entry<Integer, Integer>> standardDistanceList = StandardDistance.sortStandard(shortF);
		File file1 = new File(normalizedrescalEmbeddingDir);
		if (file1.isDirectory()) {
			String[] fileList1 = file1.list();
			for (int i = 0; i < fileList1.length; i++) {
				String info[] = new String[2];
				String normalizedrescalEmbeddingPath = normalizedrescalEmbeddingDir + "\\" + fileList1[i];
				info[0] = fileList1[i];
				Double[][] sampledDataVectorDistanceMatrix = getVectorDistanceMatrix(normalizedrescalEmbeddingPath,
						"Cosine-2");
				ArrayList<Entry<Integer, Double>> vectorDistanceList = sortVectorDistance(
						sampledDataVectorDistanceMatrix);
				int[] standard = new int[standardDistanceList.size()];
				int[] comparison = new int[standardDistanceList.size()];

				for (int j = 0; j < standardDistanceList.size(); j++) {
					// ��׼���������
					standard[j] = standardDistanceList.get(j).getKey();
					// �������������
					comparison[j] = vectorDistanceList.get(j).getKey();
					System.out.println("st id " + standard[j] + " cmp id " + comparison[j]);
				}

				tauValue = new TauCalculation().calculateTau(standard, comparison);
				info[1] = String.valueOf(tauValue);
				result.add(info);
			}
		}
		return result;

	}

	/**
	 * 
	 * @param filePath��Rescal
	 *            embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static Double[][] getVectorDistanceMatrix(String filePath, String method) {

		ArrayList<Double[]> entityVectors = new ArrayList<>();
		// �ж��ٸ�Entity���ж���ά
		// �������ĸ���
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(filePath);
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
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int dimension = NodeNum;
		// System.out.println("dimension is---- " + dimension);
		Double[][] distanceMatrix = new Double[dimension][dimension];

		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				// ���i��j֮��������
				// if(connectionMatrix[i][j] == 1){
				int orginI = reversedSampledDataMap.get(i);
				int originJ = reversedSampledDataMap.get(j);
				double distance = getTwoNodeVectorDistance(orginI, originJ, entityVectors, method);
				// ������������ľ���
				distanceMatrix[i][j] = distance;
			}
		}
		return distanceMatrix;
	}

	public static double getTwoNodeVectorDistance(int index1, int index2, ArrayList<Double[]> entityVectors,
			String method) {

		Double[] outerVector = entityVectors.get(index1);
		Double[] innerVector = entityVectors.get(index2);
		int latentNum = innerVector.length;
		Double distance = new Double(0);
		if (method.equals("Euclidean")) {
			Double sum = new Double(0);
			for (int i = 0; i < latentNum; i++) {
				sum += (outerVector[i] - innerVector[i]) * (outerVector[i] - innerVector[i]);
			}
			// ��ƽ���������
			distance = sum;

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
			if (denominator == 0) {
				System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);

			}
			// �������ѡ��Cosine��ƽ��
			if (method.equals("Cosine-square")) {
				// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
				distance = (numerator * numerator) / denominator;

				// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
			} else if (method.equals("Cosine-1")) {
				distance = numerator / denominator;

				// System.out.println(outerId + "-" + innerId + "-" +
				// distance);
				// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
			} else if (method.equals("Cosine-2")) {
				distance = numerator / denominator;
				if (distance <= 0)
					distance = -distance;

			}
		}

		return distance;
	}

	public static ArrayList<Entry<Integer, Double>> sortVectorDistance(Double[][] matrix) {
		int dimension = matrix.length;
		// ����linked hashmap��֤�������˳��һ��
		LinkedHashMap<Integer, Double> valueMap = new LinkedHashMap<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				// if(i != j){
				int id = i * dimension + j;
				valueMap.put(id, matrix[i][j]);
				// }
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
				// return o1.getValue().compareTo(o2.getValue());
				// ��������CosineֵԽ����������֮��ľ���Խ����
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		return entryList;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		String dir = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\DBLP";
		String normalizedDirPath = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\DBLP";
		/*
		 * File file1 = new File(normalizedDirPath); if (file1.isDirectory()){
		 * String[] fileList1 = file1.list(); for (int i = 0; i <
		 * fileList1.length; i++) { String normalizedrescalEmbeddingPath =
		 * normalizedDirPath + "\\" + fileList1[i]; sampledDataTau(dir,
		 * normalizedrescalEmbeddingPath);
		 * 
		 * } }
		 */
		ArrayList<String[]> result = sampledDataTau(dir, normalizedDirPath);
		System.out.println("Sample node num " + NodeNum);
		for (String[] item : result) {
			System.out.println("file is " + item[0] + " tau is " + item[1]);
		}
		// String normalizedrescalEmbeddingPath =
		// "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\NormalizedDBLP-latent10.txt";
		// System.out.println("Tau is " + sampledDataTau(dir,
		// normalizedrescalEmbeddingPath));
	}

}
