package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotation;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.xml.MSGlycanAnnotationCustomAnnotationTemplate;
import org.grits.toolbox.entry.ms.preference.IMSPreferenceWithCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
@XmlRootElement(name = "msGlycanCustomAnnotations")
public class MSGlycanCustomAnnotationPreference implements IMSPreferenceWithCustomAnnotation {
	private static final Logger logger = Logger.getLogger(MSGlycanCustomAnnotationPreference.class);
	
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.entry.ms.annotation.glycan.customannotation";
	private static final String CURRENT_VERSION = "1.1";
	private static final String VALUE_SEPERATOR = "~~~~~";
	
	List<MassSpecCustomAnnotation> customAnnotations = new ArrayList<MassSpecCustomAnnotation>();
	
	// need to have this since otherwise JAXB cannot marshall and unmarshall to the correct object type automatically. It will only create
	// MassSpecCustomAnnotation objects since the customAnnotations list contains that superclass. But we want MSGlycanAnnotationCustomAnnotation from the xml
	// but store it in a generic (MassSpecCustomAnnotation) list customAnnotations
	List<MSGlycanAnnotationCustomAnnotation> serializedCustomAnnotation = new ArrayList<>();

	@XmlTransient
	@Override
	public List<MassSpecCustomAnnotation> getCustomAnnotations() {
		return customAnnotations;
	}
	
	public void setCustomAnnotations(List<MassSpecCustomAnnotation> customAnnotations) {
		this.customAnnotations = customAnnotations;
	}
	
	@XmlElement(name="customAnnotations")
	public List<MSGlycanAnnotationCustomAnnotation> getSerializedCustomAnnotation() {
		return serializedCustomAnnotation;
	}
	
	public void setSerializedCustomAnnotation(List<MSGlycanAnnotationCustomAnnotation> serializedCustomAnnotation) {
		this.serializedCustomAnnotation = serializedCustomAnnotation;
	}
	
	public static MassSpecCustomAnnotation lookupCustomAnnotation( List<MassSpecCustomAnnotation> customAnnotations,
			String annotationName ) {
		if( customAnnotations != null && ! customAnnotations.isEmpty() ) {
			for( int i = 0; i < customAnnotations.size(); i++ ) {
				MassSpecCustomAnnotation annotation = customAnnotations.get(i);
				if( annotation.getAnnotationName().equalsIgnoreCase(annotationName) )  {
					return annotation;
				}
			}			
		}
		return null;
	}

