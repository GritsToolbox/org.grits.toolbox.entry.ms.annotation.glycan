package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanCustomFilterSelection;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterCateogoryPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationFilterWindow;
import org.grits.toolbox.ms.om.data.AnnotationFilter;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class MSGlycanAnnotationFilterWindow extends MSAnnotationFilterWindow implements FilterChangedListener{
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationFilterWindow.class);
	
	private FilterTableSetup filterTableSetup;
	
	private FiltersLibrary filterLibrary;
	private List<Filter> preFilters;
	private FilterSetting filterSetting;
	private Button keepOption;
	private Button noSelectionOption;
	private MSGlycanCustomFilterSelection customFilterSelection;
	private Category preferredCategory;
	private boolean highlightOnly = true;

	private Button btnOverrideManualAnnotations2;

	public MSGlycanAnnotationFilterWindow(Shell parentShell, Entry entry, MPart part) {
		super(parentShell, entry, part);
		loadFilterList();
		loadFilterCategoryPreference();
	}
	
	private void loadFilterCategoryPreference() {
		try {
			MSGlycanFilterCateogoryPreference preferences = MSGlycanFilterCateogoryPreference.getMSGlycanFilterCategoryPreferences (
					MSGlycanFilterCateogoryPreference.getPreferenceEntity());
			if (preferences != null) 
				preferredCategory = preferences.getCategoryPreference();
		} catch (UnsupportedVersionException e) {
			logger.error("Cannot load filter preference");
		}
	}

	private void loadFilterList() {
		try {
			filterLibrary = FileUtils.readFilters(FileUtils.getFilterPath());
			preFilters = filterLibrary.getFilters();
		} catch (UnsupportedEncodingException e) {
			logger.error("Error loading the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Cannot locate the filters file", e);
		} catch (JAXBException e) {
			logger.error("Error loading the filters", e);
		}
	}
	
	@Override
	protected void createFilterSection(Composite parent) {
		MSGlycanAnnotationMultiPageViewer parentViewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), 
				getMSAnnotationEntry());
		
		Group filterGroup = new Group (parent, SWT.SHADOW_IN);
		filterGroup.setText("Filters");
		filterGroup.setLayout(new GridLayout(4, false));
		filterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Label filterWarning = new Label(filterGroup, SWT.BOLD | SWT.WRAP);
		filterWarning.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		filterWarning.setText(MSGlycanAnnotationFilterView.FILTERWARNING);
		filterWarning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		MSGlycanAnnotationFilterSetup.addHelpButton(filterGroup, MSGlycanAnnotationFilterView.FILTERMESSAGE);
		new Label(filterGroup, SWT.NONE);
		new Label(filterGroup, SWT.NONE);
		
		Button highlightButton = new Button (filterGroup, SWT.CHECK);
		highlightButton.setText("Highlight Only");
		highlightButton.setSelection(true);
		highlightButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		highlightButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				highlightOnly = highlightButton.getSelection();
				btnOverrideManualAnnotations2.setEnabled(!highlightOnly);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		btnOverrideManualAnnotations2 = new Button(filterGroup, SWT.CHECK);
		btnOverrideManualAnnotations2.setText("Override manually selected annotations");
		btnOverrideManualAnnotations2.setEnabled(false);
		GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData3.horizontalSpan = 4;
		gridData3.verticalSpan = 1;
		btnOverrideManualAnnotations2.setLayoutData(gridData3);
		
		if (parentViewer != null && parentViewer.getFilter() != null)
			btnOverrideManualAnnotations2.setEnabled(false);
		
		Composite comp1 = new Composite(filterGroup, SWT.NONE);
		comp1.setLayout(new GridLayout(4, false));
		comp1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1));
		
		addSelectFilterItem(comp1);
		
		Composite container = new Composite(filterGroup, SWT.NONE);
		container.setLayout(new FillLayout());
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4);
		gd_container.minimumHeight = 150;
		container.setLayoutData(gd_container);
		
		if (preFilters == null)
			return;
		
		//filterTableSetup = new FilterTableSetup();
		if (filterLibrary != null)
			filterTableSetup = new MSGlycanAnnotationFilterSetup(filterLibrary.getCategories());
		else
			filterTableSetup = new MSGlycanAnnotationFilterSetup();
		filterTableSetup.setFilterList(preFilters);
		((MSGlycanAnnotationFilterSetup)filterTableSetup).setSelectedCategory(preferredCategory);
		((MSGlycanAnnotationFilterSetup)filterTableSetup).setFilterMessage(MSGlycanAnnotationFilterView.FILTERMESSAGE);
		try {
			filterTableSetup.createFilterTableSection(container);
			if (filterSetting != null)
				filterTableSetup.setExistingFilters (filterSetting);
			filterTableSetup.addFilterChangedListener(this);
		} catch (Exception e) {
			logger.error("Error creating the filter table", e);
			MessageDialog.openError(getShell(), "Error", "Error creating the filter table!");
		}	
		customFilterSelection.setFilterTableSetup(filterTableSetup);
		
		// display existing filters if any
		if (parentViewer != null && parentViewer.getFilter() != null) {
			filterTableSetup.setExistingFilters(parentViewer.getFilter().getFilterSetting());
		//	filterTableSetup.setEnabled(false);
		}
		
		Label noMatchLabel = new Label(container, SWT.NONE);
		noMatchLabel.setText("Choose how to handle 'no match' cases");
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		noMatchLabel.setLayoutData(gd);
		
		keepOption = new Button(container, SWT.RADIO);
		keepOption.setText("Keep existing selections");
		gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		keepOption.setLayoutData(gd);
		keepOption.setSelection(true);
		
		noSelectionOption = new Button(container, SWT.RADIO);
		noSelectionOption.setText("Do not select anything");
		gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		noSelectionOption.setLayoutData(gd);
	}
	
	protected void addSelectFilterItem( Composite parent ) {
		customFilterSelection = new MSGlycanCustomFilterSelection(this);
		customFilterSelection.createFilterSelectionArea(parent);
	}
		
	boolean getKeepExistingOption() {
		return keepOption.getSelection();
	}

	@Override
	protected String getTitle() {
		return "MS Glycan Annotation Filter";
	}

	@Override
	protected String getAnnotationLabelText() {
		return "MS Glycan Annotation";
	}
	
	@Override
	public void validateInput(){
		txtOutput.setText(PARAMS_OK);
		if( getMSAnnotationEntry() == null ) {
			txtOutput.setText("Please select MS Annotation Results");
		} else if( getFilterKey() == null && filterTableSetup.getFilterSetting() == null) {
			txtOutput.setText("Please select a filter");			
		} else if( getNumTopHits() == -2 ) {
			txtOutput.setText("Invalid value for 'Num Top Hits'. Please enter 'All' or an integer greater than 0.");			
		}
		if (txtOutput.getText().equals(PARAMS_OK)) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	@Override
	protected void okPressed() {
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), 
				getMSAnnotationEntry());
		if (viewer == null) {
			// should not happen
		} else {
			AnnotationFilter filter = new AnnotationFilter();
			filter.setFilterSetting(filterTableSetup.getFilterSetting());
			filter.setNumTopHits(getNumTopHits());
			filter.setColumnKey(getFilterKey());
			viewer.setFilter(filter);
			viewer.sendFilterChanged();
			viewer.applyFilter(filter.getFilterSetting(), null, -1, 
					getOverrideManualAnnotations2(), getKeepExistingOption(), highlightOnly);
		}
		super.okPressed();
	}
	
	public boolean getOverrideManualAnnotations2() {
		return this.btnOverrideManualAnnotations2.getSelection();
	}
	
    @Override
	protected void createScoreFilterSection(Composite container) {
		// no score filter section here
	}

	@Override
	public void filterChanged() {
		validateInput();
	}
}
