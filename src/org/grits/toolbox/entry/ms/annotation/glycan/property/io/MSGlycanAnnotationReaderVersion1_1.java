package org.grits.toolbox.entry.ms.annotation.glycan.property.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanCustomAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.ms.file.FileCategory;
import org.jdom.Element;

/**
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationReaderVersion1_1
{
	/**
	 * If the MSGlycanAnnotationProperty contains the PropertyDataFile for the archive, we need to remove
	 * it from the Property's list so it can be added to the meta data list. Search the Property file list 
	 * for the appropriate file. If it exists, remove and return it, otherwise return null.
	 * 
	 * @return the PropertyDataFile for the archive, if it exists, null otherwise
	 */
	private static PropertyDataFile getArchiveFile(MSGlycanAnnotationProperty msAnnotProperty) {
		if( msAnnotProperty.getDataFiles() == null || msAnnotProperty.getDataFiles().isEmpty() ) {
			return null;
		}		
		for( PropertyDataFile pdf : msAnnotProperty.getDataFiles() ) {
			if( pdf.getType().equals("file") && pdf.getName().endsWith(msAnnotProperty.getArchiveExtension())) {
				msAnnotProperty.getDataFiles().remove(pdf);
				return pdf;
			}
		}
		return null;
	}

	private static MSPropertyDataFile getNewMSPropertyDataFile( String sAnnotationFile ) {
		File file = new File( sAnnotationFile );
		MSPropertyDataFile pdf = null;
		if( file.isFile()) { // archive is a single zip file
			pdf = new MSPropertyDataFile(sAnnotationFile, 
					MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
					MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE,
					FileCategory.ANNOTATION_CATEGORY, 
					"GELATO",
					sAnnotationFile, new ArrayList<String>() );
		} else { // should be a folder then
			pdf = new MSPropertyDataFile(sAnnotationFile, 
					MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
					MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FOLDER,
					FileCategory.ANNOTATION_CATEGORY, 
					"GELATO",
					sAnnotationFile, new ArrayList<String>() );
		}
		return pdf;
	}

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
		if( msMetaData == null || ! (msMetaData instanceof MSGlycanAnnotationMetaData) ) {
			// this is technically a fix for a bug. Projects between version 1.1 and 1.2 were writing msAnnotationMetaData instead of MSGlycanAnnotationMetaData
			// once we set it here, it will get fixed at the end of the PropertyReader
			msProperty.setMSAnnotationMetaData(msMetaData); 
			return msProperty;
		}

		List<MassSpecCustomAnnotation> ca = MSGlycanCustomAnnotationPreference.unmarshalCustomAnnotationsList(((MSGlycanAnnotationMetaData) msMetaData).getCustomAnnotationText());
		((MSGlycanAnnotationMetaData) msMetaData).setCustomAnnotations(ca);
		msProperty.setMSAnnotationMetaData(msMetaData);
		return msProperty;
	}
	
	public static void updateMSAnnotationMetaData(Element propertyElement, MSGlycanAnnotationProperty msProperty) {
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");
		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String msFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ msProperty.getArchiveFolder();
		String msFile = msProperty.getMetaDataFile().getName();
		String fullPath = msFolder + File.separator + msFile;
		MSAnnotationMetaData msMetaData = msProperty.getMSAnnotationMetaData();
		boolean bWriteSettings = false;
		if( msMetaData == null || ! (msMetaData instanceof MSGlycanAnnotationMetaData) || msMetaData.getAnnotationId() == null ) {
			MSGlycanAnnotationMetaData msGlycanMetaData = new MSGlycanAnnotationMetaData();
			if( msMetaData != null ) {
				msGlycanMetaData.clone( msMetaData );
			}	
			msMetaData = msGlycanMetaData;
			msProperty.setMSAnnotationMetaData(msMetaData);	
			bWriteSettings = true;
		}
		/* Check for missing information. It's possible with certain beta versions that the meta-data wasn't set propertly
		 * or was erased. We should be able to recover this from the msFile file name.
		 */
		if( msMetaData.getAnnotationId() == null ) {
			// Invalid state w/ no annotation id. But we have the file, and the first element of the file name should 
			// be the annotation id, so attempt to recover
			String sToks[] = msFile.split("\\.");
			try {
				Integer iAnnotId = Integer.parseInt(sToks[0]);
				msMetaData.setAnnotationId(sToks[0]);					
				bWriteSettings = true;
			} catch( NumberFormatException ex ) {
				;
			}
		}
		if( msMetaData.getName() == null ) {
			msMetaData.setName(msFile);
			bWriteSettings = true;
		}
		if( msMetaData.getDescription() == null ) {
			msMetaData.setDescription("");
			bWriteSettings = true;
		}
		// Mark all files as "in use" by all the children entries
		PropertyDataFile pdf = getArchiveFile(msProperty);
		if( pdf != null ) {
			MSPropertyDataFile mspdf = getNewMSPropertyDataFile(pdf.getName());
			msMetaData.addFile(mspdf);
			bWriteSettings = true;
		}

		if( bWriteSettings ) { // if we changed the meta data, write it back out so the project is correct
			MSGlycanAnnotationProperty.marshallSettingsFile(fullPath, msMetaData);
		}
	}
}
