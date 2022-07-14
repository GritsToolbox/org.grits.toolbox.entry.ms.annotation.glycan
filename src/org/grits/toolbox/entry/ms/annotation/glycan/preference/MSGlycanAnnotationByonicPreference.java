package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.util.structure.glycan.count.SearchQueryItem;

@XmlRootElement(name="byonicPreferences")
public class MSGlycanAnnotationByonicPreference {
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.entry.ms.annotation.glycan.byonic.all";
	private static final String CURRENT_VERSION = "1.0";
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationByonicPreference.class);
	
	List<SearchQueryItem> components;
	
	public List<SearchQueryItem> getComponents() {
		return components;
	}
	
	public void setComponents(List<SearchQueryItem> components) {
		this.components = components;
	}
	
	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}
	
	public static MSGlycanAnnotationByonicPreference getByonicPreferences (PreferenceEntity preferenceEntity) {
		MSGlycanAnnotationByonicPreference preference = null;
		if (preferenceEntity == null) {
			preference = loadDefaultSettings();
		} else {
			preference = unmarshalComponentsList(preferenceEntity.getValue());
		}
		return preference;
	}
	
	private static MSGlycanAnnotationByonicPreference unmarshalComponentsList(String value) {
		StringReader reader = new StringReader(value);
		try {
			JAXBContext context = JAXBContext.newInstance(MSGlycanAnnotationByonicPreference.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			MSGlycanAnnotationByonicPreference pref= (MSGlycanAnnotationByonicPreference) unmarshaller.unmarshal(reader);
		    return pref;
		} catch (JAXBException e) {
			logger.error("Cannot read filter preferences: ", e);
			return null;
		}
	}
	
	private static String marshalComponentsList(MSGlycanAnnotationByonicPreference preference) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
        JAXBContext context;
		try {
			context = JAXBContext.newInstance(MSGlycanAnnotationByonicPreference.class);
			Marshaller marshaller = context.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
		    marshaller.marshal(preference, os);
		} catch (JAXBException e) {
			logger.error("Cannot write filter preferences: ", e);
			return null;
		}
       
        return os.toString();
	}

	public static MSGlycanAnnotationByonicPreference loadDefaultSettings() {
		List<SearchQueryItem> queryList = new ArrayList<>();		
		queryList.add(new SearchQueryItem("HexNAc", "RES\n"
				+ "1b:x-HEX-1:5\n"
				+ "2s:n-acetyl\n"
				+ "LIN\n"
				+ "1:1d(2+1)2n"));
		queryList.add(new SearchQueryItem("HexN", "RES\n"
				+ "1b:x-HEX-x:x|1:a\n"
				+ "2s:amino\n"
				+ "LIN\n"
				+ "1:1d(2+1)2n"));
		queryList.add(new SearchQueryItem("NeuAC", "RES\n"
				+ "1b:x-dgro-dgal-NON-x:x|1:a|2:keto|3:d\n"
				+ "2s:n-acetyl\n"
				+ "LIN\n"
				+ "1:1d(5+1)2n"));
		queryList.add(new SearchQueryItem("NeuGC", "RES\n"
				+ "1b:x-dgro-dgal-NON-x:x|1:a|2:keto|3:d\n"
				+ "2s:n-glycolyl\n"
				+ "LIN\n"
				+ "1:1d(5+1)2n"));
		queryList.add(new SearchQueryItem("KDN", "RES\n"
				+ "1b:x-dgro-dgal-NON-x:x|1:a|2:keto|3:d"));
		queryList.add(new SearchQueryItem("dHex", "RES\n"
				+ "1b:x-HEX-x:x|6:d"));
		queryList.add(new SearchQueryItem("Hex", "RES\n"
				+ "1b:x-HEX-x:x"));
		queryList.add(new SearchQueryItem("Pent", "RES\n"
				+ "1b:x-PEN-x:x"));
		queryList.add(new SearchQueryItem("GlcA", "RES\n" 
                   +  "1b:x-dglc-HEX-1:5|6:a"));
	    queryList.add(new SearchQueryItem("IdoA", "RES\n1b:x-lido-HEX-1:5|6:a"));
		queryList.add(new SearchQueryItem("Phosphate", "RES\n1s:phosphate"));
		queryList.add(new SearchQueryItem("Sulfate", "RES\n1s:sulfate"));
		
		MSGlycanAnnotationByonicPreference preference = new MSGlycanAnnotationByonicPreference();
		preference.setComponents(queryList);

		return preference;	
	}
	
	public boolean save() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setVersion(CURRENT_VERSION);
		preferenceEntity.setValue(MSGlycanAnnotationByonicPreference.marshalComponentsList(this));
		return PreferenceWriter.savePreference(preferenceEntity);
		
	}
	
}

