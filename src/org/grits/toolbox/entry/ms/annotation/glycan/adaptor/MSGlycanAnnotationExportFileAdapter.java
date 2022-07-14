package org.grits.toolbox.entry.ms.annotation.glycan.adaptor;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationByonicPreference;
import org.grits.toolbox.io.ms.annotation.glycan.process.export.MSGlycanAnnotationExportByonicProcess;
import org.grits.toolbox.io.ms.annotation.glycan.process.export.MSGlycanAnnotationExportDatabaseProcess;
import org.grits.toolbox.io.ms.annotation.glycan.process.export.MSGlycanAnnotationExportProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportByonicProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportDatabaseProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportProcess;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.widgets.processDialog.ProgressDialog;

public class MSGlycanAnnotationExportFileAdapter extends MSAnnotationExportFileAdapter {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationExportFileAdapter.class);

	protected FilterSetting filterSetting;
	
	@Override
	protected MSAnnotationExportProcess getNewExportProcess() {
		return new MSGlycanAnnotationExportProcess();
	}
	
	@Override
	protected void exportExcel() throws IOException, Exception {
		//create progress dialog for copying files
		ProgressDialog t_dialog = new ProgressDialog(this.shell);
		//fill parameter
		MSAnnotationExportProcess t_worker = getNewExportProcess();
		t_worker.setOutputFile(sOutputFile);
		t_worker.setTableDataObject(getTableDataObject());
		t_worker.setMasterParentScan(getMasterParentScan());
		t_worker.setLastVisibleColInx(getLastVisibleColInx());
		((MSGlycanAnnotationExportProcess)t_worker).setFilterSetting(filterSetting);
		((MSGlycanAnnotationExportProcess)t_worker).setFilterKey (filterColumn);
		((MSGlycanAnnotationExportProcess)t_worker).setThresholdValue(thresholdValue);
		((MSGlycanAnnotationExportProcess)t_worker).setNumTopHits (numTopHits);
		//set the worker
		t_dialog.setWorker(t_worker);

		//check Cancel
		if(t_dialog.open() != SWT.OK)
		{
			//delete the file
			new File(sOutputFile).delete();
		}
	}
	
	@Override
	protected void exportByonic() throws IOException, Exception {
		//create progress dialog for processing export
		ProgressDialog t_dialog = new ProgressDialog(this.shell);
		//fill parameter
		MSAnnotationExportByonicProcess t_worker = getNewExportByonicProcess();
		t_worker.setOutputFile(sOutputFile);
		t_worker.setTableDataObject(getTableDataObject());
		t_worker.setMasterParentScan(getMasterParentScan());
		t_worker.setLastVisibleColInx(getLastVisibleColInx());
	
		MSGlycanAnnotationByonicPreference preference = 
				MSGlycanAnnotationByonicPreference.getByonicPreferences(MSGlycanAnnotationByonicPreference.getPreferenceEntity());
		if (preference == null) {
			logger.error("Could not load preferences for byonic database");
			throw new Exception ("Could not load preferences for byonic database");
		}
		
		((MSGlycanAnnotationExportByonicProcess) t_worker).setComponentList(preference.getComponents()); 
		//set the worker
		t_dialog.setWorker(t_worker);

		//check Cancel
		if(t_dialog.open() != SWT.OK)
		{
			//delete the file
			new File(sOutputFile).delete();
		}
	}
	
	@Override
	protected MSAnnotationExportDatabaseProcess getNewExportDatabaseProcess() {
		return new MSGlycanAnnotationExportDatabaseProcess();
	}

	@Override
	protected MSAnnotationExportByonicProcess getNewExportByonicProcess() {
		return new MSGlycanAnnotationExportByonicProcess();
	}
	
	public FilterSetting getFilterSetting() {
		return filterSetting;
	}
	
	public void setFilterSetting(FilterSetting filterSetting) {
		this.filterSetting = filterSetting;
	}
	
}
