function doJToJSFuncReturnTests(){
    var applet = document.getElementById('jtojsFuncReturnApplet');

    var urlArgs = document.URL.split("?");
    value = eval(decodeURIComponent(urlArgs[1]));

    applet.jCallJSFunction();

    applet.writeAfterTests();
}

function jsReturningFunction(){
    return value;
}

