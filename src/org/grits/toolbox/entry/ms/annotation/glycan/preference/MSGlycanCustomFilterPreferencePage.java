package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MSGlycanCustomFilterPreferencePage extends PreferencePage implements IPropertyChangeListener {
	private static final Logger logger = Logger.getLogger(MSGlycanCustomFilterPreferencePage.class);
	
	private MSGlycanAnnotationFilterPreferenceUI filterPreferenceUI;
	private MSGlycanFilterPreference filterPreferences = null;
	private MSGlycanFilterCateogoryPreference categoryPreferences = null;
	
	public MSGlycanCustomFilterPreferencePage() {
		loadFilterPreferences();
	}
	
	private void loadFilterPreferences() {
		try {
			filterPreferences = MSGlycanFilterPreference.getMSGlycanFilterPreferences(MSGlycanFilterPreference.getPreferenceEntity());
			categoryPreferences = MSGlycanFilterCateogoryPreference.getMSGlycanFilterCategoryPreferences(MSGlycanFilterCateogoryPreference.getPreferenceEntity());
		} catch (Exception e) {
			logger.error("Error getting filter preferences", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		filterPreferenceUI = new MSGlycanAnnotationFilterPreferenceUI(parent, SWT.BORDER, this);
		filterPreferenceUI.setPreferences(filterPreferences);
		filterPreferenceUI.setCategoryPreferences(categoryPreferences);
		filterPreferenceUI.initComponents();
		
		return parent;
	}
	
	@Override
	public boolean isValid() {
    	if (!filterPreferenceUI.isPageComplete()) {
    		setErrorMessage (filterPreferenceUI.getErrorMessage());
    		return false;
    	}
        setErrorMessage(null);
        return true;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		setValid(isValid());
	}
	
	@Override
	//when apply button is clicked
	protected void performApply() {
		//to check if everything is ok or not
		//but why we need this?
		save();
	}

	@Override
	public boolean performOk() {
		//need to save
		save();
		return true;
	}
	
	private void save() {
		try {
			filterPreferenceUI.updatePreferences();
			logger.debug("Time to save values!");
			filterPreferences.saveValues();
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
