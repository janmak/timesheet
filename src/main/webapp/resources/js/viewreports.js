dojo.require("dojo.cookie");
dojo.addOnLoad(function () {
    var stavka = 8; // Стандартное кол-во часов которое нужно отработать в день.
    var durationalAll = document.getElementById("durationall");
    var durationPlan = document.getElementById("durationplan");
    var durations = dojo.query(".duration");
    var normainweak = dojo.byId("normainweak");
    var employee = dojo.byId("employeeId");
    var division = dojo.byId("divisionId");
    var duration = 0;
    
    normainweak.value = dojo.cookie("normainweak") === undefined ? 40: dojo.cookie("normainweak");
    
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
        return parseFloat(normainweak.value);
    }
    for (i = 0; i < durations.length; i++) {
        duration += parseFloat(durations[i].innerHTML) ;
    }
    dojo.connect(normainweak, 'onchange', function (){
        var value = getNormaInWeak();
        var path = getPath();
        dojo.cookie("normainweak", value, {expires: 9999, "path": path});
        changeNormal(value);
    });
    setDurationAll(duration);
    setDurationPlan();
});