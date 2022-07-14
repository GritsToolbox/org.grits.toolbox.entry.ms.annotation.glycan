package org.grits.toolbox.entry.ms.annotation.glycan.preference.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationTemplate;

@XmlRootElement(name = "msGlycanAnnotationCustomAnnotation")
public class MSGlycanAnnotationCustomAnnotationTemplate extends MassSpecCustomAnnotationTemplate{
	private List<MSGlycanAnnotationCustomAnnotationPeak> annotatedPeaks = null;

	@Override
	public MassSpecCustomAnnotation getNewAnnotation() {
		return new MSGlycanAnnotationCustomAnnotation();
	}
	
	@Override
	public MassSpecCustomAnnotation copyToNewAnnotation() {
		return super.copyToNewAnnotation();
	}

	private List<MSGlycanAnnotationCustomAnnotationPeak> getMSGlycanAnnotationCustomAnnotationPeaks() {
		return annotatedPeaks;
	}
	@XmlElement(name = "msGlycanAnnotationCustomAnnotationPeaks")
	private void setMSGlycanAnnotationCustomAnnotationPeaks(List<MSGlycanAnnotationCustomAnnotationPeak> annotatedPeaks) {
		this.annotatedPeaks = annotatedPeaks;
	}
	
	private void setMSGlycanAnnotationCustomAnnotationPeaks( MSGlycanAnnotationCustomAnnotation annotation ) {
		annotation.setAnnotatedPeaks(new HashMap<>());
		for( MSGlycanAnnotationCustomAnnotationPeak peak : getMSGlycanAnnotationCustomAnnotationPeaks() ) {
			annotation.getAnnotatedPeaks().put(peak.getPeakMz(),  peak);
		}
	}
	
	@Override
	protected void setAnnotationPeaks( MassSpecCustomAnnotation annotation ) {
		setMSGlycanAnnotationCustomAnnotationPeaks( (MSGlycanAnnotationCustomAnnotation) annotation);
	}
	
	@Override
	protected void setTemplatePeaks( MassSpecCustomAnnotation annotation ) {
		setMSGlycanAnnotationCustomTemplatePeaks( (MSGlycanAnnotationCustomAnnotation) annotation);
	}
	
	
	protected void setMSGlycanAnnotationCustomTemplatePeaks( MSGlycanAnnotationCustomAnnotation annotation ) {
		setMSGlycanAnnotationCustomAnnotationPeaks(new ArrayList<>() );
		for( Double dMz : annotation.getAnnotatedPeaks().keySet() ) {
			MSGlycanAnnotationCustomAnnotationPeak peak = (MSGlycanAnnotationCustomAnnotationPeak) annotation.getAnnotatedPeaks().get(dMz);
			getMSGlycanAnnotationCustomAnnotationPeaks().add(peak);
		}
	}
	
	public MSGlycanAnnotationCustomAnnotation unmarshalAnnotatedPeaks( String xmlFile ) {
		MSGlycanAnnotationCustomAnnotation annotationList = (MSGlycanAnnotationCustomAnnotation) XMLUtils.unmarshalObjectXML(xmlFile, MSGlycanAnnotationCustomAnnotation.class);
		return annotationList;
	}
	
	public static String marshalAnnotatedPeaks(MSGlycanAnnotationCustomAnnotation annotationList) {
		String sAnnotationText = XMLUtils.marshalObjectXML(annotationList);
		return sAnnotationText;
	}
	
	public static MSGlycanAnnotationCustomAnnotationTemplate unmarshalAnnotationTemplate( String xmlFile ) {
		MSGlycanAnnotationCustomAnnotationTemplate annotationTemplate = (MSGlycanAnnotationCustomAnnotationTemplate) XMLUtils.unmarshalObjectXML(xmlFile, MSGlycanAnnotationCustomAnnotationTemplate.class);
		return annotationTemplate;
	}
	
	public static String marshalAnnotatedTemplate(MSGlycanAnnotationCustomAnnotationTemplate annotationTemplate) {
		String sAnnotationText = XMLUtils.marshalObjectXML(annotationTemplate);
		return sAnnotationText;
	}

}
