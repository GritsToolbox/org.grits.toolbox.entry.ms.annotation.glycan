package org.grits.toolbox.entry.ms.annotation.glycan.adaptor;

import org.grits.toolbox.io.ms.annotation.glycan.process.export.MSGlycanAnnotationSummaryExportProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportProcess;

public class MSGlycanAnnotationSummartExportFileAdapter extends MSGlycanAnnotationExportFileAdapter {

	@Override
	protected MSAnnotationExportProcess getNewExportProcess() {
		return new MSGlycanAnnotationSummaryExportProcess();
	}
}
