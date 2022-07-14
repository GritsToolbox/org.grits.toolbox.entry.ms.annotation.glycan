package org.grits.toolbox.entry.ms.annotation.glycan.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.ms.annotation.glycan.Activator;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.ms.om.data.IntensityFilter;
import org.grits.toolbox.util.structure.glycan.filter.om.BooleanFilter;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.ComboFilter;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.filter.om.GlycanFilterAnd;
import org.grits.toolbox.util.structure.glycan.filter.om.GlycanFilterNot;
import org.grits.toolbox.util.structure.glycan.filter.om.GlycanFilterOr;
import org.grits.toolbox.util.structure.glycan.filter.om.IntegerFilter;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class MSGlycanAnnotationFilterSetup extends FilterTableSetup {
	public static Image helpIcon = ImageDescriptor.createFromURL(FileLocator.find(
			Platform.getBundle(Activator.PLUGIN_ID), new Path("icons"+ File.separator + "helpIcon.png"), null)).createImage();
	
	public static final String FILTERMESSAGE = "Please note that some of the descriptions below may not apply to this filter view!";
	
	private static final int COLUMNS = 10;
	public static final String CATEGORY_ALL="All";
	
	List<Category> categories = null; // no category list
	List<Filter> includedFilters = new ArrayList<>();
	Composite filterComposite;
	Composite parent;
	Map<Control, ControlDecoration> errors = new HashMap<Control, ControlDecoration>();
	
	Category selectedCategory;
	private ComboViewer categoryCombo;
	private String filterMessage = FILTERMESSAGE;
	
	public MSGlycanAnnotationFilterSetup() {
		super();
	}
	
	public MSGlycanAnnotationFilterSetup(List<Category> categoryList) {
		this();
		this.categories = categoryList;
	}
	
	@Override
	public void createFilterTableSection(Composite parent) {
		if (this.filterComposite == null || filterComposite.isDisposed()) { 
			this.parent = parent;
			parent.setLayout(new GridLayout(4, false));
			this.filterComposite = new Composite(parent, SWT.NONE);
			this.filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 4));
		} 
		
		GridLayout gridLayout = new GridLayout(COLUMNS, false);
		filterComposite.setLayout(gridLayout);
		gridLayout.horizontalSpacing=10;
		
		if (categories != null) {
			addCategorySelection (filterComposite);
		}
		
		if (filterList != null && !filterList.isEmpty()) {
			addHeaderSection(filterComposite);
			int columns = 0;
			for (Filter filter: filterList) {
				if (selectedCategory != null)
					if (!includeFilter(filter)) { // if the filter is not in the selected category, skip it!
						continue;
					}
				if (filter instanceof IntensityFilter) { // we will not add custom filters in this area
					continue;
				}
				else if (filter instanceof IntegerFilter) {
					addIntegerFilterSelection (filterComposite, (IntegerFilter) filter.copy());
					columns += COLUMNS/2;
				}
				else if (filter instanceof BooleanFilter) {
					addBooleanFilterSelection (filterComposite, (BooleanFilter) filter.copy());
					columns += COLUMNS/2;
				}
				else if (filter instanceof ComboFilter) {
					addComboFilterSelection (filterComposite, (ComboFilter) filter.copy());
					columns += COLUMNS/2;
				}
			}
			
			int remaning = columns % COLUMNS;
			// fill the empty columns 
			for (int i=0; i < remaning; i++) {
				new Label(filterComposite, SWT.NONE);
			}
			// and also add an extra line
			Label separator = new Label(filterComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, COLUMNS, 1));
			
			columns = 0;
			// add custom filters in this area
			// if there are any new filters, this code needs to be modified
			for (Filter filter: filterList) {
				if (filter instanceof IntensityFilter) {
					addIntegerFilterSelection(filterComposite, (IntegerFilter) filter.copy());
					columns += COLUMNS/2;
				}
			}
			
			remaning = columns % COLUMNS;
			// fill the empty columns and also add an extra line
			for (int i=0; i < remaning + COLUMNS; i++) {
				new Label(filterComposite, SWT.NONE);
			}
			
		}
	}
	
	private boolean includeFilter(Filter filter) {
		if (selectedCategory == null) 
			// no category selected, include all
			return true;
		return selectedCategory.containsFilter(filter.getName());
	}

	private void addCategorySelection(Composite parent) {
		Label categorySelLabel = new Label(parent, SWT.NONE);
		categorySelLabel.setText("Select Filter Category");
		categorySelLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		
		categoryCombo = new ComboViewer (parent, SWT.READ_ONLY);
		categoryCombo.setContentProvider(new ArrayContentProvider());
		categoryCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null) {
					return CATEGORY_ALL;
				} else if (element instanceof Category) 
					return ((Category) element).getLabel();
				return null;
			}
		});
		categoryCombo.setInput(categories);
		categoryCombo.insert(null, 0);
		categoryCombo.getCombo().setEnabled(parent.isEnabled());
		if (selectedCategory != null) {
			int i=1; // ALL is the first one
			for(Category c: categories) {
				if (c.getLabel().equals(selectedCategory.getLabel()))
						categoryCombo.getCombo().select(i);
				i++;
			}
		} else { 
			// select first one (ALL) by default
			categoryCombo.getCombo().select(0);
		}
		categoryCombo.getCombo().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 4, 1));
		
		categoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				int index = categoryCombo.getCombo().getSelectionIndex();
				if (index > 0) {
					IStructuredSelection selected = categoryCombo.getStructuredSelection();
					selectedCategory = (Category)selected.getFirstElement();
					resetFilters();
				} else {
					//ALL
					selectedCategory = null;
					resetFilters();
				}
			}
		});
		
		// fill in the remaining columns
		for (int i=6; i < COLUMNS; i++)
			new Label(parent, SWT.NONE);
 	}

	private void addHeaderSection(Composite parent) {
		comboOp = new ComboViewer(parent, SWT.READ_ONLY);
		comboOp.setContentProvider(new ArrayContentProvider());
		comboOp.setLabelProvider(new LabelProvider());
		comboOp.setInput(new String[]{"AND", "OR"});
		if (op.equals("AND"))
			comboOp.getCombo().select(0);
		else 
			comboOp.getCombo().select(1);
		
		comboOp.getCombo().setEnabled(parent.isEnabled());
		comboOp.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selected = comboOp.getStructuredSelection();
				op = (String)selected.getFirstElement();
				filterUpdated();
			}
		});
		
		MSGlycanAnnotationFilterSetup.addHelpButton(parent, filterMessage);
		
		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText("Range (if known)");
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 3, 1));
		titleLabel.setEnabled(parent.isEnabled());
		
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText("Range (if known)");
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 3, 1));	
		titleLabel.setEnabled(parent.isEnabled());
		
		//new Label(parent, SWT.NONE);
	}
	
	public static void addHelpButton(Composite parent, String filterMessage) {
		Button helpButton = new Button(parent, SWT.NONE);
		helpButton.addPaintListener( new PaintListener() {
			  @Override
			  public void paintControl(PaintEvent event) {
				  event.gc.setBackground(helpButton.getParent().getBackground());
				  event.gc.fillRectangle(event.x, event.y, event.width, event.height);
				  event.gc.drawImage(MSGlycanAnnotationFilterSetup.helpIcon, 5, 2);
			  }
		});
		helpButton.setToolTipText("Help with Filters (examples)");
		helpButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		helpButton.setEnabled(parent.isEnabled());
		helpButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TitleAreaDialog helpDialog = new TitleAreaDialog(Display.getCurrent().getActiveShell()) {
					
					@Override
				    protected Control createDialogArea(Composite parent) {
				        Composite composite = (Composite) super.createDialogArea(parent);
				        setTitle ("Filter Help");
				        if (filterMessage != null)
				        	setMessage(filterMessage);

				        GridLayout layout = new GridLayout(1, false);
				        composite.setLayout(layout);

				        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
				        data.widthHint = 500;
				        data.heightHint = 450;
				        composite.setLayoutData(data);

				        Browser browser = new Browser(composite, SWT.NONE);
				        browser.setUrl(FileUtils.getFilterHelpPath());
				        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				        return composite;
				    }
					
					@Override
					protected boolean isResizable() {
						return true;
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						//NO Cancel button, Only OK button
						createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
								true);
					}
				};
				helpDialog.open();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void addComboFilterSelection(Composite parent, ComboFilter filter) {
		Label filterLabel = new Label(parent, SWT.NONE);
		filterLabel.setText(filter.getLabel());
		filterLabel.setToolTipText(filter.getDescription());
		filterLabel.setEnabled(parent.isEnabled());
		
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setEnabled(parent.isEnabled());
		
		ComboViewer selection = new ComboViewer(combo);
		selection.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null) {
					return "Any";
				} else if (element instanceof Filter) 
					return ((Filter) element).getLabel();
				return null;
			}
		});
		selection.setContentProvider(new ArrayContentProvider());
		selection.setInput(filter.getFiltersInFilterOrder());
		selection.insert(null, 0);
		Filter existing = findFilter(filter);
		if (existing != null && existing instanceof ComboFilter && ((ComboFilter)existing).getSelected() != null)
			selection.getCombo().select(((ComboFilter)existing).getFiltersInFilterOrder().indexOf(((ComboFilter)existing).getSelected()) + 1);
		else 
			selection.getCombo().select(0);
		selection.addPostSelectionChangedListener(new ISelectionChangedListener() {
	        @Override
	        public void selectionChanged(SelectionChangedEvent event) {
	            if (selection.getCombo().getSelectionIndex() == -1) {
	            	selection.getCombo().select(0);
	            }
	        }
	    });
		selection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				int index = selection.getCombo().getSelectionIndex();
				if (index > 0) { // not any
					IStructuredSelection selected = (IStructuredSelection) selection.getSelection();
					if (selected.getFirstElement() instanceof Filter) {
						filter.setSelected((Filter)selected.getFirstElement());
						if (!includedFilters.contains(filter))
							includedFilters.add(filter);
					}
				}
				else {
					if (includedFilters.contains(filter))
						includedFilters.remove(filter);
				}
				filterUpdated();
			}
		});
		
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
	}

	private void addBooleanFilterSelection(Composite parent, BooleanFilter filter) {
		Label filter1 = new Label(parent, SWT.NONE);
		filter1.setText(filter.getLabel());
		filter1.setToolTipText(filter.getDescription());
		filter1.setEnabled(parent.isEnabled());
		
		Combo selection = new Combo(parent, SWT.NONE);
		selection.setEnabled(parent.isEnabled());
		selection.setItems(new String[] {"possible", "yes", "no"});
		Filter existing = findFilter(filter);
		if (existing != null)
			selection.select(1);
		else if (findNotFilter(filter) != null)
			selection.select(2);
		else
			selection.select(0);
		
		selection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = selection.getSelectionIndex();
				switch (index) {
				case 0: // do not include
					if (includedFilters.contains(filter))
						includedFilters.remove(filter);
					// go through all "not" filters in the list and remove the one matching the filter
					removeNotFilter(filter);
					break;
				case 1: // YES
					if (!includedFilters.contains(filter))
						includedFilters.add(filter);
					removeNotFilter(filter);
					break;
				case 2: // NO
					// create a NOT filter
					GlycanFilterNot notFilter = new GlycanFilterNot();
					notFilter.setFilter(filter);
					includedFilters.add(notFilter);
				}	
				filterUpdated();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
	}

	private void addIntegerFilterSelection(Composite parent, IntegerFilter filter) {
		Label filterLabel = new Label(parent, SWT.NONE);
		filterLabel.setText(filter.getLabel());
		filterLabel.setToolTipText(filter.getDescription());
		filterLabel.setEnabled(parent.isEnabled());
		
		Combo selection = new Combo(parent, SWT.NONE);
		selection.setEnabled(parent.isEnabled());
		selection.setItems(new String[] {"possible", "yes", "no"});
		final Filter existing = findFilter(filter);
		if (existing != null)
			selection.select(1);
		else if (findNotFilter(filter) != null)
			selection.select(2);
		else
			selection.select(0);
		
		Text rangeBegin = new Text(parent, SWT.BORDER);
		rangeBegin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		// Create a control decoration for the control.
		ControlDecoration dec = new ControlDecoration(rangeBegin, SWT.CENTER);
		// Specify the decoration image and description
		Image image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		dec.setImage(image);
		dec.setDescriptionText("Should enter a positive integer");
		dec.hide();
		errors.put(rangeBegin, dec);
		rangeBegin.setEnabled(parent.isEnabled());
		
		if (existing != null && existing instanceof IntegerFilter && ((IntegerFilter)existing).getMin() != null && ((IntegerFilter)existing).getMin() != 0)
			rangeBegin.setText(((IntegerFilter)existing).getMin() + "");
		rangeBegin.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (rangeBegin.getText() != null && !rangeBegin.getText().isEmpty()) {
					if (validRange(rangeBegin.getText())) {
						filter.setMin(Integer.parseInt(rangeBegin.getText()));
						if (existing != null)
							((IntegerFilter)existing).setMin(filter.getMin());
						showError(rangeBegin, false);
					} else {
						showError(rangeBegin, true);
					}
				} else {
					filter.setMin(0);
					if (existing != null)
						((IntegerFilter)existing).setMin(0);
				}
				filterUpdated();
			}
		});
		Label dash = new Label(parent, SWT.NONE);
		dash.setText("-");
		dash.setEnabled(parent.isEnabled());
		Text rangeEnd = new Text(parent, SWT.BORDER);
		rangeEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		dec = new ControlDecoration(rangeEnd, SWT.CENTER);
		dec.setImage(image);
		dec.setDescriptionText("Should enter a positive integer");
		dec.hide();
		errors.put(rangeEnd, dec);
		rangeEnd.setEnabled(parent.isEnabled());
		
		if (existing != null && existing instanceof IntegerFilter && ((IntegerFilter)existing).getMax() != null)
			rangeEnd.setText(((IntegerFilter)existing).getMax() + "");
		rangeEnd.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (rangeEnd.getText() != null && !rangeEnd.getText().isEmpty()) {
					if (validRange(rangeEnd.getText())) {
						filter.setMax(Integer.parseInt(rangeEnd.getText()));
						if (existing != null)
							((IntegerFilter)existing).setMax(filter.getMax());
						showError(rangeEnd, false);
					} else {
						showError(rangeEnd, true);
					}
				} else {
					filter.setMax(null);
					if (existing != null)
						((IntegerFilter)existing).setMax(null);
				}
				filterUpdated();
			}
		});
		
		selection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = selection.getSelectionIndex();
				switch (index) {
				case 0: // do not include
					if (includedFilters.contains(filter))
						includedFilters.remove(filter);
					// go through all "not" filters in the list and remove the one matching the filter
					removeNotFilter(filter);
					// clear range values
					rangeBegin.setText("");
					rangeEnd.setText("");
					break;
				case 1: // YES
					if (!includedFilters.contains(filter))
						includedFilters.add(filter);
					if (filter.getMin() == 0) { // not set by the user
						filter.setMin(1);
						rangeBegin.setText("1");
					}
					removeNotFilter(filter);
					break;
				case 2: // NO
					if (includedFilters.contains(filter))
						includedFilters.remove(filter);
					// create a NOT filter
					GlycanFilterNot notFilter = new GlycanFilterNot();
					notFilter.setFilter(filter);
					if (filter.getMin() == 0) { // not set by the user
						filter.setMin(1);
						rangeBegin.setText("1");
					}
					includedFilters.add(notFilter);
				}	
				filterUpdated();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private void showError (Control control, boolean show) {
		for(Control c: errors.keySet()) {
			if (c.equals(control)) {
				ControlDecoration d = errors.get(c);
				if (show) d.show();
				else d.hide();
			}
		}
	}
	private boolean validRange(String text) {
		try {
			Integer.parseInt (text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * if the given filter is added as a "NOT" filter, we need to remove it from the included filter list when
	 * its selection becomes "possible"
	 * 
	 * @param filter to match the NOT filter's filter
	 */
	private void removeNotFilter(Filter filter) {
		Filter toBeRemoved = findNotFilter(filter);
		if (toBeRemoved != null)
			includedFilters.remove(toBeRemoved);
	}
	
	private Filter findNotFilter (Filter filter) {
		for (Filter f: includedFilters) {
			if (f instanceof GlycanFilterNot) {
				if (((GlycanFilterNot) f).getFilter().equals(filter))
					return f;
			}
		}
		return null;
	}
	
	private Filter findFilter (Filter filter) {
		for (Filter f: includedFilters) {
			if (f instanceof GlycanFilterNot || f instanceof GlycanFilterAnd || f instanceof GlycanFilterOr)
				continue;
			if (f.equals(filter))
				return f;
		}
		return null;
	}

	@Override
	public FilterSetting getFilterSetting() {
		if (includedFilters.isEmpty()) // nothing is selected
			return null;
		FilterSetting setting = new FilterSetting();
		if (op.equals("AND")) {
			GlycanFilterAnd filter = new GlycanFilterAnd();
			filter.setElements(includedFilters);
			setting.setFilter(filter);
		}
		else {
			GlycanFilterOr filter = new GlycanFilterOr();
			filter.setElements(includedFilters);
			setting.setFilter(filter);
		}
		return setting;
	}
	
	@Override
	public void setExistingFilters(FilterSetting filterSetting) {
		if (filterSetting != null) {
			Filter filter = filterSetting.getFilter();
			List<Filter> filters = null;
			if (filter instanceof GlycanFilterAnd) {
				op = "AND";
				filters = ((GlycanFilterAnd) filter).getElements();
			} else if (filter instanceof GlycanFilterOr) {
				op = "OR";
				filters = ((GlycanFilterOr) filter).getElements();
			}
			if (filters != null) {
				// re-create the page
				includedFilters.clear();
				includedFilters.addAll(filters);
				for (Control child: filterComposite.getChildren()) {
					child.dispose();
				}
				createFilterTableSection(parent);
				filterComposite.layout();
				parent.layout();
				// no need to do size changes for the composite 
				// since new components are the same as the disposed ones only contents are different
			}
		}
	}
	
	/**
	 * sets the filter list, no sorting
	 */
	@Override
	public void setFilterList(List<Filter> filterList) {
		this.filterList = filterList;
	}
	
	@Override
	public void setEnabled(boolean b) {
		if (comboOp.getCombo().isDisposed()) comboOp.getCombo().setEnabled(b);
		if (filterComposite != null && !filterComposite.isDisposed()) {
			filterComposite.setEnabled(b);
			for (Control child: filterComposite.getChildren()) {
				if (!child.isDisposed()) child.setEnabled(b);
			}
		}
	}
	
	@Override
	public void resetFilters() {
		includedFilters.clear();
		for (Control child: filterComposite.getChildren()) {
			child.dispose();
		}
		createFilterTableSection(parent);
		filterComposite.layout();
		parent.layout();
		
		// we need to do this to make sure to the composite holding the newly added (or removed) components resizes accordingly
		Composite sc = findCompositeWithScrolledParent(parent);
		if (sc != null & sc.getParent() != null && sc.getParent() instanceof ScrolledComposite) {
			sc.getParent().layout(true, true);
			((ScrolledComposite)sc.getParent()).setMinSize(sc.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
	
	/** 
	 * find the outermost composite within the scrolled composite 
	 * that contains the filter section
	 * 
	 * @param c the composite to start searching parents
	 * @return the composite whose parent is the scrolled composite
	 */
	Composite findCompositeWithScrolledParent (Composite c) {
		if (c.getParent() != null && c.getParent() instanceof ScrolledComposite)
			return c;
		else if (c.getParent() != null)
			return findCompositeWithScrolledParent(c.getParent());
		else
			return null;
	}

	public Control getFilterComposite() {
		return filterComposite;
	}

	public void setSelectedCategory(Category preferredCategory) {
		this.selectedCategory = preferredCategory;
	}
	
	public String getFilterMessage() {
		return filterMessage;
	}
	
	public void setFilterMessage(String filterMessage) {
		this.filterMessage = filterMessage;
	}
}
