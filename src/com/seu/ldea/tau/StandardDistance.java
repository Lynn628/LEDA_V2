package com.seu.ldea.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * 
 * @author Lynn 计算标准的链接数据之间的距离，利用Dijkstra或者Floyd方法，计算两点之间的最短路径
 */
public class StandardDistance {

	private static int INF = Integer.MAX_VALUE;;

	/**
	 * 非对称矩阵
	 * 创建输入Matrix 首先读取entity-id文件获取有多少个实体，建立多少维的矩阵,其次读取triple文件，为矩阵填充值
	 * 
	 * @param entityFile：三元组文件
	 * @param tripleFile：实体文件
	 * @return int[][] matrix，创建的关系矩阵
	 */

	public static int[][] getMatrix(String entityFile, String tripleFile) {

		int dimension = 0;
		 //long dimension = 0;
		try {
			FileReader fileReader1 = new FileReader(new File(entityFile));
			BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
			// 获取维度
			while (bufferedReader1.readLine() != null) {
				dimension++;
			}
			bufferedReader1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[][] matrix = new int[dimension][dimension];
		try {
			FileReader fileReader2 = new FileReader(new File(tripleFile));
			BufferedReader bufferedWriter2 = new BufferedReader(fileReader2);
			String line = " ";
			while ((line = bufferedWriter2.readLine()) != null) {
				String[] item = line.split(" ");
				int origin = Integer.valueOf(item[0]);
				int end = Integer.valueOf(item[2]);
				
				matrix[origin][end] = 1;
			}
			bufferedWriter2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 将不直接相连的点位置置INF
		int length = matrix.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if (i != j && matrix[i][j] != 1) {
					matrix[i][j] = INF;
				}
			}
		}
		return matrix;
	}

	/**
	 * floyd算法计算任意两点之间的距离，返回的距离矩阵
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[][] floydDistance(int[][] matrix) {
		int dimension = matrix.length;
		for (int k = 0; k < dimension; k++) {
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < dimension; j++) {
					// 如果经过下标为k顶点路径比原两点间路径更短，则更新dist[i][j]和path[i][j]
					int tmp = (matrix[i][k] == INF || matrix[k][j] == INF) ? INF : (matrix[i][k] + matrix[k][j]);
					if (matrix[i][j] > tmp) {
						// "i到j最短路径"对应的值设，为更小的一个(即经过k)
						matrix[i][j] = tmp;
					}
				}
			}
		}
		return matrix;
	}

	/**
	 * 迪杰斯特拉方法计算任意两点之间的距离
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[][] djistraDistance(int[][] matrix) {

		return null;
	}

	/**
	 * 将标准距离进行排序
	 * @param matrix
	 * @return
	 */
	public static ArrayList<Entry<Integer, Integer>> sortStandard(int[][] matrix) {
		int dimension = matrix.length;
		// 利用linked hashmap保证读入读出顺序一致
		LinkedHashMap<Integer, Integer> valueMap = new LinkedHashMap<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int id = i * dimension + j;
				valueMap.put(id, matrix[i][j]);
				// System.out.println(matrix[i][j]);
			}
		}
		// hashmap排序
		ArrayList<Entry<Integer, Integer>> list_data = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is standard list is empty" + list_data.isEmpty());
		Collections.sort(list_data, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				// TODO Auto-generated method stub
				// 升序排序
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return list_data;
	}

	public static void printMatrix(int[][] matrix) {
		int length = matrix.length;
		for (int i = 0; i < length / 10; i++) {
			for (int j = 0; j < length / 10; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println("\n");
		}
	}

	public static void main(String[] args) {
		String entityFile = "D:\\rescalInputFile\\icpw-2009-complete\\entity-ids";
		String tripleFile = "D:\\rescalInputFile\\icpw-2009-complete\\triple";
		int[][] matrix = getMatrix(entityFile, tripleFile);

		// int[][]matrix = new int[][]{{0, 5, INF, 7}, {INF, 0, 4, 2}, {3, 3, 0,
		// 2}, {INF, INF, 1, 0}};

		printMatrix(matrix);
		// System.out.println("************************************");
		int[][] shortF = floydDistance(matrix);
		printMatrix(shortF);
		// System.out.println("Matrix dimension is " + length);

	}
}
