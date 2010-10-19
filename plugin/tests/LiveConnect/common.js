/*
 * Commonly used functions
 */

        var cell, cellText; // reused

        function updateTotals() {
            document.getElementById("totals").innerHTML =  "<table class=\"results\" width=\"100%\"><tr><th>Total tests run </th><th> Passed </th><th> Failed </th><th> Errors </th></tr>" +
                                                           "<tr><td>" + (passed+failed+errored) + " </td><td> " + passed + "  </td><td> " + failed + " </td><td> " + errored + " </td></tr>";
        }

        function pass(row) {
            cell = document.createElement("td");
            cell.setAttribute("style","color:green;text-align:center;font-weight: bold");
            cellText = document.createTextNode("passed");
            cell.appendChild(cellText);
            row.appendChild(cell);

            passed++;
            updateTotals();
        }

        function fail(row, reason) {
            cell = document.createElement("td");
            cell.setAttribute("style","color:red;text-align:center;font-weight: bold");
            if (reason)
                cellText = document.createTextNode(reason);
            else
                cellText = document.createTextNode("failed");
            cell.appendChild(cellText);
            row.appendChild(cell);

            failed++;
            updateTotals();
        }

        function error(type, expected, e, row) {

            cell = document.createElement("td");
            cell.setAttribute("style","color:red;text-align:center;font-weight: bold");
            cell.setAttribute("colspan","5");
            cellText = document.createTextNode("An error occurred when running this test: " +  e);
            cell.appendChild(cellText);
            row.appendChild(cell);

            errored++;
            updateTotals();
        }

        function check(actual, expected, expectedtype, row) {
           if (actual == expected) {
                if (typeof(actual) == expectedtype) {
                    pass(row);
                 } else {
                     fail(row, "Type mismatch: " + typeof(actual) + " != " + expectedtype);
                 }
            } else {
                     fail(row, "Failed: " + actual + " [" + typeof(actual) + "] != " + expected + " [" + typeof(expected) + "]");
            }
        }

        function doTest() {
        
            passed = 0;
            failed = 0;
            errored = 0;
            document.getElementById("results").innerHTML = "";
            updateTotals();
       
            try {
                if (document.getElementById("testForm").jsjget.checked == 1)
                    getMemberTests();
                    
                if (document.getElementById("testForm").jsjset.checked == 1)
                    setMemberTests();

                if (document.getElementById("testForm").jsjfp.checked == 1)
                    fpCallTests();

                if (document.getElementById("testForm").jsjfrt.checked == 1)
                    rtCallTests();
                    
                if (document.getElementById("testForm").jsjfr.checked == 1)
                    frCallTests();
                    
                if (document.getElementById("testForm").jsjtc.checked == 1)
                    typeCastingTests();   
                    
                if (document.getElementById("testForm").jjsget.checked == 1)
                    jjsGetMemberTests();
                    
                if (document.getElementById("testForm").jjsset.checked == 1)
                    jjsSetMemberTests();
                    
                if (document.getElementById("testForm").jjcparam.checked == 1)
                    jjsCallParameterTests();
                    
                if (document.getElementById("testForm").jjcrt.checked == 1)
                    jjsCallReturnTypeTests();

                if (document.getElementById("testForm").jjeval.checked == 1)
                    jjsEvalTests();
            } catch (e) {
                document.getElementById("results").innerHTML += "<font color=\"red\">ERROR:<BR>" + e;
            }
        }
        
        function testAll() {
            document.getElementById("testForm").jsjget.checked = 1;
            document.getElementById("testForm").jsjset.checked = 1;
            document.getElementById("testForm").jsjfp.checked = 1;
            document.getElementById("testForm").jsjfrt.checked = 1;
            document.getElementById("testForm").jsjfr.checked = 1;
            document.getElementById("testForm").jsjtc.checked = 1;
            document.getElementById("testForm").jjsget.checked = 1;
            document.getElementById("testForm").jjsset.checked = 1;
            document.getElementById("testForm").jjcparam.checked = 1;
            document.getElementById("testForm").jjcrt.checked = 1;
            document.getElementById("testForm").jjeval.checked = 1;

            doTest();
        }


var intvar;
var doublevar;
var boolvar;
var stringvar;
var objectvar;
var arrayvar;
var arrayvar2;
var setvar;

function initVars() {
    intvar = 1;
    doublevar = 1.1;
    boolvar = true;
    stringvar = "stringvar";
    objectvar = new PluginTest.Packages.DummyObject("DummyObject1");
    arrayvar = new Array();
    arrayvar[1] = 100;

    arrayvar2 = new Array();
    arrayvar2[1] = new Array();
    arrayvar2[1][2] = 200;
}

function createResultTable(tbl, tblBody, columnNames) {
    tbl.setAttribute("border", "5");
    tbl.setAttribute("width", "100%");
    tbl.setAttribute("class", "results");
    row = document.createElement("tr");

    for (var i=0; i < columnNames.length; i++) {
        cell = document.createElement("th");
        cellText = document.createTextNode(columnNames[i]);
        cell.appendChild(cellText);
        row.appendChild(cell);
    }

    tblBody.appendChild(row);
    tbl.appendChild(tblBody);
    document.getElementById("results").appendChild(tbl);
}

function addResult() {

    var row = arguments[arguments.length-1];

    // Different length arguments imply different width distributions

    if (arguments.length == 4) {

        cell = document.createElement("td");
        cell.setAttribute("width","25%");
        cellText = document.createTextNode(arguments[0]);
        cell.appendChild(cellText);
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.setAttribute("width","20%");
        cellText = document.createTextNode(arguments[1]);
        cell.appendChild(cellText);
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.setAttribute("width","40%");
        cellText = document.createTextNode(arguments[2]);
        cell.appendChild(cellText);
        row.appendChild(cell);

    }  else if (arguments.length == 5) {

        cell = document.createElement("td");
        cell.setAttribute("width","25%");
        cellText = document.createTextNode(arguments[0]);
        cell.appendChild(cellText);
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.setAttribute("width","20%");
        cellText = document.createTextNode(arguments[1]);
        cell.appendChild(cellText);
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.setAttribute("width","20%");
        cellText = document.createTextNode(arguments[2]);
        cell.appendChild(cellText);
        row.appendChild(cell);

        cell = document.createElement("td");
        cell.setAttribute("width","20%");
        cellText = document.createTextNode(arguments[3]);
        cell.appendChild(cellText);
        row.appendChild(cell);

    }
}
