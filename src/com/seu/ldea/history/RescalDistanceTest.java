package com.seu.ldea.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.jena.base.Sys;

/**
 * ��ȡrescal����֮��ľ���
 * 
 * @author Lynn
 *
 */
public class RescalDistanceTest {

	/**
	 * 
	 * @param filePath��Rescal
	 *            embedding�ļ�
	 *
	 * @param method:������㷽����Euclidean����Cosine
	 * @return ��Ӧʵ������֮��ľ���
	 */
	public static HashMap<Integer, HashMap<Integer, BigDecimal>> calcVectorDistance(String filePath, String method) {
		System.out.println("-------begin of vector distance calculation-------");
		long t1 = System.currentTimeMillis();
		//
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();
		// �ж��ٸ�Entity���ж���ά
		int dimension = 0;
		// �������ĸ���
		int latentNum = 0;
		//
		HashMap<Integer, HashMap<Integer, BigDecimal>> result = new HashMap<>();
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// �洢����entity�Լ���Ӧ�ķֽ�ĳ���rankά������<id, int[]>
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
				dimension++;

			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Iterator<BigDecimal[]> outerIterator = entityVectors.iterator();
		int iterNum = 0;
		while (outerIterator.hasNext()) {
			iterNum++;

			BigDecimal[] outerVector = outerIterator.next();
			// System.out.println(outerVector.toString());
			int outerId = entityVectors.indexOf(outerVector);
			HashMap<Integer, BigDecimal> outerToinner = new HashMap<>();
			result.put(outerId, outerToinner);
			// �ڲ��������λ��ָ����λ��
			Iterator<BigDecimal[]> innerIterator = entityVectors.iterator();
			for (int i = 0; i < iterNum - 1; i++) {
				innerIterator.next();
			}
			// ����ʣ������id�뵱ǰid֮��ľ���
			while (innerIterator.hasNext()) {
				BigDecimal[] innerVector = innerIterator.next();
				// System.out.println(innerVector.toString());
				int innerId = entityVectors.indexOf(innerVector);
				if (method.equals("Euclidean")) {
					BigDecimal sum = new BigDecimal(0);
					// ��ƽ���������
					for (int i = 0; i < latentNum; i++) 
						sum = sum.add((outerVector[i].subtract(innerVector[i])).pow(2));
					BigDecimal distance = sum;
				
					outerToinner.put(innerId, distance);
					if(!result.containsKey(innerId)){
					HashMap<Integer, BigDecimal> innerToOuter = new HashMap<>();
				     innerToOuter.put(outerId, distance);
				     result.put(innerId, innerToOuter);
					}else{
						result.get(innerId).put(outerId, distance);
					}
					
				} else {
					BigDecimal numerator = new BigDecimal(0);
					BigDecimal f1 = new BigDecimal(0);
					BigDecimal f2 = new BigDecimal(0);
					for (int i = 0; i < latentNum; i++) {
						numerator = numerator.add(outerVector[i].multiply(innerVector[i]));
						f1 = f1.add(outerVector[i].pow(2));
						f2 = f2.add(innerVector[i].pow(2));
					}
					// denominator-��ĸ���˴���ĸΪcosine��ʽ��ĸ��ƽ��
					BigDecimal denominator = f1.multiply(f2);
					//System.out.println(">>>>>>>>>>>>" + f1);
					// �������ѡ��Cosine��ƽ��
					if (method.equals("Cosine-square")) {
						// �˴�����Ϊcosine��ʽ���ӵ�ƽ��
						BigDecimal distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
						
						outerToinner.put(innerId, distance);
						if(!result.containsKey(innerId)){
						HashMap<Integer, BigDecimal> innerToOuter = new HashMap<>();
					     innerToOuter.put(outerId, distance);
					     result.put(innerId, innerToOuter);
						}else{
							result.get(innerId).put(outerId, distance);
						}
						
						// �������ѡ��cosine���㹫ʽ�ķ��ӳ��Է�ĸ��ƽ��
					} else if (method.equals("Cosine-1")) {
						BigDecimal distance = numerator.divide(denominator, 10, 4);
					
						outerToinner.put(innerId, distance);
						if(!result.containsKey(innerId)){
						HashMap<Integer, BigDecimal> innerToOuter = new HashMap<>();
					     innerToOuter.put(outerId, distance);
					     result.put(innerId, innerToOuter);
						}else{
							result.get(innerId).put(outerId, distance);
						}
						
						// System.out.println(outerId + "-" + innerId + "-" +
						// distance);
						// �������ѡ��cosine���㹫ʽ�ķ��Ӿ���ֵ���Է�ĸ��ƽ��
					} else if (method.equals("Cosine-2")) {
						BigDecimal distance = (numerator.abs()).divide(denominator, 10, 4);
						
						outerToinner.put(innerId, distance);
						if(!result.containsKey(innerId)){
						HashMap<Integer, BigDecimal> innerToOuter = new HashMap<>();
					     innerToOuter.put(outerId, distance);
					     result.put(innerId, innerToOuter);
						}else{
							result.get(innerId).put(outerId, distance);
						}
						
					}
				}
			}

		}
		long t2 = System.currentTimeMillis();
		System.out.println("End of vector calculation--------time cost -------" + (t2-t1)/1000 + " s");
		return result;
	}

}
