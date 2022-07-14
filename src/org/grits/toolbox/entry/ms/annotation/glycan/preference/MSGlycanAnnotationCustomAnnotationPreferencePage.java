package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.grits.toolbox.entry.ms.preference.MassSpecCustomAnnotationPreferencePage;

public class MSGlycanAnnotationCustomAnnotationPreferencePage extends MassSpecCustomAnnotationPreferencePage {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationCustomAnnotationPreferencePage.class);
	MSGlycanCustomAnnotationPreference customPreferences;
	
	public MSGlycanAnnotationCustomAnnotationPreferencePage() {
		customPreferences = (MSGlycanCustomAnnotationPreference) MSGlycanAnnotationPreferenceLoader.getMSGlycanCustomAnnotationPreference();
	}

	@Override
	protected Control createContents(Composite parent) {
		customAnnotationPreference = new MSGlycanAnnotationCustomAnnotationsPreferenceUI(parent, SWT.BORDER, this, false);
		customAnnotationPreference.setLocalAnnotations(customPreferences);
		customAnnotationPreference.initComponents();
		
		return parent;
	}
	
	@Override
	protected void performDefaults() {
		boolean load = MessageDialog.openConfirm(getShell(), "Are you sure?", 
				"This will remove all the preferences you've created and load the default ones if any. Do you want to continue?");
		if (load) {
			customPreferences = new MSGlycanCustomAnnotationPreference();
			customPreferences.loadDefaultCustomAnnotations();
			
			customPreferences.saveValues();
			customAnnotationPreference.clearValues();
			customAnnotationPreference.initStoredAnnotations();
			customAnnotationPreference.initLocalAnnotations();
			customAnnotationPreference.processSelection(null);
			customAnnotationPreference.updateUI();			
		}
	}
	
}
