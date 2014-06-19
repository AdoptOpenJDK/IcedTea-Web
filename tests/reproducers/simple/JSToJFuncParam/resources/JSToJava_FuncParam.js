//dummy javascript class whose instance is passed as JSObject parameter:
function JSCar(mph, color) {
    this.mph = mph;
    this.color = color;
}

// the main routine used for all tests:
function attemptFuncParamTests() {
    var urlArgs = document.URL.split("?");
    var testParams = urlArgs[1].split(";");

    var func = testParams[0];
    var value = decodeURIComponent(testParams[1]);
    notice.innerHTML = notice.innerHTML + func + " " + value;

    eval('applet.' + func + '(' + value + ')');

    applet.writeAfterTest();
}

doTest(attemptFuncParamTests, applet);
