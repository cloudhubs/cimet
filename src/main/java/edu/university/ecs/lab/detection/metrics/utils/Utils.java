package edu.university.ecs.lab.detection.metrics.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static <T> ArrayList<ArrayList<T>> pairs(List<T> list) {
		int size = list.size();
		ArrayList<ArrayList<T>> returned = new ArrayList<ArrayList<T>>();

		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				ArrayList<T> l = new ArrayList<T>();
				l.add(list.get(i));
				l.add(list.get(j));

				returned.add(l);
			}
		}

		return returned;
	}
}
