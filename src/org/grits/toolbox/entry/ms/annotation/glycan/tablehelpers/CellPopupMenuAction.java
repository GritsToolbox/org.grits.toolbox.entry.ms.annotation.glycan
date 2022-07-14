package org.grits.toolbox.entry.ms.annotation.glycan.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

public class CellPopupMenuAction implements IMouseAction {

    private Menu menu;

	public CellPopupMenuAction(Menu bodyMenu) {
		this.menu = bodyMenu;
	}

	@Override
    public void run(NatTable natTable, MouseEvent event) {
        int columnPosition = natTable.getColumnPositionByX(event.x);
        int rowPosition = natTable.getRowPositionByY(event.y);

        ILayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);

        if (!cell.getDisplayMode().equals(DisplayMode.SELECT)) {
            natTable.doCommand(
                    new SelectCellCommand(
                            natTable,
                            columnPosition,
                            rowPosition,
                            false,
                            false));
        }

        menu.setData(event.data);
        menu.setData("columnIndex", columnPosition);
        menu.setVisible(true);
    }
}