package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.DatabaseSettingsTableComposite;
import org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.content.GlycanSettingsTableComposite;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPropertyView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.content.AnnotationSettingsTableComposite;
import org.grits.toolbox.ms.om.data.AnalyteSettings;

public class MSGlycanAnnotationPropertyView extends MSAnnotationPropertyView {
	
	@Inject
	public MSGlycanAnnotationPropertyView(Entry entry) {
		super(entry);
	}

	@Override
	protected void addAnalyteSettings(List<AnalyteSettings> aSettings,
			AnnotationSettingsTableComposite settingsComposite) {
		if( aSettings != null && !aSettings.isEmpty()) {
			settingsComposite.createAnalyteSettingsTable();
			addDatabaseSettingTable(aSettings);
		}		
	}
	
	@Override
	protected AnnotationSettingsTableComposite getSettingsComposite() {
		return new GlycanSettingsTableComposite(getContainer(), SWT.NONE);
	}
	
	protected void addDatabaseSettingTable(List<AnalyteSettings> aSettings) {
		Label lSpacer1 = new Label(getContainer(), SWT.NONE);	// column 1	
		lSpacer1.setText("Database Settings");
		GridData gridDataSpacer1 = new GridData();
		gridDataSpacer1.horizontalSpan = 6;
		lSpacer1.setLayoutData(gridDataSpacer1);
		
		DatabaseSettingsTableComposite dbSettingTable = new DatabaseSettingsTableComposite(getContainer(), SWT.NONE);
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		dbSettingTable.setLayoutData(gd2);
		dbSettingTable.setAnaylteSettings(aSettings);
		dbSettingTable.createTable();
	}

	
	
}
