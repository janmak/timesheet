package com.aplana.timesheet.util;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Project;

import java.util.List;
import java.util.Set;

public abstract class ProjectHelper {

	public static String getProjectListJson(List<Division> divisions) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < divisions.size(); i++) {
			sb.append("{divId:'");
			sb.append(divisions.get(i).getId());
			Set<Project> projects = divisions.get(i).getProjects();
			sb.append("', divProjs:[");
			if (projects.size() > 0) {
				int count = 0;
				//logger.debug("For division {} available {} projects.", divisions.get(i).getId(), projects.size());
				for (Project project : projects) {
					if (project.isActive()) {
						sb.append("{id:'");
						sb.append(project.getId());
						sb.append("', value:'");
						sb.append(project.getName());
						sb.append("', state:'");
						sb.append(project.getState().getId());
						sb.append("'}");
						sb.append(", ");
					}
					count++;
				}
				sb.deleteCharAt(sb.length() - 2);
				sb.append("]}");
			} else {
				sb.append("{id:'0', value:''}]}");
			}

			if (i < (divisions.size() - 1)) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
