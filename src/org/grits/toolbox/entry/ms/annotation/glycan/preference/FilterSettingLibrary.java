package org.grits.toolbox.entry.ms.annotation.glycan.preference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;

@XmlRootElement(name="filter-settings")
public class FilterSettingLibrary {
	
	List<FilterSetting> filterSettings;
	List<Category> categories;
	
	@XmlElement(name="filter-setting")
	public List<FilterSetting> getFilterSettings() {
		return filterSettings;
	}
	
	public void setFilterSettings(List<FilterSetting> filterSettings) {
		this.filterSettings = filterSettings;
	}

	public void add(FilterSetting newSetting) {
		if (filterSettings == null)
			filterSettings = new ArrayList<>();
		
		filterSettings.add(newSetting);
	}

	public void remove(FilterSetting filter) {
		if (filterSettings != null)
			filterSettings.remove(filter);
	}
	
	@XmlElement(name="filter-category")
	public List<Category> getCategories() {
		return categories;
	}
	
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public void addCategory (Category newCategory) {
		if (categories == null)
			categories = new ArrayList<>();
		categories.add(newCategory);
	}
	
	public void removeCategory (Category category) {
		if (categories != null)
			categories.remove(category);
	}
}
