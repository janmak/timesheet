/**
 * Created with IntelliJ IDEA.
 * User: rshamsutdinov
 * Date: 22.01.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */

function setCurrentYear(year) {
    if (year == 0) {
        dojo.byId("year").value = new Date().getFullYear();
    } else {
        dojo.byId("year").value = year;
    }
}

function isNotNilOrNull(obj) {
    return (obj != null && obj != 0);
}

function isNilOrNull(obj) {
    return !isNotNilOrNull(obj);
}

function checkEmployeeData(divisionId, empId) {
    if (isNotNilOrNull(divisionId) && isNotNilOrNull(empId)) {
        return true;
    }

    var error = "";

    if (!isNotNilOrNull(divisionId)) {
        error += ("Необходимо выбрать подразделение и сотрудника!\n");
    } else if (!isNotNilOrNull(empId)) {
        error += ("Необходимо выбрать сотрудника!\n");
    }

    alert(error);

    return false;
}

function getVacationsNeedsApprovalCountString(){
    dojo.xhrGet({
        url: getContextPath() + "/vacations/count",
        timeout:10000,
        load:function (data) {
            var component = dojo.byId("vacationCount");
            if (component) {
                if (data) {
                    dojo.byId("vacationCount").innerHTML = data;
                } else {
                    dojo.byId("vacationCount").innerHTML = "";
                }
            }
        }
    });
}
