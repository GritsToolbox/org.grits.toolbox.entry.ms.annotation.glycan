package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationCustomAnnotationsPreferenceUI;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanCustomAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.dialog.MassSpecCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

/**
 * Extends the parent MassSpecCustomAnnotationModifyDialog to allow adding, removing, and editing of glycan annotation
 * of MS data.
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationCustomAnnotationDialog extends MassSpecCustomAnnotationDialog  {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationCustomAnnotationDialog.class);

	public MSGlycanAnnotationCustomAnnotationDialog(Shell parentShell, MassSpecMultiPageViewer curView) {
		super(parentShell, curView);
		setShellStyle(SWT.MODELESS | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.ON_TOP);
		initStoredAnnotations();
	}

	@Override
	public void initStoredAnnotations() {
		storedAnnotations = MSGlycanAnnotationCustomAnnotationsPreferenceUI.loadWorkspacePreferences();				
	}

	@Override
	protected void initLocalAnnotations() {
		localAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(getMassSpecEntry(), localAnnotations);
		entryAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(getMassSpecEntry(), entryAnnotations);
	}	

	@Override
	protected void setCurrentAnnotationValues(String selAnnotName) {
		// TODO Auto-generated method stub
		super.setCurrentAnnotationValues(selAnnotName);
	}
	
	@Override
	public MassSpecMultiPageViewer getCurrentViewer() {
		try {
			EPartService partService = getContextViewer().getPartService();
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
						if(viewer != null && viewer.getEntry().getProperty() != null && viewer.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {						
							return viewer;
						}
					}
				}
			}	
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected void clearAnnotation() {
		MSAnnotationEntityProperty ep = (MSAnnotationEntityProperty) getMassSpecEntry().getProperty();
		MSAnnotationProperty prop = (MSAnnotationProperty) ep.getParentProperty();
		MSGlycanAnnotationMetaData msSettings = null;
		if( ! (prop.getMSAnnotationMetaData() instanceof MSGlycanAnnotationMetaData) ) {
			msSettings = new MSGlycanAnnotationMetaData();
			msSettings.clone( prop.getMSAnnotationMetaData() );
			prop.setMSAnnotationMetaData(msSettings);
		} else {
			msSettings = (MSGlycanAnnotationMetaData) prop.getMSAnnotationMetaData();
		}		
		setCurrentAnnotationValues( cmbSelectAnnotation.getText().trim() );
		try {
			String sFileName = prop.getFullyQualifiedMetaDataFileName(getMassSpecEntry());
			if( msSettings.getCustomAnnotations().contains(selAnnot) ) {
				msSettings.getCustomAnnotations().remove(selAnnot);
			}
			msSettings.updateCustomAnotationData();
			MSAnnotationProperty.updateMSSettings(msSettings, sFileName);
			initLocalAnnotations();
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) getCurrentViewer();
		try {
			MSAnnotationTableDataProcessor proc = (MSAnnotationTableDataProcessor) viewer.getPeaksView().get(0).getTableDataProcessor();
			proc.writeArchive();
//			viewer.reLoadStructureAnnotationTab(selAnnot);
			updateViewer(null);
			initStoredAnnotationsList();
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}		
	}

	@Override
	protected void performAnnotation() {
		MSAnnotationEntityProperty ep = (MSAnnotationEntityProperty) getMassSpecEntry().getProperty();
		MSAnnotationProperty prop = (MSAnnotationProperty) ep.getParentProperty();
		MSGlycanAnnotationMetaData msSettings = null;
		if( ! (prop.getMSAnnotationMetaData() instanceof MSGlycanAnnotationMetaData) ) {
			msSettings = new MSGlycanAnnotationMetaData();
			msSettings.clone( prop.getMSAnnotationMetaData() );
			prop.setMSAnnotationMetaData(msSettings);
		} else {
			msSettings = (MSGlycanAnnotationMetaData) prop.getMSAnnotationMetaData();
		}
		setCurrentAnnotationValues( cmbSelectAnnotation.getText().trim() );
		try {
			if( msSettings.getCustomAnnotations().contains(selAnnot) ) {
				msSettings.getCustomAnnotations().remove(selAnnot);
			}
			if( selAnnot != null ) { // nice place to fix the possible scenario where a null annotation was added before..ugh
				msSettings.getCustomAnnotations().add(selAnnot);
			}
			String sFileName = prop.getFullyQualifiedMetaDataFileName(getMassSpecEntry());				
			msSettings.updateCustomAnotationData();
			MSAnnotationProperty.updateMSSettings(msSettings, sFileName);
			initLocalAnnotations();
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
//		MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) getCurrentViewer();
		try {
//			viewer.reLoadStructureAnnotationTab(selAnnot);
			List<String> sKeyVals = getColumnKeyLabels();
			updateViewer(sKeyVals);
			initStoredAnnotationsList();
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}		
	}
	
	/**
	 * Determines the current viewer and refreshes the GRITS tables that were updated with the quantitation.
	 * For MSGlycanAnnotation data, this is Structure Annotation tab. It then attempts to move the columns containing the specified key
	 * values to the beginning of the table.
	 * 
	 * @param sKeyVals, list of key values to move to beginning of the table
	 */
	protected void updateViewer(List<String> sKeyVals) {
		try {
			MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) getCurrentViewer();
			viewer.reLoadStructureAnnotationTab(sKeyVals);
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);			
		}
	}
	
	@Override
	protected void modifyAnnotations() {
		MSGlycanAnnotationMultiPageViewer curView = (MSGlycanAnnotationMultiPageViewer) getCurrentViewer();
		if (curView == null)
			return;
		Entry msEntry = curView.getEntry();
		if( msEntry == null ) {
			return;
		}
		Property msProp = msEntry.getProperty();
		if( msProp instanceof MSAnnotationEntityProperty ) {
			if( win == null ) {
				win = new MSGlycanAnnotationAnnotationModifyDialog(getShell(), curView);
				win.setLocalAnnotations(getLocalAnnotations());
				win.setEntryAnnotations(getEntryAnnotations());
				win.setSelectedAnnotation(cmbSelectAnnotation.getText());
				win.addListener(this);
				win.open();
				return;
			} else {
				win.getShell().forceActive();
			}
		} 		
	}

	@Override
	protected String getFormTitle() {
		return "Modify MS Glycan Custom Annotations";
	}

}
