package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class MSGlycanFilterPreference {
	
	private static final Logger logger = Logger.getLogger(MSGlycanFilterPreference.class);
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.entry.ms.annotation.glycan.filter";
	private static final String CURRENT_VERSION = "1.0";
	
	FilterSettingLibrary filterSettings;
	
	public void setFilterSettings(FilterSettingLibrary filterSettings) {
		this.filterSettings = filterSettings;
	}
	
	public FilterSettingLibrary getFilterSettings() {
		return filterSettings;
	}
	
	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}
	
	public static MSGlycanFilterPreference getMSGlycanFilterPreferences(PreferenceEntity preferenceEntity) throws UnsupportedVersionException {
		MSGlycanFilterPreference preferenceSettings = new MSGlycanFilterPreference();
		if(preferenceEntity != null) {
			FilterSettingLibrary filters = MSGlycanFilterPreference.unmarshalFiltersList(preferenceEntity.getValue());
			preferenceSettings.setFilterSettings(filters);
		} else {
			// load defaults
			preferenceSettings.setFilterSettings(new FilterSettingLibrary());
		}
		return preferenceSettings;
	}

	private static FilterSettingLibrary unmarshalFiltersList(String value) {
		StringReader reader = new StringReader(value);
        //List<Class> contextList = new ArrayList<>(Arrays.asList(FilterUtils.filterClassContext));
        List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
        contextList.add(FilterSettingLibrary.class);
        JAXBContext context;
		try {
			context = JAXBContext.newInstance( contextList.toArray(new Class[contextList.size()]));
			Unmarshaller unmarshaller = context.createUnmarshaller();
		    FilterSettingLibrary filters = (FilterSettingLibrary) unmarshaller.unmarshal(reader);
		    return filters;
		} catch (JAXBException e) {
			logger.error("Cannot read filter preferences: ", e);
			return null;
		}
	}
	
	private static String marshalFiltersList(MSGlycanFilterPreference preference) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
       // List<Class> contextList = new ArrayList<Class>(Arrays.asList(FilterUtils.filterClassContext));
        List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
	  //  contextList.add(FiltersLibrary.class);
		contextList.add(FilterSettingLibrary.class);
        JAXBContext context;
		try {
			context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
			Marshaller marshaller = context.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
		    marshaller.marshal(preference.getFilterSettings(), os);
		} catch (JAXBException e) {
			logger.error("Cannot write filter preferences: ", e);
			return null;
		}
       
        return os.toString();
	}
	
	public static FilterSetting unmarshalFilter(String value) throws JAXBException {
		StringReader reader = new StringReader(value);
        List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
        contextList.add(FilterSetting.class);
        JAXBContext context = JAXBContext.newInstance( contextList.toArray(new Class[contextList.size()]));
		Unmarshaller unmarshaller = context.createUnmarshaller();
		FilterSetting filter = (FilterSetting) unmarshaller.unmarshal(reader);
	    return filter;
	}
	
	public static String marshalFilter(FilterSetting filter) throws JAXBException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();  
        List<Class> contextList = new ArrayList<Class>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
		contextList.add(FilterSetting.class);
        JAXBContext context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
		Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
	    marshaller.marshal(filter, os);

        return os.toString();
	}

	public boolean saveValues() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setVersion(CURRENT_VERSION);
		preferenceEntity.setValue(MSGlycanFilterPreference.marshalFiltersList(this));
		return PreferenceWriter.savePreference(preferenceEntity);
		
	}

}
