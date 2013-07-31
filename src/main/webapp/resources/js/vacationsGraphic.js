function ProjectEmployees(projectName, projectEmployees){
    var project = projectName;
    var employees = new Array();
    for (var i in projectEmployees){
        var employeeName = projectEmployees[i].employee;
        var employeeVacations = projectEmployees[i].vacations;
        var vacations = new EmployeeVacations(employeeName, employeeVacations);
        employees.push(vacations);
    }

    this.getProject   = function(){ return project};
    this.getEmployees = function(){ return employees};
}

function EmployeeVacations(employeeName, employeeVacations){
    var employee = employeeName;
    var vacations = new Array();
    for (var i in employeeVacations){

        var from = new Date();
        var to = new Date();
        var dvArr = employeeVacations[i].beginDate.split('.');
        from.setFullYear(parseInt(dvArr[2], 10), parseInt(dvArr[1], 10) - 1, parseInt(dvArr[0], 10));
        dvArr = employeeVacations[i].endDate.split('.');
        to.setFullYear(parseInt(dvArr[2], 10), parseInt(dvArr[1], 10) - 1, parseInt(dvArr[0], 10));

        employeeVacations[i].beginDate = from;
        employeeVacations[i].endDate = to;

        vacations.push(employeeVacations[i]);
    }

    this.getEmployee  = function(){ return employee};
    this.getVacations = function(){ return vacations};
}

function Gantt(gDiv)
{
    var GanttDiv = gDiv;
    var projectEmployeeList = new Array();
    this.AddProjectEmployeeList = function(value)
    {
        projectEmployeeList.push(value);

    }
    this.Draw = function()
    {
        var offSet = 0;
        var dateDiff = 0;
        var gStr = "";

        if(projectEmployeeList.length <= 0){
            return
        }

        var vacations = createFullVacationsList(projectEmployeeList);
        var maxDate = findMaxDate(vacations);
        var minDate = findMinDate(vacations);
        gStr = fillTableHeader(vacations, minDate, maxDate);
        var employeeCount = 0;
        for (var i in projectEmployeeList){
            var employeeList = projectEmployeeList[i].getEmployees();
            var indent = parseInt(i) + parseInt(employeeCount);
            gStr += "<div style='position:absolute; top:" + (20 * indent + 38) + "px; left:5px'><b>" + projectEmployeeList[i].getProject() + "</b></div>";
            employeeCount += employeeList.length;
            for (var j in employeeList){
                var vacationList = sortVacationsByType(employeeList[j].getVacations());
                indent += 1;
                for(var k in vacationList){
                    var color = getColorByType(vacationList[k].type);
                    offSet = (Date.parse(vacationList[k].beginDate) - Date.parse(minDate)) / (24 * 60 * 60 * 1000);
                    dateDiff = (Date.parse(vacationList[k].endDate) - Date.parse(vacationList[k].beginDate)) / (24 * 60 * 60 * 1000) + 1;
                    gStr += "<div style='position:absolute; top:" + (20 * indent + 37) + "px; left:" + (offSet * 27 + 202) + "px; width:" + (27 * dateDiff - 1 + 200) + "px'><div title='" + employeeList[j].getEmployee() + "' class='GVacation' style='float:left;padding-left:3;" + "background-color:" + color + "; width:" + (27 * dateDiff - 1) + "px;'>" + "</div></div>"; //+ vacationList[k].status
                }
                gStr += "<div style='position:absolute; top:" + (20 * indent + 38) + "px; left:5px'>" +  employeeList[j].getEmployee() + "</div>";
            }
        }

        GanttDiv.innerHTML = gStr;
    }
}

function sortVacationsByType(vacations){
    var arrayWithPlanVacations = new Array();
    var arrayWithRealVacations = new Array();
    for (var i in vacations){
        if (vacations[i].type == VACATION_PLANNED) {// планируемый отпуск
            arrayWithPlanVacations.push(vacations[i]);
        }else{
            arrayWithRealVacations.push(vacations[i]);
        }
    }
    return arrayWithPlanVacations.concat(arrayWithRealVacations);
}

function getColorByType(type){
    switch (type){
        case VACATION_WITH_PAY : return "lightgreen";    // отпуск с сохранением содержания
        case VACATION_WITHOUT_PAY : return "lightgreen"; // отпуск без сохранения содержания
        case VACATION_WITH_WORK : return "lightgreen";   // отпуск с последующей отработкой
        case VACATION_PLANNED : return "yellow";         // планируемый отпуск
    }
}

function createFullVacationsList(projectEmployeeList){
    var vacations = new Array();
    for (var i in projectEmployeeList){
        var employeeList = projectEmployeeList[i].getEmployees();
        for (var j in employeeList){
            var employeeVacations = employeeList[j].getVacations();
            for (var k in employeeVacations){
                vacations.push(employeeVacations[k]);
            }
        }
    }
    return vacations;
}

