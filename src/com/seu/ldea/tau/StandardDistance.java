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
 * @author Lynn �����׼����������֮��ľ��룬����Dijkstra����Floyd��������������֮������·��
 */
public class StandardDistance {

	private static int INF = Integer.MAX_VALUE;;

	/**
	 * �ǶԳƾ���
	 * ��������Matrix ���ȶ�ȡentity-id�ļ���ȡ�ж��ٸ�ʵ�壬��������ά�ľ���,��ζ�ȡtriple�ļ���Ϊ�������ֵ
	 * 
	 * @param entityFile����Ԫ���ļ�
	 * @param tripleFile��ʵ���ļ�
	 * @return int[][] matrix�������Ĺ�ϵ����
	 */

	public static int[][] getMatrix(String entityFile, String tripleFile) {

		int dimension = 0;
		 //long dimension = 0;
		try {
			FileReader fileReader1 = new FileReader(new File(entityFile));
			BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
			// ��ȡά��
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
		// ����ֱ�������ĵ�λ����INF
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
	 * floyd�㷨������������֮��ľ��룬���صľ������
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[][] floydDistance(int[][] matrix) {
		int dimension = matrix.length;
		for (int k = 0; k < dimension; k++) {
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < dimension; j++) {
					// ��������±�Ϊk����·����ԭ�����·�����̣������dist[i][j]��path[i][j]
					int tmp = (matrix[i][k] == INF || matrix[k][j] == INF) ? INF : (matrix[i][k] + matrix[k][j]);
					if (matrix[i][j] > tmp) {
						// "i��j���·��"��Ӧ��ֵ�裬Ϊ��С��һ��(������k)
						matrix[i][j] = tmp;
					}
				}
			}
		}
		return matrix;
	}

	/**
	 * �Ͻ�˹��������������������֮��ľ���
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[][] djistraDistance(int[][] matrix) {

		return null;
	}

	/**
	 * ����׼�����������
	 * @param matrix
	 * @return
	 */
	public static ArrayList<Entry<Integer, Integer>> sortStandard(int[][] matrix) {
		int dimension = matrix.length;
		// ����linked hashmap��֤�������˳��һ��
		LinkedHashMap<Integer, Integer> valueMap = new LinkedHashMap<>();
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int id = i * dimension + j;
				valueMap.put(id, matrix[i][j]);
				// System.out.println(matrix[i][j]);
			}
		}
		// hashmap����
		ArrayList<Entry<Integer, Integer>> list_data = new ArrayList<>(valueMap.entrySet());
		System.out.println("Is standard list is empty" + list_data.isEmpty());
		Collections.sort(list_data, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				// TODO Auto-generated method stub
				// ��������
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
