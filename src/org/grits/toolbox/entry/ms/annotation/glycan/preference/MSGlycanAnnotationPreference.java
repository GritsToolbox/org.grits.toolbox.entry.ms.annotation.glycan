package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.ms.file.reader.IMSFileReader;
import org.grits.toolbox.ms.om.data.Method;


@XmlRootElement(name = "msGlycanAnnotationPreference")
public class MSGlycanAnnotationPreference {
	public final static String PPM = "ppm";
	public final static String DALTON = "Dalton";
	
	String name;
	Method method;

	@XmlAttribute
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public String getMassType() {
		if (method.getMonoisotopic())
			return "Monoisotopic";
		else return "Average";
	}

	public Double getPrecursorTol() {
		return method.getAccuracy();
	}
	
	public boolean isPrecursorTolIsPPM() {
		return method.getAccuracyPpm();
	}
	
	public Double getFragmentTol() {
		return method.getFragAccuracy();
	}
	
	public boolean isFragmentTolIsPPM() {
		return method.getFragAccuracyPpm();
	}
	
	public Double getIntensityCutoff() {
		return method.getIntensityCutoff();
	}
	
	public boolean isIntensityCutoffIsPercentage() {
		return method.getIntensityCutoffType().equals(IMSFileReader.FILTER_PERCENTAGE);
	}
	

	public Double getPrecursorIntensityCutoff() {
		return method.getPrecursorIntensityCutoff();
	}
	

	public boolean isPrecursorIntensityCutoffIsPercentage() {
		return method.getPrecursorIntensityCutoffType().equals(IMSFileReader.FILTER_PERCENTAGE);
	}
	
	public boolean isTrustCharge() {
		return method.getTrustMzCharge();
	}
	
	
}
