/*****************************************
 * Tests for reading JS values from Java *
 *****************************************/

function jjsGetMemberTests() {

    initVars();

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java get tests:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Member Type";
    columnNames[1] = "Expected Value";
    columnNames[2] = "Actual value";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    try {
        row = document.createElement("tr");
        type = "int";
        expectedvalue = intvar;
        tpassed = PluginTest.jjsReadIntTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "double";
        expectedvalue = doublevar;
        tpassed = PluginTest.jjsReadDoubleTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "boolean";
        expectedvalue = boolvar;
        tpassed = PluginTest.jjsReadBooleanTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "string";
        expectedvalue = stringvar;
        tpassed = PluginTest.jjsReadStringTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "object";
        expectedvalue = objectvar;
        tpassed = PluginTest.jjsReadObjectTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "1D Array";
        expectedvalue = 100;
        tpassed = PluginTest.jjsRead1DArrayTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "2D Array";
        expectedvalue = 200;
        tpassed = PluginTest.jjsRead2DArrayTest();
        actualValue = PluginTest.value;
        addResult(type, expectedvalue, PluginTest.value, row);
        check(tpassed, true, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); return;

}

