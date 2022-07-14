package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanCustomFilterSelection;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterCateogoryPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationFilterView;
import org.grits.toolbox.ms.om.data.AnnotationFilter;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class MSGlycanAnnotationFilterView extends MSAnnotationFilterView implements FilterChangedListener, IPropertyChangeListener {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationFilterView.class);
	
	public static final String FILTERMESSAGE = "Please note that, the \"stucture based\" filters you select are applied first to change the selected candidates, "
			+ "then the score based filters are applied to further restrict the selections";
	public static final String FILTERWARNING = "The following settings will affect the glycan candidate selection in the structure table. "
			+ "\nIf you don't want the structure filters to modify your selections, select \"Highlight Only\" option.";

	private FiltersLibrary filterLibrary;
	private List<Filter> filterList;
	private Button applyButton;
	private Button applyScoreFilterButton;
	private Combo filterCombo;
	private FilterTableSetup filterTable;
	private Label txtOutput;
	private Text txtNumTopHits;
	private Button btnOverrideManualAnnotations2;
	private List<CustomExtraData> customExtraData;
	private Category preferredCategory;
	private boolean highlightOnly = true;

	private Color sectionColor;
	private Color backgroundColor;

	@Inject
	public MSGlycanAnnotationFilterView(Entry entry) {
		super(entry);
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
			filterList = filterLibrary.getFilters();
		} catch (UnsupportedEncodingException e) {
			logger.error("Error loading the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Cannot locate the filters file", e);
		} catch (JAXBException e) {
			logger.error("Error loading the filters", e);
		}
	}
	
	@Override
	protected void addFilterSettings () {
		sectionColor = new Color(Display.getCurrent(), 20, 199, 255);
		backgroundColor = Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND);
		MSGlycanAnnotationMultiPageViewer parentViewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(MSGlycanAnnotationFilterView.this.getPart().getContext(), this.entry.getParent());
		createGlycanFilterSection (parentViewer);
		createScoreFilterSection (parentViewer);
	}

	private void createGlycanFilterSection (MSGlycanAnnotationMultiPageViewer parentViewer) {
		Section section = new Section(getContainer(), Section.TREE_NODE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Filter Settings");
		
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		section.setLayoutData(gridData);
		
		Composite filterComposite = new Composite(section, SWT.WRAP);
		filterComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 4));
		filterComposite.setBackground(backgroundColor);
		filterComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		Label filterWarning = new Label(filterComposite, SWT.BOLD | SWT.WRAP);
		filterWarning.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		filterWarning.setText(FILTERWARNING);
		filterWarning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2));
		
		MSGlycanAnnotationFilterSetup.addHelpButton(filterComposite, FILTERMESSAGE);
		
		Button highlightButton = new Button (filterComposite, SWT.CHECK);
		highlightButton.setText("Highlight Only");
		highlightButton.setSelection(true);
		highlightButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		highlightButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				highlightOnly = highlightButton.getSelection();
				btnOverrideManualAnnotations2.setEnabled(!highlightOnly);
				if (highlightOnly)
					btnOverrideManualAnnotations2.setSelection(false);
				applyButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		btnOverrideManualAnnotations2 = new Button(filterComposite, SWT.CHECK);
		btnOverrideManualAnnotations2.setText("Override manually selected annotations");
		btnOverrideManualAnnotations2.setEnabled(false);
		GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData3.horizontalSpan = 4;
		gridData3.verticalSpan = 1;
		btnOverrideManualAnnotations2.setLayoutData(gridData3);
		btnOverrideManualAnnotations2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		if (parentViewer != null && parentViewer.getFilter() != null)
			btnOverrideManualAnnotations2.setEnabled(false);
		
		Label dummy = new Label(filterComposite, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
				
		MSGlycanCustomFilterSelection customSelection = new MSGlycanCustomFilterSelection(this);
		customSelection.createFilterSelectionArea(filterComposite);
		
		if (filterLibrary != null)
			filterTable = new MSGlycanAnnotationFilterSetup(filterLibrary.getCategories());
		else
			filterTable = new MSGlycanAnnotationFilterSetup();
		filterTable.setFilterList(filterList);
		((MSGlycanAnnotationFilterSetup)filterTable).setSelectedCategory(preferredCategory);
		((MSGlycanAnnotationFilterSetup)filterTable).setFilterMessage(MSGlycanAnnotationFilterView.FILTERMESSAGE);
		try {
			filterTable.createFilterTableSection(filterComposite);			
			filterTable.addFilterChangedListener(this);
		} catch (Exception e) {
			logger.error("Cannot create filter table. ", e);
		}
		customSelection.setFilterTableSetup(filterTable);
		
		if (parentViewer != null && parentViewer.getFilter() != null) {
			filterTable.setExistingFilters(parentViewer.getFilter().getFilterSetting());
			filterTable.setEnabled(false);
		}
		
		Label noMatchLabel = new Label(filterComposite, SWT.NONE);
		noMatchLabel.setText("Choose how to handle 'no match' cases (Applicable only when \"Highlight Only\" is NOT checked)");
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		noMatchLabel.setLayoutData(gd);
		
		Button keepOption = new Button(filterComposite, SWT.RADIO);
		keepOption.setText("Keep existing selections");
		gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		keepOption.setLayoutData(gd);
		keepOption.setSelection(true);
		keepOption.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		if (parentViewer != null && parentViewer.getFilter() != null) {
			keepOption.setEnabled(false);
		}
		
		Button noSelectionOption = new Button(filterComposite, SWT.RADIO);
		noSelectionOption.setText("Do not select anything");
		gd = new GridData(SWT.LEFT, SWT.TOP, true, true, 4, 1);
		noSelectionOption.setLayoutData(gd);
		noSelectionOption.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		if (parentViewer != null && parentViewer.getFilter() != null) {
			noSelectionOption.setEnabled(false);
		}
		
		applyButton = new Button(filterComposite, SWT.PUSH);
		applyButton.setText("Apply Filters");
		applyButton.setEnabled(false);
		
		applyButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewer(MSGlycanAnnotationFilterView.this.getPart().getContext());
				if (viewer.getPeaksView().isEmpty()) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "Open MS Annotation Table first before applying filters");
					return;
				} else if (viewer.getPeaksView().get(0).getViewBase() == null) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "You cannot apply filters for this table");
					return;
				}
				AnnotationFilter filter = new AnnotationFilter();
				filter.setFilterSetting(filterTable.getFilterSetting());
				filter.setNumTopHits(getNumTopHits());
				filter.setColumnKey(getFilterKey());
				viewer.setFilter(filter);
				viewer.applyFilter(filter.getFilterSetting(), null, -1, btnOverrideManualAnnotations2.getSelection(), keepOption.getSelection(), highlightOnly);
			}

			private String getFilterKey() {
				if( ! filterCombo.getText().equals("") ) {
						for (CustomExtraData cnd : customExtraData) {
							if( filterCombo.getText().equals(cnd.getLabel()) ) {
								return cnd.getKey();
							}
						}			
				}
				return null;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		section.setClient(filterComposite);
	}
	
	private void createScoreFilterSection (MSGlycanAnnotationMultiPageViewer parentViewer) {
		Section scoreSection = new Section(getContainer(), Section.TITLE_BAR | Section.EXPANDED);
		scoreSection.setText("Score Filters");
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		scoreSection.setLayoutData(gridData);
		
		scoreSection.setTitleBarBackground(sectionColor);
		scoreSection.setBackground(backgroundColor);
		scoreSection.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		Composite scoreFilterComposite = new Composite(scoreSection, SWT.NONE);
		scoreFilterComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 6));
		scoreFilterComposite.setLayout(new GridLayout(6, false));
		scoreFilterComposite.setBackground(backgroundColor);
		scoreFilterComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		Label scoreWarning = new Label(scoreFilterComposite, SWT.BOLD | SWT.WRAP);
		scoreWarning.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		scoreWarning.setText("The following settings will affect the glycan candidate selection in the structure table!");
		scoreWarning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		
		Label dummy = new Label(scoreFilterComposite, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		
		dummy = new Label(scoreFilterComposite, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 1;
		Label lblFilter = new Label(scoreFilterComposite, SWT.NONE);
		lblFilter.setText("Choose a criterion to use as score filter");
		lblFilter.setLayoutData(gridData);
		
		createLists(parentViewer, scoreFilterComposite);
		
		Label lblNumTopHits = new Label(scoreFilterComposite, SWT.NONE);
		lblNumTopHits.setText("Number of Top Hits to Select: ");
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalSpan = 2;
		gridData2.verticalSpan = 1;
		lblNumTopHits.setLayoutData(gridData2);
		txtNumTopHits = new Text(scoreFilterComposite, SWT.BORDER);
		txtNumTopHits.setText("All");
		GridData gridData4 = new GridData(GridData.FILL_HORIZONTAL);
		gridData4.horizontalSpan = 4;
		gridData4.verticalSpan = 1;
		txtNumTopHits.setLayoutData(gridData4);
		
		if (parentViewer != null && parentViewer.getFilter() != null) {
			if (parentViewer.getFilter().getNumTopHits() == -1)
				txtNumTopHits.setText("All");
			else
				txtNumTopHits.setText(String.valueOf(parentViewer.getFilter().getNumTopHits()));
			txtNumTopHits.setEnabled(false);
		}
		txtNumTopHits.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!txtNumTopHits.getText().equals("All")) {
					try {
						Integer.parseInt(txtNumTopHits.getText());
						txtOutput.setText("");
						applyScoreFilterButton.setEnabled(true);
					} catch( NumberFormatException ex ) {
						txtOutput.setText("Invalid value for 'Num Top Hits'. Please enter 'All' or an integer greater than 0.");
						applyScoreFilterButton.setEnabled(false);
					}	
				} else {
					txtOutput.setText("");
					applyScoreFilterButton.setEnabled(true);
				}
			}
		});
		
		Button btnOverrideManualAnnotations = new Button(scoreFilterComposite, SWT.CHECK);
		btnOverrideManualAnnotations.setText("Override manually selected annotations");
		GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData3.horizontalSpan = 6;
		gridData3.verticalSpan = 1;
		btnOverrideManualAnnotations.setLayoutData(gridData3);
		btnOverrideManualAnnotations.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyScoreFilterButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		if (parentViewer != null && parentViewer.getFilter() != null)
			btnOverrideManualAnnotations.setEnabled(false);
		
		txtOutput = new Label(scoreFilterComposite, SWT.NONE);
		txtOutput.setLayoutData(gridData3);
		txtOutput.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		
		applyScoreFilterButton =  new Button(scoreFilterComposite, SWT.PUSH);
		applyScoreFilterButton.setText("Apply Score Filter");
		applyScoreFilterButton.setEnabled(false);
		
		applyScoreFilterButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewer(MSGlycanAnnotationFilterView.this.getPart().getContext());
				if (viewer.getPeaksView().isEmpty()) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "Open MS Annotation Table first before applying filters");
					return;
				} else if (viewer.getPeaksView().get(0).getViewBase() == null) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "You cannot apply filters for this table");
					return;
				}
				AnnotationFilter filter = new AnnotationFilter();
				filter.setFilterSetting(filterTable.getFilterSetting());
				filter.setNumTopHits(getNumTopHits());
				filter.setColumnKey(getFilterKey());
				viewer.setFilter(filter);
				viewer.applyFilter(null, getFilterKey(), getNumTopHits(), btnOverrideManualAnnotations.getSelection(), true, highlightOnly);
			}

			private String getFilterKey() {
				if( ! filterCombo.getText().equals("") ) {
						for (CustomExtraData cnd : customExtraData) {
							if( filterCombo.getText().equals(cnd.getLabel()) ) {
								return cnd.getKey();
							}
						}			
				}
				return null;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	
		scoreSection.setClient(scoreFilterComposite);
	}
	
	private int getNumTopHits() {
		if( txtNumTopHits.getText().equals("All") ) {
			return -1;
		}
		try {
			return Integer.parseInt(txtNumTopHits.getText());
		} catch( NumberFormatException ex ) {
			logger.error("Invalid value for 'Number of Top Hits'", ex);
		}
		return -2;	
	}
	
	private void createLists(MSGlycanAnnotationMultiPageViewer parentViewer, Composite parent) {
		if (this.filterCombo != null)
			this.filterCombo.removeAll();
		else {			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 4;
			gridData.verticalSpan = 1;
			filterCombo = new Combo(parent, SWT.SINGLE);
			filterCombo.setLayoutData(gridData);
		}
		filterCombo.add("");
		filterCombo.setEnabled(false);
		
		customExtraData = MSAnnotationTableDataProcessor.getMSAnnotationFeatureCustomExtraData(entry);
		if( customExtraData == null )
			return;
		int i=1; // since the first one is empty we need to skip that one
		for (CustomExtraData cnd : customExtraData) {
			filterCombo.add(cnd.getLabel());
			if (parentViewer != null && parentViewer.getFilter() != null) {
				if (cnd.getKey().equals(parentViewer.getFilter().getColumnKey()))
					filterCombo.select(i);
			}
			i++;
		}
		if (filterCombo.getItemCount() > 1) {
			if (parentViewer != null && parentViewer.getFilter() != null)
				filterCombo.setEnabled(false);
			else {
				filterCombo.setEnabled(true);
				filterCombo.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (!filterCombo.getText().equals("")) {
							if (getNumTopHits() != -2) {
								txtOutput.setText("");
								applyScoreFilterButton.setEnabled(true);
							}
							else {
								txtOutput.setText("Please select a criterion for the score filter");
								applyScoreFilterButton.setEnabled(false);
							}
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {	
					}
				});
			}
		}
	}

	/**
	 * this is called by the FilterTableSetup whenever a change has been made in the filter table
	 * 
	 */
	@Override
	public void filterChanged() {
		applyButton.setEnabled(true);
	}
	
	
	/**
	 * This event is generated when filter is applied by the MSGlycanAnnotationMultiPageViewer
	 * @param the viewer in which filter has changed (since there are multiple of these viewers)
	 */
	@Optional @Inject
	public void filterChangedInViewer (@UIEventTopic (MSGlycanAnnotationMultiPageViewer.EVENT_TOPIC_FILTER_CHANGED) MSGlycanAnnotationMultiPageViewer viewer) {
		if (viewer != null ) {
			AnnotationFilter filter = viewer.getFilter();
			if (filter != null) {
				if (filter.getFilterSetting() == null) {  // remove existing filters
					filterTable.resetFilters();
				} else { // change the existing filters
					filterTable.setExistingFilters(filter.getFilterSetting());
				}
				if (filter.getNumTopHits() == -1)
					txtNumTopHits.setText("All");
				else
					txtNumTopHits.setText(String.valueOf(filter.getNumTopHits()));
				if (filter.getColumnKey() != null) {
					int i=1;  // since the first one is empty we need to skip that one
					for (CustomExtraData cnd: customExtraData) {
						if (cnd.getKey().equals(filter.getColumnKey()))
							filterCombo.select(i);
						i++;
					}
				} else {
					filterCombo.select(0);
				}
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
