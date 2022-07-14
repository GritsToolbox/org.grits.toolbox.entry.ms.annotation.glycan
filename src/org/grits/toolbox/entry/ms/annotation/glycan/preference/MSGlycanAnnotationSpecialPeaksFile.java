package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotation;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotationTemplate;
import org.grits.toolbox.entry.ms.preference.MassSpecCustomAnnotationFile;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationTemplate;

public class MSGlycanAnnotationSpecialPeaksFile extends MassSpecCustomAnnotationFile {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSpecialPeaksFile.class);
	public static final String[] HEADER_COLUMN_LABELS = new String[] {"Peak m/z", "MS Level", "Peak Label", "Glycan Sequence"};

	public static String[] getHeaderColumns() {
		return HEADER_COLUMN_LABELS;
	}


	public static boolean testFile( File f ) {
		BufferedReader reader;
		boolean bPass = false;
		try {
			reader = new BufferedReader(new FileReader(f));
			String header = reader.readLine();
			String[] toks = header.split(MassSpecCustomAnnotationFile.DELIMITER);
			if( toks[0].equalsIgnoreCase( MSGlycanAnnotationSpecialPeaksFile.HEADER_COLUMN_LABELS[0] ) ) {
				bPass = true;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return bPass;
	}

	public static boolean readFile(MassSpecCustomAnnotation msca, String sFilePath) {
		BufferedReader reader;
		boolean bPass = false;
		try {
			File f = new File( sFilePath );			
			reader = new BufferedReader(new FileReader(f));
			String sCurLine = reader.readLine();
			boolean hasHeader = MassSpecCustomAnnotationFile.hasHeaders(sCurLine);
			if( hasHeader ) {
				sCurLine = reader.readLine();
			} 
			if( msca.getAnnotatedPeaks() == null ) {
				msca.setAnnotatedPeaks(new HashMap<Double, MassSpecCustomAnnotationPeak>());
			}
			while( sCurLine != null ) {
				try {
					if( ! sCurLine.trim().equals("") ) { 
						String[] toks = sCurLine.split(MassSpecCustomAnnotationFile.DELIMITER);
						MSGlycanAnnotationCustomAnnotationPeak peak = new MSGlycanAnnotationCustomAnnotationPeak();
						Double dMz = Double.parseDouble(toks[0]);
						peak.setPeakMz(dMz);
						Integer iMSLevel = Integer.parseInt(toks[1]);
						peak.setMSLevel(iMSLevel);
						peak.setPeakLabel("");
						if( toks.length >= 3 && toks[2] != null) {
							String sLabel = toks[2].trim();
							peak.setPeakLabel(sLabel);
						}
						if( toks.length >= 4 && toks[3] != null) {
							String sSequence = toks[3].trim();
							peak.setPeakSequence(sSequence);
						}
						msca.getAnnotatedPeaks().put(peak.getPeakMz(), peak);
					}
				} catch( Exception e ) {
					logger.error(e.getMessage(), e);
				}
				sCurLine = reader.readLine();
			}
			reader.close();
			bPass = true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return bPass;		

	}

	public static boolean writeFile(MassSpecCustomAnnotation msca, String sFilePath) {
		BufferedWriter writer;
		boolean bPass = false;
		try {
			File f = new File( sFilePath );			
			writer = new BufferedWriter(new FileWriter(f));
			String sw = "";
			for( int i = 0; i < MSGlycanAnnotationSpecialPeaksFile.HEADER_COLUMN_LABELS.length; i++ ) {
				writer.write(sw);
				writer.write(MSGlycanAnnotationSpecialPeaksFile.HEADER_COLUMN_LABELS[i]);
				if( sw.equals("") ) {
					sw = MassSpecCustomAnnotationFile.DELIMITER;
				}
			}
			writer.write("\n");
			Object[] peaks = msca.getAnnotatedPeaks().values().toArray();
			for( int i = 0; i < peaks.length; i++ ) {
				MSGlycanAnnotationCustomAnnotationPeak peak = (MSGlycanAnnotationCustomAnnotationPeak) peaks[i];
				writer.write(Double.toString(peak.getPeakMz()));
				writer.write(MassSpecCustomAnnotationFile.DELIMITER);
				writer.write(Integer.toString(peak.getMSLevel()));
				writer.write(MassSpecCustomAnnotationFile.DELIMITER);
				writer.write(peak.getPeakLabel() == null ? "" : peak.getPeakLabel());
				writer.write(MassSpecCustomAnnotationFile.DELIMITER);
				writer.write(peak.getPeakSequence() == null ? "" : peak.getPeakSequence());
				writer.write("\n");
			}			
			writer.close();
			bPass = true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return bPass;		
	}

	public static MSGlycanAnnotationCustomAnnotation readXMLFile(String sFilePath) {
		BufferedReader reader;
		try {
			File f = new File( sFilePath );			
			reader = new BufferedReader(new FileReader(f));
			MSGlycanAnnotationCustomAnnotationTemplate template = MSGlycanAnnotationCustomAnnotationTemplate.unmarshalAnnotationTemplate(sFilePath);
			MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) template.copyToNewAnnotation();
			reader.close();
			return msca;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	public static boolean writeXMLFile(MassSpecCustomAnnotation msca, String sFilePath) {
		BufferedWriter writer;
		boolean bPass = false;
		try {
			File f = new File( sFilePath );			
			writer = new BufferedWriter(new FileWriter(f));
			MassSpecCustomAnnotationTemplate template = msca.copyToNewTemplate();
			String sXML = MassSpecCustomAnnotationTemplate.marshalAnnotatedTemplate(template);
			writer.write(sXML);
			writer.write(System.getProperty("line.separator"));
			writer.close();
			bPass = true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return bPass;		
	}


}
