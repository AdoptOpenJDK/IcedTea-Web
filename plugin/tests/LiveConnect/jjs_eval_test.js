/******************************************************
 * Tests for parameter conversion between Java and JS *
 ******************************************************/


function jjsEvalTests() {

    document.getElementById("results").innerHTML +=  "<h2>Java -> JS Eval Test:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Evaluating";
    columnNames[1] = "Expected result";
    columnNames[2] = "Result";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    try {
        row = document.createElement("tr");
        evalstr = "document.location";
        expectedvalue = eval(evalstr);
        actualValue = PluginTest.jjsEvalTest(evalstr);
        addResult(evalstr, expectedvalue, actualValue, row);
        check(actualValue, expectedvalue, "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        evalstr = "1+1";
        expectedvalue = eval(evalstr);
        actualValue = PluginTest.jjsEvalTest(evalstr);
        addResult(evalstr, expectedvalue, actualValue, row);
        check(actualValue, expectedvalue, "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        evalstr = "typeof(true)";
        expectedvalue = eval(evalstr);
        actualValue = PluginTest.jjsEvalTest(evalstr);
        addResult(evalstr, expectedvalue, actualValue, row);
        check(actualValue, expectedvalue, "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
}

