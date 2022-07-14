package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.entry.ms.annotation.glycan.Activator;
import org.grits.toolbox.entry.ms.annotation.glycan.util.PreferenceUtils;

@XmlRootElement(name="msGlycanAnnotationSettings")
public class MSGlycanAnnotationSettingsPreference {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationSettingsPreference.class);
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.entry.ms.annotation.glycan.settings";
	private static final String CURRENT_VERSION = "1.0";

	List<MSGlycanAnnotationPreference> preferenceList = new ArrayList<>();

	public List<MSGlycanAnnotationPreference> getPreferenceList() {
		return preferenceList;
	}

	public void setPreferenceList(List<MSGlycanAnnotationPreference> preferenceList) {
		this.preferenceList = preferenceList;
	}

	public boolean saveValues() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setVersion(CURRENT_VERSION);
		try {
			preferenceEntity.setValue(PreferenceUtils.marshalXML(this));
			return PreferenceWriter.savePreference(preferenceEntity);
		} catch (JAXBException e) {
			logger.error("Could not serialize the preference to the preferences file", e);
		}
		return false;

	}

	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}

	/**
	 * Loads the XML from a default GELATO file, creates a preference object, and adds it to the preference list
	 *
	 * @param file, a default XML file
	 */
	private void processPreferenceFile( File file ) {
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			String line;
			String xmlString = "";
			while ((line = bufferedReader.readLine()) != null) {
				xmlString += line + "\n";
			}
			bufferedReader.close();
			MSGlycanAnnotationPreference pref = (MSGlycanAnnotationPreference) PreferenceUtils
					.unmarshallFromXML(xmlString, MSGlycanAnnotationPreference.class);
			preferenceList.add(pref);
		} catch (JAXBException e) {
			logger.warn(file.getName() + " is not a valid preference file");
		} catch (FileNotFoundException e1) {
			logger.warn(file.getName() + " is not a valid preference file");
		} catch (IOException e) {
			logger.warn(file.getName() + " is not a valid preference file");
		}
	}

	/**
	 * Loads the default GELATO preferences from the default files
	 */
	public void loadDefaults() {
		// load all files in preference folder
		URL resourceURL;
		try {
			resourceURL = FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"));
			File preferenceDir= new File(resourceURL.getPath());
			if (preferenceDir.exists() && preferenceDir.isDirectory()) {
				File[] prefSubDirs = preferenceDir.listFiles();
				for (File subDir : prefSubDirs) {
					if( subDir.isDirectory() && subDir.getName().equals("glycanAnnotation") ) {
						File[] files = subDir.listFiles();
						for (File file : files) {
							if (file.getName().endsWith(".xml")) {
								processPreferenceFile(file);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not load default preference files", e);
		} 
	}

	
	public void remove(MSGlycanAnnotationPreference setting) {
		preferenceList.remove(setting);
	}
}
