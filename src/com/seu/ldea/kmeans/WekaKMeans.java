package com.seu.ldea.kmeans;

import java.io.File;

import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class WekaKMeans {
	public static int[] wekaKMeans(String filePath) throws Exception{  
        Instances ins = null;  
          
        SimpleKMeans KM = null;  
        DistanceFunction disFun = null;  
          
        try {  
            // ������������  
            File file = new File(filePath);  
            ArffLoader loader = new ArffLoader();  
            loader.setFile(file);  
            ins = loader.getDataSet();  
              
            // ��ʼ�������� �������㷨��  
            KM = new SimpleKMeans();  
            KM.setPreserveInstancesOrder(true);
            KM.setNumClusters(5);       //���þ���Ҫ�õ����������  
            KM.buildClusterer(ins);
           //��ʼ���о���  
         //   System.out.println(KM.preserveInstancesOrderTipText());  
            // ��ӡ������  
         //   System.out.println(KM.toString());  
              
        } catch(Exception e) {  
            e.printStackTrace();  
        }
        return KM.getAssignments();
    }  
	
	
	public static void main(String[] args) throws Exception{
		String path = "D:\\0-kmeansInput.txt";
		wekaKMeans(path);
	}
}  

