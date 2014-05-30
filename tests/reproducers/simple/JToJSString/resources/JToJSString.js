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

function attemptTest() {
    try {
        failMessage.innerHTML = failMessage.innerHTML + new Date().getTime() / 1000
        notice.innerHTML = "1";
        testapplet.printFromJS("Stage 1 reached");

        var byids = document.getElementById("byids");
        notice.innerHTML = "2";
        testapplet.printFromJS("Stage 2 reached");

        byids.innerHTML = "String by Id: " + testapplet.myString;
        notice.innerHTML = "3";
        testapplet.printFromJS("Stage 3 reached");

        var byid = document.getElementById("byid");
        notice.innerHTML = "4";
        testapplet.printFromJS("Stage 4 reached");

        byid.innerHTML = "StringArray by Id: " + testapplet.myStringArray[0]
                + ", " + testapplet.myStringArray[1] + ", "
                + testapplet.myStringArray[2];
        notice.innerHTML = "5";
        testapplet.printFromJS("Stage 5 reached");

        testapplet.readStringAndFinish();
        notice.innerHTML = "6";
        testapplet.printFromJS("Stage 6 reached");
    } catch (err) {
        failMessage.innerHTML = failMessage.innerHTML + err.message;
        setTimeout(attemptTest, 100);
    }
}

attemptTest();