package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.command.ViewMSOverviewCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationScanHierarchyView;

public class MSGlycanAnnotationScanHierarchyView extends MSAnnotationScanHierarchyView {

	@Override
	protected void showMSOverview(Entry newEntry) {
		ViewMSOverviewCommandExecutor.showMSOverview(parentView.getContext(), newEntry);		
	}
}
