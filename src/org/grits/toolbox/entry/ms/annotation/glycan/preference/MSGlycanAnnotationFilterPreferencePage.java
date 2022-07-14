package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;

public class MSGlycanAnnotationFilterPreferencePage extends PreferencePage {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationFilterPreferencePage.class);
	
	MSGlycanFilterCateogoryPreference categoryPreference;
	private ComboViewer categoryCombo;
	
	List<Category> categories = new ArrayList<Category>();
	
	public MSGlycanAnnotationFilterPreferencePage() {
		loadFilterCategories();
		loadCategoryPreference();
	}

	private void loadFilterCategories() {
		try {
			FiltersLibrary filterLibrary = FileUtils.readFilters(FileUtils.getFilterPath());
			categories = filterLibrary.getCategories();
		} catch (UnsupportedEncodingException e) {
			logger.error("Error loading the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Cannot locate the filters file", e);
		} catch (JAXBException e) {
			logger.error("Error loading the filters", e);
		}
	}

	private void loadCategoryPreference() {
		try {
			categoryPreference = MSGlycanFilterCateogoryPreference.getMSGlycanFilterCategoryPreferences(MSGlycanFilterCateogoryPreference.getPreferenceEntity());
		} catch (Exception e) {
			logger.error("Cannot get filter category preference", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 15;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		Label label = new Label(container, SWT.None);
		label.setText("Filter Category Preference");
		
		categoryCombo = new ComboViewer (container, SWT.READ_ONLY);
		categoryCombo.setContentProvider(new ArrayContentProvider());
		categoryCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null) {
					return MSGlycanAnnotationFilterSetup.CATEGORY_ALL;
				} else if (element instanceof Category) 
					return ((Category) element).getLabel();
				return null;
			}
		});
		categoryCombo.setInput(categories);
		categoryCombo.insert(null, 0);
		
		if (categoryPreference != null) {
			Category current =  categoryPreference.getCategoryPreference();
			if (current == null) //ALL
				categoryCombo.getCombo().select(0);
			else {
				int i=1; // skip first one
				for (Category c: categories) {
					if (c.getLabel().equals(current.getLabel())) 
						categoryCombo.getCombo().select(i);
					i++;
				}
				
			}
		}
		
		categoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				int index = categoryCombo.getCombo().getSelectionIndex();
				if (index > 0) {
					IStructuredSelection selected = categoryCombo.getStructuredSelection();
					categoryPreference.setCategoryPreference((Category)selected.getFirstElement());
				} else {
					//ALL
					categoryPreference.setCategoryPreference(null);
				}
			}
		});
		
		return container;
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
	
	private void save() {
		try {
			logger.debug("Time to save values!");
			categoryPreference.saveValues();
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

}
