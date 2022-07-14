package org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel;

import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanCustomAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
@XmlRootElement(name = "msGlycanAnnotationMetaData")
public class MSGlycanAnnotationMetaData extends MSAnnotationMetaData {
	public static final String CURRENT_VERSION = "1.0";

	public MSGlycanAnnotationMetaData() {
		super();
	}
	
	@Override
	public Object clone() {
		MSGlycanAnnotationMetaData newSettings = new MSGlycanAnnotationMetaData();
		super.cloneSettings(newSettings);
//		newSettings.setDescription(this.getDescription());
//		newSettings.setAnnotationId(this.getAnnotationId());
//		newSettings.setVersion(this.getVersion());
//		newSettings.setDescription(this.getDescription());
//		newSettings.setCustomAnnotationText(customAnnotationsText);
//		newSettings.setCustomAnnotationText(this.getCustomAnnotationText());
		newSettings.updateCustomAnnotationList();
//		newSettings.setAnnotationFile(this.getAnnotationFile());
		return newSettings;
	}

	public void clone(MSAnnotationMetaData metaData) {	
		setDescription(metaData.getDescription());
		setAnnotationId(metaData.getAnnotationId());
		setVersion(CURRENT_VERSION);
		setDescription(metaData.getDescription());
		setName(metaData.getName());
//		setAnnotationFile(metaData.getAnnotationFile());
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSGlycanAnnotationMetaData) )
			return false;
		
		MSGlycanAnnotationMetaData castObj = (MSGlycanAnnotationMetaData) obj;
		boolean bRes = getDescription() != null && getDescription().equals( castObj.getDescription() );
		bRes &= getAnnotationId() != null && getAnnotationId().equals( castObj.getAnnotationId() );
		return bRes;
	}
	
	public void updateCustomAnnotationList() {
		setCustomAnnotations( MSGlycanCustomAnnotationPreference.unmarshalCustomAnnotationsList( getCustomAnnotationText() ));
	}
	
	public void updateCustomAnotationData() {
		setCustomAnnotationText( MSGlycanCustomAnnotationPreference.createCustomAnnotationsText( getCustomAnnotations() ));		
	}

}
