package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationMenuItemProviders;

/**
 * Adds items to the pop-up menu of the row header (left-hand side of row). 
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSGlycanAnnotationSummaryMenuItemProviders extends MSAnnotationMenuItemProviders {
	public static IMenuItemProvider hideCommonFragmentsMenuItemProvider() {
		return hideCommonFragmentsMenuItemProvider("Hide common fragments"); //$NON-NLS-1$
	}

	public static IMenuItemProvider hideCommonFragmentsMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {				
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if( natTable instanceof MSGlycanAnnotationSummaryTable ) {
							MSGlycanAnnotationSummaryTable msTable = (MSGlycanAnnotationSummaryTable) natTable;
							msTable.hideCommonFragments();
							msTable.updatePreferenceSettingsFromCurrentView();
							msTable.getPreference().writePreference();		
						}

					}
				});
			}
		};
	}

	public static IMenuItemProvider showCommonFragmentsRowMenuItemProvider() {
		return showCommonFragmentsMenuItemProvider("Show common fragments"); //$NON-NLS-1$
	}

	public static IMenuItemProvider showCommonFragmentsMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
				showAllColumns.setText(menuLabel);
				showAllColumns.setEnabled(true);

				showAllColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if( natTable instanceof MSGlycanAnnotationSummaryTable ) {
							MSGlycanAnnotationSummaryTable msTable = (MSGlycanAnnotationSummaryTable) natTable;
//							msTable.showUnannotatedRows();
							msTable.showCommonFragments();
							msTable.updatePreferenceSettingsFromCurrentView();
							msTable.getPreference().writePreference();	
						}
					}
				});
			}
		};
	}

}
