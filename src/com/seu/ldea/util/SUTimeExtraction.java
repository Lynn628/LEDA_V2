package com.seu.ldea.util;

import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.apache.jena.n3.RelativeURIException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CacheMap;
import edu.stanford.nlp.util.CoreMap;

public class SUTimeExtraction {
	
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
	
	public static String getTimeInLiteral(List<CoreMap> list, String uri) {
	    double maxPercentage = 0;
	    String timeInfo = "";
	    String result = "";
		// 将识别出来的时间信息与当前谓语以<p, timeSpan>与资源绑定
		for (CoreMap cm : list) {
			double percentage = cm.size()/(uri.length()*1.0);
			if(maxPercentage < percentage){
				maxPercentage = percentage;
				timeInfo = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
			}
		}
		//设置阈值为0.6
		if(maxPercentage >0.6){
			result = timeInfo;
			
		}
		System.out.println("%----- " + maxPercentage);
		return result;
           
}
	
	public static void main(String[] args){
		AnnotationPipeline pipeline = PipeInit();
		//http://data.semanticweb.org/conference/xperience/2012  XML Summer School 2015
		//String text = "In order to make SPARQL queries more accessible to users, we have developed the visual query language QueryVOWL. It defines SPARQL mappings for graphical elements of the ontology visualization VOWL. In this demo, we present a web-based prototype that supports the creation, modification, and evaluation of QueryVOWL graphs. Based on the selected SPARQL endpoint, it provides suggestions for extending the query, and retrieves IRIs and literals according to the selections in the QueryVOWL graph. In contrast to related work, SPARQL queries can be created entirely with visual elements.";
		String text = "2012-05-30";
		//String text = URIUtil.processURI(uri);
		System.out.println(text);
		List<CoreMap> timexAnnsAll = SUTimeJudgeFunc(pipeline, text);
		double maxPercentage = 0;
		for(CoreMap cm : timexAnnsAll){
			System.out.println("cm size " + cm.size() +" " + cm.toString().length() + " text length " + text.length());
			double percentage = cm.size()/(text.length()*1.0);
			if(maxPercentage < percentage){
				maxPercentage = percentage;
			}
		  // if(maxPercentage > 0.6){
			List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
			System.out.println(cm  + "[from char offset" + 
			          tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + 
			          " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
			          "-->" + cm.get(TimeExpression.Annotation.class).getTemporal() );
			System.out.println(" -- ");
		    System.out.println(SUTimeExtraction.SUTimeJudgeFunc(pipeline, text).size());
		   }
		System.out.println(getTimeInLiteral(timexAnnsAll, text));
	//}
	}
	
}
	
	

