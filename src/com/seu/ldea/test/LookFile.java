package com.seu.ldea.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LookFile {

	public static void main(String[] args) throws IOException{
		String path1 = "E:\\Dbp60-latent50.embeddings.txt";
		String path2 = "E:\\TopK60-latent50.embeddings.txt";
		FileReader fr1 = new FileReader(path1);
		FileReader fr2 = new FileReader(path2);
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = new BufferedReader(fr2);
		int i = 0;
		 while(i < 30){
			 i++;
			 System.out.println(br1.readLine());
			 System.out.println(br2.readLine() + " \n");
		 }
		 br1.close();
		 br2.close();
	}
}
