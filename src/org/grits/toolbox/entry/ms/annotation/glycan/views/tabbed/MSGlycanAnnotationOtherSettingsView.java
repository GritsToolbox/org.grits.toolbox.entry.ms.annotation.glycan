package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.inject.Inject;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.dialog.MSGlycanAnnotationCustomAnnotationDialog;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationCustomAnnotationsPreferenceUI;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanCustomAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationOtherSettingsView;

public class MSGlycanAnnotationOtherSettingsView extends MSAnnotationOtherSettingsView implements IPropertyChangeListener {
		
	@Inject
	public MSGlycanAnnotationOtherSettingsView(Entry entry) {
		super(entry);
	}
	
	@Override
	protected MSAnnotationMultiPageViewer getCurrentViewer () {
		return MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(MSGlycanAnnotationOtherSettingsView.this.getPart().getContext(), this.entry.getParent());
	}
	
	@Override
	protected void openModifyDialog(MSAnnotationMultiPageViewer curView) {
		if( MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog == null || 
				MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell() == null || 
						MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell().isDisposed() ) {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog = 
					new MSGlycanAnnotationCustomAnnotationDialog(Display.getCurrent().getActiveShell(), curView);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.addListener(curView);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.addListener(this);
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.open();
		} else {
			MSGlycanAnnotationMultiPageViewer.msGlycanAnnotationCustomAnnotationDialog.getShell().forceActive();
		}
	}
	
	@Override
	protected void initializeCustomAnnotations() {
		localAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(this.entry, localAnnotations);
		entryAnnotations = new MSGlycanCustomAnnotationPreference();
		MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(this.entry, entryAnnotations);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// refresh table
		if (event.getSource() instanceof MSGlycanAnnotationCustomAnnotationDialog ) {
			MSGlycanAnnotationCustomAnnotationsPreferenceUI.initAnnotationFromEntry(this.entry, entryAnnotations);
			refreshCustomAnnotations ();
		}		
	}

	
}
