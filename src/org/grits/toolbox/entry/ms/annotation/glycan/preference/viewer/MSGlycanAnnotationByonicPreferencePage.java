package org.grits.toolbox.entry.ms.annotation.glycan.preference.viewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationByonicPreference;
import org.grits.toolbox.util.structure.glycan.count.SearchQueryItem;

public class MSGlycanAnnotationByonicPreferencePage extends PreferencePage {
	
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationByonicPreferencePage.class);
	private TableViewer tableViewer;
	private Button upButton;
	private Button downButton;
	private Button deleteComponent;
	
	MSGlycanAnnotationByonicPreference preference=null;
	private ComponentsSelectionListAdapter selectionChangedListener;

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout(4, false);
		parent.setLayout(layout);
		
		Button addComponent = new Button(parent, SWT.PUSH);
		addComponent.setText("Add");
		GridData gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1);
		addComponent.setLayoutData(gridData);
		
		addComponent.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddSequenceDialog d= new AddSequenceDialog(getShell(), 
						"Add new component", "Enter the GlycoCT sequence for the new component", "", new IInputValidator() {
					
					@Override
					public String isValid(String newText) {
						// validate the sequence
						SugarImporterGlycoCTCondensed importer = new SugarImporterGlycoCTCondensed();
						try {
							importer.parse(newText);
						} catch (SugarImporterException e) {
							return "Sequence is not valid. Reason: " + e.getMessage();
						}
						
						return null;
					}
				});
				if (d.open() == Window.OK) {
					String sequence = d.getValue();
					InputDialog d2 = new InputDialog(getShell(), 
							"Add a new component", "Enter a label for the new component", "", new IInputValidator() {
								
						@Override
						public String isValid(String newText) {
							if (newText == null || newText.trim().isEmpty())
								return "Cannot leave label empty";
							if (preference != null) {
								for (SearchQueryItem s : preference.getComponents()) {
									if (s.getName().equals(newText))
										return "This component already exists! Enter a different name";
								}
							}
							return null;
						}
					});
					if (d2.open() == Window.OK) {
						String label = d2.getValue();
						SearchQueryItem newComponent = new SearchQueryItem(label, sequence);
						if (preference != null) {
							preference.getComponents().add(newComponent);
							tableViewer.refresh();
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		deleteComponent = new Button(parent, SWT.PUSH);
		deleteComponent.setText("Delete");
		gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1);
		deleteComponent.setLayoutData(gridData);
		deleteComponent.setEnabled(false);
		
		deleteComponent.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection= (IStructuredSelection) tableViewer.getSelection();
				List<?> selections= selection.toList();
				if (!selections.isEmpty()) {
					SearchQueryItem component = (SearchQueryItem) selections.get(0);
					if (preference != null) {
						preference.getComponents().remove(component);
						tableViewer.refresh();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(parent, SWT.NONE);
		
		initComponentsTable(parent);
	
		return parent;
	}
	

	private void initComponentsTable(Composite parent) {
		
		Group tableContainer = new Group(parent, SWT.NONE);
		tableContainer.setText("");
		GridData gridData1 = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData1.horizontalSpan = 4;		
		tableContainer.setLayoutData(gridData1);		
		GridLayout layout = new GridLayout(4, false);
		//		layout.marginWidth = 10;
		layout.horizontalSpacing = 25;
		layout.marginLeft = 10;
		layout.marginTop = 10;
		tableContainer.setLayout( layout );
		
		GridData gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false, 3, 6);
		gridData.heightHint = 200;
		tableViewer = new TableViewer(tableContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.getTable().setLayoutData(gridData);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		
		TableViewerColumn orderColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn order = orderColumn.getColumn();
		order.setText("Order");
		order.setWidth(50);
		orderColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SearchQueryItem) {
					ComponentSelectionListContentProvider cp= (ComponentSelectionListContentProvider) tableViewer.getContentProvider();
					return String.valueOf(cp.getComponents().indexOf(element)+1);
				}
				else
					return null;
			}
		});
		
		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setText("Label");
		nameColumn.getColumn().setWidth(100);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof SearchQueryItem) {
					return ((SearchQueryItem) element).getName();
				}
				else
					return null;
			}
		});
		
		TableViewerColumn sequenceColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		sequenceColumn.getColumn().setText("Sequence");
		sequenceColumn.getColumn().setWidth(100);
		sequenceColumn.setLabelProvider(new OwnerDrawLabelProvider() {
			private static final int TEXT_MARGIN = 3;

		    @Override
		    protected void erase(final Event event, final Object element)
		    {
		      event.detail &= ~SWT.FOREGROUND;
		    }
		    
			@Override
			protected void paint(Event event, Object element) {
				if (element instanceof SearchQueryItem) {
					final String text = ((SearchQueryItem) element).getSequence();
				    event.gc.drawText(text, event.x + TEXT_MARGIN, event.y, true);
				}
			}
			
			@Override
			protected void measure(Event event, Object element) {
				if (element instanceof SearchQueryItem) {
					final String text = ((SearchQueryItem) element).getSequence();
					final Point size = event.gc.textExtent(text);
					event.width = size.x + 2 * TEXT_MARGIN;
					event.height = Math.max(event.height, size.y + TEXT_MARGIN);
				}
			}
		});
		
		addUpDownButtons(tableContainer);
		
		loadPreferences();
		
		if (preference == null) {
			tableViewer.setContentProvider(new ComponentSelectionListContentProvider(new ArrayList<SearchQueryItem>()));
			tableViewer.setInput(new ArrayList<SearchQueryItem>());
		} else {
			tableViewer.setContentProvider(new ComponentSelectionListContentProvider(preference.getComponents()));
			tableViewer.setInput(preference.getComponents());
		}

		selectionChangedListener = new ComponentsSelectionListAdapter();
		tableViewer.addSelectionChangedListener(selectionChangedListener);
	}

	private void addUpDownButtons(Composite container) {
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		upButton = new Button(container, SWT.PUSH);
		upButton.setText("Up");
		GridData gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1);
		upButton.setLayoutData(gridData);
		upButton.setEnabled(false);
		upButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComponentSelectionListContentProvider cp = (ComponentSelectionListContentProvider) tableViewer.getContentProvider();
	    		cp.up(getElementList(), tableViewer);
	    		preference.setComponents(cp.getComponents());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		downButton = new Button(container, SWT.PUSH);
		downButton.setText("Down");
		gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1);
		downButton.setLayoutData(gridData);
		downButton.setEnabled(false);
		downButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComponentSelectionListContentProvider cp = (ComponentSelectionListContentProvider) tableViewer.getContentProvider();
	    		cp.down(getElementList(), tableViewer);
	    		preference.setComponents(cp.getComponents());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new Label(container, SWT.NONE);
	}
	
	List<?> getElementList() {
		IStructuredSelection selection= (IStructuredSelection) tableViewer.getSelection();
		List<?> elements= selection.toList();
		ArrayList<Object> elementList= new ArrayList<>();

		for (int i= 0; i < elements.size(); i++) {
			elementList.add(elements.get(i));
		}
		return elementList;
	}

	class ComponentsSelectionListAdapter implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection= (IStructuredSelection) tableViewer.getSelection();

			List<?> selections= selection.toList();
			ComponentSelectionListContentProvider cp= (ComponentSelectionListContentProvider) tableViewer.getContentProvider();

			upButton.setEnabled(cp.canMoveUp(selections));
			downButton.setEnabled(cp.canMoveDown(selections));
			deleteComponent.setEnabled(!selections.isEmpty());
		}
	}
	
	class AddSequenceDialog extends InputDialog {

		public AddSequenceDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		
		@Override
	    protected int getInputTextStyle() {
	      return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
	    }

	    @Override
	    protected Control createDialogArea(Composite parent) {
	      Control res = super.createDialogArea(parent);
	      ((GridData) this.getText().getLayoutData()).heightHint = 100;
	      return res;
	    }
	}
	

	@Override
	protected void performDefaults() {	
		preference = MSGlycanAnnotationByonicPreference.loadDefaultSettings();
		if (tableViewer != null) {
			// we have to set the content provider again since setInput tries to keep the current order in the table
			// causing it to not to refresh properly after order changes and after deletion
			tableViewer.setContentProvider(new ComponentSelectionListContentProvider(preference.getComponents()));
			tableViewer.setInput(preference.getComponents());
		}
	}
	
	void loadPreferences() {
		try {
			preference = MSGlycanAnnotationByonicPreference.getByonicPreferences(
					MSGlycanAnnotationByonicPreference.getPreferenceEntity());
			
		} catch (UnsupportedVersionException e) {
			logger.warn("Preferences are not up to date. Cannot read the old version");
		}
	}
	
	@Override
	public boolean performOk() {
		if (preference == null)
			return false;
		return preference.save();
	}
}
