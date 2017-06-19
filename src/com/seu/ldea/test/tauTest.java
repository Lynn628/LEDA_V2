package com.seu.ldea.test;

import java.math.BigDecimal;

public class tauTest {
  public static void main(String[] args){
	  int N= 244036;
	  BigDecimal N2 = BigDecimal.valueOf(-576216884);
	  BigDecimal result = N2.multiply(N2.subtract(BigDecimal.valueOf(1)));
	  BigDecimal nBigDecimal = new BigDecimal(N * (N - 1) );
     System.out.println(nBigDecimal + " " + N * (N - 1) );
     System.out.println("Result " + result);
 	/*	BigDecimal distance = new BigDecimal("199842445");
		
		BigDecimal nBigDecimal = new BigDecimal("855006840");
		System.out.println("N*(N-1)" + nBigDecimal);
		BigDecimal one = new BigDecimal(1.0);
		BigDecimal mutiple = distance.multiply(BigDecimal.valueOf(4));
		BigDecimal divide = mutiple.divide(nBigDecimal, 5, 4);
		BigDecimal kendellTau = one.subtract(mutiple.divide(nBigDecimal,5,4));
		System.out.println(one);
		System.out.println("distance " + distance);
		System.out.println("multiple " + mutiple);
		System.out.println("divide " + divide);*/
		//System.out.println(kendellTau);*/
		//printMatrix(matrix);
		//distance is 310065418 N is 32761
  }
}
