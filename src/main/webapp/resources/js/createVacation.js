dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.Calendar");

dojo.declare("Calendar", dijit.Calendar, {
    getClassForDate: function (date) {
        return '';
    }
});

dojo.declare("DateTextBox", dijit.form.DateTextBox, {
    popupClass: "Calendar",

    openDropDown: function() {
        this.inherited(arguments);

        this.dropDown.isDisabledDate = function(date) {
            return (date <= new Date());
        };

        this.dropDown._populateGrid();
    }
});

function setDate(date_picker, date) {
    date_picker.set("displayedValue", date);
}