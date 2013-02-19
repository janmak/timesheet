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
    setDefaultDateIfNeed(beginDate, startDate);
}

function setLastDate(){
    var today = (new Date()).format("dd.mm.yyyy");
    var endDate = dijit.byId("endDate");
    setDefaultDateIfNeed(endDate, today);
}

function setDefaultDateIfNeed(widget, defaultDate) {
    var date = widget.get('value');
    widget.set('displayedValue', (date == null || date == undefined) ? defaultDate : date);
}