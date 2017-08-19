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
            // 读入样本数据  
            File file = new File(filePath);  
            ArffLoader loader = new ArffLoader();  
            loader.setFile(file);  
            ins = loader.getDataSet();  
              
            // 初始化聚类器 （加载算法）  
            KM = new SimpleKMeans();  
            KM.setPreserveInstancesOrder(true);
            KM.setNumClusters(5);       //设置聚类要得到的类别数量  
            KM.buildClusterer(ins);
           //开始进行聚类  
         //   System.out.println(KM.preserveInstancesOrderTipText());  
            // 打印聚类结果  
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

