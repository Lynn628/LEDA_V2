package com.seu.ldea.tau;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map.Entry;

import java.util.Scanner;

/**
 * 计算tau值选用Cosine-2计算向量距离效果较好
 * @author Lynn 将两种距离进行tau值计算
 */
public class TauCalculation {

	// 排序计算kendall tau值
	public static BigDecimal calculateTau(int[] standard, int[] comparison) {

		if (standard.length != comparison.length) {
			throw new IllegalArgumentException("Array dimensions is not same");
		}
		BigDecimal bigN = new BigDecimal(String.valueOf(standard.length));
		int N = bigN.intValue();
		int[] aIndex = new int[N];// 记录a数组的索引
		for (int i = 0; i < N; i++) {
			aIndex[standard[i]] = i;
		}
		int[] bIndex = new int[N];// b数组引用a数组的索引
		for (int i = 0; i < N; i++) {
			bIndex[i] = aIndex[comparison[i]];
		}
		BigDecimal distance = insertionCount(bIndex);
		System.out.println("distance is " + distance + " N is " + N + "bigN" + bigN);
		// Kendell correlation co-efficient
		// BigDecimal nBigDecimal = new BigDecimal(N * (N - 1) );
		BigDecimal denominator = bigN.multiply(bigN.subtract(BigDecimal.valueOf(1)));
		System.out.println("denominator " + denominator);
		// BigDecimal kendellTau =
		// BigDecimal.valueOf(1.0).subtract(distance.multiply(BigDecimal.valueOf(4).divide(nBigDecimal,
		// 5, 4)));
		BigDecimal one = new BigDecimal(1.0);
		BigDecimal inconsist = distance.multiply(BigDecimal.valueOf(4));
		// BigDecimal divide = mutiple.divide(nBigDecimal, 5, 4);
		BigDecimal kendellTau = one.subtract(inconsist.divide(denominator, 5, 4));
		return kendellTau;
	}

	// 使用插入排序方法求逆序数
	public static BigDecimal insertionCount(int[] a) {
		BigDecimal counter = new BigDecimal(0);
		for (int i = 1; i < a.length; i++) {
			for (int j = i; j > 0 && a[j] < a[j - 1]; j--) {
				int temp = a[j];
				a[j] = a[j - 1];
				a[j - 1] = temp;
				counter = counter.add(BigDecimal.valueOf(1));
				// System.out.println(counter);// 插入排序每交换一次，就存在一对逆序数
			}
		}

		return counter;
	}

	
	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();

		Scanner sc = new Scanner(System.in);
		System.out.println("Please input embedding fileName");
		String fileName = sc.nextLine();
		System.out.println("Please input latentNumber");
		int latentNumber = Integer.valueOf(sc.nextLine());
		System.out.println("Please input lambda number ");
		int lambdaNumber = Integer.valueOf(sc.nextLine());
		System.out.println("Please input distance calculation method ");
		// int dimension = Integer.valueOf(sc.nextLine());
		String method = sc.nextLine();
		sc.close();
		// 分解好的矩阵
		String rescalEmbending = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\" + fileName + "-latent"
				+ latentNumber + "-lambda" + lambdaNumber + ".embeddings.txt";
		// RESCAL张量的对象距离矩阵
		BigDecimal[][] matrix1 = RescalDistance.calcVectorDistance(rescalEmbending, method);
	    RescalDistance.printMatrix(matrix1);
		System.out.println(matrix1.length );
		// 将向量距离排序
		ArrayList<Entry<Integer, BigDecimal>> rescalDistanceList = RescalDistance.sortVectorDistance(matrix1,
				"BigDecimal");
		String entityFile = "D:\\rescalInputFile\\" + fileName + "\\entity-ids";
		String tripleFile = "D:\\rescalInputFile\\" + fileName + "\\triple";
		// 标准距离
		int[][] matrix2 = StandardDistance.getMatrix(entityFile, tripleFile);
		int[][] shortF = StandardDistance.floydDistance(matrix2);
		// 标准距离排序
		ArrayList<Entry<Integer, Integer>> standardDistanceList = StandardDistance.sortStandard(shortF);
		String resultFile = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\tauResult\\tauResult-" + fileName
				+ "-latent" + latentNumber + "-lambda" + lambdaNumber + "-" + method + ".txt";
		FileWriter fileWriter = new FileWriter(resultFile);
		int length = rescalDistanceList.size();
		int[] standard = new int[length];
		int[] comparison = new int[length];
		for (int i = 0; i < length; i++) {
			standard[i] = standardDistanceList.get(i).getKey();
			comparison[i] = rescalDistanceList.get(i).getKey();
			fileWriter.write(standard[i] + "   " + standardDistanceList.get(i).getValue() + " ; " + comparison[i] + "  "
					+ rescalDistanceList.get(i).getValue() + "\n");
		}
		fileWriter.close();
		// int[] standard1 = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
		// int[] comparison1 = new int[]{2,0,3,1,4,6,7,5};
		System.out.println(calculateTau(standard, comparison));
		long t2 = System.currentTimeMillis();
		System.out.println("Calculation time cost " + (t2 - t1) / 60000.0);
		System.out.println("********************************");

	}
//Cosine-abs-square, Euclidean, Cosine-2
}