	@Override
	public boolean saveValues() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setVersion(CURRENT_VERSION);
		serializedCustomAnnotation.clear();
		for (MassSpecCustomAnnotation ca: customAnnotations) {
			if (ca instanceof MSGlycanAnnotationCustomAnnotation) {
				serializedCustomAnnotation.add((MSGlycanAnnotationCustomAnnotation) ca);
				ca.updateAnnotatedPeakText();
			}
		}
		preferenceEntity.setValue(XMLUtils.marshalObjectXML(this));
		return PreferenceWriter.savePreference(preferenceEntity);
	}

	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}
	
	public static MSGlycanCustomAnnotationPreference getMSGlycanCustomAnnotationPreferences(PreferenceEntity preferenceEntity) throws UnsupportedVersionException {
		MSGlycanCustomAnnotationPreference preferenceSettings = null;
		if(preferenceEntity != null) {
			preferenceSettings = MSGlycanCustomAnnotationPreference.getMSGlycanCustomAnnotationPreferencesFromXML(preferenceEntity.getValue());
		}
		return preferenceSettings;
	}
	
	private static MSGlycanCustomAnnotationPreference getMSGlycanCustomAnnotationPreferencesFromXML(String xmlString) {
		MSGlycanCustomAnnotationPreference msPreference = null;
		try
		{
			JAXBContext context = JAXBContext.newInstance(MSGlycanCustomAnnotationPreference.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			msPreference  = (MSGlycanCustomAnnotationPreference) unmarshaller.unmarshal(new StringReader(xmlString));
			for (MSGlycanAnnotationCustomAnnotation ca : msPreference.serializedCustomAnnotation) {
				msPreference.customAnnotations.add(ca);
				ca.unmarshalAnnotatedPeakList();
			}
		} catch (JAXBException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
		}
		return msPreference;
	}
	
	public static String createCustomAnnotationsText(MSGlycanAnnotationCustomAnnotation annotation) {
		annotation.updateAnnotatedPeakText();
		String asText = XMLUtils.marshalObjectXML(annotation);
		return asText;
	}

	public static String createCustomAnnotationsText(List<MassSpecCustomAnnotation> customAnnotations) {
		StringBuilder sb = new StringBuilder();
		if( customAnnotations != null && ! customAnnotations.isEmpty() ) {
			int iAnnotCnt = 0;
			for( int i = 0; i < customAnnotations.size(); i++ ) {
				MSGlycanAnnotationCustomAnnotation annotation = (MSGlycanAnnotationCustomAnnotation) customAnnotations.get(i);
				if( annotation == null ) {
					continue;
				}
				if( iAnnotCnt > 0 ) {
					sb.append(VALUE_SEPERATOR);
				}
				String sXML = MSGlycanCustomAnnotationPreference.createCustomAnnotationsText(annotation);
				sb.append(sXML);
				iAnnotCnt++;
			}
		}
		return sb.toString();
	}

	public static List<MassSpecCustomAnnotation> unmarshalCustomAnnotationsList( String sCustomAnnotationText ) {
		List<MassSpecCustomAnnotation> msca = new ArrayList<MassSpecCustomAnnotation>();
		try {
			if( sCustomAnnotationText != null ) {
				if( sCustomAnnotationText.startsWith(VALUE_SEPERATOR) ) { // this is an error....fix just in case
					int iStartPos = sCustomAnnotationText.indexOf(VALUE_SEPERATOR) + VALUE_SEPERATOR.length();
					sCustomAnnotationText = sCustomAnnotationText.substring(iStartPos);
				}
				if( ! sCustomAnnotationText.equals("") ) {
					String[] annotations = sCustomAnnotationText.split(VALUE_SEPERATOR);
					if( annotations != null && annotations.length > 0 ) {
						for( int i = 0; i < annotations.length; i++ ) {
							String annotXML = annotations[i];
							MSGlycanAnnotationCustomAnnotation annotation = (MSGlycanAnnotationCustomAnnotation) XMLUtils.getObjectFromXML(annotXML, MSGlycanAnnotationCustomAnnotation.class);
							annotation.unmarshalAnnotatedPeakList();
							if( annotation != null ) {
								annotation.unmarshalAnnotatedPeakList();
								msca.add(annotation);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error loading default options from xml files.", ex);
		}
		return msca;
	}

	public void loadDefaultCustomAnnotations() {
		// load all files in preference folder
		URL resourceURL;
		serializedCustomAnnotation.clear();
		try {
			resourceURL = FileLocator.toFileURL(
					Platform.getBundle(org.grits.toolbox.entry.ms.annotation.glycan.Activator.PLUGIN_ID).getResource("preference"));
			File preferenceDir= new File(resourceURL.getPath());
			if (preferenceDir.exists() && preferenceDir.isDirectory()) {
				File[] prefSubDirs = preferenceDir.listFiles();
				for (File subDir : prefSubDirs) {
					if( subDir.isDirectory() && subDir.getName().equals("customAnnotation") ) {
						File[] files = subDir.listFiles();
						for (File file : files) {
							if (file.getName().endsWith(".xml")) {
								processMSGlycanCustomAnnotation(file.getAbsolutePath());
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not load default preference files", e);
		} 
	}

	private void processMSGlycanCustomAnnotation( String fileName ) {
		try {
			MSGlycanAnnotationCustomAnnotationTemplate template = MSGlycanAnnotationCustomAnnotationTemplate.unmarshalAnnotationTemplate(fileName);
			MSGlycanAnnotationCustomAnnotation msca = (MSGlycanAnnotationCustomAnnotation) template.copyToNewAnnotation();
			customAnnotations.add(msca);
		} catch (Exception e) {
			logger.warn(fileName + " is not a valid preference file");
		}
	}

}
