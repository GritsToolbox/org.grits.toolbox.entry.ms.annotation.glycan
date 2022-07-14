package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExternalQuantDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

/**
 * Extends the parent MassSpecExternalQuantDialog to facilitate adding and/or removing external quant data to 
 * annotated MS data.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationExternalQuantDialog extends MSAnnotationExternalQuantDialog  {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationExternalQuantDialog.class);

	public MSGlycanAnnotationExternalQuantDialog(Shell parentShell, MassSpecMultiPageViewer curView) {
		super(parentShell, curView);
		setShellStyle(SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.DIALOG_TRIM );
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

}
