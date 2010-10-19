/********************************************
 * Tests for getting members from Java side *
 ********************************************/

function getMemberTests() {
    document.getElementById("results").innerHTML +=  "<h2>JS -> Java get tests:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Member Type";
    columnNames[1] = "Expected Value";
    columnNames[2] = "Actual Value";
    columnNames[3] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    PluginTest.setUpForGMTests();

    try {
        row = document.createElement("tr");
        type = "int";
        expectedvalue = 42;
        addResult(type, expectedvalue, PluginTest.i, row);
        check(PluginTest.i, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "double";
        expectedvalue = 42.42;
        addResult(type, expectedvalue, PluginTest.d, row);
        check(PluginTest.d, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "float";
        expectedvalue = 42.099998474121094;
        addResult(type, expectedvalue, PluginTest.f, row);
        check(PluginTest.f, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "long";
        expectedvalue = 4294967296;
        addResult(type, expectedvalue, PluginTest.l, row);
        check(PluginTest.l, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "boolean";
        expectedvalue = true;
        addResult(type, expectedvalue, PluginTest.b, row);
        check(PluginTest.b, expectedvalue, "boolean", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "char";
        expectedvalue = 8995;
        addResult(type, expectedvalue, PluginTest.c, row);
        check(PluginTest.c, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "byte";
        expectedvalue = 43;
        addResult(type, expectedvalue, PluginTest.by, row);
        check(PluginTest.by, expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "int[] (element access)";
        expectedvalue = "1024";
        addResult(type, expectedvalue, PluginTest.ia[4], row);
        check(PluginTest.ia[4], expectedvalue, "number", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "int[] (beyond length)";
        expectedvalue = null;
        addResult(type, expectedvalue, PluginTest.ia[30], row);
        check(PluginTest.ia[30], expectedvalue, "undefined", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Regular String";
        expectedvalue = "I'm a string!";
        addResult(type, expectedvalue, PluginTest.rs, row);
        check(PluginTest.rs, expectedvalue, "string", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "String with special characters";
        expectedvalue = "𠁎〒£$ǣ€𝍖";
        addResult(type, expectedvalue, PluginTest.ss, row);
        check(PluginTest.ss, expectedvalue, "string", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "null";
        expectedvalue = null;
        addResult(type, expectedvalue, PluginTest.n, row);
        check(PluginTest.n, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Integer";
        expectedvalue = 24;
        addResult(type, expectedvalue, PluginTest.I, row);
        check(PluginTest.I, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Double";
        expectedvalue = 24.24;
        addResult(type, expectedvalue, PluginTest.D, row);
        check(PluginTest.D, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Float";
        expectedvalue = 24.124;
        addResult(type, expectedvalue, PluginTest.F, row);
        check(PluginTest.F, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Long";
        expectedvalue = 6927694924;
        addResult(type, expectedvalue, PluginTest.L, row);
        check(PluginTest.L, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean";
        expectedvalue = "false";
        addResult(type, expectedvalue, PluginTest.B, row);
        check(PluginTest.B, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Character";
        expectedvalue = 'ᔦ';
        addResult(type, expectedvalue, PluginTest.C, row);
        check(PluginTest.C, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Byte";
        expectedvalue = 34;
        addResult(type, expectedvalue, PluginTest.By, row);
        check(PluginTest.By, expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Double[] (element access)";
        expectedvalue = "24.24";
        addResult(type, expectedvalue, PluginTest.Da1[9], row);
        check(PluginTest.Da1[9], expectedvalue, "object", row);
    } catch (e) {
        error(type, expectedvalue, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Double[] (Full array)";
        expectedvalue = "[Ljava.lang.Double;@";
        addResult(type, expectedvalue+"*", PluginTest.Da1, row);
        
        if (PluginTest.Da1.toString().substr(0,20)  == expectedvalue)
            if (typeof(PluginTest.Da1) == "object") {
                pass(row);
            } else {
                fail(row, "Type mismatch: " + typeof(PluginTest.Da1) + " != object");
            }
        else
            fail(row, "");
    } catch (e) {
        error(type, expectedvalue, e, row);
    }

    tblBody.appendChild(row);
}
