//dummy javascript class whose instance is passed as JSObject parameter:
function JSCar(mph, color) {
    this.mph = mph;
    this.color = color;
}

function attemptSetTests() {
    var decodedUrl = decodeURI(document.URL);
    var urlArgs = decodedUrl.split("?");
    var testParams = urlArgs[1].split(";");
    var field = testParams[0];
    var value = testParams[1];

    if (value === "JavaScript") {
        if (field === "_char") {
            value = 97;
        }

        if (field === "_Character") {
            value = new (applet.Packages).java.lang.Character(65);
        }

        if (field === "_specialString") {
            value = "𠁎〒£$ǣ€𝍖";
        }

        if (field === "_JSObject") {
            value = new JSCar(100, "red");
        }
    } else if (value.indexOf('[') != -1) {

        var elem = value.substring(1);
        value = new Array();
        value[0] = elem;
    }

    eval('applet.' + field + ' = value');

    // modifying _intArray[0] into _intArray
    // _DoubleArray[0] into _DoubleArray
    var nameEnd = field.indexOf('[');
    if (nameEnd != -1) {
        field = field.substring(0, nameEnd);
    }

    applet.printNewValueAndFinish(field);
}

doTest(attemptSetTests, applet);
