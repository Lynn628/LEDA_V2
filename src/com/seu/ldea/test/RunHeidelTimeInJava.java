package com.seu.ldea.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.seu.ldea.timeutil.URIUtil;

import de.unihd.dbs.heideltime.standalone.*;
import de.unihd.dbs.heideltime.standalone.components.impl.XMIResultFormatter;
import de.unihd.dbs.heideltime.standalone.exceptions.*;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;

public class RunHeidelTimeInJava {
	public static void main(String[] args) throws DocumentCreationTimeMissingException, ParseException {
		// some parameters
		OutputType outtype = OutputType.TIMEML;
		POSTagger postagger = POSTagger.TREETAGGER;
		// or: faster, but worse results; no TreeTagger required
		// POSTagger postagger = POSTagger.NO;
		String conffile = "lib/config.props";
		// initialize HeidelTime for English news
		HeidelTimeStandalone hsNews = new HeidelTimeStandalone(Language.ENGLISH, DocumentType.NEWS, outtype, conffile,
				postagger);
		// initialize HeidelTime for English narrative
		HeidelTimeStandalone hsNarratives = new HeidelTimeStandalone(Language.ENGLISH, DocumentType.NARRATIVES, outtype,
				conffile, postagger);
		// process English narratives
		String narrativeText = "This is a text with a date in English: " + "January 24, 2009 and also two weeks later.";
		//String narrativeText = "c035f026a8f23ff0406c877256fcdb0565f1fab4";
		String narrtiveTextProcessed = URIUtil.processURI(narrativeText);
		System.err.println("NARRATIVE*****");
		// process English news (after handling DCT)
		String dctString = "2016-04-29";
		
		String newsText = "Today, I write a text with a date in English: "
				+ "January 24, 2009 and also two weeks later. But what was two weeks ago?";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date dct = df.parse(dctString);
		
		//Date XMIResultFormatter;
		//String xmiNarrativeOutput = hsNarratives.process(narrtiveTextProcessed,dct);
		//System.out.println(xmiNarrativeOutput);
		String xmiNewsOutput = hsNews.process(newsText, dct);
		//hsNews.process(newsText, XMIResultFormatter);
		System.err.println("NEWS*******");
		System.out.println(xmiNewsOutput);
	}
}
