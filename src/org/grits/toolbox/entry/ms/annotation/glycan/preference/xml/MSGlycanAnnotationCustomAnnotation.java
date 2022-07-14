package org.grits.toolbox.entry.ms.annotation.glycan.preference.xml;

import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationTemplate;

@XmlRootElement(name = "msGlycanAnnotationCustomAnnotation")
public class MSGlycanAnnotationCustomAnnotation extends MassSpecCustomAnnotation {
	

	@Override
	public MassSpecCustomAnnotationPeak getPeakObjectFromXML( String peakXML ) {
		MSGlycanAnnotationCustomAnnotationPeak peak = (MSGlycanAnnotationCustomAnnotationPeak) XMLUtils.getObjectFromXML(peakXML, MSGlycanAnnotationCustomAnnotationPeak.class);		
		return peak;
	}

	protected MassSpecCustomAnnotation getNewCustomAnnotationObject() {
		return new MSGlycanAnnotationCustomAnnotation();
	}

	@Override
	public Object clone() {
		MSGlycanAnnotationCustomAnnotation newAnnot = (MSGlycanAnnotationCustomAnnotation) super.clone();
		
		return newAnnot;
	}
	
	@Override
	public MassSpecCustomAnnotationTemplate getNewTemplate() {
		return new MSGlycanAnnotationCustomAnnotationTemplate();
	}

	@Override
	public MassSpecCustomAnnotationTemplate copyToNewTemplate() {
		// TODO Auto-generated method stub
		return super.copyToNewTemplate();
	}
}
