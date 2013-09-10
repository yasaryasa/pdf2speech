package com.yaser.pdf2speech.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.yaser.pdf2speech.util.SpeechConstants;


public final class PDFOperations {

	public static int pageNumer = 1;
	public static int sentenceStart = 0;
	public static int sentenceEnd = 0;
	public static String currentSentence = null;
	public static String currentPageContent = null;
	private static BreakIterator breakIterator;
	public static PdfReader reader;
	private static PdfTextExtractor textExtractor;
	public static int numberOfPages = 0;
	
	private PDFOperations() {}
	
	
	public static PdfReader setupPDF(String fileName) {
		try {
			reader = new PdfReader(fileName);
			numberOfPages  = reader.getNumberOfPages(); 
			textExtractor = new PdfTextExtractor(reader);
			currentPageContent = textExtractor.getTextFromPage(pageNumer);
			breakIterator = BreakIterator.getSentenceInstance();
			breakIterator.setText(currentPageContent.trim());
			pageNumer++;
		} catch (Exception e) {
			currentPageContent = e.getMessage();
		}
		return reader;
	}
	
	public static void setNextPage() {
		try {
			currentPageContent = textExtractor.getTextFromPage(pageNumer);
			breakIterator = BreakIterator.getSentenceInstance();
			breakIterator.setText(currentPageContent);
			pageNumer++;
		} catch (IOException e) {
			currentPageContent = e.getMessage();
		}
	}
	
	public static String getNextPage() {
		try {
			currentPageContent = textExtractor.getTextFromPage(pageNumer);
			breakIterator = BreakIterator.getSentenceInstance();
			breakIterator.setText(currentPageContent);
			pageNumer++;
		} catch (IOException e) {
			currentPageContent = e.getMessage();
		}
		return currentPageContent;
	}
	
	
	public static String nextSentence() {
		int current = breakIterator.current();
		int end = breakIterator.next();
		if (end == -1) {
			return SpeechConstants.END_OF_PAGE;
		}
		currentSentence = currentPageContent.substring(current, end);
		return currentSentence;
	}
	
	public static String prevSentence() {
		int current = breakIterator.current();
		int previous = breakIterator.previous();
		if (previous == -1) {
			return null;
		}
		currentSentence = currentPageContent.substring(previous, current);
		return currentSentence;
	}


	public static void setupPDF(InputStream open) {
		try {
			reader = new PdfReader(open);
			textExtractor = new PdfTextExtractor(reader);
			numberOfPages  = reader.getNumberOfPages();
			currentPageContent = textExtractor.getTextFromPage(1);
			breakIterator = BreakIterator.getSentenceInstance();
			breakIterator.setText(currentPageContent.trim());
			pageNumer++;
		} catch (IOException e) {
			currentPageContent = e.getMessage();
		}
	}
}
