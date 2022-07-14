package org.grits.toolbox.entry.ms.annotation.glycan.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class ShowMSGlycanAnnotationStandardQuantificationDialog {
	private static final Logger logger = Logger.getLogger(ShowMSGlycanAnnotationStandardQuantificationDialog.class);

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell) {
		MSGlycanAnnotationMultiPageViewer curView = null;
		if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer ) {
			curView = (MSGlycanAnnotationMultiPageViewer) part.getObject();
		}
		if( curView == null ) {
			logger.error("An MS Glycan report must be open and active in order to use the Standard Quantification option");
			ErrorUtils.createWarningMessageBox(shell, "No open report", "An MS Glycan report must be open and active in order to use the Standard Quantification option");
			return null;
		}
		Entry msEntry = curView.getEntry();
		Property msProp = msEntry.getProperty();
		if( msProp instanceof MassSpecEntityProperty ) {
			if( MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog == null || 
					MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell() == null || 
					MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell().isDisposed() ) {
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog  = new MSGlycanAnnotationStandardQuantApplyDialog(shell, curView);
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.addListener(curView);
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.open();
			} else {
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationStandardQuantApplyDialog.getShell().forceActive();
			}
			return null;
		} 
		logger.error("An MS Glycan report must be open and active in order to use the Filter option");
		ErrorUtils.createWarningMessageBox(shell, "No open report", "An MS Glycan report must be open and active in order to use the Standard Quantification option");
		return null;

	}

	@CanExecute
	public boolean isEnabled(@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService) {
		MassSpecMultiPageViewer curView = null;
		if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer ) {
			curView = (MSGlycanAnnotationMultiPageViewer) part.getObject();
		}
		else { // try to find an open part of the required type
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						curView = (MSGlycanAnnotationMultiPageViewer) part.getObject();
					}
				}
			}
		}
		if (curView == null)
			return false;
		else {
			Entry msEntry = curView.getEntry();
			Property msProp = msEntry.getProperty();
			if( msProp instanceof MSAnnotationEntityProperty ) {
				MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) msProp;
				if( msAnnotEntityProp.getMsLevel() == 2 ) {
					return true;
				}
			}
			return false;
		}
	}
}
