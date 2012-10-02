/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aplana.timesheet.reports;

/**
 *
 * @author aimamutdinov
 */
public enum OverTimeCategory {
	Holiday("В выходные и праздники"),
	Simple("В рабочии дни"),
	All("Все");
	
	public String title;

	private OverTimeCategory() {
	}
	
	private OverTimeCategory(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return title;
	}

	public String getTitle() {
		return title;
	}
			
}
