package org.grits.toolbox.entry.ms.annotation.glycan.property;

import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;

public class MSGlycanAnnotationEntityProperty extends MSAnnotationEntityProperty {
	public static final String TYPE = MSGlycanAnnotationEntityProperty.class.getName();

	public MSGlycanAnnotationEntityProperty(MassSpecProperty msParentProperty,
			MSAnnotationProperty annotParentProperty) {
		super(msParentProperty, annotParentProperty);
	}
	
	public static Entry getTableCompatibleEntry( Entry parentEntry ) {
		Entry newEntry = MassSpecEntityProperty.getTableCompatibleEntry(parentEntry);
				
		Entry msAnnotEntry = MSAnnotationProperty.getFirstAnnotEntry(parentEntry);
		MSAnnotationProperty msAnnotProp = null;
		MSAnnotationEntityProperty msAnnotEntityProp = null;
		if( msAnnotEntry != null ) {
			msAnnotProp = (MSAnnotationProperty) msAnnotEntry.getProperty();
			MassSpecEntityProperty msEntityProp = (MassSpecEntityProperty) newEntry.getProperty();
			msAnnotEntityProp = new MSGlycanAnnotationEntityProperty((MassSpecProperty) msEntityProp.getMassSpecParentProperty(), msAnnotProp);
			newEntry.setProperty(msAnnotEntityProp);
			newEntry.setDisplayName(parentEntry.getDisplayName());
		} 		
		return newEntry;
	}

	@Override
	public Object clone() {
		MSGlycanAnnotationEntityProperty newProp = new MSGlycanAnnotationEntityProperty(this.getMassSpecParentProperty(), this.getMSAnnotationParentProperty());
		newProp.setDescription(this.getDescription());
		newProp.setId(this.getId());
		newProp.setAnnotationId(this.getAnnotationId());
		newProp.setScanNum(this.getScanNum());
		newProp.setMsLevel(this.getMsLevel());
		newProp.setParentScanNum(this.getParentScanNum());
		newProp.setFeatureId(this.getFeatureId());
		return newProp;
	}
	
	@Override
	public String getType() {
		return MSGlycanAnnotationEntityProperty.TYPE;
	}
	
	
}
