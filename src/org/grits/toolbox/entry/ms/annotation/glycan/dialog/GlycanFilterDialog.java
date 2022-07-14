package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.dialog.FilterDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.adaptor.MSGlycanAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.FilterSettingLibrary;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationFilterPreferenceUI;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterCateogoryPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterPreference;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class GlycanFilterDialog extends FilterDialog implements FilterChangedListener {
	private static final Logger logger = Logger.getLogger(GlycanFilterDialog.class);

	private List<Filter> preFilters = null;
	FilterTableSetup filterTableSetup;
	FilterSetting filterSetting;

	private Combo cmbSelectFilter;

	private FilterSettingLibrary library;
	private List<Category> filterCategories;
	private Category preferredCategory;
	
	public GlycanFilterDialog(Shell parentShell) {
		super(parentShell);
		loadFilterPreferences();
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	private void loadFilterPreferences() {
		try {
			MSGlycanFilterPreference preferences = MSGlycanFilterPreference.getMSGlycanFilterPreferences (
					MSGlycanFilterPreference.getPreferenceEntity());
			MSGlycanFilterCateogoryPreference categoryPreferences = MSGlycanFilterCateogoryPreference.getMSGlycanFilterCategoryPreferences (
					MSGlycanFilterCateogoryPreference.getPreferenceEntity());
			if (preferences != null) 
				library = preferences.getFilterSettings();
			if (categoryPreferences != null)
				preferredCategory = categoryPreferences.getCategoryPreference();
		} catch (UnsupportedVersionException e) {
			logger.error("Cannot load filter preference");
		}
	}

	public void setPreFilters(List<Filter> preFilters) {
		this.preFilters = preFilters;
	}
	
	@Override
	public void filterChanged() {
		if (filterTableSetup != null) {
			filterSetting = filterTableSetup.getFilterSetting();
		}
	}
	
	public FilterSetting getFilterSetting() {
		return filterSetting;
	}
	
	public void setFilterSetting(FilterSetting filterSetting) {
		this.filterSetting = filterSetting;
	}
	
	@Override
	protected void createFilterTable(Composite parent) {
		
		addSelectFilterItem(parent);
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		GridData gd_container = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 4);
		gd_container.minimumHeight = 150;
		container.setLayoutData(gd_container);
		
		if (filterCategories != null)
			filterTableSetup = new MSGlycanAnnotationFilterSetup(filterCategories);
		else
			filterTableSetup = new MSGlycanAnnotationFilterSetup();
		
		filterTableSetup.setFilterList(preFilters);
		if (preferredCategory != null)
			((MSGlycanAnnotationFilterSetup)filterTableSetup).setSelectedCategory(preferredCategory);
		try {
			filterTableSetup.createFilterTableSection(container);
			if (filterSetting != null)
				filterTableSetup.setExistingFilters (filterSetting);
			filterTableSetup.addFilterChangedListener(this);
		} catch (Exception e) {
			logger.error("Error creating the filter table", e);
			MessageDialog.openError(getShell(), "Error", "Error creating the filter table!");
		}
	}
	
	protected void addSelectFilterItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectFilter = new Label(parent, SWT.NONE);
		lblSelectFilter.setText("Current Filters");
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
	
	@Override
	protected void okPressed() {
		if (this.exportFileAdapter != null) {
			((MSGlycanAnnotationExportFileAdapter)this.exportFileAdapter).setFilterSetting(getFilterSetting());
			this.exportFileAdapter.setFilterColumn(getFilterKey());
			this.exportFileAdapter.setTopHits(getNumTopHits());
			this.exportFileAdapter.setThresholdValue(getThresholdValue());
		}
		super.okPressed();
	}

	public void setCategories(List<Category> categories) {
		this.filterCategories = categories;
	}

}
