package com.seu.ldea.util;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.util.CoreMap;

public class SUTimeTool2 {
	
	/**
	 * Initialize the annotationPipeline
	 * @return
	 */
	public static AnnotationPipeline PipeInit(){
		Properties properties = new Properties();
		AnnotationPipeline pipeline = new AnnotationPipeline();
		pipeline.addAnnotator(new TokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		pipeline.addAnnotator(new TimeAnnotator("sutime", properties));
		return pipeline;
	}
	
	/**
	 * Judge whether the text has time information
	 * @param pipeline
	 * @param text
	 * @return
	 */
	public static List<CoreMap> SUTimeJudgeFunc(AnnotationPipeline pipeline, String text){
		//create annotation with text 
		Annotation annotation = new Annotation(text);
		annotation.set(CoreAnnotations.DocDateAnnotation.class, "2017-03-02");
		//annotate text with pipeline, pipeline的annotate(Annotation annotation),run the pipeline on an input annotation
		pipeline.annotate(annotation);
	    
		List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
	    return timexAnnsAll;
	    
		/*if(timexAnnsAll.size() == 0){
	    	return false;
	    }else{
	    	*/
	    	
	    	//加一个排除概念层含有时间信息的消息（或者说是含有时间信息的命名空间）
		/*  for(CoreMap cm : timexAnnsAll){
			List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
			System.out.println(cm + "[from char offset" + 
			          tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + 
			          " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
			          "-->" + cm.get(TimeExpression.Annotation.class).getTemporal());
			System.out.println(" -- ");
		 }
			return true*/
	     }
	    }
	

