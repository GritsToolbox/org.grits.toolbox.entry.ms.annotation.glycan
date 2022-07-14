package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class MSGlycanAnnotationFilterPreferenceUI extends Composite implements FilterChangedListener {
	
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationFilterPreferenceUI.class);

	private static final String PAGE_COMPLETE_PROPERTY = "Page Complete";
	
	MSGlycanFilterPreference preferences=null;
	MSGlycanFilterCateogoryPreference categoryPreferences=null;
	FilterSettingLibrary library=null;
	private IPropertyChangeListener listener;
	private boolean isComplete=true;
	protected String errorMessage = null;

	private Layout gridLayout;
	private Combo cmbSelectFilter;
	private Button btnCreateNew;
	private Button btnDeleteCurrent;
	private Button btnEditCurrent;
	private Text txtDescription;
	private Text txtName;
	private Label lblDescription;
	private Label lblName;

	private boolean bIsDirty;

	private FilterSetting currentFilter;

	private String sFilterName;
	private String sDescription;
	
	List<Filter> filterList = new ArrayList<>();
	FiltersLibrary filterLibrary;

	private FilterTableSetup filterTableSetup;

	private Button btnExport;

	private Button btnImport;

	public MSGlycanAnnotationFilterPreferenceUI(Composite parent, int style, IPropertyChangeListener listener) {
		super(parent, style);
		this.listener = listener;
		loadFilterList();
	}

	private void loadFilterList() {
		try {
			filterLibrary = FilterUtils.readFilters(FileUtils.getFilterPath());
			filterList = filterLibrary.getFilters();
		} catch (UnsupportedEncodingException e) {
			logger.error("Error loading the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Cannot locate the filters file", e);
		} catch (JAXBException e) {
			logger.error("Error loading the filters", e);
		}
	}

	public void setPreferences(MSGlycanFilterPreference preferences) {
		this.preferences = preferences;
	}
	
	public MSGlycanFilterPreference getPreferences() {
		return preferences;
	}
	
	public void setCategoryPreferences(MSGlycanFilterCateogoryPreference categoryPreferences) {
		this.categoryPreferences = categoryPreferences;
	}
	
	public MSGlycanFilterCateogoryPreference getCategoryPreferences() {
		return categoryPreferences;
	}
	
	public List<FilterSetting> getPreferenceFilterSettings () {
		return this.preferences.filterSettings.getFilterSettings();
	}
	
	public void setPreferenceFilterSettings(FilterSettingLibrary l) {
		this.preferences.setFilterSettings(l);
	}
	
	private void addCurrentFilterSetting() {
		if (currentFilter == null)
			 return;
		if (library == null) {
			library = new FilterSettingLibrary();
		}
		
		if (library.getFilterSettings() == null)
			library.add(currentFilter);
		else { // remove the one with the same name, add the currentFilter
			FilterSetting toBeRemoved = null;
			for (FilterSetting filter : library.getFilterSettings()) {
				if (filter.equals(currentFilter))
					toBeRemoved = filter;
			}
			if (toBeRemoved != null)
				library.remove(toBeRemoved);
			library.add(currentFilter);
		}	
	}
	
	public void initComponents() {
		initGridLayout();
		setLayout(gridLayout);
		addSelectFilterItem(this);
		addExportCurrent(this);
		addDeleteCurrent(this);
		addEditCurrent(this);
		addCreateNewItem(this);
		addImport(this);
		addSeparatorLine1(this);
		
		addFilterNameItem(this);
		addDescriptionItem(this);
		
		addFilterTableSetup(this);
		
		setEditEnabled(false); // disable bottom entries until something is loaded
	}
	
	private void addFilterTableSetup(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		GridData gd_container = new GridData(SWT.LEFT, SWT.TOP, true, true, 6, 1);
		gd_container.minimumHeight = 150;
		container.setLayoutData(gd_container);
		if (filterLibrary.getCategories() != null)
			filterTableSetup = new MSGlycanAnnotationFilterSetup(filterLibrary.getCategories());
		else
			filterTableSetup = new MSGlycanAnnotationFilterSetup();
		filterTableSetup.setFilterList(filterList);
		if (categoryPreferences != null)
			((MSGlycanAnnotationFilterSetup)filterTableSetup).setSelectedCategory(categoryPreferences.getCategoryPreference());
		try {
			filterTableSetup.createFilterTableSection(container);
			filterTableSetup.addFilterChangedListener(this);
		} catch (Exception e) {
			logger.error("Error creating the filter table", e);
			MessageDialog.openError(getShell(), "Error", "Error creating the filter table!");
		}
		
	}

	protected void initGridLayout() {
		gridLayout = new GridLayout(6, false);
	}
	
	protected void addSelectFilterItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectFilter = new Label(parent, SWT.NONE);
		lblSelectFilter.setText("Current Filters");
		lblSelectFilter.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
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
	
	protected void processSelection() {
		if( cancelIfDirty() ) {
			return;
		}				
		setEditEnabled(false);
		if( ! cmbSelectFilter.getText().trim().equals("") ) {
			btnEditCurrent.setEnabled(true);
			btnDeleteCurrent.setEnabled(true);
			btnExport.setEnabled(true);
			setCurrentFilterValues(cmbSelectFilter.getText().trim());
		} 		
	}
	
	protected void initStoredFiltersList() {
		library = new FilterSettingLibrary();
		cmbSelectFilter.removeAll();
		cmbSelectFilter.add("");
		if( preferences != null && getPreferenceFilterSettings() != null ) {
			for (FilterSetting filter : getPreferenceFilterSettings()) {
				cmbSelectFilter.add(filter.getName());
				library.add(filter);
			}
		}
	}
	
	protected void addDeleteCurrent( Composite parent ) {
		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnDeleteCurrent = new Button(parent, SWT.NONE);
		btnDeleteCurrent.setText("Delete Selected");
		btnDeleteCurrent.setLayoutData(gd3);				

		btnDeleteCurrent.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
				int iSelInx = cmbSelectFilter.getSelectionIndex();
				if (iSelInx == 0) // nothing is loaded from the list, do not delete
					return;
				boolean bVal = MessageDialog.openConfirm(getShell(), "Delete Selected?", "Delete selected. Are you sure?");
				if( bVal ) {
					FilterSetting filter = getCurrentFilter(cmbSelectFilter.getItem(iSelInx));
					if (filter == null)
						return;
					library.remove(filter);
					cmbSelectFilter.remove(iSelInx);
					currentFilter = null;
					clearValues();
					setEditEnabled(false);
					updatePreferences();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	protected void addEditCurrent( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnEditCurrent = new Button(parent, SWT.NONE);
		btnEditCurrent.setText("Edit Selected");
		btnEditCurrent.setLayoutData(gd1);					
		btnEditCurrent.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				setEditEnabled(true);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	private void addExportCurrent(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnExport = new Button(parent, SWT.NONE);
		btnExport.setText("Export");
		btnExport.setLayoutData(gd1);					
		btnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentFilter != null) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					dialog.setOverwrite(true);
					dialog.setFileName(currentFilter.getName() + ".xml");
					dialog.setFilterExtensions(new String[]{"*.xml"});
					String fileName = dialog.open();
					if (fileName != null && !fileName.isEmpty()) {
						try {
							String xmlString = MSGlycanFilterPreference.marshalFilter(currentFilter);
							FileWriter fileWriter = new FileWriter(fileName);
							fileWriter.write(xmlString);
							fileWriter.close();
						} catch (IOException e1) {
							logger.error("Could not write the selected preference settings to the given file: " + fileName, e1);
							MessageDialog.openError(getShell(), "Error", "Could not write the selected preference settings to the given file: " + fileName);
						} catch (JAXBException e1) {
							logger.error("Could not write the selected preference settings to the given file: " + fileName, e1);
							MessageDialog.openError(getShell(), "Error", "Could not write the selected preference settings to the given file: " + fileName);
						}
					}
				}
			};
		});
	}
	
	private void addImport(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnImport = new Button(parent, SWT.NONE);
		btnImport.setText("Import");
		btnImport.setLayoutData(gd1);					
		btnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*.xml"});
				String fileName = dialog.open();
				if (fileName != null && !fileName.isEmpty()) {
					try {
					    BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
					    String line;
					    String xmlString = "";
					    while ((line = bufferedReader.readLine()) != null) {
					       xmlString += line + "\n";
					    }
					    bufferedReader.close();
						FilterSetting newPref = (FilterSetting) MSGlycanFilterPreference.unmarshalFilter(xmlString);
						if (newPref != null) {
							if (library.getFilterSettings() == null)
								library.add(newPref);
							else { // remove the one with the same name, add the currentFilter
								FilterSetting toBeRemoved = null;
								for (FilterSetting filter : library.getFilterSettings()) {
									if (filter.getName() != null && filter.getName().equals(newPref.getName())) {
										MessageDialog.openInformation(getShell(), "Info", "Already exists! Updating the existing");
										toBeRemoved = filter;
										break;
									}
								}
								if (toBeRemoved != null) {
									library.remove(toBeRemoved);
									cmbSelectFilter.remove(toBeRemoved.getName());
									clearValues();
								}
								library.add(newPref);
							}
							
							setPreferenceFilterSettings(library);
							//update the combo box
							cmbSelectFilter.add(newPref.getName());
							cmbSelectFilter.select(cmbSelectFilter.getItemCount()-1);
							cmbSelectFilter.notifyListeners(SWT.Selection, new Event());
							currentFilter = newPref;
						}
					} catch (FileNotFoundException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file cannot be found");
						logger.error("Selected file cannot be found", e1);
					} catch (IOException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file cannot be opened");
						logger.error("Selected file Selected file cannot be opened", e1);
					} catch (JAXBException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file does not contain a valid preference");
						logger.error("Selected file does not contain a valid preference", e1);
					}
				}
			};
		});
	}
	
	protected void addCreateNewItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnCreateNew = new Button(parent, SWT.NONE);
		btnCreateNew.setText("Create New");
		btnCreateNew.setLayoutData(gd1);					
		btnCreateNew.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
				if( cancelIfDirty() ) {
					return;
				}				
				setEditEnabled(true);
				btnEditCurrent.setEnabled(false);
				btnDeleteCurrent.setEnabled(false);
				btnExport.setEnabled(false);
				clearValues();	
				currentFilter = new FilterSetting();				
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}


	protected void addSeparatorLine1( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
		Label lblSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
		lblSeparator.setLayoutData(gd1);
	}

	protected FilterSetting getCurrentFilter( String selFilter ) {
		if( preferences == null || getPreferenceFilterSettings() == null ) {
			return null;
		}
		for( int i = 0; i < getPreferenceFilterSettings().size(); i++ ) {
			FilterSetting curFilter =  getPreferenceFilterSettings().get(i);
			if( curFilter.getName().equals(selFilter) ) {
				return curFilter;
			}

		}
		return null;
	}
	
	protected boolean cancelIfDirty() {
		if( bIsDirty ) {
			boolean bContinue = MessageDialog.openConfirm(getShell(), "Values Changed", "The values in the current selection have changed. Discard?");
			if( ! bContinue ) {
				return true;
			}
		}		
		return false;
	}
	
	protected void setCurrentFilterValues(String selFilterName) {
		if( selFilterName == null ) {
			return;
		}
		FilterSetting selFilter = getCurrentFilter(selFilterName);
		if( selFilter == null ) {
			return;
		}
		currentFilter = selFilter;
		txtName.setText(selFilter.getName());
		if (selFilter.getDescription() != null)
			txtDescription.setText(selFilter.getDescription());
		else
			txtDescription.setText("");
		initFilterTableValues(selFilter);
		setIsDirty(false);
	}
	
	private void initFilterTableValues(FilterSetting selFilter) {
		if (filterTableSetup != null) {
			filterTableSetup.setExistingFilters(selFilter);
			if (isReadyToFinish()) {
				setPageComplete(true);
			} else {
				setPageComplete(false);
			}
		}
	}

	public void setEditEnabled( boolean _bVal ) {
		btnDeleteCurrent.setEnabled(_bVal);
		btnEditCurrent.setEnabled(_bVal);
		btnExport.setEnabled(_bVal);
		lblName.setEnabled(_bVal);
		txtName.setEnabled(_bVal);
		lblDescription.setEnabled(_bVal);
		txtDescription.setEnabled(_bVal);
		filterTableSetup.setEnabled(_bVal);
	}

	protected void clearValues() {
		txtName.setText("");
		txtDescription.setText("");
		cmbSelectFilter.select(0);
		setIsDirty(false);
	}

	protected void addFilterNameItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		lblName = new Label(parent, SWT.NONE);
		lblName.setText("Name of Filter Setting");
		lblName.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		txtName = new Text(parent, SWT.BORDER);
		txtName.setLayoutData(gd2);
		txtName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setIsDirty(true);			
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
	}

	protected void addDescriptionItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 5);
		lblDescription = new Label(parent, SWT.NONE);
		lblDescription.setText("Description");
		lblDescription.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 5);
		txtDescription = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL );
		txtDescription.setText("");
		txtDescription.setLayoutData(gd2);
		txtDescription.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setIsDirty(true);
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
	}

	public void setIsDirty(boolean bIsDirty) {
		this.bIsDirty = bIsDirty;
	}
	
	public void setPageComplete(boolean isComplete) {
		PropertyChangeEvent e = new PropertyChangeEvent(this, PAGE_COMPLETE_PROPERTY, this.isComplete, isComplete);
		this.isComplete = isComplete;
		listener.propertyChange(e);
	}

	public boolean isPageComplete() {
		return this.isComplete;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void updatePreferences() {
		save();
		setPreferenceFilterSettings(library);
	}

	private void save() {
		if( currentFilter != null && bIsDirty) {
			currentFilter.setName(sFilterName);
			currentFilter.setDescription(sDescription);
			if (filterTableSetup.getFilterSetting() != null)
				currentFilter.setFilter(filterTableSetup.getFilterSetting().getFilter());	
			addCurrentFilterSetting();
		}
		setIsDirty(false);
	}

	private boolean isReadyToFinish() {
		sFilterName = null;
		sDescription = null;
		
		if( ! txtName.getText().trim().equals("") ) {
			sFilterName = txtName.getText().trim();
			//need to check if a filter with this name already exists
			if (!sFilterName.equals(currentFilter.getName()) && checkFilterExists (sFilterName)) {
				setErrorMessage("A filter with this name already exists!");
				return false;
			}
		} else if (currentFilter != null) { // we don't need to check if we are not editing or creating a new one
			setErrorMessage("Filter setting name cannot be empty");
			return false;
		}
		sDescription = txtDescription.getText().trim();
		
		if (currentFilter != null) {
			if (filterTableSetup.getFilterSetting() == null || filterTableSetup.getFilterSetting().getFilter() == null) {
				setErrorMessage("You haven't added any filters yet");
				return false;
			}
		}
		setErrorMessage(null);
		return true;
	}
	
	private boolean checkFilterExists(String sFilterName) {
		if (library.getFilterSettings() != null) {
			for (FilterSetting filter : library.getFilterSettings()) {
				if (filter.getName().equals(sFilterName))
					return true;
			}
		}
		return false;
	}

	@Override
	public void filterChanged() {
		if (currentFilter != null && filterTableSetup.getFilterSetting() != null) {
			currentFilter.setFilter(filterTableSetup.getFilterSetting().getFilter());
			if (isReadyToFinish()) {
				setPageComplete(true);
			} else {
				setPageComplete(false);
			}
		} else if (currentFilter != null) {
			currentFilter.setFilter(null);
			if (isReadyToFinish()) {
				setPageComplete(true);
			} else {
				setPageComplete(false);
			}
		}
	}	
}
