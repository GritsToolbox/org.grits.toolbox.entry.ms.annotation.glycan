package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.tablemodel.MassSpecTableDataObject;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.dialog.MassSpecPeakIntensityApplyDialog;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSGlycanAnnotationPeakIntensityApplyDialog extends MassSpecPeakIntensityApplyDialog {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationPeakIntensityApplyDialog.class);

	public MSGlycanAnnotationPeakIntensityApplyDialog(Shell parentShell, MassSpecMultiPageViewer contextViewer,
			MassSpecTableDataObject msTableDataObject) {
		super(parentShell, contextViewer, msTableDataObject);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.dialog.MassSpecPeakIntensityApplyDialog#getCurrentViewer()
	 */
	public MassSpecMultiPageViewer getCurrentViewer() {
		try {
			EPartService partService = getContextViewer().getPartService();
			MPart mPart = partService.getActivePart();
			if( mPart != null && mPart.equals(mPart.getParent().getSelectedElement())) {
				if( mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer ) {
					MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
					if( viewer.getEntry().getProperty() != null && viewer.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {
						return viewer;
					}
				}
			}	
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.dialog.MassSpecPeakIntensityApplyDialog#getEntryParentProperty()
	 */
	public Property getEntryParentProperty() {
		try {
			Entry entry = getEntryForCurrentViewer();
			MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) entry.getProperty();

			// we're storing all info back in the MS meta data to keep things consistent.
			MassSpecProperty msp = msep.getMassSpecParentProperty();
			//			MSAnnotationProperty pp = msep.getMSAnnotationParentProperty();
			return msp;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	@Override
	public MassSpecUISettings getSourceMassSpecEntrySettings() {
		MassSpecProperty msp = (MassSpecProperty) getEntryParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		return entrySettings;
	}


	@Override
	public MassSpecUISettings getEntrySettingsForInternalStandardFiles() {
		Entry entry = getEntryForCurrentViewer();
		MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) entry.getProperty();
		MassSpecUISettings entrySettings = msep.getMSAnnotationParentProperty().getMSAnnotationMetaData();
		return entrySettings;
	}
	
	@Override
	protected void updateSettings() {
		MassSpecProperty property = (MassSpecProperty) getEntryParentProperty();
		// need to save the projectEntry to cause the data files for the MassSpecProperty to be updated
		try {
			Entry projectEntry = getEntryForCurrentViewer();
			Entry topEntry = MSAnnotationProperty.getFirstAnnotEntry(projectEntry);

			Entry msEntry = topEntry.getParent();

			String settingsFile = MassSpecProperty.getFullyQualifiedFolderName(msEntry) + File.separator + property.getMSSettingsFile().getName();
			
			property
			.marshallSettingsFile(settingsFile, property.getMassSpecMetaData());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);			
		}
	}

	@Override
	public void updateViewer() {
		try {
			MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) getCurrentViewer();
			List<String> sKeyVals = getColumnKeyLabels();
			viewer.reLoadStructureAnnotationTab(sKeyVals);
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);			
		}
	}


}
