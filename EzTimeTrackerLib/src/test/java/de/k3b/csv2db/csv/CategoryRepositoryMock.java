package de.k3b.csv2db.csv;

import java.util.HashMap;

import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.model.TimeSliceCategory;

public class CategoryRepositoryMock implements ICategoryRepsitory {
	private HashMap<String, TimeSliceCategory> nameToCategory = new HashMap<String, TimeSliceCategory>();
	@Override
	public TimeSliceCategory getOrCreateCategory(String name) {
		// TODO Auto-generated method stub
		TimeSliceCategory result = nameToCategory.get(name);
		if (result == null) {
			result = new TimeSliceCategory();
			result.setCategoryName(name);
			nameToCategory.put(name, result);
		}
		return result;
	}		
}