package org.grits.toolbox.entry.ms.annotation.glycan.util;

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
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationSettingsPreference;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class PreferenceUtils {
	
	private static final Logger logger = Logger.getLogger(PreferenceUtils.class);

	public static MSGlycanAnnotationSettingsPreference getMSGlycanAnnotationSettingsPreferences(PreferenceEntity preferenceEntity, Class<? extends MSGlycanAnnotationSettingsPreference> thisClass) throws UnsupportedVersionException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		MSGlycanAnnotationSettingsPreference preferenceSettings = null;
		if(preferenceEntity != null) {
			preferenceSettings = PreferenceUtils.getMSGlycanAnnotationSettingsPreferencesFromXML(preferenceEntity.getValue(), thisClass);
			if (preferenceSettings == null) {
				preferenceSettings = (MSGlycanAnnotationSettingsPreference) thisClass.newInstance();
				preferenceSettings.loadDefaults();
			}
		} else {
			preferenceSettings = (MSGlycanAnnotationSettingsPreference) thisClass.newInstance(); // empty one
			preferenceSettings.loadDefaults();
		}
		return preferenceSettings;
	}
	
	private static MSGlycanAnnotationSettingsPreference getMSGlycanAnnotationSettingsPreferencesFromXML(String xmlString, Class<? extends MSGlycanAnnotationSettingsPreference> thisClass) {
		MSGlycanAnnotationSettingsPreference msPreference = null;
		try {
			msPreference = (MSGlycanAnnotationSettingsPreference) PreferenceUtils.unmarshallFromXML(xmlString, thisClass);
		} catch (JAXBException e) {
			logger.error("The object could not be read from xml." + e.getMessage(), e);
		}
		return msPreference;
	}

	public static Object unmarshallFromXML(String xmlString, Class<?> destClass) throws JAXBException {
		Object obj = null;
		List<Class> contextList = new ArrayList<>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
		contextList.add(destClass);
		JAXBContext context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
		Unmarshaller unmarshaller = context.createUnmarshaller();
		if (xmlString != null && !xmlString.isEmpty())
			obj  = unmarshaller.unmarshal(new StringReader(xmlString));
		return obj;
	}

	public static String marshalXML(Object object) throws JAXBException
	{
		String xmlString = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<Class> contextList = new ArrayList<>(Arrays.asList(AnnotationReader.filterClassContext));
		contextList.addAll(Arrays.asList(FilterUtils.filterClassContext));
		contextList.add(object.getClass());
		contextList.add(Method.class);
		JAXBContext context = JAXBContext.newInstance(contextList.toArray(new Class[contextList.size()]));
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);

		marshaller.marshal(object, os);
		xmlString = os.toString();

		return xmlString ;
	}

}
