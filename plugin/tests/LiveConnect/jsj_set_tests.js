/******************************************
 * Tests for setting members on Java side *
 ******************************************/

function setMemberTests() {

    document.getElementById("results").innerHTML +=  "<h2>JS -> Java set tests:</h2>";

    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    var columnNames = new Array();
    columnNames[0] = "Member Type";
    columnNames[1] = "Old Value";
    columnNames[2] = "Setting To";
    columnNames[3] = "New Value";
    columnNames[4] = "Status";
    var row;

    createResultTable(tbl, tblBody, columnNames);

    PluginTest.setUpForSMTests();

    try {
        row = document.createElement("tr");
        type = "int";
        setto = 42;
        curr = PluginTest.i;
        PluginTest.i = setto;
        now = PluginTest.i;
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "double";
        setto = 42.42;
        curr = PluginTest.d;
        PluginTest.d = setto;
        now = PluginTest.d;
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "float";
        setto = 42.421;
        curr = PluginTest.f;
        PluginTest.f = setto;
        now = PluginTest.f;
        addResult(type, curr, 42.42100143432617, now, row);
        check(now, 42.42100143432617, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "long";
        setto = 4294967296;
        curr = PluginTest.l;
        PluginTest.l = setto;
        now = PluginTest.l;
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "boolean";
        setto = true;
        curr = PluginTest.b;
        PluginTest.b = setto;
        now = PluginTest.b;
        addResult(type, curr, setto, now, row);
        check(now, setto, "boolean", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "char";
        setto = 58;
        curr = PluginTest.c;
        PluginTest.c = setto;
        now = PluginTest.c;
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "byte";
        setto = 43;
        curr = PluginTest.by;
        PluginTest.by = setto;
        now = PluginTest.by;
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "int[] (element)";
        setto = 100;
        curr = PluginTest.ia[4];
        PluginTest.ia[4] = setto;
        now = PluginTest.ia[4];
        addResult(type, curr, setto, now, row);
        check(now, setto, "number", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "int[] (beyond length)";
        setto = 100;
        curr = PluginTest.ia[30];
        PluginTest.ia[30] = setto;
        now = PluginTest.ia[30];
        addResult(type, curr, setto, now, row);
        check(now, null, "undefined", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Regular string";
        setto = 'Test string';
        curr = PluginTest.rs;
        PluginTest.rs = setto;
        now = PluginTest.rs;
        addResult(type, curr, setto, now, row);
        check(now, setto, "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "String with special chars";
        setto = "𠁎〒£$ǣ€𝍖";
        curr = PluginTest.ss;
        PluginTest.ss = setto;
        now = PluginTest.ss;
        addResult(type, curr, setto, now, row);
        check(now, setto, "string", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "null";
        setto = null;
        curr = PluginTest.n;
        PluginTest.n = setto;
        now = PluginTest.n;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Integer";
        setto = 24;
        curr = PluginTest.I;
        PluginTest.I = setto;
        now = PluginTest.I;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Double";
        setto = 24.24;
        curr = PluginTest.D;
        PluginTest.D = setto;
        now = PluginTest.D;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Float";
        setto = 24.124;
        curr = PluginTest.F;
        PluginTest.F = setto;
        now = PluginTest.F;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Long";
        setto = 6927694924;
        curr = PluginTest.L;
        PluginTest.L = setto;
        now = PluginTest.L;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Boolean";
        setto = new java.lang.Boolean("true");
        curr = PluginTest.B;
        PluginTest.B = setto;
        now = PluginTest.B;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Character";
        setto = new java.lang.Character(64);
        curr = PluginTest.C;
        PluginTest.C = setto;
        now = PluginTest.C;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Byte";
        setto = new java.lang.Byte(39);
        curr = PluginTest.By;
        PluginTest.By = setto;
        now = PluginTest.By;
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);

    try {
        row = document.createElement("tr");
        type = "Double[] (element)";
        setto = 100.100;
        curr = PluginTest.Da1[9];
        PluginTest.Da1[9] = setto;
        now = PluginTest.Da1[9];
        addResult(type, curr, setto, now, row);
        check(now, setto, "object", row);
    } catch (e) {
        error(type, setto, e, row);
    }
    tblBody.appendChild(row);
    
    try {
        row = document.createElement("tr");
        type = "Double[] (Full array)";
        curr = PluginTest.Da2;
        PluginTest.Da2 = java.lang.reflect.Array.newInstance(java.lang.Double, 3);
        PluginTest.Da2[0] = 1.1;
        PluginTest.Da2[1] = 2.1;
        addResult(type, curr, "[1.1,2.1,null]", "["+PluginTest.Da2[0]+","+PluginTest.Da2[1]+","+PluginTest.Da2[2]+"]", row);
        check("["+PluginTest.Da2[0]+","+PluginTest.Da2[1]+","+PluginTest.Da2[2]+"]", "[1.1,2.1,null]", "string", row);
    } catch (e) {
        error(type, "[1.0,2.0,]", e, row);
    }

    tblBody.appendChild(row);
}

