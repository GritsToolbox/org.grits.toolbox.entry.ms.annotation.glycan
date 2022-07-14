package org.grits.toolbox.entry.ms.annotation.glycan.property.io;

import java.io.File;
import java.util.List;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanCustomAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.jdom.Element;

/**
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationReaderVersion1_2
{
	public static Property read(Element propertyElement, MSGlycanAnnotationProperty msProperty)  {
		msProperty.adjustPropertyFilePaths();
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");
		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String msFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ msProperty.getArchiveFolder();
		String msFile = msProperty.getMetaDataFile().getName();
		String fullPath = msFolder + File.separator + msFile;
		MSAnnotationMetaData msMetaData = MSGlycanAnnotationProperty.unmarshallSettingsFile(fullPath);
		List<MassSpecCustomAnnotation> ca = MSGlycanCustomAnnotationPreference.unmarshalCustomAnnotationsList(((MSGlycanAnnotationMetaData) msMetaData).getCustomAnnotationText());
		((MSGlycanAnnotationMetaData) msMetaData).setCustomAnnotations(ca);
		msProperty.setMSAnnotationMetaData(msMetaData);
		return msProperty;
	}

}
