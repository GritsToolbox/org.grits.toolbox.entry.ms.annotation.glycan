package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.display.control.table.preference.TableViewerPreferenceLoader;

public class MSGlycanAnnotationPreferenceLoader {
	private static final Logger logger = Logger.getLogger(TableViewerPreferenceLoader.class);
	
	public static MSGlycanCustomAnnotationPreference getMSGlycanCustomAnnotationPreference()  {
		MSGlycanCustomAnnotationPreference preferences = null;
		try {
			PreferenceEntity preferenceEntity = MSGlycanCustomAnnotationPreference.getPreferenceEntity(); 
			if( preferenceEntity != null ) { 
				preferences = MSGlycanCustomAnnotationPreference.getMSGlycanCustomAnnotationPreferences(preferenceEntity);
			}
		} catch (UnsupportedVersionException ex) {
			logger.error(ex.getMessage(), ex);
			
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}		
		if( preferences == null ) { // well, either no preferences yet or some error. 
			preferences = new MSGlycanCustomAnnotationPreference();
			preferences.loadDefaultCustomAnnotations();
		}
		return preferences;
	}

}
