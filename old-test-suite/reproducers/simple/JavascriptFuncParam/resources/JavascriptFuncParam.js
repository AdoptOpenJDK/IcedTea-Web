function JJSParameterTypeFunc(type_parameter, js_str_param) {
    var str = "Call with " + type_parameter.toString() + ":"
            + typeof (type_parameter) + " from J";
    applet.printOut(str);

    var value = eval(js_str_param);
    JSSubFunc(value);
}

function JSSubFunc(type_parameter) {
    var str = "Call with " + type_parameter.toString() + ":"
            + typeof (type_parameter) + " from JS";
    applet.printOut(str);
}

function attemptFuncParamTest() {
    var urlArgs = document.URL.split("?");
    var func = urlArgs[1];

    applet[func]();
    applet.printOut("afterTests");
}

doTest(attemptFuncParamTest, applet);