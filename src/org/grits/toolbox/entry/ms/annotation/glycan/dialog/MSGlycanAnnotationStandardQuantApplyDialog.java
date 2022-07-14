package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationStandardQuantPreferenceUI;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantFileGrid;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantModifyDialog;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.MassSpecStandardQuantPreferenceUI;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSGlycanAnnotationStandardQuantApplyDialog extends MSAnnotationStandardQuantApplyDialog {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationStandardQuantApplyDialog.class);

	public MSGlycanAnnotationStandardQuantApplyDialog(Shell parentShell, MassSpecMultiPageViewer contextViewer) {
		super(parentShell, contextViewer);
	}
	
	/**
	 * @return the current open MSAnnotationMultiPageViewer
	 */
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
	protected MassSpecStandardQuantFileGrid getNewMassSpecStandardQuantFileGrid( Composite parent, MassSpecMultiPageViewer curView ) {
		return new MSGlycanAnnotationStandardQuantFileGrid(parent, curView);
	}
	
	@Override
	protected MassSpecStandardQuantModifyDialog getNewQuantModifyDialog( Shell shell, MassSpecMultiPageViewer viewer ) {
		return new MSGlycanAnnotationStandardQuantModifyDialog(shell, viewer);
	}
		
	/**
	 * Initializes the 2 MassSpecPreference objects according to what is stored in the MS Metadata for this entry.
	 */
	protected void initLocalStandardQuant() {
		localStandardQuant = new MassSpecPreference();
		MassSpecUISettings msSettings = MSGlycanAnnotationStandardQuantPreferenceUI.getMassSpecUISettingsFromEntry(getMassSpecEntry());
		MassSpecStandardQuantPreferenceUI.initStandardQuantFromEntry(msSettings, localStandardQuant);
		entryStandardQuant = new MassSpecPreference();
		MassSpecStandardQuantPreferenceUI.initStandardQuantFromEntry(msSettings, entryStandardQuant);
	}	

}
