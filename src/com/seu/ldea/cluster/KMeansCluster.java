package com.seu.ldea.cluster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class KMeansCluster {
	private static double[][] DATA = { { 5.1, 3.5, 1.4, 0.2},  
            { 4.9, 3.0, 1.4, 0.2 },{ 4.7, 3.2, 1.3, 0.2 },  
            { 4.6, 3.1, 1.5, 0.2 },{ 5.0, 3.6, 1.4, 0.2 },  
            { 7.0, 3.2, 4.7, 1.4 },{ 6.4, 3.2, 4.5, 1.5 },  
            { 6.9, 3.1, 4.9, 1.5 },{ 5.5, 2.3, 4.0, 1.3 },   
            { 6.5, 2.8, 4.6, 1.5 },{ 5.7, 2.8, 4.5, 1.3 },  
            { 6.5, 3.0, 5.8, 2.2 },{ 7.6, 3.0, 6.6, 2.1 },  
            { 4.9, 2.5, 4.5, 1.7 },{ 7.3, 2.9, 6.3, 1.8 },   
            { 6.7, 2.5, 5.8, 1.8 },{ 6.9, 3.1, 5.1, 2.3 } };  
    public int k;//k�����ĵ�  
    public int[] memberShip;  
    public int[] centersIndex;  
    public double[][] centers;  
    public int[] elementsInCenters;  
      
      
    public static void main(String[] args) {  
        KMeansCluster kmeans = new KMeansCluster(5);  
        String lastMembership = "";  
        String nowMembership = "";  
        int i=0;  
        while(true){  
            i++;  
            kmeans.randomCenters();  
            System.out.println("���ѡ�е�����indexΪ��"+Arrays.toString(kmeans.centersIndex));  
            kmeans.calMemberShip();  
            nowMembership = Arrays.toString(kmeans.memberShip);  
            System.out.println("DATA����֮��MembershipΪ��"+nowMembership);  
            System.out.println("Elements in centers cnt:"+Arrays.toString(kmeans.elementsInCenters));  
            if(nowMembership.equals(lastMembership)){  
                System.out.println("���ξ������ϴ���ͬ���˳�ִ�У�");  
                System.out.println("һ�������� "+i+" �Σ�");  
                kmeans.calNewCenters();  
                System.out.println("�����ĵ�Ϊ��"+Arrays.deepToString(kmeans.centers));  
                double totalDistance = kmeans.computeTotalDistance();  
                System.out.println("totalDistance �� "+totalDistance);  
                break;  
            }else{  
                lastMembership = nowMembership;  
            }  
            System.out.println("----------------�����ķָ���----------------");  
        }  
    }  
      
    public KMeansCluster(int k){  
        this.k = k;  
    }  
  
    //�����ٽ�����  
    public double manhattanDistince(double []paraFirstData,double []paraSecondData){  
        double tempDistince = 0;  
        if((paraFirstData!=null && paraSecondData!=null) && paraFirstData.length==paraSecondData.length){  
            for(int i=0;i<paraFirstData.length;i++){  
                tempDistince += Math.abs(paraFirstData[i] - paraSecondData[i]);  
            }  
        }else{  
            System.out.println("firstData �� secondData ���ݽṹ��һ��");  
        }  
        return tempDistince;  
    }  
      
    public void randomCenters(){  
        centersIndex = new int[k];  
        Random random = new Random();  
        Map map = new HashMap();  
        for(int i=0;i<k;i++){  
            int index = Math.abs(random.nextInt())%DATA.length;  
            if(map.containsKey(index)){  
                i--;  
            }else{  
                //�����ĵ���±�浽MAP�У���֤�´�ѡ�������ĵ㲻��ͬһ��  
                map.put(index, DATA[index]);  
                //�����ĵ���±����centers[]��  
                centersIndex[i] = index;  
            }  
        }  
    }  
      
    public void calMemberShip(){  
        memberShip = new int[DATA.length];  
        elementsInCenters = new int[k];  
        for(int j=0;j<DATA.length;j++){  
            double currentDistance = Double.MAX_VALUE;  
            int currentIndex = -1;  
            double[] item = DATA[j];  
            for(int i=0;i<k;i++){  
                //���ĵ�  
                double[] tempCentersValue = DATA[centersIndex[i]];  
                double distance = this.manhattanDistince(item, tempCentersValue);  
                if(distance<currentDistance){  
                    currentDistance = distance;  
                    currentIndex = i;  
                }  
            }  
            memberShip[j] = currentIndex;  
        }  
          
        for(int i=0;i<memberShip.length;i++){  
            elementsInCenters[memberShip[i]]++;  
        }  
    }  
      
    public void calNewCenters(){  
        centers = new double[k][DATA[0].length];  
        for(int i=0;i<memberShip.length;i++){  
            for(int j=0;j<DATA[0].length;j++){  
                centers[memberShip[i]][j] += DATA[i][j];  
            }  
        }  
          
        for(int i=0;i<centers.length;i++){  
            for(int j=0;j<DATA[0].length;j++){  
                centers[i][j] /= elementsInCenters[i];  
            }  
        }  
    }  
      
    public double computeTotalDistance() {  
        double tempTotal = 0;  
        for (int i = 0; i < DATA.length; i ++) {  
            tempTotal += manhattanDistince(DATA[i], centers[memberShip[i]]);  
        }  
        return tempTotal;  
    }  
}  

