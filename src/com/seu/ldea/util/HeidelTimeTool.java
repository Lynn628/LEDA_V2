package com.seu.ldea.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;

public class HeidelTimeTool {
   public static void main(String[] args) throws DocumentCreationTimeMissingException, IOException{
	   String conffile = "lib/config.props";
	   HeidelTimeStandalone heidelTime = new HeidelTimeStandalone(Language.ENGLISH, DocumentType.COLLOQUIAL, OutputType.TIMEML,conffile, POSTagger.TREETAGGER);
	   String string = "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\DataSet\\BTChallenge2014\\data0.nq";
	 //  String aString = URIUtil.processURI(string);
	 //  System.out.println(aString);
	   FileReader fileReader = new FileReader(new File("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\heideltime-standalone\\test.txt"));
	   BufferedReader bufferedReader = new BufferedReader(fileReader);
	   String line = "Today Lynn test heideltime with treeTagger at 2017/6/15 ,find it's use day after tomorrow.";
	   String result = heidelTime.process(line);
	   System.out.println(result);
	  /* while((line = bufferedReader.readLine()) != null){
		   System.out.println(line + "****");
		   String result = heidelTime.process(line);
		   System.out.println(result);
	   }
	   bufferedReader.close();*/
	 //  System.out.println(result);
	  /* HeidelTime heidelTime = new HeidelTime(Language.UNSPECIFIED_LANGUAGE,
               DocumentType.COLLOQUIAL,
               OutputType.TIMEML,
               "path/to/config.props",
               POSTagger.TREETAGGER, true);;*/
   }
   
   // heideltime/conf/config.props
    /**
     * 解析Timex3中的值
     * 
     */
 
   
}
