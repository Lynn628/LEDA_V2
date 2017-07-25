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
 * 获取rescal两点之间的距离
 * 
 * @author Lynn
 *
 */
public class RescalDistanceTest {

	/**
	 * 
	 * @param filePath：Rescal
	 *            embedding文件
	 *
	 * @param method:距离计算方法：Euclidean或者Cosine
	 * @return 相应实体向量之间的距离
	 */
	public static HashMap<Integer, HashMap<Integer, BigDecimal>> calcVectorDistance(String filePath, String method) {
		System.out.println("-------begin of vector distance calculation-------");
		long t1 = System.currentTimeMillis();
		//
		ArrayList<BigDecimal[]> entityVectors = new ArrayList<>();
		// 有多少个Entity就有多少维
		int dimension = 0;
		// 隐变量的个数
		int latentNum = 0;
		//
		HashMap<Integer, HashMap<Integer, BigDecimal>> result = new HashMap<>();
		try {
			FileReader fileReader = new FileReader(new File(filePath));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// 存储所有entity以及对应的分解的出的rank维的向量<id, int[]>
			String currenttLine = "";
			// 读取每一行分解出的向量
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
			// 内层迭代器定位到指定的位置
			Iterator<BigDecimal[]> innerIterator = entityVectors.iterator();
			for (int i = 0; i < iterNum - 1; i++) {
				innerIterator.next();
			}
			// 计算剩余所有id与当前id之间的距离
			while (innerIterator.hasNext()) {
				BigDecimal[] innerVector = innerIterator.next();
				// System.out.println(innerVector.toString());
				int innerId = entityVectors.indexOf(innerVector);
				if (method.equals("Euclidean")) {
					BigDecimal sum = new BigDecimal(0);
					// 用平方代替距离
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
					// denominator-分母，此处分母为cosine公式分母的平方
					BigDecimal denominator = f1.multiply(f2);
					//System.out.println(">>>>>>>>>>>>" + f1);
					// 距离计算选用Cosine的平方
					if (method.equals("Cosine-square")) {
						// 此处分子为cosine公式分子的平方
						BigDecimal distance = (numerator.multiply(numerator)).divide(denominator, 10, 4);
						
						outerToinner.put(innerId, distance);
						if(!result.containsKey(innerId)){
						HashMap<Integer, BigDecimal> innerToOuter = new HashMap<>();
					     innerToOuter.put(outerId, distance);
					     result.put(innerId, innerToOuter);
						}else{
							result.get(innerId).put(outerId, distance);
						}
						
						// 距离计算选用cosine计算公式的分子除以分母的平方
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
						// 距离计算选用cosine计算公式的分子绝对值除以分母的平方
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
