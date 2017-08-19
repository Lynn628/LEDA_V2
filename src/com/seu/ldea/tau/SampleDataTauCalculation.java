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
	 * 从数据集中采样部分点进行，选取部分点构建成矩阵，计算标准距离，然后利用ArrayList<Double[]> entityVectors，
	 * 实时计算出两者之间的距离后然后， 要建立选出的点和矩阵上面的点的映射关系，然后利用矩阵上面的点进行距离计算，给出顺序。
	 * vector依据实际距离计算出来后，排序，给出一个顺序，然后计算两个顺序之间的相关性
	 */
	public static final int NodeNum = 100;
	// 随机数选取范围，最好是entity 的个数,DBLP为 2395434 Jamendo为412576
	public static final int BOUND = 2395434;
	// 原始id和新id的映射
	public static HashMap<Integer, Integer> sampledDataMap = new HashMap<>();
	// 新id和原始id映射
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
	 * @param sampledDataMap,原始id和新id的映射
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static int[][] sampledDataMatrix(String dir) throws NumberFormatException, IOException {
		// 初始化矩阵
		int[][] matrix = new int[NodeNum][NodeNum];
		// 初始化标准矩阵距离
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
	 * //初始化map initSampleDataMap(); int[][] sampledDataConnctionMatrix =
	 * sampledDataMatrix(dir); int[][] shortF =
	 * StandardDistance.floydDistance(sampledDataConnctionMatrix); // 标准距离排序
	 * ArrayList<Entry<Integer, Integer>> standardDistanceList =
	 * StandardDistance.sortStandard(shortF); Double[][]
	 * sampledDataVectorDistanceMatrix =
	 * getVectorDistanceMatrix(normalizedrescalEmbeddingPath, "Cosine-2");
	 * ArrayList<Entry<Integer, Double>> vectorDistanceList =
	 * sortVectorDistance(sampledDataVectorDistanceMatrix); int[] standard = new
	 * int[standardDistanceList.size()]; int[] comparison = new
	 * int[standardDistanceList.size()];
	 * 
	 * for(int i = 0; i < standardDistanceList.size(); i++){ //标准的排列序号
	 * standard[i] = standardDistanceList.get(i).getKey(); //向量距离的排序
	 * comparison[i] = vectorDistanceList.get(i).getKey();
	 * System.out.println("st id " + standard[i] + " cmp id " + comparison[i]);
	 * }
	 * 
	 * double tauValue = new TauCalculation().calculateTau(standard,
	 * comparison); return tauValue; }
	 */

	public static ArrayList<String[]> sampledDataTau(String dir, String normalizedrescalEmbeddingDir)
			throws NumberFormatException, IOException {
		// 初始化map
		initSampleDataMap();
		double tauValue = 0;
		ArrayList<String[]> result = new ArrayList<>();
		int[][] sampledDataConnctionMatrix = sampledDataMatrix(dir);
		int[][] shortF = StandardDistance.floydDistance(sampledDataConnctionMatrix);
		// 标准距离排序
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
					// 标准的排列序号
					standard[j] = standardDistanceList.get(j).getKey();
					// 向量距离的排序
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
	 * @param filePath：Rescal
	 *            embedding文件
	 *
	 * @param method:距离计算方法：Euclidean或者Cosine
	 * @return 相应实体向量之间的聚类
	 */
	public static Double[][] getVectorDistanceMatrix(String filePath, String method) {

		ArrayList<Double[]> entityVectors = new ArrayList<>();
		// 有多少个Entity就有多少维
		// 隐变量的个数
		int latentNum = 0;
		try {
			FileReader fileReader = new FileReader(filePath);
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
				// 如果i，j之间有连接
				// if(connectionMatrix[i][j] == 1){
				int orginI = reversedSampledDataMap.get(i);
				int originJ = reversedSampledDataMap.get(j);
				double distance = getTwoNodeVectorDistance(orginI, originJ, entityVectors, method);
				// 给出向量矩阵的距离
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
			// 用平方代替距离
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
			// denominator-分母，此处分母为cosine公式分母的平方
			Double denominator = f1 * f2;
			if (denominator == 0) {
				System.out.println(">>>>>denominator is zero >>>>>>>" + f1 + ">>>" + f2);

			}
			// 距离计算选用Cosine的平方
			if (method.equals("Cosine-square")) {
				// 此处分子为cosine公式分子的平方
				distance = (numerator * numerator) / denominator;

				// 距离计算选用cosine计算公式的分子除以分母的平方
			} else if (method.equals("Cosine-1")) {
				distance = numerator / denominator;

				// System.out.println(outerId + "-" + innerId + "-" +
				// distance);
				// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
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
		// 利用linked hashmap保证读入读出顺序一致
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
		// hashmap排序
		ArrayList<Entry<Integer, Double>> entryList = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is original vecotr list is empty" + entryList.isEmpty());
		Collections.sort(entryList, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// 升序排序
				// return o1.getValue().compareTo(o2.getValue());
				// 降序排序，Cosine值越大，两个向量之间的距离越相似
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
