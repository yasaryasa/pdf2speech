package com.yaser.pdf2speech.pdf;

import android.app.ProgressDialog;

public class PDFExtractor extends Thread{

	ProgressDialog pDialog;
	String filePath;
	
	public PDFExtractor(ProgressDialog pDialog, String filePath) {
		this.pDialog = pDialog;
		this.filePath = filePath;
	}
	
	@Override
	public void run() {
		PDFOperations.setupPDF(filePath);
		pDialog.cancel();
	}
	
}
