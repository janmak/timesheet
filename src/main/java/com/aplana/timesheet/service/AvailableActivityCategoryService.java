package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailableActivityCategoryService {
	@Autowired
	AvailableActivityCategoryDAO availableActivityCategoryDAO;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private DictionaryItemService dictionaryItemService;
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	public List<AvailableActivityCategory> getAvailableActivityCategories(
														DictionaryItem actType, ProjectRole projectRole) {
		return availableActivityCategoryDAO.getAvailableActivityCategories(actType, projectRole);
	}
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param project
	 * 			Проект\Пресейл.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	public List<AvailableActivityCategory> getAvailableActivityCategories(
										DictionaryItem actType, Project project, ProjectRole projectRole) {
		return availableActivityCategoryDAO.getAvailableActivityCategories(actType, project, projectRole);
	}

    public String getAvailableActCategoriesJson() {
        StringBuilder sb = new StringBuilder();
        List<DictionaryItem> actTypes = dictionaryItemService.getTypesOfActivity();
        List<ProjectRole> projectRoles = projectRoleService.getProjectRoles();
        sb.append("[");
        for (int i = 0; i < actTypes.size(); i++) {
            for ( ProjectRole projectRole : projectRoles ) {
                sb.append( "{actType:'" ).append( actTypes.get( i ).getId() ).append( "', " );
                sb.append( "projRole:'" ).append( projectRole.getId() ).append( "', " );
                List<AvailableActivityCategory> avActCats = getAvailableActivityCategories( actTypes.get( i ), projectRole );
                sb.append( "avActCats:[" );
                for ( int k = 0; k < avActCats.size(); k++ ) {
                    sb.append( "'" ).append( avActCats.get( k ).getActCat().getId() ).append( "'" );
                    if ( k < ( avActCats.size() - 1 ) ) {
                        sb.append( ", " );
                    }
                }
                sb.append( "]}" );
                if ( i < ( actTypes.size() ) ) {
                    sb.append( ", " );
                }
            }
        }
        return sb.toString().substring(0, (sb.toString().length() - 2)) + "]";
    }

}