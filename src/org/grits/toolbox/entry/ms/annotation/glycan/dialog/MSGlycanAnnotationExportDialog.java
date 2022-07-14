package org.grits.toolbox.entry.ms.annotation.glycan.dialog;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.dialog.FilterDialog;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;

public class MSGlycanAnnotationExportDialog extends MSAnnotationExportDialog {
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationExportDialog.class);

	private FiltersLibrary library;
	

	public MSGlycanAnnotationExportDialog(Shell parentShell,
			MSAnnotationExportFileAdapter msAnnotationExportFileAdapter) {
		super(parentShell, msAnnotationExportFileAdapter);
		try {
			library = FileUtils.readFilters(FileUtils.getFilterPath());
		} catch (UnsupportedEncodingException e) {
			logger.error("Error loading the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Cannot locate the filters file", e);
		} catch (JAXBException e) {
			logger.error("Error loading the filters", e);
		}
	}
	
	@Override
	protected FilterDialog getNewFilterDialog () {
		GlycanFilterDialog dialog = new GlycanFilterDialog(getParentShell());
		dialog.setPreFilters(library.getFilters());
		dialog.setCategories(library.getCategories());
		return dialog;
	}

}
