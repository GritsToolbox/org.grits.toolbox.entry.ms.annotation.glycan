package org.grits.toolbox.entry.ms.annotation.glycan.preference.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.util.structure.glycan.count.SearchQueryItem;

public class ComponentSelectionListContentProvider implements IStructuredContentProvider {
	
	List<SearchQueryItem> components;
	
	public ComponentSelectionListContentProvider(List<SearchQueryItem> elements) {
		this.components = elements;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return components.toArray();
	}

	@Override
	public void dispose() {
		//do nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
	
	public boolean canMoveDown(List<?> selectedElements) {
		int nSelected= selectedElements.size();
		for (int index= components.size() - 1; index >= 0 && nSelected > 0; index--) {
			if (!selectedElements.contains(components.get(index))) {
				return true;
			}
			nSelected--;
		}
		return false;
	}

	public boolean canMoveUp(List<?> selected) {
		int nSelected= selected.size();
		for (int index= 0; index < components.size() && nSelected > 0; index++) {
			if (!selected.contains(components.get(index))) {
				return true;
			}
			nSelected--;
		}
		return false;
	}
	
	private List<SearchQueryItem> moveUp(List<SearchQueryItem> elements, List<?> move) {
		List<SearchQueryItem> result= new ArrayList<>(elements.size());
		SearchQueryItem floating= null;
		for (int index= 0; index < elements.size(); index++) {
			SearchQueryItem current= elements.get(index);
			if (move.contains(current)) {
				result.add(current);
			} else {
				if (floating != null) {
					result.add(floating);
				}
				floating= current;
			}
		}
		if (floating != null) {
			result.add(floating);
		}
		return result;
	}

	private List<SearchQueryItem> reverse(List<SearchQueryItem> list) {
		List<SearchQueryItem> reverse= new ArrayList<>(list.size());
		for (int index= list.size() - 1; index >= 0; index--) {
			reverse.add(list.get(index));
		}
		return reverse;
	}

	public void setElements(List<SearchQueryItem> elements, TableViewer table) {
		this.components= new ArrayList<>(elements);
		if (table != null)
			table.refresh();
	}

	public void up(List<?> checked, TableViewer table) {
		if (checked.size() > 0) {
			setElements(moveUp(components, checked), table);
			table.reveal(checked.get(0));
		}
		table.setSelection(new StructuredSelection(checked));
	}
	
	public void down(List<?> checked, TableViewer table) {
		if (checked.size() > 0) {
			setElements(reverse(moveUp(reverse(components), checked)), table);
			table.reveal(checked.get(checked.size() - 1));
		}
		table.setSelection(new StructuredSelection(checked));
	}

	public List<SearchQueryItem> getComponents() {
		return this.components;
	}	

}
