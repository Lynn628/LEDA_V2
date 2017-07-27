package com.seu.ldea.tau;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * ����tauֵѡ��Cosine-2������������Ч���Ϻ�
 * @author Lynn �����־������tauֵ����
 */
public class TauCalculationBigDecimal {
	private  long counter = 0;
	// �������kendall tauֵ
	public  BigDecimal calculateTau(int[] standard, int[] comparison) {

		if (standard.length != comparison.length) {
			throw new IllegalArgumentException("Array dimensions is not same");
		}
		BigDecimal bigN = new BigDecimal(String.valueOf(standard.length));
		int N = bigN.intValue();
		int[] aIndex = new int[N];// ��¼a���������
		for (int i = 0; i < N; i++) {
			aIndex[standard[i]] = i;
		}
		int[] bIndex = new int[N];// b��������a���������
		for (int i = 0; i < N; i++) {
			bIndex[i] = aIndex[comparison[i]];
		}
		//BigDecimal distance = insertionCount(bIndex);
		BigDecimal distance = mergeCount(bIndex);
		System.out.println("distance is " + distance + " N is " + N + "bigN" + bigN);
		// Kendell correlation co-efficient
		// BigDecimal nBigDecimal = new BigDecimal(N * (N - 1) );
		BigDecimal denominator = bigN.multiply(bigN.subtract(BigDecimal.valueOf(1)));
		System.out.println("denominator " + denominator);
		
		BigDecimal one = new BigDecimal(1.0);
		BigDecimal nominator = distance.multiply(BigDecimal.valueOf(2));
		//BigDecimal inconsist = distance.multiply(BigDecimal.valueOf(4));
		// BigDecimal divide = mutiple.divide(nBigDecimal, 5, 4);
		BigDecimal kendellTau = one.subtract(nominator.divide(denominator, 5, 4));
		System.out.println("Tau bigDecimal " + kendellTau);
		return kendellTau;
	}

	// ʹ�ò������򷽷���������
	public  BigDecimal insertionCount(int[] a) {
		BigDecimal counter = new BigDecimal(0);
		for (int i = 1; i < a.length; i++) {
			for (int j = i; j > 0 && a[j] < a[j - 1]; j--) {
				int temp = a[j];
				a[j] = a[j - 1];
				a[j - 1] = temp;
				counter = counter.add(BigDecimal.valueOf(1));
				// System.out.println(counter);// ��������ÿ����һ�Σ��ʹ���һ��������
			}
		}

		return counter;
	}
	
	
	// ʹ�ù鲢���򷽷���������
    private  int[] aux;

    public  BigDecimal mergeCount(int[] a) {
        aux = new int[a.length];
        mergeSort(a, 0, a.length-1);
        return new BigDecimal(counter);
    }

    
    private  void mergeSort(int[] a, int lo, int hi) {
        if (hi <= lo) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(a, lo, mid);
        mergeSort(a, mid + 1, hi);
        merge(a, lo, mid, hi);
    }

    public  void merge(int[] a, int lo, int mid, int hi) {
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = aux[j++];
            } else if (j > hi) {
                a[k] = aux[i++];
            } else if (aux[j] < aux[i]) {
                a[k] = aux[j++];
                counter += mid - i + 1;// ÿ����ǰ������С�ĺ�������Ԫ�أ�������Ϊǰ���������еĳ���
            } else {
                a[k] = aux[i++];
            }
        }
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
		// �ֽ�õľ���
		String rescalEmbending = "D:\\RESCAL\\Ext-RESCAL-master\\Ext-RESCAL-master\\" + fileName + "-latent"
				+ latentNumber + "-lambda" + lambdaNumber + ".embeddings.txt";
		// RESCAL�����Ķ���������
		BigDecimal[][] matrix1 = TauRescalDistanceBigDecimal.getVectorDistanceMatrix(rescalEmbending, method);
	    TauRescalDistanceBigDecimal.printMatrix(matrix1);
		System.out.println(matrix1.length );
		// ��������������
		ArrayList<Entry<Integer, BigDecimal>> rescalDistanceList = TauRescalDistanceBigDecimal.sortVectorDistance(matrix1,
				"BigDecimal");
		String entityFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\" + fileName + "\\entity-ids";
		String tripleFile = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\rescalInput\\" + fileName + "\\triple";
		// ��׼����
		int[][] matrix2 = StandardDistance.getMatrix(entityFile, tripleFile);
		int[][] shortF = StandardDistance.floydDistance(matrix2);
		// ��׼��������
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
		System.out.println(new TauCalculationBigDecimal().calculateTau(standard, comparison));
		long t2 = System.currentTimeMillis();
		System.out.println("Calculation time cost " + (t2 - t1) / 60000.0);
		System.out.println("********************************");

	}
//Cosine-abs-square, Euclidean, Cosine-2
}
