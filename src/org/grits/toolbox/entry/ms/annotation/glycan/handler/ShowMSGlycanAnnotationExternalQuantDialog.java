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
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationExternalQuantDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class ShowMSGlycanAnnotationExternalQuantDialog {
	private static final Logger logger = Logger.getLogger(ShowMSGlycanAnnotationExternalQuantDialog.class);

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
			MSGlycanAnnotationExternalQuantDialog mseqd = new MSGlycanAnnotationExternalQuantDialog(shell, curView);
			mseqd.addListener(curView);
			mseqd.open();
			//			}
		} else {
			logger.error("An MS Glycan Annotation must be open and active in order to apply external quantitation");
			ErrorUtils.createWarningMessageBox(shell, "No open annotation", "An MS Glycan Annotation must be open and active in order to use the Custom Annotation");
		}
		return null;
	}
	
	@CanExecute
	public boolean isEnabled(@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService) {
		MSGlycanAnnotationMultiPageViewer curView = null;
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
