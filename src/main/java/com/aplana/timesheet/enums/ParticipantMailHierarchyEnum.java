package com.aplana.timesheet.enums;

import com.google.common.collect.Sets;

import java.util.Set;

import static com.aplana.timesheet.enums.ProjectRolesEnum.*;

/**
 * User: vsergeev
 * Date: 05.02.13
 */
public enum ParticipantMailHierarchyEnum {
    CHIEFS_OF_ANALYST (ANALYST, Sets.newHashSet(LEADING_ANALYST)),
    CHIEFS_OF_DEVELOPER (DEVELOPER, Sets.newHashSet(HEAD_OF_DEVELOPMENT)),
    CHIEFS_OF_HEAD_OF_DEVELOPMENT (HEAD_OF_DEVELOPMENT, Sets.newHashSet(HEAD_OF_DEVELOPMENT)),
    CHIEF_OF_SYSTEM_ENGINEER (SYSTEM_ENGINEER, Sets.newHashSet(HEAD_OF_DEVELOPMENT));

    private ProjectRolesEnum employeeProjectRolesEnum;
    private Set<ProjectRolesEnum> chiefsProjectRolesEnums;

    ParticipantMailHierarchyEnum(ProjectRolesEnum employeeRole, Set<ProjectRolesEnum> additionChiefRole) {
        this.employeeProjectRolesEnum = employeeRole;
        this.chiefsProjectRolesEnums = getDafaultChiefsRolesEnum();
        if (additionChiefRole != null) {
            this.chiefsProjectRolesEnums.addAll(additionChiefRole);
        }
    }

    public static Set<ProjectRolesEnum> getDafaultChiefsRolesEnum() {
        return Sets.newHashSet(PROJECT_MANAGER);
    }


    public ProjectRolesEnum getEmployeeProjectRoleEnum() {
        return employeeProjectRolesEnum;
    }

    public Set<ProjectRolesEnum> getChiefsProjectRolesEnums() {
        return chiefsProjectRolesEnums;
    }

    public static ParticipantMailHierarchyEnum tryFindEnumByRoleId( int roleId) {
        for (ParticipantMailHierarchyEnum enumValue : ParticipantMailHierarchyEnum.values()) {
            if (enumValue.getEmployeeProjectRoleEnum().getId() == roleId) {
                return enumValue;
            }
        }
        return null;
    }
}
