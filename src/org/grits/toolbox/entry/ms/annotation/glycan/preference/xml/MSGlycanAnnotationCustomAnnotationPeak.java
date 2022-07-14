package org.grits.toolbox.entry.ms.annotation.glycan.preference.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;

@XmlRootElement(name = "msGlycanAnnotationCustomAnnotationPeak")
public class MSGlycanAnnotationCustomAnnotationPeak extends MassSpecCustomAnnotationPeak {
	private String sPeakSequence = null;
	
	public MSGlycanAnnotationCustomAnnotationPeak() {
		// TODO Auto-generated constructor stub
	}
	
	public String getPeakSequence() {
		return sPeakSequence;
	}
	@XmlElement(name="peakSequence")
	public void setPeakSequence(String sPeakSequence) {
		this.sPeakSequence = sPeakSequence;
	}
		
}
