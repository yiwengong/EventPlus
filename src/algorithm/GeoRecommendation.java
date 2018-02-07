package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getDBConnection();
		Set<String> favoriteItems = conn.getFavoriteItemIds(userId);
		Map<String, Integer> allCategories = new HashMap<>();
		for (String item : favoriteItems) {
			Set<String> categories = conn.getCategories(item);
			for (String category : categories) {
				if (allCategories.containsKey(category)) {
					allCategories.put(category, allCategories.get(category) + 1);
				} else {
					allCategories.put(category, 1);
				}
			}
		}
		
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return Integer.compare(e2.getValue(), e1.getValue());
			}
		});
		
		Set<Item> visitedItems = new HashSet<>();
		for (Entry<String, Integer> entry : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, entry.getKey());
			List<Item> filteredItems = new ArrayList<>();
			for (Item item : items) {
				if (!favoriteItems.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					// return the increasing order of distance.
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}

		return recommendedItems;
	}
}
