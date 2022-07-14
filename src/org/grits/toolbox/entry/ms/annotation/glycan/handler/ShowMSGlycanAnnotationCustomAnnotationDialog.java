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
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class ShowMSGlycanAnnotationCustomAnnotationDialog {
	private static final Logger logger = Logger.getLogger(ShowMSGlycanAnnotationCustomAnnotationDialog.class);

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, EPartService partService) {
		MSGlycanAnnotationMultiPageViewer curView = null;
		if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer ) {
			curView = (MSGlycanAnnotationMultiPageViewer) part.getObject();
		} else {
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						curView = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
					}
				}
			}
		}
		if( curView == null ) {
			logger.error("An MS Glycan Annotation must be open and active in order to use the Custom Annotation");
			ErrorUtils.createWarningMessageBox(shell, "No open annotation", "An MS Glycan Annotation must be open and active in order to use the Custom Annotation");
			return null;
		}
		Entry msEntry = curView.getEntry();
		Property msProp = msEntry.getProperty();
		if( msProp instanceof MassSpecEntityProperty ) {
			if( MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog == null || 
					MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell() == null || 
							MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell().isDisposed() ) {
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog = new MSGlycanAnnotationCustomAnnotationDialog(shell, curView);
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.addListener(curView);
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.open();
			} else {
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell().forceActive();
			}
			return null;
			//			}
		} 
		ErrorUtils.createWarningMessageBox(shell, "No open annotation", "An MS Glycan Annotation must be open and active in order to use the Custom Annotation");
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
						curView = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
					}
				}
			}
		}
		if (curView == null)
			return false;
		return (curView.getClass().equals(MSGlycanAnnotationMultiPageViewer.class));
	}
}
