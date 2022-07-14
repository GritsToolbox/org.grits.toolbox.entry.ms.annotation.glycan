package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.GlycanSettings;

public class DatabaseSettingsTableComposite extends Composite {
	
	List<AnalyteSettings> anaylteSettings = new ArrayList<>();
	TableViewer settingsTableViewer;

	private Table settingsTable;

	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public DatabaseSettingsTableComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	/**
	 * sets the anaylteSettings to be displayed in the table
	 * 
	 * @param anaylteSettings list of analyteSettings
	 */
	public void setAnaylteSettings(List<AnalyteSettings> anaylteSettings) {
		this.anaylteSettings = anaylteSettings;
		if (settingsTableViewer != null) {
			settingsTableViewer.setInput(anaylteSettings);
			settingsTableViewer.refresh();
		}
	}
	
	/**
	 * return the analyteSettings in the table
	 * 
	 * @return the list of analyte settings in the table
	 */
	public List<AnalyteSettings> getAnaylteSettings() {
		return anaylteSettings;
	}
	
	public TableViewer getSettingsTableViewer() {
		return settingsTableViewer;
	}

	/**
	 * adds the table viewer to the composite
	 */
	public void createTable() {	
		GridLayout layout = new GridLayout(1, true);
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    this.setLayout(layout);
	    
	    settingsTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
	    settingsTable = settingsTableViewer.getTable();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_table_2.heightHint = 80;
		settingsTable.setLayoutData(gd_table_2);
		settingsTable.setHeaderVisible(true);
		settingsTable.setLinesVisible(true);
		
		TableViewerColumn databaseNameColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		TableColumn dColumn = databaseNameColumn.getColumn();
		dColumn.setText("Database Name");
		dColumn.setWidth(200);
		databaseNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AnalyteSettings) {
					GlycanSettings gSettings = ((AnalyteSettings) element).getGlycanSettings();
					if (gSettings != null && gSettings.getFilter() != null) {
						String dbName = gSettings.getFilter().getDatabase();
						if (dbName.lastIndexOf(File.separator) != -1)
							dbName = dbName.substring (dbName.lastIndexOf(File.separator) + 1);
						return dbName + " (version: " + (gSettings.getFilter().getVersion() != null ? gSettings.getFilter().getVersion() : "1.0") + ")";
					}
				}
				return "";
			}
		});
		
		TableViewerColumn derivColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		derivColumn.getColumn().setText("Derivazitation Name");;
		derivColumn.getColumn().setWidth(150);
		derivColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AnalyteSettings) {
					GlycanSettings gSettings = ((AnalyteSettings) element).getGlycanSettings();
					if (gSettings != null) {
						return gSettings.getPerDerivatisationType();
					}
				}
				return "";
			}
		});
		
		TableViewerColumn reducingEndColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		reducingEndColumn.getColumn().setText("Reducing End Name");
		reducingEndColumn.getColumn().setWidth(150);
		reducingEndColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AnalyteSettings) {
					GlycanSettings gSettings = ((AnalyteSettings) element).getGlycanSettings();
					if (gSettings != null && gSettings.getReducingEnd() != null) {
						return gSettings.getReducingEnd().getLabel();
					}
				}
				return "";
			}
		});
		
		TableViewerColumn useDBMetadateColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		useDBMetadateColumn.getColumn().setText("Use Database Metadata");
		useDBMetadateColumn.getColumn().setWidth(100);
		useDBMetadateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AnalyteSettings) {
					GlycanSettings gSettings = ((AnalyteSettings) element).getGlycanSettings();
					if (gSettings != null && gSettings.getFilter() != null) {
						if (gSettings.getFilter().getUseDatabaseStructureMetaInfo()) {
							//TODO how to grey out the columns??
						}
						return gSettings.getFilter().getUseDatabaseStructureMetaInfo() ? "yes" : "no";
					}
				}
				return "";
			}
		});
		TableViewerColumn filterColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);		
		filterColumn.getColumn().setText("Filter");
		filterColumn.getColumn().setWidth(200);
		filterColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AnalyteSettings) {
					GlycanSettings gSettings = ((AnalyteSettings) element).getGlycanSettings();
					if (gSettings != null && gSettings.getFilterSetting() != null) {
						return gSettings.getFilterSetting().getName() != null ? gSettings.getFilterSetting().getName() : gSettings.getFilterSetting().getFilter().toString();
					}
				}
				return "";
			}
		});
		settingsTableViewer.setContentProvider(new ArrayContentProvider());
		settingsTableViewer.setInput(anaylteSettings);	
	}
}
