package org.grits.toolbox.entry.ms.annotation.glycan.views.tabbed.content;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.content.AnnotationSettingsTableComposite;
import org.grits.toolbox.ms.om.data.Fragment;
import org.grits.toolbox.ms.om.data.FragmentPerActivationMethod;
import org.grits.toolbox.ms.om.data.FragmentPerMsLevel;
import org.grits.toolbox.ms.om.data.GlycanSettings;

public class GlycanSettingsTableComposite extends AnnotationSettingsTableComposite {

	public GlycanSettingsTableComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	public void createAnalyteSettingsTable() {
		Label label = new Label(this, SWT.NONE);
		label.setText("Glycan Settings");
		
		Tree glycanSettingsTree = new Tree(this, SWT.NONE);
		glycanSettingsTree.setHeaderVisible(true);
		glycanSettingsTree.setLinesVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.heightHint = 100;
		glycanSettingsTree.setLayoutData(gd);
		
		TreeColumn nameColumn = new TreeColumn(glycanSettingsTree, SWT.NONE);
		nameColumn.setText("Setting");
		nameColumn.setWidth(200);
		
		TreeColumn valueColumn = new TreeColumn(glycanSettingsTree, SWT.NONE);
		valueColumn.setText("Value");
		valueColumn.setWidth(150);
		
		if (!this.method.getAnalyteSettings().isEmpty() && this.method.getAnalyteSettings().get(0) != null)
			addTreeData (glycanSettingsTree, this.method.getAnalyteSettings().get(0).getGlycanSettings());
		
		glycanSettingsTree.redraw();
	}

	/**
	 * add rows to the tree table for the given glycan settings
	 * @param glycanSettingsTree tree to add the items into
	 * @param gSettings glycan settings to display
	 */
	private void addTreeData(Tree glycanSettingsTree, GlycanSettings gSettings) {
		TreeItem item = new TreeItem(glycanSettingsTree, SWT.NONE);
		String sIons = "";
		String sw = "";
		for( Fragment frag : gSettings.getGlycanFragments() ) {
			sIons += sw + frag.getType();
			sw = ", ";
		}
		item.setText(new String[] {"Cleavage Types", sIons});
		
		item = new TreeItem(glycanSettingsTree, SWT.NONE);
		item.setText(new String[] {"Max # Cleavages", gSettings.getMaxNumOfCleavages() + ""});
		
		item = new TreeItem(glycanSettingsTree, SWT.NONE);
		item.setText(new String[] {"Max # Cross-ring Cleavages", gSettings.getMaxNumOfCrossRingCleavages() + ""});
		
		if (gSettings.getGlycanFragments() != null && !gSettings.getGlycanFragments().isEmpty()) {
			item = new TreeItem(glycanSettingsTree, SWT.NONE);
			item.setText(0, "Fragments:");
			item.setText(1, "");
			addFragments (item, gSettings.getGlycanFragments());
			item.setExpanded(true);
		}
		
		if (gSettings.getPerActivation() != null && !gSettings.getPerActivation().isEmpty()) {
			item = new TreeItem(glycanSettingsTree, SWT.NONE);
			item.setText(0, "Fragments Per Activation:");
			item.setText(1, "");
			addFragmentsPerActivation (item, gSettings.getPerActivation());
			item.setExpanded(true);
		}
		
		if (gSettings.getPerMsLevel() != null && !gSettings.getPerMsLevel().isEmpty()) {
			item = new TreeItem(glycanSettingsTree, SWT.NONE);
			item.setText(0, "Fragments Per Ms Level:");
			item.setText(1, "");
			addFragmentsPerMsLevel (item, gSettings.getPerMsLevel());
			item.setExpanded(true);
		}
	}

	/**
	 * add fragment info to the tree
	 * @param parent parent row to add items
	 * @param glycanFragments list of fragments
	 */
	private void addFragments(TreeItem parent, List<Fragment> glycanFragments) {
		int i=0;
		for (Fragment fragment: glycanFragments) {
			TreeItem item = new TreeItem(parent, SWT.NONE);
			item.setText(0, "Fragment Num: " + (i+1));
			item.setText(1, "");
			TreeItem child1 = new TreeItem (item, SWT.NONE);
			child1.setText(new String[] {"Fragmentation Type", fragment.getType()});
			TreeItem child2 = new TreeItem (item, SWT.NONE);
			child2.setText(new String[] {"Fragmentation Number", fragment.getNumber()});
			item.setExpanded(true);
			i++;
		}
	}
	
	/**
	 * adds fragments per activation method into the tree
	 * @param parent parent row to add items
	 * @param perActivation list of fragments per activation method
	 */
	private void addFragmentsPerActivation(TreeItem parent,
			List<FragmentPerActivationMethod> perActivation) {
		int i=0; 
		for (FragmentPerActivationMethod frag: perActivation) {
			TreeItem item = new TreeItem(parent, SWT.NONE);
			item.setText(0, "Fragments Per Activation: " + (i+1));
			item.setText(1, "");
			TreeItem child1 = new TreeItem (item, SWT.NONE);
			child1.setText(new String[] {"Activation Method", frag.getActivationMethod()});
			TreeItem child2 = new TreeItem (item, SWT.NONE);
			child2.setText(new String[] {"Max Num Cleavages", frag.getMaxNumOfCleavages() + ""});
			TreeItem child3= new TreeItem (item, SWT.NONE);
			child3.setText(new String[] {"Max Num Cross-ring Cleavages", frag.getMaxNumOfCrossRingCleavages() + ""});
			TreeItem child4 = new TreeItem(item, SWT.NONE);
			child4.setText(0, "Fragments:");
			child4.setText(1, "");
			if (frag.getFragments() != null)
				addFragments (child4, frag.getFragments());
			child4.setExpanded(true);
			item.setExpanded(true);
			i++;
		}
	}
	
	/**
	 * adds fragments per ms level into the tree
	 * @param parent parent row to add items
	 * @param perActivation list of fragments per ms level
	 */
	private void addFragmentsPerMsLevel(TreeItem parent, List<FragmentPerMsLevel> perMsLevel) {
		int i=0; 
		for (FragmentPerMsLevel frag: perMsLevel) {
			TreeItem item = new TreeItem(parent, SWT.NONE);
			item.setText(0, "Fragments Per MS Level: " + (i+1));
			item.setText(1, "");
			TreeItem child1 = new TreeItem (item, SWT.NONE);
			child1.setText(new String[] {"MS Level", frag.getMsLevel() + ""});
			TreeItem child2 = new TreeItem (item, SWT.NONE);
			child2.setText(new String[] {"Max Num Cleavages", frag.getM_maxNumOfCleavages() + ""});
			TreeItem child3= new TreeItem (item, SWT.NONE);
			child3.setText(new String[] {"Max Num Cross-ring Cleavages", frag.getM_maxNumOfCrossRingCleavages() + ""});
			TreeItem child4 = new TreeItem(item, SWT.NONE);
			child4.setText(0, "Fragments:");
			child4.setText(1, "");
			if (frag.getFragments() != null)
				addFragments (child4, frag.getFragments());
			child4.setExpanded(true);
			item.setExpanded(true);
			i++;
		}
	}

}
