package com.seu.ldea.test;

import java.math.BigDecimal;

public class tauTest {
  public static void main(String[] args){
	  int N= 244036;
	  BigDecimal N2 = BigDecimal.valueOf(-576216884);
	  BigDecimal distance = new BigDecimal("156412314");
	  long distance2 = 156412314;
	//  BigDecimal result = N2.multiply(N2.subtract(BigDecimal.valueOf(1)));
	 /* BigDecimal nBigDecimal = new BigDecimal(N * (N - 1) );
     System.out.println(nBigDecimal + " " + N * (N - 1) );
     System.out.println("Result " + result);
     BigDecimal denominator = bigN.multiply(bigN.subtract(BigDecimal.valueOf(1)));
		System.out.println("denominator " + denominator);*/
		// BigDecimal kendellTau =
		// BigDecimal.valueOf(1.0).subtract(distance.multiply(BigDecimal.valueOf(4).divide(nBigDecimal,
		// 5, 4)));
	    BigDecimal denominator = new BigDecimal(N * (N - 1) );
		BigDecimal one = new BigDecimal(1.0);
		BigDecimal nominator = distance.multiply(BigDecimal.valueOf(2));
		//BigDecimal inconsist = distance.multiply(BigDecimal.valueOf(4));
		// BigDecimal divide = mutiple.divide(nBigDecimal, 5, 4);
		BigDecimal kendellTau = one.subtract(nominator.divide(denominator, 5, 4));
		
		int denominator2 = N * (N - 1);
		//System.out.println("denominator " + denominator2);
		//这个地方的系数到底是2还是4？
		long nominator2 = distance2 * 2;
		double kendellTau2 = 1 - (nominator2 / (denominator2 * 1.0));
		
		System.out.println(kendellTau + " \n  " + kendellTau2);
  }
}
