package com.seu.ldea.history;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class SUTimeTool {
	
	public static void SUTimeFunc(String text) {
	    Properties props = new Properties();
	    AnnotationPipeline pipeline = new AnnotationPipeline();
	    pipeline.addAnnotator(new TokenizerAnnotator(false));
	    pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
	    pipeline.addAnnotator(new POSTaggerAnnotator(false));
	    pipeline.addAnnotator(new TimeAnnotator("sutime", props));
        //String[] aStrings = {"Three interesting dates are 18 Feb 1997, the 20th of july and 4 days from today.", "Three interesting dates are 18 Feb 1999, the 20th of july and 4 days from today."};
	    //char[] aString = "Three interesting dates are 18 Feb 1997, the 20th of july and 4 days from today.".toCharArray();	   
	      Annotation annotation = new Annotation(text);
	      annotation.set(CoreAnnotations.DocDateAnnotation.class, "2013-07-14");
	      pipeline.annotate(annotation);
	      //System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
	      List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
	      for (CoreMap cm : timexAnnsAll) {
	        List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
	        System.out.println(cm + " [from char offset " +
	            tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
	            " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
	            " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());  
	      System.out.println("--");
	    }
	  }
	
	public static boolean SUTimeJudgeFunc(String text){
		
		if(text.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")){
    		return false;
    	}else{
		Properties props = new Properties();
		//AnnotationPipeline是多个annotator的整合，本身就是一个annotator
		AnnotationPipeline pipeline = new AnnotationPipeline();
		pipeline.addAnnotator(new TokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		pipeline.addAnnotator(new POSTaggerAnnotator(false));
		pipeline.addAnnotator(new TimeAnnotator("sutime", props));
		//create annotation with text 
		Annotation annotation = new Annotation(text);
		annotation.set(CoreAnnotations.DocDateAnnotation.class, "2017-03-02");
		//annotate text with pipeline, pipeline的annotate(Annotation annotation),run the pipeline on an input annotation
		pipeline.annotate(annotation);
		
		List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
	    if(timexAnnsAll.size() == 0){
	    	return false;
	    }else{
	    	//加一个排除概念层含有时间信息的消息（或者说是含有时间信息的命名空间）
		  for(CoreMap cm : timexAnnsAll){
			List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
			System.out.println(cm + "[from char offset" + 
			          tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + 
			          " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
			          "-->" + cm.get(TimeExpression.Annotation.class).getTemporal());
			System.out.println(" -- ");
		 }
			return true;
	     }
	    }
	}
}
