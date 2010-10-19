/*****************************************
 * Tests for setting JS values from Java *
 *****************************************/

function jjsSetMemberTests() {

    initVars();

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java set tests:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Java Member Type";
    columnNames[1] = "Old Value";
    columnNames[2] = "Expected value";
    columnNames[3] = "Actual Value";
    columnNames[4] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    try {
        row = document.createElement("tr");
        type = "int";
        oldvalue = setvar;
        PluginTest.jjsSetIntTest();
        expectedvalue = 1;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 

    try {
        row = document.createElement("tr");
        type = "java.lang.Integer";
        oldvalue = setvar;
        PluginTest.jjsSetIntegerTest();
        expectedvalue = 2;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "double";
        oldvalue = setvar;
        PluginTest.jjsSetdoubleTest();
        expectedvalue = 2.1;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Double";
        oldvalue = setvar;
        PluginTest.jjsSetDoubleTest();
        expectedvalue = 2.2;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "float";
        oldvalue = setvar;
        PluginTest.jjsSetfloatTest();
        expectedvalue = 2.299999952316284 ;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Float";
        oldvalue = setvar;
        PluginTest.jjsSetFloatTest();
        expectedvalue = 2.4000000953674316;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "long";
        oldvalue = setvar;
        PluginTest.jjsSetlongTest();
        expectedvalue = 4294967296;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Long";
        oldvalue = setvar;
        PluginTest.jjsSetLongTest();
        expectedvalue = 4294967297;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "short";
        oldvalue = setvar;
        PluginTest.jjsSetshortTest();
        expectedvalue = 3;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Short";
        oldvalue = setvar;
        PluginTest.jjsSetShortTest();
        expectedvalue = 4;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "byte";
        oldvalue = setvar;
        PluginTest.jjsSetbyteTest();
        expectedvalue = 5;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Byte";
        oldvalue = setvar;
        PluginTest.jjsSetByteTest();
        expectedvalue = 6;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "char";
        oldvalue = setvar;
        PluginTest.jjsSetcharTest();
        expectedvalue = 8995;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Character";
        oldvalue = setvar;
        PluginTest.jjsSetCharacterTest();
        expectedvalue = 8996;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "boolean";
        oldvalue = setvar;
        PluginTest.jjsSetbooleanTest();
        expectedvalue = true;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row); 
    
    
    try {
        row = document.createElement("tr");
        type = "java.lang.Boolean";
        oldvalue = setvar;
        PluginTest.jjsSetBooleanTest();
        expectedvalue = false;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "boolean", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "java.lang.String";
        oldvalue = setvar;
        PluginTest.jjsSetStringTest();
        expectedvalue = "𠁎〒£$ǣ€𝍖";
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "string", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "(Complex java object)";
        oldvalue = setvar;
        PluginTest.jjsSetObjectTest();
        expectedvalue = PluginTest.dummyObject;
        addResult(type, oldvalue, expectedvalue, setvar, row);
        check(setvar, expectedvalue, "object", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "1D Array";
	setvar = new Array();
        oldvalue = setvar[1];
        PluginTest.jjsSet1DArrayTest();
        expectedvalue = 100;
        addResult(type, oldvalue, expectedvalue, setvar[1], row);
        check(setvar[1], expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "2D Array";
	setvar = new Array();
	setvar[1] = new Array();
        oldvalue = setvar[1][2];
        PluginTest.jjsSet2DArrayTest();
        expectedvalue = 200;
        addResult(type, oldvalue, expectedvalue, setvar[1][2], row);
        check(setvar[1][2], expectedvalue, "number", row);
    } catch (e) {
        error(type, "", e, row);
    }
    tblBody.appendChild(row);
}

function java_to_js_call_test_info (type, expectedreply, functionreply, row) {

    cell = document.createElement("td");
    cell.setAttribute("width","25%");
    cellText = document.createTextNode(type);
    cell.appendChild(cellText);
    row.appendChild(cell);

    cell = document.createElement("td");
    cell.setAttribute("width","20%");
    cellText = document.createTextNode(expectedreply);
    cell.appendChild(cellText);
    row.appendChild(cell);
    
    cell = document.createElement("td");
    cell.setAttribute("width","20%");
    cellText = document.createTextNode(functionreply);
    cell.appendChild(cellText);
    row.appendChild(cell);

}

