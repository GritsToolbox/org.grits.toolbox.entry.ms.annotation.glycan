package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class MSGlycanCustomFilterCategoryPage extends PreferencePage {
	private static final Logger logger = Logger.getLogger(MSGlycanCustomFilterCategoryPage.class);
	private FiltersLibrary filterLibrary;
	private GridLayout gridLayout;
	private Button btnDeleteCurrent;
	private Button btnEditCurrent;
	private Button btnCreateNew;
	private boolean bIsDirty;
	private Label lblName;
	private Text txtName;
	private Label lblLabel;
	private Text txtLabel;
	private Label lblDescription;
	private Text txtDescription;
	private String sCategoryLabel;
	private String sCategoryName;
	private String sDescription;
	private Combo cmbSelectCategory;
	
	private Category currentCategory;
	private Button lAdd;
	private Button lRemove;
	private ListViewer filterListViewer;
	private ListViewer selectedFilterListViewer;

	public MSGlycanCustomFilterCategoryPage() {
		loadFilterLibrary();
	}

	private void loadFilterLibrary() {
		try {
			// only load filters defined in the filters file, do not include custom filters
			filterLibrary = FilterUtils.readFilters(FileUtils.getFilterPath());
		} catch (Exception e) {
			logger.error("could not load filter library", e);
		}
	}
	
	private boolean checkCategoryExists (String name) {
		if (filterLibrary != null && filterLibrary.getCategories() != null) {
			for (Category c: filterLibrary.getCategories()) {
				if (name != null && c.getName().equals(name))
					return true;
			}
		}
		return false;
	}
	
	private Category getCurrentCategory(String name) {
		if (filterLibrary != null && filterLibrary.getCategories() != null) {
			for (Category c: filterLibrary.getCategories()) {
				if (c.getName().equals(name))
					return c;
			}
		}
		return null;
	}
	
	private void deleteFromLibrary (String name) {
		if (filterLibrary != null && filterLibrary.getCategories() != null) {
			Category toBeRemoved = null;
			for (Category c: filterLibrary.getCategories()) {
				if (c.getName().equals(name)) {
					toBeRemoved = c;
					break;
				}
			}
			if (toBeRemoved != null)
				filterLibrary.getCategories().remove(toBeRemoved);
		}
	}
	
	private void addCategoryToLibrary () {
		if (currentCategory == null)
			return;
		if (filterLibrary == null) {
			logger.error("Filter library is empty, cannot add categories");
			setErrorMessage("Filter library is empty, cannot add categories");
		}
		else {
			// check if the category already exists
			if (!checkCategoryExists(currentCategory.getName()))
				filterLibrary.getCategories().add(currentCategory);
			else { // editing
				deleteFromLibrary(currentCategory.getName());
				filterLibrary.getCategories().add(currentCategory);
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		initGridLayout();
		container.setLayout(gridLayout);
		addSelectCategoryItem(container);
		addDeleteCurrent(container);
		addEditCurrent(container);
		addCreateNewItem(container);
		addSeparatorLine1(container);
		
		addCategoryNameItem(container);
		addCategoryLabelItem(container);
		addDescriptionItem(container);
		
		addCategorySelectionTable(container);
		
		setEditEnabled(false); // disable bottom entries until something loaded
		return container;
	}

	private void addCategorySelectionTable(Composite container) {
		Composite comp = new Composite(container, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 3));
		final GridLayout lLayout = new GridLayout(3, false);
		lLayout.marginWidth = 0;
		comp.setLayout(lLayout);

		final GridLayout columnLayout = new GridLayout();
		columnLayout.makeColumnsEqualWidth = false;
		columnLayout.marginRight = 15;
		final Composite lCol1 = new Composite(comp, SWT.NONE);
		lCol1.setLayout(columnLayout);
		lCol1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		final Composite lCol2 = new Composite(comp, SWT.NONE);
		lCol2.setLayout(columnLayout);
		lCol2.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		final Composite lCol3 = new Composite(comp, SWT.NONE);
		lCol3.setLayout(columnLayout);
		lCol3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		List filterList = new List(lCol1, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		filterList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		filterListViewer = new ListViewer(filterList);
		filterListViewer.setContentProvider(new ArrayContentProvider());
		filterListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).getLabel();
				return super.getText(element);
			}
		});
		
		filterListViewer.setInput(copyFilterListLibrary());
		filterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent inEvent) {
				final String[] lSelection = ((List) inEvent.widget)
						.getSelection();
				if (lSelection != null && lSelection.length > 0) {
					moveItem(filterListViewer, selectedFilterListViewer);
				}
			}
		});
		

		final Composite lButtons = new Composite(lCol2, SWT.NONE);
		final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.spacing = 10;
		rowLayout.fill = true;
		lButtons.setLayout(rowLayout);
		lAdd = new Button(lButtons, SWT.PUSH);
		lAdd.setText("»"); // >>
		lAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				moveItem(filterListViewer, selectedFilterListViewer);
			}
		});
		lRemove = new Button(lButtons, SWT.PUSH);
		lRemove.setText("«"); // <<
		lRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				moveItem(selectedFilterListViewer, filterListViewer);
			}
		});

		List selectedFilterList;
		selectedFilterList = new List(lCol3, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		selectedFilterList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		selectedFilterListViewer = new ListViewer(selectedFilterList);
		selectedFilterListViewer.setContentProvider(new ArrayContentProvider());
		selectedFilterListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).getLabel();
				return super.getText(element);
			}
		});
		selectedFilterListViewer.setInput(new ArrayList<Filter>());
		selectedFilterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent inEvent) {
				final String[] lSelection = ((List) inEvent.widget)
						.getSelection();
				if (lSelection != null && lSelection.length > 0) {
					moveItem(selectedFilterListViewer, filterListViewer);
				}
			}
		});
	}
	
	private java.util.List<Filter> copyFilterListLibrary () {
		java.util.List<Filter> filterListCopy = new ArrayList<Filter> ();
		for (Filter f: filterLibrary.getFilters()) {
			filterListCopy.add(f.copy());
		}
		return filterListCopy;
	}

	/**
	 * move items between two lists
	 * 
	 * @param fromList the list to remove the item from
	 * @param intoList the list to put the item into
	 */
	@SuppressWarnings("unchecked")
	private void moveItem(final ListViewer fromList, final ListViewer intoList) {
		Iterator<?> itr = fromList.getStructuredSelection().iterator();
		while (itr.hasNext()) {
			Filter selected = (Filter)itr.next();
			((java.util.List<Filter>)intoList.getInput()).add(selected);
			((java.util.List<Filter>)fromList.getInput()).remove(selected);
		}
		intoList.refresh();
		fromList.refresh();
		setIsDirty(true);	
		if (isReadyToFinish()) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}
	
	private void initCategoryTableValues(Category selCategory) {
		selectedFilterListViewer.getList().removeAll();
		java.util.List<Filter> filterList = getFiltersFromLibrary (selCategory.getFilters());
		selectedFilterListViewer.setInput(filterList);
		java.util.List<Filter> remainingFilters = new ArrayList<>();
		for (Filter f: filterLibrary.getFilters()) {
			if (selCategory.getFilters().contains(f.getName())) // skip
				continue;
			remainingFilters.add(f.copy());
		}
		filterListViewer.setInput(remainingFilters);
	}

	private java.util.List<Filter> getFiltersFromLibrary(java.util.List<String> filters) {
		java.util.List<Filter> filterList = new ArrayList<>();
		for (String filterName: filters) {
			for (Filter f: filterLibrary.getFilters()) {
				if (f.getName().equals(filterName)) {
					filterList.add(f.copy());
					break;
				}
			}
		}
		return filterList;
	}

	/** 
	 * add a combo box to select category from the filter library for displaying its current filters
	 * and/or modifying it if necessary
	 * @param container
	 */
	private void addSelectCategoryItem(Composite container) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectFilter = new Label(container, SWT.NONE);
		lblSelectFilter.setText("Current Categories");
		lblSelectFilter.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		cmbSelectCategory = new Combo(container, SWT.NONE);
		cmbSelectCategory.setLayoutData(gd2);
		initCategoryList();
		cmbSelectCategory.addSelectionListener(new SelectionListener() {

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
		if( ! cmbSelectCategory.getText().trim().equals("") ) {
			btnEditCurrent.setEnabled(true);
			btnDeleteCurrent.setEnabled(true);
			setCurrentCategoryFilterValues(cmbSelectCategory.getText().trim());
		} 		
	}

	private void setCurrentCategoryFilterValues(String selCategoryName) {
		if( selCategoryName == null ) {
			return;
		}
		Category selCategory = getCurrentCategory(selCategoryName);
		if( selCategory == null ) {
			return;
		}
		currentCategory = selCategory;
		txtName.setText(selCategory.getName());
		txtLabel.setText(selCategory.getLabel());
		if (selCategory.getDescription() != null)
			txtDescription.setText(selCategory.getDescription());
		else
			txtDescription.setText("");
		initCategoryTableValues(selCategory);
		setIsDirty(false);
	}

	private void initCategoryList() {
		cmbSelectCategory.removeAll();
		cmbSelectCategory.add("");
		if( filterLibrary != null && filterLibrary.getCategories() != null ) {
			for (Category c : filterLibrary.getCategories()) {
				cmbSelectCategory.add(c.getName());
			}
		}
	}

	protected void initGridLayout() {
		gridLayout = new GridLayout(6, false);
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
				int iSelInx = cmbSelectCategory.getSelectionIndex();
				if (iSelInx == 0)
					// do not delete, nothing is selected from the category list
					return;
				boolean bVal = MessageDialog.openConfirm(getShell(), "Delete Selected?", "Delete selected. Are you sure?");
				if( bVal ) {
					deleteFromLibrary(cmbSelectCategory.getItem(iSelInx));
					cmbSelectCategory.remove(iSelInx);
					currentCategory = null;
					clearValues();
					setEditEnabled(false);
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
				clearValues();	
				currentCategory = new Category();				
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
	
	protected boolean cancelIfDirty() {
		if( bIsDirty ) {
			boolean bContinue = MessageDialog.openConfirm(getShell(), "Values Changed", "The values in the current selection have changed. Discard?");
			if( ! bContinue ) {
				return true;
			}
		}		
		return false;
	}
	
	public void setEditEnabled( boolean _bVal ) {
		btnDeleteCurrent.setEnabled(_bVal);
		btnEditCurrent.setEnabled(_bVal);
		lblName.setEnabled(_bVal);
		txtName.setEnabled(_bVal);
		lblLabel.setEnabled(_bVal);
		txtLabel.setEnabled(_bVal);
		lblDescription.setEnabled(_bVal);
		txtDescription.setEnabled(_bVal);
		selectedFilterListViewer.getList().setEnabled(_bVal);
		lAdd.setEnabled(_bVal);
		lRemove.setEnabled(_bVal);
		filterListViewer.getList().setEnabled(_bVal);
	}

	protected void clearValues() {
		txtName.setText("");
		txtDescription.setText("");
		txtLabel.setText("");
		cmbSelectCategory.select(0);
		selectedFilterListViewer.setInput(new ArrayList<Filter>());
		filterListViewer.setInput(copyFilterListLibrary());
		setIsDirty(false);
	}
	
	protected void addCategoryNameItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		lblName = new Label(parent, SWT.NONE);
		lblName.setText("Category Name");
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
	
	private void addCategoryLabelItem(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		lblLabel = new Label(parent, SWT.NONE);
		lblLabel.setText("Category Label");
		lblLabel.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		txtLabel = new Text(parent, SWT.BORDER);
		txtLabel.setLayoutData(gd2);
		txtLabel.addModifyListener(new ModifyListener() {
			
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
		setValid(isComplete);
	}
	
	private boolean isReadyToFinish() {
		sCategoryName = null;
		sDescription = null;
		sCategoryLabel = null;
		
		if( ! txtName.getText().trim().equals("") ) {
			sCategoryName = txtName.getText().trim();
			if (!sCategoryName.equals(currentCategory.getName()) && checkCategoryExists(sCategoryName)) { // while editing the same, should not complain
				setErrorMessage("A category already exists with this name!");
				return false;
			} 
		} else if (currentCategory != null) {
			setErrorMessage("Category name cannot be empty");
			return false;
		}
		
		if( ! txtLabel.getText().trim().equals("") ) {
			sCategoryLabel = txtLabel.getText().trim();
			
		} else if (currentCategory != null){
			setErrorMessage("Category label cannot be empty");
			return false;
		}
		
		sDescription = txtDescription.getText().trim();
		
		if (selectedFilterListViewer.getList().getItemCount() == 0) {
			setErrorMessage("You haven't added any filters yet");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
	
	private void save() {
		if( currentCategory != null && bIsDirty) {
			currentCategory.setName(sCategoryName);
			currentCategory.setLabel(sCategoryLabel);
			currentCategory.setDescription(sDescription);
			currentCategory.setFilters(Arrays.asList(selectedFilterListViewer.getList().getItems()));	
			addCategoryToLibrary();
			saveFilterLibrary();
		}
		setIsDirty(false);
	}
	
	private void saveFilterLibrary() {
		try {
			FileUtils.updateLibrary(FileUtils.getFilterPath(), filterLibrary);
		} catch (Exception e) {
			setErrorMessage("Cannot save the filter category changes. Reason: " + e.getMessage());
			logger.error("Cannot save the filter category changes", e);
		}
	}

	@Override
	//when apply button is clicked
	protected void performApply() {
		save();
	}

	@Override
	public boolean performOk() {
		//need to save
		save();
		return true;
	}

}
