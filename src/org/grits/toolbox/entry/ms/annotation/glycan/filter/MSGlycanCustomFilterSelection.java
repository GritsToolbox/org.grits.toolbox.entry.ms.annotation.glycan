package org.grits.toolbox.entry.ms.annotation.glycan.filter;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.FilterSettingLibrary;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterPreference;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class MSGlycanCustomFilterSelection {
	private static final Logger logger = Logger.getLogger(MSGlycanCustomFilterSelection.class);
	
	private FilterSettingLibrary library;

	private FilterTableSetup filterTableSetup;

	private Combo cmbSelectFilter;

	private FilterSetting filterSetting;

	private FilterChangedListener listener;

	public MSGlycanCustomFilterSelection() {
		// load filter preferences
		loadFilterPreferences();
	}
	
	public MSGlycanCustomFilterSelection(FilterChangedListener listener) {
		this();
		this.listener = listener;	
	}
	
	private void loadFilterPreferences() {
		try {
			MSGlycanFilterPreference preferences = MSGlycanFilterPreference.getMSGlycanFilterPreferences (
					MSGlycanFilterPreference.getPreferenceEntity());
			if (preferences != null) 
				library = preferences.getFilterSettings();
		} catch (UnsupportedVersionException e) {
			logger.error("Cannot load filter preference");
		}
	}
	
	public void setFilterTableSetup(FilterTableSetup filterTableSetup) {
		this.filterTableSetup = filterTableSetup;
	}
	
	public void createFilterSelectionArea (Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectFilter = new Label(parent, SWT.NONE);
		lblSelectFilter.setText("Select from Stored Filters");
		lblSelectFilter.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		cmbSelectFilter = new Combo(parent, SWT.NONE);
		cmbSelectFilter.setLayoutData(gd2);
		initStoredFiltersList();
		cmbSelectFilter.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				processSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	protected void initStoredFiltersList() {
		cmbSelectFilter.removeAll();
		if ( library != null && library.getFilterSettings() != null ) {
			for (FilterSetting filter : library.getFilterSettings()) {
				cmbSelectFilter.add(filter.getName());
			}
		}
	}
	
	protected void processSelection() {
		if(!cmbSelectFilter.getText().trim().equals("") ) {
			setCurrentFilterValues(cmbSelectFilter.getText().trim());
			filterSetting = filterTableSetup.getFilterSetting();
			if (listener != null)
				listener.filterChanged();
		} 		
	}
	
	protected void setCurrentFilterValues(String selFilterName) {
		if( selFilterName == null ) {
			return;
		}
		FilterSetting selFilter = getCurrentFilter(selFilterName);
		if( selFilter == null ) {
			return;
		}
		if (filterTableSetup != null)
			filterTableSetup.setExistingFilters(selFilter);
	}
	
	protected FilterSetting getCurrentFilter( String selFilter ) {
		if (library != null && library.getFilterSettings() != null) {
			for( int i = 0; i < library.getFilterSettings().size(); i++ ) {
				FilterSetting curFilter =  library.getFilterSettings().get(i);
				if( curFilter.getName().equals(selFilter) ) {
					return curFilter;
				}
	
			}
		}
		return null;
	}
	
	public FilterSetting getFilterSetting() {
		return filterSetting;
	}
}
