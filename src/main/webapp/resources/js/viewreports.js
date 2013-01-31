dojo.require("dojo.cookie");
dojo.addOnLoad(function () {
    var stavka = 8; // Стандартное кол-во часов которое нужно отработать в день.
    var durationalAll = document.getElementById("durationall");
    var durationPlan = document.getElementById("durationplan");
    var durations = dojo.query(".duration");
    var normainweak = dojo.byId("normainweak");
    var duration = 0;
    
    normainweak.value = dojo.cookie("normainweak") === undefined ? 40: dojo.cookie("normainweak");
    
    function setDuration(duration) {
        durationalAll.innerHTML = duration.toFixed(1);
    }
    function getDuration() {
        return parseFloat(durationalAll.innerHTML);
    }
    function setDurationPlan() {
        durationPlan.innerHTML = (dojo.query(".toplan").length * stavka).toFixed(1);
    }
    function changeNormal(evt) {
        evt = parseFloat(evt);
        stavka = evt / 5;
        setDurationPlan();
    }
    for (i = 0; i < durations.length; i++) {
        duration += parseFloat(durations[i].innerHTML) ;
    }
    dojo.connect(normainweak, 'onchange', function (){
        var value = parseFloat(normainweak.value);
        dojo.cookie("normainweak", value, {expires: 9999});
        changeNormal(value);
    });
    setDuration(duration);
    setDurationPlan();
});