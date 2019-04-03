function doJToJSFuncReturnTests(){
    var urlArgs = document.URL.split("?");
    value = eval(decodeURIComponent(urlArgs[1]));

    applet.jCallJSFunction();

    applet.writeAfterTests();
}

function jsReturningFunction(){
    return value;
}

var value;

doTest(doJToJSFuncReturnTests, applet);