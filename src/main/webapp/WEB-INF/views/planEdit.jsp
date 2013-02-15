<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="com.aplana.timesheet.controller.CreatePlanForPeriodContoller" %>
<%@ page import="static com.aplana.timesheet.constants.TimeSheetConstants.DOJO_PATH" %>
<%@ page import="static com.aplana.timesheet.controller.PlanEditController.*" %>
<%@ page import="static com.aplana.timesheet.form.PlanEditForm.*" %>
<%@ page import="static com.aplana.timesheet.controller.PlanEditController.PERCENT_OF_CHARGE" %>
<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%! private static final String GRID_JS_ID = "myGrid"; %>

<html>
<head>
    <title><fmt:message key="title.planEdit" /></title>
    <script src="<%= getResRealPath("/resources/js/DataGrid.ext.js", application) %>" type="text/javascript"></script>
    <script src="<%= getResRealPath("/resources/js/utils.js", application) %>" type="text/javascript"></script>

    <style type="text/css">
        /* REQUIRED STYLES!!! */
        @import "<%= DOJO_PATH %>/dojox/grid/resources/Grid.css";
        @import "<%= DOJO_PATH %>/dojox/grid/resources/tundraGrid.css";
        @import "<%= getResRealPath("/resources/css/DataGrid.ext.css", application) %>";

        #planEditForm > table {
            margin-bottom: 10px;
        }

        #planEditForm td {
            padding: 2px;
        }

        #planEditForm table table {
            width: 100%;
        }

        #planEditForm label {
            padding-left: 2px;
        }

    </style>

    <script type="text/javascript">
        var hasChanges = false;

        dojo.addOnLoad(function() {
            updateMultipleForSelect(dojo.byId("<%= REGIONS %>"));
            updateMultipleForSelect(dojo.byId("<%= PROJECT_ROLES %>"));

            var prevValue;

            <%= GRID_JS_ID %>.onStartEdit = function(inCell, inRowIndex) {
                prevValue = myStoreObject.items[inRowIndex][inCell.field][0];
            }

            <%= GRID_JS_ID %>.onApplyCellEdit = function(inValue, inRowIndex, inFieldIndex) {
                var newValue = replacePeriodsWithDots(inValue);

                if (!isNumber(newValue)) {
                    newValue = '';
                }

                myStoreObject.items[inRowIndex][inFieldIndex][0] = newValue;

                if (prevValue != newValue) {
                    hasChanges = true;
                    cellHasBeenEdited(<%= GRID_JS_ID %>, inFieldIndex, inRowIndex);
                }
            }

            setTimeout(function() {
                restoreHiddenStateFromCookie(<%= GRID_JS_ID %>);
            }, 1);
        });

        dojo.addOnUnload(function() {
            if (hasChanges) {
                return "Изменения не были сохранены.";
            }
        });

        function replacePeriodsWithDots(value) {
            if (typeof value == "string") {
                value = value.replace(/,/, ".");
            }

            return value;
        }

        var dataJson = '${jsonDataToShow}';
        var projectListJson = '${projectListJson}';
        var projectList = (projectListJson.length > 0) ? dojo.fromJson(projectListJson) : [];

        if (dataJson.length > 0) {
            dojo.require("dojox.layout.ContentPane");

            var myQuery = { "<%= EMPLOYEE_ID %>": "*" };
            var myStoreObject = {
                identifier: "<%= EMPLOYEE_ID %>",
                items: []
            };
            var modelFields = ["<%= EMPLOYEE_ID %>"];
            var modelFieldsForSave = [
                    "<%= EMPLOYEE_ID %>", "<%= OTHER_PROJECTS_AND_PRESALES_PLAN %>", "<%= NON_PROJECT_PLAN %>",
                    "<%= ILLNESS_PLAN %>", "<%= VACATION_PLAN %>"
            ];
            var myStore = new dojo.data.ItemFileWriteStore({
                data: myStoreObject
            });
            var myLayout = createLayout(createHeaderViewsAndFillModelFields());

            dojo.forEach(normalize(modelFields, dojo.fromJson(dataJson)), function(row) {
                for (var field in row) {
                    if (typeof row[field] == typeof undefined) {
                        row[field] = "";
                    }
                }

                myStoreObject.items.push(row);
            });

            function createHeaderViewsAndFillModelFields() {
                var firstView = {
                    noscroll: true,
                    expand: true
                };

                var secondView = {
                    expand: true
                };

                var views = [
                    {
                        noscroll: true,
                        cells: [{
                            name: "Сотрудник",
                            field: "<%= EMPLOYEE %>",
                            noresize: true,
                            width: "120px",
                            editable: false
                        }]
                    },
                    firstView,
                    secondView
                ];

                firstView.groups = [
                    { name: "Итог", field: "<%= SUMMARY %>" },
                    {
                        name: "Процент загрузки",
                        field: "<%= PERCENT_OF_CHARGE %>",
                        cellsFormatter: function(text) {
                            var number = parseFloat(replacePeriodsWithDots(text));

                            if (number > 100) {
                                return dojo.create(
                                    "span",
                                    {
                                        innerHTML: text,
                                        style: "color: red; font-weight: bold"
                                    }
                                ).outerHTML;
                            }

                            return text;
                        }
                    },
                    { name: "Проекты центра", field: "<%= CENTER_PROJECTS %>" },
                    { name: "Пресейлы центра", field: "<%= CENTER_PRESALES %>" },
                    { name: "Проекты/Пресейлы других центров", field: "<%= OTHER_PROJECTS_AND_PRESALES %>" },
                    { name: "Непроектная", field: "<%= NON_PROJECT %>" },
                    { name: "Болезнь", field: "<%= ILLNESS %>" },
                    { name: "Отпуск", field: "<%= VACATION %>" }
                ];

                secondView.groups = [];

                dojo.forEach(projectList, function(project) {
                    var projectId = project.<%= PROJECT_ID %>;

                    secondView.groups.push({
                        name: project.<%= PROJECT_NAME %>,
                        field: projectId
                    });

                    modelFieldsForSave.push(projectId + "<%= _PLAN %>");
                });

                function generateCellsAndFillModelFields(view) {
                    if (typeof view.cells == typeof undefined) {
                        view.cells = [];
                    }

                    function createCell(name, field) {
                        var scale = 2;
                        <c:if test="${planEditForm.showPlans and planEditForm.showFacts}">
                        scale = 1;
                        </c:if>
                        return {
                            name: name,
                            field: field,
                            noresize: true,

                            width: (49 * scale) + "px",
                            <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
                            editable: dojo.some(modelFieldsForSave, function(fieldForSave) {
                                return (field == fieldForSave);
                            })
                            </sec:authorize>
                        };
                    }

                    if (view.groups && view.groups.length != 0) {
                        dojo.forEach(view.groups, function(group) {
                            <c:choose>
                            <c:when test="${planEditForm.showPlans and planEditForm.showFacts}">
                                var planField = group.field + "<%= _PLAN %>";
                                var factField = group.field + "<%= _FACT %>";

                                view.cells.push(createCell("План", planField), createCell("Факт", factField));
                                modelFields.push(planField, factField);

                                group.colSpan = 2;
                                group.expand = true;
                            </c:when>
                            <c:otherwise>
                            var fieldComponent = "<%= _PLAN %>";

                            <c:if test="${planEditForm.showFacts}">
                            fieldComponent = "<%= _FACT %>";
                            </c:if>

                            var field = group.field + fieldComponent;

                            view.cells.push(
                                    createCell(group.name, field)
                            );
                            modelFields.push(field);
                            </c:otherwise>
                            </c:choose>
                        });
                        <c:if test="${not (planEditForm.showPlans and planEditForm.showFacts)}">
                        view.groups = undefined;
                        </c:if>
                    } else {
                        dojo.forEach(view.cells, function(cell) {
                            modelFields.push(cell.field);
                        });
                    }
                }

                dojo.forEach(views, function(view) {
                    generateCellsAndFillModelFields(view);
                });

                return views;
            }
        }


        function updateMonthList(year) {
            var monthNode = dojo.byId("<%= MONTH %>");
            var emptyOption = monthNode.options[0];
            var month = monthNode.value;

            monthNode.options.length = 0;

            monthNode.add(emptyOption);
            monthNode.selectedIndex = 0;

            var monthMapJson = '${monthMapJson}';

            if (monthMapJson.length > 0) {
                var monthMap = dojo.fromJson(monthMapJson);

                dojo.filter(monthMap, function(monthData) {
                    return (monthData.year == year);
                }).forEach(function(monthData) {
                    dojo.forEach(monthData.months, function(monthObj) {
                        var option = document.createElement("option");

                        option.text = monthObj.name;
                        option.value = monthObj.number;

                        if (monthObj.number == month) {
                            option.selected = "selected";
                        }

                        monthNode.add(option);
                    });
                });
            }
        }

        function updateMultipleForSelect(select) {
            var allOptionIndex;
            var isAllOption = dojo.some(select.options, function(option, idx) {
                if (option.value == <%= ALL_VALUE %> && option.selected) {
                    allOptionIndex = idx;
                    return true;
                }

                return false;
            });

            if (isAllOption) {
                select.removeAttribute("multiple");
                select.selectedIndex = allOptionIndex;
            } else {
                select.setAttribute("multiple", "multiple");
            }
        }

        function validate() {
            var errors = [];

            if (dojo.byId("<%= DIVISION_ID %>").value == 0) {
                errors.push("Не выбрано подразделение");
            }

            if (dojo.byId("<%= YEAR %>").value == 0) {
                errors.push("Не выбран год");
            }

            if (dojo.byId("<%= MONTH %>").value == 0) {
                errors.push("Не выбран месяц");
            }

            if (!dojo.byId("<%= REGIONS %>").value) {
                errors.push("Не выбран ни один регион");
            }

            if (!dojo.byId("<%= PROJECT_ROLES %>").value) {
                errors.push("Не выбрана ни одна должность");
            }

            return !showErrors(errors);
        }

        function save() {
            var items = normalize(modelFieldsForSave, myStoreObject.items);
            var errors = [];

            dojo.forEach(items, function(item, idx) {
                for (var field in item) {
                    var value = item[field];

                    if (field == "<%= PROJECTS_PLANS %>") {
                        continue;
                    }

                    if ((value || "").length == 0) {
                        delete item[field];
                    } else {
                        value = replacePeriodsWithDots(value);

                        if (!isNumber(value)) {
                            errors.push("Неверный формат числа в строке №" + (idx + 1));
                            return;
                        } else {
                            value = parseFloat(value);

                            var match = field.match(/^(\d+?)<%= _PLAN %>$/);

                            if (match) {
                                if (!item.<%= PROJECTS_PLANS %>) {
                                    item.<%= PROJECTS_PLANS %> = [];
                                }

                                item.<%= PROJECTS_PLANS %>.push({
                                    "<%= PROJECT_ID %>": parseInt(match[1]),
                                    "<%= _PLAN %>": value
                                });

                                delete item[field];
                            }
                        }
                    }
                }
            });

            if (!showErrors(errors)) {
                var object = {
                    "<%= JSON_DATA_YEAR %>": ${planEditForm.year},
                    "<%= JSON_DATA_MONTH %>": ${planEditForm.month},
                    "<%= JSON_DATA_ITEMS %>": items
                }

                hasChanges = false;

                var form = dojo.byId("<%= FORM %>");

                form.action = "<%= PLAN_SAVE_URL %>";
                form.<%= JSON_DATA %>.value = dojo.toJson(object);
                form.submit();
            }
        }

        function createPlanForPeriod() {
            window.location = getContextPath() + "<%= CreatePlanForPeriodContoller.CREATE_PLAN_FOR_PERIOD_URL %>";
        }

    </script>
