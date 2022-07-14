package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationCustomAnnotationsPreferenceUI;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.MSGlycanAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.dialog.MassSpecCustomAnnotationModifyDialog;
import org.grits.toolbox.entry.ms.preference.MassSpecCustomAnnotationsPreferenceUI;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

/**
 * Extends the parent MassSpecCustomAnnotationModifyDialog to provide annotation information specific to glycan annotation
 * of MS data.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationAnnotationModifyDialog extends MassSpecCustomAnnotationModifyDialog {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationAnnotationModifyDialog.class);
	
	public MSGlycanAnnotationAnnotationModifyDialog(Shell parentShell, MassSpecMultiPageViewer curView) {
		super(parentShell, curView);
		setShellStyle(SWT.MODELESS | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.ON_TOP);
	}

	@Override
	protected Entry getEntryForCurrentViewer() {
		MSGlycanAnnotationMultiPageViewer viewer = (MSGlycanAnnotationMultiPageViewer) getContextViewer();
		if( viewer == null || ! (viewer.getEntry().getProperty() instanceof MassSpecEntityProperty) ) {
			logger.warn("No MS Glycan Results are open.\nPlease open the view and then apply custom annotations.");
			ErrorUtils.createWarningMessageBox(
					Display.getCurrent().getActiveShell(),
					"Unable to Perform Filter", "No MS Glycan Results are open.\nPlease open the view and then apply custom annotations.");
			return null;
		}
		return viewer.getEntry();
	}


	@Override
	protected String getFormTitle() {
		return "MS Glycan Custom Annotation Filter";
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.dialog.MassSpecCustomAnnotationModifyDialog#getNewMassSpecCustomAnnotationsPreferenceUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected MassSpecCustomAnnotationsPreferenceUI getNewMassSpecCustomAnnotationsPreferenceUI(Composite container) {
		return new MSGlycanAnnotationCustomAnnotationsPreferenceUI(container, SWT.BORDER, this, true);
	}	
}
