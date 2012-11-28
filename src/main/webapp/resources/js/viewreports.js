dojo.addOnLoad(function () {
    var duration = 0;
    var durations = dojo.query(".duration");
    for (i = 0; i < durations.length; i++) {
        duration += parseFloat(durations[i].innerHTML) ;
    }
    document.getElementById("durationall").innerHTML = duration.toString();
});
