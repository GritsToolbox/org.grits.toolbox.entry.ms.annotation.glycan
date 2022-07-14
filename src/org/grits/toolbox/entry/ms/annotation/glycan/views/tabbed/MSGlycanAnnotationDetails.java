package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.entry.ms.annotation.command.ViewRowChooserInTabCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationDetails;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPeaksView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSelectionView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.utils.data.CartoonOptions;

public class MSGlycanAnnotationDetails extends MSAnnotationDetails {
	public static final String VIEW_ID = "ms.annotation.glycan.views.MSGlycanAnnotationDetails";
	private CartoonOptions cartoonOptions = null;

	@Inject
	public MSGlycanAnnotationDetails(MSAnnotationMultiPageViewer parentViewer, Entry entry, 
			Property msEntityProperty, CartoonOptions cartoonOptions, 
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super(parentViewer, entry, msEntityProperty, iMinMSLevel);
		this.cartoonOptions = cartoonOptions;
	}
	
	@Override
	protected void initPeakViews() {
		super.initPeakViews();
	}
	
	@Override
	public String toString() {
		return "MSGlycanAnnotationDetails (" + entry + ")";
	}
	
	public void setCartoonOptions(CartoonOptions cartoonOptions) {
		this.cartoonOptions = cartoonOptions;
	}
	
	public CartoonOptions getCartoonOptions() {
		return cartoonOptions;
	}
	
	@Override
	protected MSAnnotationEntityScroller getNewMSAnnotationEntityScroller() {
		return new MSGlycanAnnotationEntityScroller(compositeTop, SWT.None, this, this.cartoonOptions);
	}

	@Override
	protected MSAnnotationPeaksView getNewPeaksView( Entry entry, MSAnnotationEntityProperty msEntityProperty ) {
		getPart().getContext().set(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, msEntityProperty);
		getPart().getContext().set(Entry.class, entry);
		MSGlycanAnnotationPeaksView view = ContextInjectionFactory.make(MSGlycanAnnotationPeaksView.class, getPart().getContext());
		return view;
				//new MSGlycanAnnotationPeaksView(getParent(), entry, msEntityProperty, getMinMSLevel());
	}
	
	@Override
	protected MSAnnotationMultiPageViewer getParentMultiPageViewer() {
		Entry parentEntry = getEntry().getParent();
		MSGlycanAnnotationMultiPageViewer viewer = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(getPart().getContext(), parentEntry);		
		return viewer;
	}	
	
	public static void showRowSelection(IEclipseContext context, Entry entry, MSAnnotationTable parentTable, int iRowNumber, int iScanNum, String sRowId ) {
		MSGlycanAnnotationMultiPageViewer parent = MSGlycanAnnotationMultiPageViewer.getActiveViewerForEntry(context, entry);
		if ( parent != null ) {
			MSGlycanAnnotationPeaksView me = (MSGlycanAnnotationPeaksView) parent.getAnnotationDetails().getCurrentPeaksView();
			MSAnnotationSelectionView viewer = ViewRowChooserInTabCommandExecutor.showRowChooser(me, parentTable, iRowNumber, iScanNum, sRowId);
			me.setSelectionView(viewer);
			me.getBottomPane().layout();
		}
	}
	
	/**
	 * This method is called whenever a preference change occurs
	 * We need to act upon cartoon preference changes for this view 
	 * 
	 * @param preferenceName
	 */
	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	 					String preferenceName)
	{
	 	if (MSGlycanAnnotationCartoonPreferences.getPreferenceID().equals(preferenceName)) {
			if( this.getPeaksViews() != null ) {
				MSGlycanAnnotationEntityScroller entityScroller = (MSGlycanAnnotationEntityScroller) this.getEntityScroller();
				entityScroller.refreshImages();
			}
	 	}
	}
}
