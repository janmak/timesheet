dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.Calendar");

dojo.declare("Calendar", dijit.Calendar, {
    getClassForDate: function (date) {
        return '';
    }
});

function setDate(date_picker, date) {
    date_picker.set("displayedValue", date);
}