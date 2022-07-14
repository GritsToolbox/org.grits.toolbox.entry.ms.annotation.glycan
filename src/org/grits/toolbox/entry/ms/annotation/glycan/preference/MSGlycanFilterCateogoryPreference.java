package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;

public class MSGlycanFilterCateogoryPreference {
	private static final String PREFERENCE_NAME_ALL = "org.grits.toolbox.entry.ms.annotation.glycan.filter.category";
	private static final String CURRENT_VERSION = "1.0";
	
	Category categoryPreference;
	
	public void setCategoryPreference(Category categoryPreference) {
		this.categoryPreference = categoryPreference;
	}
	
	public Category getCategoryPreference() {
		return categoryPreference;
	}
	
	public static PreferenceEntity getPreferenceEntity() throws UnsupportedVersionException {
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
		return preferenceEntity;
	}
	
	public static MSGlycanFilterCateogoryPreference getMSGlycanFilterCategoryPreferences(PreferenceEntity preferenceEntity) throws UnsupportedVersionException {
		MSGlycanFilterCateogoryPreference preferenceSettings = new MSGlycanFilterCateogoryPreference();
		if(preferenceEntity != null && preferenceEntity.getValue() != null && !preferenceEntity.getValue().isEmpty()) {
			Category category = (Category) XMLUtils.getObjectFromXML(preferenceEntity.getValue(), Category.class);
			preferenceSettings.setCategoryPreference(category);
		} else {
			// load defaults
			// NULL for "All" categories selection
			preferenceSettings.setCategoryPreference(null);
		}
		return preferenceSettings;
	}
	
	public boolean saveValues() {
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setVersion(CURRENT_VERSION);
		if (this.getCategoryPreference() != null) {
			preferenceEntity.setValue(XMLUtils.marshalObjectXML(this.getCategoryPreference()));
			return PreferenceWriter.savePreference(preferenceEntity);
		} else // nothing to save - default is to see ALL filters (no category selection)
			return true;
	}

	
}
