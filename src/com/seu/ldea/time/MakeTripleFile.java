package com.seu.ldea.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

import org.apache.jena.sparql.function.library.now;

import com.seu.ldea.timeutil.URIUtil;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.HeidelTimeAnnotator;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.SUTimeMain;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;


/**
 * 解析rdf文件,生成三元组文件
 * 评估思想，先评估识别URI + Literal 中时间信息的准确率，再加上限制条件，抽取出有效的时间信息
 * 
 * @author Lynn
 *
 */
public class MakeTripleFile {

//	public static void main(String[] args) throws IOException{
		
		/*StanfordCoreNLP pipe = new StanfordCoreNLP();
		Scanner scanner = new Scanner(System.in);
		String filePath = scanner.nextLine();
		System.out.println("Please give name to dst file 1 ");
		String name1 = scanner.nextLine();
		//System.out.println("Please give name to dst file 2");
		//String name2 = scanner.nextLine();
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath);
	    StmtIterator iterator =  model.listStatements();
	    FileWriter fw1 = new FileWriter(new File("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\" + name1));
	   // FileWriter fw2 = new FileWriter(new File("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\" + name2));
	    BufferedWriter bw1 = new BufferedWriter(fw1);
	 //   BufferedWriter bw2 = new BufferedWriter(fw1);
	    int i = 0;
	    while(iterator.hasNext()){
	    	i++ ;
		   Statement statement = iterator.next();
		   Resource subject = statement.getSubject();
		   Property predicate = statement.getPredicate();
		   RDFNode object = statement.getObject();
		   String processedSub = URIUtil.processURI2(subject.toString());
		   if(object.isLiteral()){
		   bw1.write(i + " : " + processedSub + "  "  + object.toString());
		   bw1.newLine();
		   }else{
			String processedObj = URIUtil.processURI2(object.toString());
			bw1.write(i + " : " + processedSub + "  "  + processedObj);
		    bw1.newLine();
		   }
		   
		   
	   }
	    bw1.close();
	   // bw2.close();
	}*/
	
	//public static void tokenize(File inputFile) throws IOException{
	 public static void main(String[] args) throws IOException{
		 Scanner scanner = new Scanner(System.in);
		 String inputFile = scanner.nextLine();
		 System.out.println("Input result file name ");
		 String fileName = scanner.nextLine();
		 scanner.close();
	    FileReader fr = new FileReader(new File(inputFile));
		BufferedReader bufferedReader = new BufferedReader(fr);
		FileWriter fw = new FileWriter(new File("C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\" + fileName + ".txt"));
		BufferedWriter bw = new BufferedWriter(fw);
		String line = "";
		AnnotationPipeline pipeLine = new AnnotationPipeline();
		Properties props = new Properties();
		
	   pipeLine.addAnnotator(new TokenizerAnnotator(false));
       pipeLine.addAnnotator(new WordsToSentencesAnnotator(false));
	//	pipeLine.addAnnotator(new POSTaggerAnnotator(true));
		//pipeLine.addAnnotator(new HeidelTimeAnnotator());
		pipeLine.addAnnotator(new TimeAnnotator("sutime", props));
		int i = 0;
		while( (line = bufferedReader.readLine()) != null){
			i++;
			bw.write(i + " - " + line);
			String processLine = URIUtil.processURI(line);
			bw.newLine();
			bw.write(processLine);
			bw.newLine();
			Annotation annotation = new Annotation(processLine);
			annotation.set(CoreAnnotations.DocDateAnnotation.class, "2017-6-14");
			pipeLine.annotate(annotation);
			bw.write("*" + annotation.get(TextAnnotation.class));
			bw.newLine();
			bw.write("**" + annotation.get(TokensAnnotation.class));
			bw.newLine();
			bw.write("Recognized time info in sentence " + annotation.get(TimeAnnotations.TimexAnnotations.class));
		    bw.newLine();
			Iterator<CoreMap>  iterator = annotation.get(TimeAnnotations.TimexAnnotations.class).iterator();
	    while(iterator.hasNext()){
	    	CoreMap coreMap = (CoreMap) iterator.next();
	    	bw.write("Core map(time recognized) is " + coreMap.toString() + "     ");
	    	
	    	//System.out.println("Get matched text " + coreMap.get(TimeExpression.Annotation.class).getText());
	    	bw.write("Get temporal " + coreMap.get(TimeExpression.Annotation.class).getTemporal() + "  ");
	    }
	    bw.newLine();
	    bw.write("--");
	    bw.newLine();
	    //SUTimeMain.processText(pipeLine, line, "C:\\Users\\Lynn\\Desktop\\Academic\\LinkedDataProject\\TimeExtractionResultFile\\timetoolEstimation\\test-out.txt", "2017-6-16");
		}
		bufferedReader.close();
	}
}
