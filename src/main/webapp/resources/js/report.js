/**
 * User: iziyangirov
 * Date: 29.01.13
 * Здесь собраны общие скрипты для отчетов
 */

dojo.addOnLoad(function() {
    setBeginDate();
    setLastDate();
});

function setBeginDate(){
    var today = new Date();
    var year = today.getFullYear();
    var month = today.getMonth();
    var startDate = (new Date(today.getFullYear(), today.getMonth(), 1)).format("dd.mm.yyyy");
    var beginDate = dijit.byId("beginDate");
    beginDate.set("displayedValue", startDate);
}

function setLastDate(){
    var today = (new Date()).format("dd.mm.yyyy");
    var endDate = dijit.byId("endDate");
    endDate.set("displayedValue", today);
}