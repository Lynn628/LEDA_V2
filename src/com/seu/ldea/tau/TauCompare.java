package com.seu.ldea.tau;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * 比较规格化embedding文件后Tau值的变化
 * 
 * @author Lynn
 *
 */
public class TauCompare {
	public static void main(String[] args) throws IOException {
		long t1 = System.currentTimeMillis();
		String rescalInputFileName = "iswc-2006-complete";
		String dirpath1 = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\toBeNormalized\\iswc2006";
		String dirpath2 = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\NormalizedEmbeddingFile\\iswc2006";
		String entityFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\" + rescalInputFileName
				+ "\\entity-ids";
		String tripleFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\" + rescalInputFileName
				+ "\\triple";
		FileWriter fileWriter = new FileWriter("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TauResult\\"
				+ rescalInputFileName + "-TauCompare-ReverseCosine.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write("Tau compare data set is " + rescalInputFileName);
		bufferedWriter.newLine();
		bufferedWriter.flush();
		// 标准距离
		int[][] matrix2 = StandardDistance.getMatrix(entityFile, tripleFile);
		int[][] shortF = StandardDistance.floydDistance(matrix2);
		// 标准距离排序
		ArrayList<Entry<Integer, Integer>> standardDistanceList = StandardDistance.sortStandard(shortF);
		File file1 = new File(dirpath1);
		File file2 = new File(dirpath2);
		BigDecimal tauValue1 = new BigDecimal(0);
		double tauValue2 = 0;
		if (file1.isDirectory() && file2.isDirectory()) {
			String[] fileList1 = file1.list();
			String[] fileList2 = file2.list();
			System.out.println(fileList1.length + " >>> " + fileList2.length);
			for (int i = 0; i < fileList1.length; i++) {
				// System.out.println("fileList1 " + fileList1[i]);
				//File fileA = new File(dirpath1 + "\\" + fileList1[i]);
				String rescalEmbeddingPath = dirpath1 + "\\" + fileList1[i];
				System.out.println("rescalEmbeddingpath " + rescalEmbeddingPath.toString());
				//File fileB = new File(dirpath2 + "\\" + fileList2[i]);
				String normalizedrescalEmbeddingPath = dirpath2 + "\\" + fileList2[i];
				System.out.println("normalizedEmbeddingpath " + normalizedrescalEmbeddingPath.toString());
				tauValue1 = bigDecimalGetTau(rescalEmbeddingPath, fileList1[i], standardDistanceList);
				tauValue2 = normalizedGetTau(normalizedrescalEmbeddingPath, fileList2[i], standardDistanceList);
				bufferedWriter.write(
						fileList1[i] + " tau: " + tauValue1.toString() + " ; " + fileList2[i] + " tau:  " + tauValue2);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Time cost  " + (t2-t1)/1000 + " s");

	}

	/**
	 * 
	 * @param rescalDir,
	 *            存放rescal embedding 路径
	 * @throws IOException
	 */
	public static BigDecimal bigDecimalGetTau(String rescalEmbeddingPath, String bigtauResultName,
			ArrayList<Entry<Integer, Integer>> standardDistanceList) throws IOException {

		/** BigDecimal的Tau计算 **/

		// RESCAL张量的对象距离矩阵
		BigDecimal[][] matrix = TauRescalDistanceBigDecimal.getVectorDistanceMatrix(rescalEmbeddingPath, "Cosine-2");
		// RescalDistance.printMatrix(matrix);
		// System.out.println(matrix.length );
		// 将向量距离排序
		ArrayList<Entry<Integer, BigDecimal>> rescalDistanceList = TauRescalDistanceBigDecimal
				.sortVectorDistance(matrix, "");

	/*	String resultFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TauResult\\" + bigtauResultName;
		FileWriter fileWriter = new FileWriter(resultFile);*/
		int length = rescalDistanceList.size();
		int[] standard = new int[length];
		int[] comparison = new int[length];
		for (int i = 0; i < length; i++) {
			standard[i] = standardDistanceList.get(i).getKey();
			comparison[i] = rescalDistanceList.get(i).getKey();
			/*fileWriter.write(standard[i] + "   " + standardDistanceList.get(i).getValue() + " ; " + comparison[i] + "  "
					+ rescalDistanceList.get(i).getValue() + "\n");*/
		}
		//fileWriter.close();
		// int[] standard1 = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
		// int[] comparison1 = new int[]{2,0,3,1,4,6,7,5};
		BigDecimal tauValue = new TauCalculationBigDecimal().calculateTau(standard, comparison);
		return tauValue;
	}

	public static double normalizedGetTau(String normalizedrescalEmbeddingPath, String tauResultName,
			ArrayList<Entry<Integer, Integer>> standardDistanceList) throws IOException {

		/** 规格化后的tau计算 **/
		// RESCAL张量的对象距离矩阵
		Double[][] rescalDistanceMatrix = TauRescalDistance.getVectorDistanceMatrix(normalizedrescalEmbeddingPath,
				"Cosine-2");
	//	TauRescalDistance.printMatrix(rescalDistanceMatrix);
		System.out.println(rescalDistanceMatrix.length);
		// 将向量距离排序
		ArrayList<Entry<Integer, Double>> rescalDistanceList = TauRescalDistance
				.sortVectorDistance(rescalDistanceMatrix);
       
	/*	String resultFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TauResult\\noramlizedtauResult-"
				+ tauResultName;

		FileWriter fileWriter = new FileWriter(resultFile);*/
		int length = rescalDistanceList.size();
		int[] standard = new int[length];
		int[] comparison = new int[length];
		for (int i = 0; i < length; i++) {
			standard[i] = standardDistanceList.get(i).getKey();
			comparison[i] = rescalDistanceList.get(i).getKey();
			/*fileWriter.write(standard[i] + "   " + standardDistanceList.get(i).getValue() + " ; " + comparison[i] + "  "
					+ rescalDistanceList.get(i).getValue() + "\n");*/
			//fileWriter.flush();
		}
		//fileWriter.close();

		double tauValue = new TauCalculation().calculateTau(standard, comparison);
		return tauValue;
	}

}
