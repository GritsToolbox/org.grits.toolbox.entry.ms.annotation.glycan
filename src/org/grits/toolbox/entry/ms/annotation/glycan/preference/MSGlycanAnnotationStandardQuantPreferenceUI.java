package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.preference.IMSPreferenceWithStandardQuant;
import org.grits.toolbox.entry.ms.preference.MassSpecPreferenceLoader;
import org.grits.toolbox.entry.ms.preference.MassSpecStandardQuantPreferenceUI;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;

public class MSGlycanAnnotationStandardQuantPreferenceUI extends MassSpecStandardQuantPreferenceUI {

	public MSGlycanAnnotationStandardQuantPreferenceUI(Composite parent, int style, IPropertyChangeListener listener,
			boolean bAddSaveAsDefault) {
		super(parent, style, listener, bAddSaveAsDefault);
	}
	
	public static MassSpecUISettings getMassSpecUISettingsFromEntry( Entry entry ) {
		Property p = entry.getProperty();
		if( p != null ) {				
			MSAnnotationProperty ep = ((MSAnnotationEntityProperty) p).getMSAnnotationParentProperty();				
			MSGlycanAnnotationMetaData metaData = (MSGlycanAnnotationMetaData) ep.getMSAnnotationMetaData();
			return metaData;
		}		
		return null;		
	}
	
	/**
	 * Initializes the storedStandardQuant member variable by loading the standard quantification from preferences in the workspace XML
	 */
	public void initStoredStandardQuant() {
		storedStandardQuant = MassSpecStandardQuantPreferenceUI.loadWorkspacePreferences();	
	}
	
	/**
	 * Loads the Mass Spec preferences from the workspace XML.
	 * 
	 * @return
	 */
	public static IMSPreferenceWithStandardQuant loadWorkspacePreferences() {
		try {
			return MassSpecPreferenceLoader.getMassSpecPreferences();
		} catch (Exception ex) {
			logger.error("Error getting the mass spec preferences", ex);
		}
		return null;
	}


}
