package org.grits.toolbox.entry.ms.annotation.glycan.property;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.ImageRegistry;
import org.grits.toolbox.entry.ms.annotation.glycan.Activator;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;

/**
 * An extension of the MSGlycanAnnotationProperty class in order to support MSGlycanAnnotation data.
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationProperty extends MSAnnotationProperty
{
	private static final Logger logger = Logger.getLogger(MSAnnotationProperty.class);
	// 01/12/18:  Changed to version 1.3 because Internal Standard Quant was added to the MetaData. No changes to the property, but this affects the reader
	public static final String CURRENT_VERSION = "1.3";
	public static final String TYPE = "org.grits.toolbox.property.ms_annotation.glycan";
	private static ImageDescriptor imageDescriptor = ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ImageRegistry.MSImage.MSANNOTATION_ICON);
	private static final String ARCHIVE_EXTENSION = ".zip";
	private static final String ARCHIVE_FOLDER = "glycan_annotation";

	public MSGlycanAnnotationProperty()
	{
		super();
		setVersion(CURRENT_VERSION);
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSGlycanAnnotationProperty) )
			return false;

		return super.equals(obj);
	}

	@Override
	public Object clone() {
		MSGlycanAnnotationProperty newProp = new MSGlycanAnnotationProperty();
		if ( getMSAnnotationMetaData() != null ) {
			MSGlycanAnnotationMetaData settings = (MSGlycanAnnotationMetaData) getMSAnnotationMetaData().clone();
			newProp.setMSAnnotationMetaData(settings);
		}
		return newProp;
	}

	@Override
	public String getType() {
		return MSGlycanAnnotationProperty.TYPE;
	}

	@Override
	public ImageDescriptor getImage() {
		return MSGlycanAnnotationProperty.imageDescriptor;
	}

	@Override		
	public String getArchiveExtension() {
		return MSGlycanAnnotationProperty.ARCHIVE_EXTENSION;
	}

	@Override
	public String getArchiveFolder() {
		return MSGlycanAnnotationProperty.ARCHIVE_FOLDER;
	}

	public static MSAnnotationMetaData unmarshallSettingsFile( String sFileName ) {
		MSAnnotationMetaData metaData = null;
		try {
			metaData = (MSAnnotationMetaData) XMLUtils.unmarshalObjectXML(sFileName, MSGlycanAnnotationMetaData.class);
		} catch (Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return metaData;
	}
	
	@Override
	protected MSAnnotationProperty getNewAnnotationProperty(String msAnnotationFolder) {
		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
		MSGlycanAnnotationMetaData metaData = new MSGlycanAnnotationMetaData();		
		t_property.setMSAnnotationMetaData(metaData);
		try {
			metaData.setAnnotationId(createRandomId(msAnnotationFolder));
			metaData.setVersion(MSGlycanAnnotationMetaData.CURRENT_VERSION);
			metaData.setName(t_property.getMetaDataFileName());
		} catch (IOException e2) {
			logger.error(e2.getMessage(), e2);
			return null;
		}

		return t_property;
	}
}