function findMaxDate(vacations){
    var maxDate = new Date();
    maxDate.setFullYear(vacations[0].endDate.getFullYear(),
                        vacations[0].endDate.getMonth(),
                        vacations[0].endDate.getDate());
    // найдем максимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].endDate) > Date.parse(maxDate)){
            maxDate.setFullYear(vacations[i].endDate.getFullYear(), vacations[i].endDate.getMonth(), vacations[i].endDate.getDate());
        }
    }
    //---- Fix maxDate value for better displaying-----
    // Add at least 5 days
    if(maxDate.getMonth() == 11){//December
        if(maxDate.getDay() + 5 > getDaysInMonth(maxDate.getMonth() + 1, maxDate.getFullYear())){
            maxDate.setFullYear(maxDate.getFullYear() + 1, 1, 5); //The fifth day of next month will be used
        }else{
            maxDate.setFullYear(maxDate.getFullYear(), maxDate.getMonth(), maxDate.getDate() + 5); //The fifth day of next month will be used
        }
    }else{
        if(maxDate.getDay() + 5 > getDaysInMonth(maxDate.getMonth() + 1, maxDate.getFullYear())){
            maxDate.setFullYear(maxDate.getFullYear(), maxDate.getMonth() + 1, 5); //The fifth day of next month will be used
        }else{
            maxDate.setFullYear(maxDate.getFullYear(), maxDate.getMonth(), maxDate.getDate() + 5); //The fifth day of next month will be used
        }
    }
    return maxDate;
}
function findMinDate(vacations){
    var minDate = new Date();
    minDate.setFullYear(vacations[0].beginDate.getFullYear(),
                        vacations[0].beginDate.getMonth(),
                        vacations[0].beginDate.getDate());
    // найдем минимальную дату
    for (var i in vacations){
        if(Date.parse(vacations[i].beginDate) < Date.parse(minDate)){
            minDate.setFullYear(vacations[i].beginDate.getFullYear(), vacations[i].beginDate.getMonth(), vacations[i].beginDate.getDate());
        }
    }
    return minDate;
}

function getProgressDiv(progress)
{
    return "<div class='GProgress' style='width:" + progress + "%; overflow:hidden'></div>"
}
// GET NUMBER OF DAYS IN MONTH
function getDaysInMonth(month, year)
{
    var days;
    switch(month)
    {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            days = 31;
            break;
        case 4:
        case 6:
        case 9:
        case 11:
            days = 30;
            break;
        case 2:
            if (((year% 4)==0) && ((year% 100)!=0) || ((year% 400)==0))
                days = 29;
            else
                days = 28;
            break;
    }
    return (days);
}

function getMonthByNumber(number){
    var month;
    switch(number)
    {
        case 1:  month = 'январь'; break;
        case 2:  month = 'февраль'; break;
        case 3:  month = 'март'; break;
        case 4:  month = 'апрель'; break;
        case 5:  month = 'май'; break;
        case 6:  month = 'июнь'; break;
        case 7:  month = 'июль'; break;
        case 8:  month = 'август'; break;
        case 9:  month = 'сентябрь'; break;
        case 10: month = 'октябрь'; break;
        case 11: month = 'ноябрь'; break;
        case 12: month = 'декабрь'; break;
    }
    return (month);
}

function fillTableHeader(vacations, minDate, maxDate){

    var currentDate = new Date();
    currentDate.setFullYear(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
    var dTemp = new Date();
    var firstRowStr = "<table id='tableGraphic' border=1 style='border-collapse:collapse'><tr><td rowspan='2' width='200px' style='width:200px;'><div class='GVacationTitle' style='width:200px;'>Сотрудник</div></td>";
    var thirdRow = "";
    var gStr = "";
    var colSpan = 0;
    var counter = 0;
    var length = vacations.length;

    gStr = "";
    gStr += "</tr><tr>";
    thirdRow = "<tr><td>&nbsp;</td>";
    dTemp.setFullYear(minDate.getFullYear(), minDate.getMonth(), minDate.getDate());


    while(Date.parse(dTemp) <= Date.parse(maxDate))
    {
        if(dTemp.getDay() % 6 == 0) //Weekend
        {
            gStr += "<td class='GWeekend'><div style='width:26px;'>" + dTemp.getDate() + "</div></td>";
            if(Date.parse(dTemp) == Date.parse(currentDate))
                thirdRow += "<td id='GC_" + (counter++) + "' class='GToDay' style='height:" + (length * 21) + "px'>&nbsp;</td>";
            else
                thirdRow += "<td id='GC_" + (counter++) + "' class='GWeekend' style='height:" + (length * 21) + "px'>&nbsp;</td>";
        }
        else
        {
            gStr += "<td class='GDay'><div style='width:26px;'>" + dTemp.getDate() + "</div></td>";
            if(Date.parse(dTemp) == Date.parse(currentDate))
                thirdRow += "<td id='GC_" + (counter++) + "' class='GToDay' style='height:" + (length * 21) + "px'>&nbsp;</td>";
            else
                thirdRow += "<td id='GC_" + (counter++) + "' class='GDay'>&nbsp;</td>";
        }
        if(dTemp.getDate() < getDaysInMonth(dTemp.getMonth() + 1, dTemp.getFullYear()))
        {
            if(Date.parse(dTemp) == Date.parse(maxDate))
            {
                firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
            }
            dTemp.setDate(dTemp.getDate() + 1);
            colSpan++;
        }
        else
        {
            firstRowStr += "<td class='GMonth' align='center' colspan='" + (colSpan + 1) + "'>" + getMonthByNumber(dTemp.getMonth() + 1) + " " + dTemp.getFullYear() + "</td>";
            colSpan = 0;
            if(dTemp.getMonth() == 11) //December
            {
                dTemp.setFullYear(dTemp.getFullYear() + 1, 0, 1);
            }
            else
            {
                dTemp.setFullYear(dTemp.getFullYear(), dTemp.getMonth() + 1, 1);
            }
        }
    }

    thirdRow += "</tr>";
    gStr += "</tr>" + thirdRow;
    gStr += "</table>";
    gStr = firstRowStr + gStr;

    return gStr;
}

