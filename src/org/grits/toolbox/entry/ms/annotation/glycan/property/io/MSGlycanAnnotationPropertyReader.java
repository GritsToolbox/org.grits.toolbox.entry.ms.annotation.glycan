package org.grits.toolbox.entry.ms.annotation.glycan.property.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedTypeException;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.io.MSAnnotationPropertyReader;
import org.grits.toolbox.entry.ms.annotation.property.io.MSAnnotationReaderVersion0;
import org.grits.toolbox.entry.ms.annotation.property.io.MSAnnotationReaderVersion1;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.jdom.Element;

/**
 * Reader for sample entry. Should check for empty values
 * @author Brent Weatherly
 *
 */
public class MSGlycanAnnotationPropertyReader extends MSAnnotationPropertyReader
{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationPropertyReader.class);

	
	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException
	{
		MSGlycanAnnotationProperty property = (MSGlycanAnnotationProperty) getNewMSAnnotationProperty();

		PropertyReader.addGenericInfo(propertyElement, property);

		String origVersion = property.getVersion();
		if(property.getVersion() == null) {
			// we must also convert the meta-data to the model and write out. Do that here?
			try {
				MSAnnotationReaderVersion0.read(propertyElement, property);
				property.setVersion(MSGlycanAnnotationProperty.CURRENT_VERSION);
				PropertyReader.UPDATE_PROJECT_XML = true;
			} catch (UnsupportedTypeException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		else if(property.getVersion().equals("1.0")) {
			MSAnnotationReaderVersion1.read(propertyElement, property);
			property.setVersion(MSGlycanAnnotationProperty.CURRENT_VERSION);
			PropertyReader.UPDATE_PROJECT_XML = true;
		}
		else if(property.getVersion().equals("1.1")) {
			MSGlycanAnnotationReaderVersion1_1.read(propertyElement, property);
			property.setVersion(MSGlycanAnnotationProperty.CURRENT_VERSION);
			PropertyReader.UPDATE_PROJECT_XML = true;
		}
		else if(property.getVersion().equals("1.2")) {
			MSGlycanAnnotationReaderVersion1_2.read(propertyElement, property);
			property.setVersion(MSGlycanAnnotationProperty.CURRENT_VERSION);
			PropertyReader.UPDATE_PROJECT_XML = true;
		}
		else if(property.getVersion().equals(MSGlycanAnnotationProperty.CURRENT_VERSION)) {
			MSGlycanAnnotationReaderVersion1_3.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException("This version is currently not supported.", property.getVersion());

		if( origVersion == null || ! origVersion.equals(MSGlycanAnnotationProperty.CURRENT_VERSION) ) { // fix stuff for new version
			MSGlycanAnnotationReaderVersion1_1.updateMSAnnotationMetaData(propertyElement, property);
			MSAnnotationMetaData msMetaData = property.getMSAnnotationMetaData();
			if( msMetaData == null || ! (msMetaData instanceof MSGlycanAnnotationMetaData) || msMetaData.getAnnotationId() == null) {
				Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
				String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");
				logger.error("Invalid entry for project: " + projectName);
			}
		}
		
		// need to adjust source file path between different operating systems
		adjustSourceFileListFilePaths(propertyElement, property);
		return property;
	}

	private void adjustSourceFileListFilePaths(Element propertyElement, MSGlycanAnnotationProperty property) {
		boolean changed = false;
		if (property.getMSAnnotationMetaData() != null) {
			if (property.getMSAnnotationMetaData().getSourceDataFileList() != null) {
				for (MSPropertyDataFile file : property.getMSAnnotationMetaData().getSourceDataFileList()) {
					if( file.getName().contains("\\") && ! File.separator.equals("\\") ) {
						file.setName( file.getName().replace("\\", File.separator));
						changed = true;
					} else if( file.getName().contains("/") && ! File.separator.equals("/") ){
						file.setName( file.getName().replace("/", File.separator));
						changed = true;
					}
				}
				if (changed) {
					Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
					String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");
					String workspaceFolder = PropertyHandler.getVariable("workspace_location");
					String msFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
							+ File.separator
							+ projectName + File.separator
							+ property.getArchiveFolder();
					String msFile = property.getMetaDataFile().getName();
					String fullPath = msFolder + File.separator + msFile;
					MSGlycanAnnotationProperty.marshallSettingsFile(fullPath, property.getMSAnnotationMetaData());
				}
			}
		}
		
	}

	protected MSAnnotationProperty getNewMSAnnotationProperty() {
		return new MSGlycanAnnotationProperty();
	}
}
