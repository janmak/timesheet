dojo.require("dojo.cookie");
dojo.addOnLoad(function () {
    var stavka = 8; // Стандартное кол-во часов которое нужно отработать в день.
    var durationalAll = document.getElementById("durationall");
    var durationPlan = document.getElementById("durationplan");
    var durations = dojo.query(".duration");
    var normInWeak = dojo.byId("normInWeak");
    var employee = dojo.byId("employeeId");
    var division = dojo.byId("divisionId");
    var duration = 0;
    
    normInWeak.value = dojo.cookie("normInWeak") === undefined ? 40: dojo.cookie("normInWeak");
    
    function setDurationAll(duration) {
        durationalAll.innerHTML = duration.toFixed(1);
    }
    function getPath() {
        return "/viewreports/" + division.value + "/" + employee.value;
    }
    function setDurationPlan() {
        stavka = getNormaInWeak() / 5;
        durationPlan.innerHTML = (dojo.query(".toplan").length * stavka).toFixed(1);
    }
    function changeNormal(evt) {
        evt = parseFloat(evt);
        stavka = evt / 5;
        setDurationPlan();
    }
    function getNormaInWeak() {
        return parseFloat(normInWeak.value);
    }
    for (i = 0; i < durations.length; i++) {
        duration += parseFloat(durations[i].innerHTML) ;
    }
    dojo.connect(normInWeak, 'onchange', function (){
        var value = getNormaInWeak();
        var path = getPath();
        dojo.cookie("normInWeak", value, {expires: 9999, "path": path});
        changeNormal(value);
    });
    setDurationAll(duration);
    setDurationPlan();
});