function writeToJS(myStringArray) {
    var tojs = document.getElementById("writetojs");
    tojs.innerHTML = "" + myStringArray[0] + myStringArray[1]
            + myStringArray[2];
}

function writeToJSs(myString) {
    var tojss = document.getElementById("writetojss");
    tojss.innerHTML = myString;
}

function getDOMElementByID(id) {
    return document.getElementById(id).innerHTML;
}

function attemptJToJSStringTest() {
        notice.innerHTML = "1";
        applet.printFromJS("Stage 1 reached");

        var byids = document.getElementById("byids");
        notice.innerHTML = "2";
        applet.printFromJS("Stage 2 reached");

        byids.innerHTML = "String by Id: " + applet.myString;
        notice.innerHTML = "3";
        applet.printFromJS("Stage 3 reached");

        var byid = document.getElementById("byid");
        notice.innerHTML = "4";
        applet.printFromJS("Stage 4 reached");

        byid.innerHTML = "StringArray by Id: " + applet.myStringArray[0]
                + ", " + applet.myStringArray[1] + ", "
                + applet.myStringArray[2];
        notice.innerHTML = "5";
        applet.printFromJS("Stage 5 reached");

        applet.readStringAndFinish();
        notice.innerHTML = "6";
        applet.printFromJS("Stage 6 reached");
}

try {
    doTest(attemptJToJSStringTest, applet);
} catch (err) {
    alert(err);
}