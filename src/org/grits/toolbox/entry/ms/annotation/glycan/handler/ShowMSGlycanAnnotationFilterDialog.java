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
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationFilterWindow;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class ShowMSGlycanAnnotationFilterDialog {
	private static final Logger logger = Logger.getLogger(ShowMSGlycanAnnotationFilterDialog.class);

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell,
			EPartService partService) {
		MassSpecMultiPageViewer curView = null;
		if (part != null && part.getObject() instanceof MSGlycanAnnotationMultiPageViewer ) {
			curView = (MSGlycanAnnotationMultiPageViewer) part.getObject();
		}
		else { // try to find an open part of the required type
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSGlycanAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						curView = (MSGlycanAnnotationMultiPageViewer) mPart.getObject();
						if (curView.getEntry().getProperty() instanceof MSAnnotationEntityProperty) {
							MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) curView.getEntry().getProperty();
							if( msAnnotEntityProp.getMsLevel() == 2 ) {
								break;
							}
						}
					}
				}
			}
		}
		if( curView == null ) {
			logger.warn("An MS Glycan table must be open and active in order to use the Filter option");
			ErrorUtils.createWarningMessageBox(shell, "No open annotation table", "An annotation table must be open and active in order to use the Filter option");
			return;
		}
		Entry msEntry = curView.getEntry();
		Property msProp = msEntry.getProperty();
		if( msProp instanceof MSAnnotationEntityProperty ) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) msProp;
			if( msAnnotEntityProp.getMsLevel() == 2 ) {
				final MSGlycanAnnotationFilterWindow win = new MSGlycanAnnotationFilterWindow(shell, msEntry, part);
				win.open();
				return;
			}
		} 
		logger.warn("An MS Glycan master table must be open and active in order to use the Filter option");
		ErrorUtils.createWarningMessageBox(shell, "No open Annotation table", "You cannot apply filters for this level");
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
						if (curView.getEntry().getProperty() instanceof MSAnnotationEntityProperty) {
							MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) curView.getEntry().getProperty();
							if( msAnnotEntityProp.getMsLevel() == 2 ) {
								break;
							}
						}
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
