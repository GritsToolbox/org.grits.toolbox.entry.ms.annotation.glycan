package org.grits.toolbox.entry.ms.annotation.glycan.property.io;

import java.util.List;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecStandardQuant;
import org.jdom.Element;

/**
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationReaderVersion1_3
{
	public static Property read(Element propertyElement, MSGlycanAnnotationProperty msProperty)  {
		msProperty = (MSGlycanAnnotationProperty) MSGlycanAnnotationReaderVersion1_2.read(propertyElement, msProperty);
		MSAnnotationMetaData msMetaData = msProperty.getMSAnnotationMetaData();	
		
		List<MassSpecStandardQuant> sq = MassSpecPreference.unmarshalStandardQuantList(msMetaData.getStandardQuantText());
		msMetaData.setStandardQuant(sq);
		return msProperty;
	}

}
