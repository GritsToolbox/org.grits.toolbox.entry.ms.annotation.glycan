package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationHeaderMenuConfiguration;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationMenuItemProviders;

/**
 * Builds the row pop-up menu to provide options for showing/hiding rows.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MSGlycanAnnotationSummaryMenuItemProviders
 */
public class MSGlycanAnnotationSummaryHeaderMenuConfiguration extends MSAnnotationHeaderMenuConfiguration {

	public MSGlycanAnnotationSummaryHeaderMenuConfiguration(NatTable natTable) {
		super(natTable);
	}
	
	@Override
	protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
		PopupMenuBuilder pmb = new PopupMenuBuilder(natTable);
		pmb.withAutoResizeSelectedRowsMenuItem();
		pmb.withSeparator();
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.showAllRowMenuItemProvider());
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.hideRowMenuItemProvider());
		pmb.withSeparator();
		pmb.withMenuItemProvider(MSGlycanAnnotationSummaryMenuItemProviders.showCommonFragmentsRowMenuItemProvider());
		pmb.withMenuItemProvider(MSGlycanAnnotationSummaryMenuItemProviders.hideCommonFragmentsMenuItemProvider());
		return pmb;
								
	}

}
