function showErrors(/* Array */ errors) {
    var errorsStr = '';

    dojo.forEach(errors, function (error) {
        errorsStr += error + "\n\n";
    });

    if (errorsStr.length == 0) {
        return false;
    }

    alert(errorsStr);
    return true;
}

function isNumber(n) {
    return (typeof n != typeof undefined) && !isNaN(parseFloat(n)) && isFinite(n);
}