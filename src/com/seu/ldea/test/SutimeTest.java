package com.seu.ldea.test;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class SutimeTest {
	 public static void main(String[] args) {
		    Properties props = new Properties();
		    AnnotationPipeline pipeline = new AnnotationPipeline();
		    pipeline.addAnnotator(new TokenizerAnnotator(true));
		    pipeline.addAnnotator(new WordsToSentencesAnnotator(true));
		    pipeline.addAnnotator(new POSTaggerAnnotator(true));
		    pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	        String[] aStrings = {"http://data.semanticweb.org/conference/iswc/2011", "2011-10-24T18:00:00+02:00^^http://www.w3.org/2001/XMLSchema#dateTime",
	        		"Three interesting dates are 18 Feb 1997, the 20th of july and 4 days from today.", "Three interesting dates are 18 Feb 1999, the 20th of july and 4 days from today."};
		    //char[] aString = "Three interesting dates are 18 Feb 1997, the 20th of july and 4 days from today.".toCharArray();
		    for (String text : aStrings) {
		    	//Annotation继承ArrayCoreMap方法，可调用其set，get方法
		      Annotation annotation = new Annotation(text);
		      //ArrayCoreMap的set方法set(Class<? extends TypesafeMap.Key<VALUE>> key, VALUE value)
		      //用来Associates the given value with the given type for future calls to get.
		    annotation.set(CoreAnnotations.DocDateAnnotation.class, "2013-07-14");
		    //  annotation.set(CoreAnnotations.SentencesAnnotation.class,"2017-06-15" );
		      pipeline.annotate(annotation);
		     
		      //get(Class<? extends TypesafeMap.Key<VALUE>> key)
		      //Returns the value associated with the given key or null if none is provided.
		      //获取文本处理结果
		      
		      List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
		      if(timexAnnsAll.isEmpty()){
		    	  System.out.println("No time information\n\n");
		      }else{
		       System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
		      for (CoreMap cm : timexAnnsAll) {
		    	  
		    	// traversing the words in the current sentence  
	            // a CoreLabel is a CoreMap with additional token-specific methods 
		        List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
		      /*  System.out.println(cm + " [from char offset " + tokens.get(0).get(CoreAnnotations.TokensAnnotation.class)+ " **** " +
		            tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
		            " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
		            " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());
		       }*/
		        //cm 指的是原句中识别出的字符串，
		        System.out.println(cm + " * " + tokens.toString() + " * " + cm.get(TimeExpression.Annotation.class).getTemporal());
		      System.out.println("--");
		     }
		    }
		   }
		  }
	 }	