</head>
<body>

<br/>

<form:form method="post" commandName="<%= FORM %>">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" />
    <form:hidden path="<%= JSON_DATA %>"  />

    <table>
        <tr>
            <td>
                <span class="label">Подразделение</span>
                <form:select path="<%= DIVISION_ID %>" class="without_dojo" onmouseover="showTooltip(this);"
                             onmouseout="tooltip.hide();">
                    <form:option label="" value="0"/>
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <span class="label">Год:</span>
                        </td>
                        <td>
                            <form:select path="<%= YEAR %>" class="without_dojo" onmouseover="showTooltip(this);"
                                         onmouseout="tooltip.hide();" onchange="updateMonthList(this.value)">
                                <form:option label="" value="0"/>
                                <form:options items="${yearList}" itemLabel="year" itemValue="year"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <span class="label">Месяц:</span>
                        </td>
                        <td>
                            <form:select path="<%= MONTH %>" class="without_dojo" onmouseover="showTooltip(this);" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                                <form:options items="${monthList}" itemLabel="monthTxt" itemValue="month" />
                            </form:select>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table>
                    <tr>
                        <td>
                            <span class="label">Регионы</span>
                        </td>
                        <td>
                            <form:select path="<%= REGIONS %>" onmouseover="showTooltip(this)" size="5"
                                         onmouseout="tooltip.hide()" multiple="true" onchange="updateMultipleForSelect(this)">
                                <form:option value="<%= ALL_VALUE %>" label="Все регионы" />
                                <form:options items="${regionList}" itemLabel="name" itemValue="id" />
                            </form:select>
                        </td>
                    </tr>
                </table>
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <span class="label">Должности</span>
                        </td>
                        <td>
                            <form:select path="<%= PROJECT_ROLES %>" onmouseover="showTooltip(this)" size="5"
                                         onmouseout="tooltip.hide()" multiple="true" onchange="updateMultipleForSelect(this)">
                                <form:option value="<%= ALL_VALUE %>" label="Все должности" />
                                <form:options items="${projectRoleList}" itemLabel="name" itemValue="id" />
                            </form:select>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <div>
                    <form:checkbox path="<%= SHOW_PLANS %>" label="Показывать плановые показатели" />
                </div>
                <div>
                    <form:checkbox path="<%= SHOW_FACTS %>" label="Показывать фактические показатели" />
                </div>
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <div>
                                <form:checkbox path="<%= SHOW_PROJECTS %>" label="Проекты" />
                            </div>
                            <div>
                                <form:checkbox path="<%= SHOW_PRESALES %>" label="Пресейлы" />
                            </div>
                        </td>
                        <td style="text-align: right">
                            <button id="show" style="width:150px;vertical-align: middle;" type="submit"
                                    onclick="return validate()">Показать</button>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>

    <sec:authorize access="hasRole('ROLE_PLAN_EDIT')">
    <c:if test="${fn:length(jsonDataToShow) > 0}">
    <br/>
    <button style="width:150px;vertical-align: middle;" onclick="save()" type="button">Сохранить планы</button>
    <button style="vertical-align: middle;" onclick="createPlanForPeriod()" type="button">
        Запланировать на период
    </button>
    </c:if>
    </sec:authorize>
</form:form>

<br/>

<c:if test="${fn:length(jsonDataToShow) > 0}">
<div dojoType="dojox.layout.ContentPane" style="width: 100%; min-width: 1260px;">
    <div id="myTable" jsId="<%= GRID_JS_ID %>" dojoType="dojox.grid.DataGrid" store="myStore"
           selectionMode="none" canSort="false" query="myQuery" <%--autoHeight="true"--%> style="height: 435px;"
           structure="myLayout"></div>
</div>
</c:if>
</body>
</html>