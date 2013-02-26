package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class AvailableActivityCategoryServiceTest extends AbstractJsonTest {

    @Autowired
    private AvailableActivityCategoryService availableActivityCategoryService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    private ProjectRoleService projectRoleService;

    private String getAvailableActCategoriesJson() {
        StringBuilder sb = new StringBuilder();
        List<DictionaryItem> actTypes = dictionaryItemService.getTypesOfActivity();
        List<ProjectRole> projectRoles = projectRoleService.getProjectRoles();
        sb.append("[");
        for (int i = 0; i < actTypes.size(); i++) {
            for ( ProjectRole projectRole : projectRoles ) {
                sb.append( "{\"actType\":\"" ).append( actTypes.get( i ).getId() ).append( "\"," );
                sb.append( "\"projRole\":\"" ).append( projectRole.getId() ).append( "\"," );
                List<AvailableActivityCategory> avActCats = availableActivityCategoryService
                        .getAvailableActivityCategories( actTypes.get( i ), projectRole );
                sb.append( "\"avActCats\":[" );
                for ( int k = 0; k < avActCats.size(); k++ ) {
                    sb.append( "\"" ).append( avActCats.get( k ).getActCat().getId() ).append( "\"" );
                    if ( k < ( avActCats.size() - 1 ) ) {
                        sb.append( "," );
                    }
                }
                sb.append( "]}" );
                if ( i < ( actTypes.size() ) ) {
                    sb.append( "," );
                }
            }
        }
        return sb.toString().substring(0, (sb.toString().length() - 1)) + "]";
    }

    @Test
    public void testGetAvailableActCategoriesJson() throws Exception {
        assertJsonEquals(getAvailableActCategoriesJson(), availableActivityCategoryService.getAvailableActCategoriesJson());
    }
}
