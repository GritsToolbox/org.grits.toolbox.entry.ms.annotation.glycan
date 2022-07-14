package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.datamodel.ms.annotation.glycan.tablemodel.MSGlycanAnnotationTableDataObject;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;

/**
 * 
 * @author sena
 * 
 * configuration for right click popup menu for MSGlycanAnnotationTable
 *
 */
public class GlycanStructureExportConfiguration extends AbstractUiBindingConfiguration {
	static final Logger logger = Logger.getLogger(GlycanStructureExportConfiguration.class);
	
	private Menu bodyMenu;
	private List<Integer> cartoonCols = null;
	private MSGlycanAnnotationTable nattable;
	private DataLayer bodyLayer;
	
	public GlycanStructureExportConfiguration(List<Integer> cartoonCols, MSGlycanAnnotationTable nattable) {
		this.cartoonCols = cartoonCols;
		this.nattable = nattable;
		this.bodyLayer = nattable.getBottomDataLayer();
		this.bodyMenu = createBodyMenu(nattable).build();

		nattable.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				bodyMenu.dispose();
			}
		});
	}
	
	/**
	 * construct the menu for right click on the cartoon columns
	 * 
	 * @param natTable table to add the menu
	 * @return menu builder
	 */
	private PopupMenuBuilder createBodyMenu(NatTable natTable) {
		Menu menu = new Menu(natTable);
		
		final MenuItem item1 = new MenuItem(menu, SWT.PUSH);
		item1.setText("Export Structure as an image");
		item1.addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				String sequence = (String) item1.getData();
				if (sequence == null) // nothing to export, should not happen
					return;
				
				GlycanImageObject gio = MSGlycanAnnotationTableDataObject.glycanImageProvider.getImage(sequence);
				
				FileDialog dialog = new FileDialog(new Shell(), SWT.SAVE);
				// Set filter on .gws files
				dialog.setFilterExtensions(new String [] {"*.png", "*.svg", "*.jpg"});
                // Put in a readable name for the filter
                dialog.setFilterNames(new String[] { "PNG (*.png)", "SVG (*.svg)", "JPEG (*.jpg)" });
                dialog.setOverwrite(true);
				String result = dialog.open();
				if (result != null) {
					File file = new File (result);
					try {
						if (result.endsWith("jpg")) {
							BufferedImage image = gio.getAwtBufferedImage();
							// fix BufferedImage, otherwise it saves a black rectangle
							BufferedImage imageRGB = new BufferedImage(image.getWidth(),
							    image.getHeight(), BufferedImage.TYPE_INT_RGB);
							imageRGB.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
							ImageIO.write(imageRGB, "jpg", file);
						} else if (result.endsWith("png")) {
							ImageIO.write(gio.getAwtBufferedImage(), "png", file);
						} else if (result.endsWith("svg")) {
							gio.exportSVG(file);
						}
					} catch (IOException e1) {
						logger.error ("Error writing the sequence to the file", e1);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", 
								"Error exporting the structure to the selected file. Reason: " + e1.getMessage());
					} catch (Exception e1) {
						logger.error ("Error writing the sequence to the file in the specified format", e1);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", 
								"Error exporting the structure to the selected file in the specified format. Reason: " + e1.getMessage());
					}
				}
			}
	
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
		
			}
		});
		
		final MenuItem item2 = new MenuItem(menu, SWT.PUSH);
		item2.setText("Export as GlycoWorkbench Sequence");
		item2.addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				String sequence = (String) item2.getData();
				if (sequence == null) // nothing to export, should not happen
					return;
				
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
				// Set filter on .gws files
				dialog.setFilterExtensions(new String [] {"*.gws"});
                // Put in a readable name for the filter
                dialog.setFilterNames(new String[] { "GlycoWorkbench (*.gws)" });
                dialog.setOverwrite(true);
				String result = dialog.open();
				if (result != null) {
					File file = new File (result);
					BufferedWriter writer;
					try {
						writer = new BufferedWriter(new FileWriter(file));
						writer.write (sequence);
					    //Close writer
					    writer.close();
					} catch (IOException e1) {
						logger.error ("Error writing the sequence to the file", e1);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", 
								"Error exporting the structure to the selected file. Reason: " + e1.getMessage());
					}
				}
			}
	
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
		
			}
		});
		 
		return new PopupMenuBuilder(natTable, menu);
	}
	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// create a specific mouse event matcher to get right clicks on 
        // the cartoon columns only
		MouseEventMatcher matcher = new MouseEventMatcher(SWT.NONE, GridRegion.BODY, 3) {

			@Override
			public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
				if (super.matches(natTable, event, regionLabels)) {
					int columnPosition = natTable.getColumnPositionByX(event.x);
					int rowPosition = natTable.getRowPositionByY(event.y);
					int columnIndex = LayerUtil.convertColumnPosition(natTable, columnPosition, bodyLayer);
					int rowIndex = LayerUtil.convertRowPosition(natTable, rowPosition, bodyLayer);		
					if (cartoonCols.contains(columnIndex)) {
						// check if there is actually a cartoon
						String sCartoonID = (String) bodyLayer.getDataValueByPosition( columnIndex, rowIndex);	
						if ( sCartoonID == null || sCartoonID.equals("") ) 
							return false;
						
						if (((MSGlycanAnnotationTableDataObject)nattable.getGRITSTableDataObject()).getSequenceCols() == null ||
								((MSGlycanAnnotationTableDataObject)nattable.getGRITSTableDataObject()).getSequenceCols().isEmpty()) {
							MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Cannot find the glycan sequence");
							return false;		
						}
						Integer sequenceCol = ((MSGlycanAnnotationTableDataObject)nattable.getGRITSTableDataObject()).getSequenceCols().get(0);
						String sequence = (String) bodyLayer.getDataValueByPosition( sequenceCol, rowIndex);
						bodyMenu.getItems()[0].setData(sequence);
						bodyMenu.getItems()[1].setData(sequence);
						return true;
					}
				}
				
				return false;
			}
		};
		
		uiBindingRegistry.registerMouseDownBinding(matcher, new CellPopupMenuAction(bodyMenu));
		
		// need the following configurations to allow row header column to be resizable by the user
		uiBindingRegistry.registerMouseMoveBinding(new MouseEventMatcher(), new ClearCursorAction());
		uiBindingRegistry.registerFirstMouseMoveBinding(new ColumnResizeEventMatcher(SWT.NONE, GridRegion.CORNER, 0), new ColumnResizeCursorAction());
		// Column resize
		uiBindingRegistry.registerFirstMouseDragMode(new ColumnResizeEventMatcher(SWT.NONE, GridRegion.CORNER, 1), new ColumnResizeDragMode());
	}

}
