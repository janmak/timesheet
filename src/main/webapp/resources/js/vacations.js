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