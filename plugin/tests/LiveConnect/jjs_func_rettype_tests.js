/******************************************************
 * Tests for parameter conversion between Java and JS *
 ******************************************************/

function JJSReturnTypeCallTest(type) {

    if (type == "Number")
        return 1;

    if (type == "Boolean")        
        return false;

    if (type == "String")
        return "𠁎〒£$ǣ€𝍖";

    if (type == "Object")
        return window;
}

function runSingleJJSReturnTypeTest(type, row) {
    try {
        expectedvalue = JJSReturnTypeCallTest(type);
        actualvalue = PluginTest.jjsReturnTypeTest(type);
        addResult(type, expectedvalue, actualvalue, row);
        check(actualvalue, expectedvalue + "", "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
}

function jjsCallReturnTypeTests() {

    document.getElementById("results").innerHTML +=  "<h2>Java -> JS Call tests (Return Type):</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Parameter Type (Java side)";
    columnNames[1] = "Expected return value";
    columnNames[2] = "Actual return value";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    row = document.createElement("tr");
    runSingleJJSReturnTypeTest("Number", row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJJSReturnTypeTest("Boolean", row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJJSReturnTypeTest("String", row);
    tblBody.appendChild(row);
    
    row = document.createElement("tr");
    runSingleJJSReturnTypeTest("Object", row);
    tblBody.appendChild(row);
}
