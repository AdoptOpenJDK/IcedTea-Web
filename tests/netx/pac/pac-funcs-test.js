
var ICEDTEA_CLASSPATH_ORG_IP = "208.78.240.231";
var CLASSPATH_ORG_IP = "199.232.41.10";

var testsFailed = 0;
var testsPassed = 0;

print("loading needed files\n");
file = arguments[0] + "";
load(file)
print("finished loaded needed files\n");


function main() {

	testIsPlainHostName();
	testDnsDomainIs();
	testLocalHostOrDomainIs();
	testIsResolvable();
	testIsInNet();
	testDnsResolve();
	testDnsDomainLevels();
	testShExpMatch();
	testDateRange();
	testTimeRange();
	testWeekdayRange();
	testDateRange2();
	testDateRange3();

	java.lang.System.out.println("Test results: passed: " + testsPassed + "; failed: " + testsFailed + ";");
}

function runTests(name, tests) {
	var undefined_var;
	for ( var i = 0; i < tests.length; i++) {
		runTest(name, tests[i]);
	}
}

function runTest(name, test) {
    var expectedVal = test[0];
    var args = test.slice(1);
    var returnVal;
    try {
        returnVal = name.apply(null, args);
    } catch (e) {
        returnVal = e;
    }
    if (returnVal === expectedVal) {
        java.lang.System.out.println("Passed: " + name.name + "(" + args.join(", ") + ")");
        testsPassed++;
    } else {
        java.lang.System.out.println("FAILED: " + name.name + "(" + args.join(", ") + ")");
        java.lang.System.out.println("        Expected '" + expectedVal + "' but got '" + returnVal + "'");
        testsFailed++;
    }
}


function testIsPlainHostName() {
    var tests = [
        [ false, "icedtea.classpath.org" ],
        [ false, "classpath.org" ],
        [ true, "org" ],
        [ true, "icedtea" ],
        [ false, ".icedtea.classpath.org" ],
        [ false, "icedtea." ],
        [ false, "icedtea.classpath." ]
    ];

    runTests(isPlainHostName, tests);
}

function testDnsDomainIs() {
    var tests = [
        [ true, "icedtea.classpath.org", "icedtea.classpath.org" ],
        [ true, "icedtea.classpath.org", ".classpath.org" ],
        [ true, "icedtea.classpath.org", ".org" ],
        [ false, "icedtea.classpath.org", "icedtea.classpath.com" ],
        [ false, "icedtea.classpath.org", "icedtea.classpath" ],
        [ false, "icedtea.classpath", "icedtea.classpath.org" ],
        [ false, "icedtea", "icedtea.classpath.org" ]
    ];

    runTests(dnsDomainIs, tests);
}

function testLocalHostOrDomainIs() {

    var tests = [
        [ true, "icedtea.classpath.org", "icedtea.classpath.org" ],
        [ true, "icedtea", "icedtea.classpath.org" ],
        [ false, "icedtea.classpath.org", "icedtea.classpath.com" ],
        [ false, "icedtea.classpath", "icedtea.classpath.org" ],
        [ false, "foo.classpath.org", "icedtea.classpath.org" ],
        [ false, "foo", "icedtea.classpath.org" ]
    ];

    runTests(localHostOrDomainIs, tests);
}

function testIsResolvable() {

    var tests = [
        [ true, "icedtea.classpath.org", "icedtea.classpath.org" ],
        [ true, "classpath.org" ],
        [ false, "NotIcedTeaHost" ],
        [ false, "foobar.classpath.org" ],
        [ false, "icedtea.classpath.com" ]
    ];

    runTests(isResolvable, tests);
}

function testIsInNet() {

	var parts = ICEDTEA_CLASSPATH_ORG_IP.split("\.");

	var fakeParts = ICEDTEA_CLASSPATH_ORG_IP.split("\.");
	fakeParts[0] = fakeParts[0] + 1;

	function createIp(array) {
		return array[0] + "." + array[1] + "." + array[2] + "." + array[3];
	}

	var tests = [
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "255.255.255.255"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "255.255.255.0"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "255.255.0.0"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "255.0.0.0"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "0.0.0.0"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "255.255.255.255"],
	    [ true, "icedtea.classpath.org", ICEDTEA_CLASSPATH_ORG_IP, "0.0.0.0"],
	    [ true, "icedtea.classpath.org", createIp(parts), "255.255.255.255" ],
	    [ false, "icedtea.classpath.org", createIp(fakeParts), "255.255.255.255"],
	    [ false, "icedtea.classpath.org", createIp(fakeParts), "255.255.255.0"],
	    [ false, "icedtea.classpath.org", createIp(fakeParts), "255.255.0.0"],
	    [ false, "icedtea.classpath.org", createIp(fakeParts), "255.0.0.0"],
	    [ true, "icedtea.classpath.org", createIp(fakeParts), "0.0.0.0"]
	];

	runTests(isInNet, tests);
}

function testDnsResolve() {
    var tests = [
        [ ICEDTEA_CLASSPATH_ORG_IP, "icedtea.classpath.org" ],
        //[ CLASSPATH_ORG_IP, "classpath.org" ],
        [ "127.0.0.1", "localhost" ]
    ];

    runTests(dnsResolve, tests);
}

function testDnsDomainLevels() {
    var tests = [
        [ 0, "org" ],
        [ 1, "classpath.org" ],
        [ 2, "icedtea.classpath.org" ],
        [ 3, "foo.icedtea.classpath.org" ]
    ];

    runTests(dnsDomainLevels, tests);

}
function testShExpMatch() {
    var tests = [
         [ true, "icedtea.classpath.org", "icedtea.classpath.org"],
         [ false, "icedtea.classpath.org", ".org"],
         [ false, "icedtea.classpath.org", "icedtea."],
         [ false, "icedtea", "icedtea.classpath.org"],

         [ true, "icedtea.classpath.org", "*" ],
         [ true, "icedtea.classpath.org", "*.classpath.org" ],
         [ true, "http://icedtea.classpath.org", "*.classpath.org" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*.classpath.org/*" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*/foobar/*" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*foobar*" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*foo*" ],
         [ false, "http://icedtea.classpath.org/foobar/", "*/foo/*" ],
         [ false, "http://icedtea.classpath.org/foobar/", "*/foob/*" ],
         [ false, "http://icedtea.classpath.org/foobar/", "*/fooba/*" ],
         [ false, "http://icedtea.classpath.org/foo/", "*foobar*" ],

         [ true, "1", "?" ],
         [ true, "12", "??" ],
         [ true, "123", "1?3" ],
         [ true, "123", "?23" ],
         [ true, "123", "12?" ],
         [ true, "1234567890", "??????????" ],
         [ false, "1234567890", "?????????" ],
         [ false, "123", "1?1" ],
         [ false, "123", "??" ],

         [ true, "http://icedtea.classpath.org/f1/", "*/f?/*" ],
         [ true, "http://icedtea1.classpath.org/f1/", "*icedtea?.classpath*/f?/*" ],
         [ false, "http://icedtea.classpath.org/f1/", "*/f2/*" ],
         [ true, "http://icedtea.classpath.org/f1/", "*/f?/*" ],
         [ false, "http://icedtea.classpath.org/f1", "f?*"],
         [ false, "http://icedtea.classpath.org/f1", "f?*"],
         [ false, "http://icedtea.classpath.org/f1", "f?*"],

         [ true, "http://icedtea.classpath.org/foobar/", "*.classpath.org/*" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*.classpath.org/*" ],
         [ true, "http://icedtea.classpath.org/foobar/", "*.classpath.org/*" ],

         [ true, "http://icedtea.classpath.org/foo.php?id=bah", "*foo.php*" ],
         [ false, "http://icedtea.classpath.org/foo_php?id=bah", "*foo.php*" ]
     ];

     runTests(shExpMatch, tests);
}

function testWeekdayRange() {

    var today = new Date();
    var day = today.getDay();

    function dayToStr(day) {
        switch (day) {
            case -2: return "FRI";
            case -1: return "SAT";
            case 0: return "SUN";
            case 1: return "MON";
            case 2: return "TUE";
            case 3: return "WED";
            case 4: return "THU";
            case 5: return "FRI";
            case 6: return "SAT";
            case 7: return "SUN";
            case 8: return "MON";
            default: return "FRI";
        }

    }

    var tests = [
       [ true, dayToStr(day) ],
       [ false, dayToStr(day+1) ],
       [ false, dayToStr(day-1) ],
    ];

    runTests(weekdayRange, tests);
}

/** Returns an array: [day, month, year] */
function incDate() {
    if ((arguments.length >= 3) && (arguments.length % 2 === 1)) {
        var date = arguments[0];
        var result = date;
        var index = 1;
        while (index < arguments.length) {
            var whichThing = arguments[index];
            var by = arguments[index+1];
            switch (whichThing) {
                case 'year':
                    result = new Date(result.getFullYear()+by, result.getMonth(), result.getDate());
                    break;
                case 'month':
                    result = new Date(result.getFullYear(), result.getMonth()+by, result.getDate());
                    break;
                case 'day':
                    result = new Date(result.getFullYear(), result.getMonth(), result.getDate()+by);
                    break;
            }
            index += 2;
        }
        return [result.getDate(), result.getMonth(), result.getFullYear()];
    }
    throw "Please call incDate properly";
}

function monthToStr(month) {
    switch (month) {
        case -1: return "DEC";
        case 0: return "JAN";
        case 1: return "FEB";
        case 2: return "MAR";
        case 3: return "APR";
        case 4: return "MAY";
        case 5: return "JUN";
        case 6: return "JUL";
        case 7: return "AUG";
        case 8: return "SEP";
        case 9: return "OCT";
        case 10: return "NOV";
        case 11: return "DEC";
        case 12: return "JAN";
        default: throw "Invalid Month" + month;
    }
}

function testDateRange() {

    {
        var current = new Date();
        var date = current.getDate();
        var month = current.getMonth();
        var year = current.getFullYear();

        var today = incDate(current, 'day', 0);
        var tomorrow = incDate(current, 'day', 1);
        var yesterday = incDate(current, 'day', -1);

        runTest(dateRange, [ true, date ]);
        runTest(dateRange, [ false, tomorrow[0] ]);
        runTest(dateRange, [ false, yesterday[0] ]);

        runTest(dateRange, [ true, monthToStr(month) ]);
        runTest(dateRange, [ false, monthToStr(month+1) ]);
        runTest(dateRange, [ false, monthToStr(month-1) ]);

        runTest(dateRange, [ true, year ]);
        runTest(dateRange, [ false, year - 1]);
        runTest(dateRange, [ false, year + 1]);

        runTest(dateRange, [ true, date, date ]);
        runTest(dateRange, [ true, today[0], tomorrow[0] ]);
        runTest(dateRange, [ true, yesterday[0], today[0] ]);
        runTest(dateRange, [ true, yesterday[0], tomorrow[0] ]);
        runTest(dateRange, [ false, tomorrow[0], yesterday[0] ]);
        runTest(dateRange, [ false, incDate(current,'day',-2)[0], yesterday[0] ]);
        runTest(dateRange, [ false, tomorrow[0], incDate(current,'day',2)[0] ]);

        runTest(dateRange, [ true, monthToStr(month), monthToStr(month) ]);
        runTest(dateRange, [ true, monthToStr(month), monthToStr(month+1) ]);
        runTest(dateRange, [ true, monthToStr(month-1), monthToStr(month) ]);
        runTest(dateRange, [ true, monthToStr(month-1), monthToStr(month+1) ]);
        runTest(dateRange, [ true, "JAN", "DEC" ]);
        runTest(dateRange, [ true, "FEB", "JAN" ]);
        runTest(dateRange, [ true, "DEC", "NOV" ]);
        runTest(dateRange, [ true, "JUL", "JUN"]);
        runTest(dateRange, [ false, monthToStr(month+1), monthToStr(month+1) ]);
        runTest(dateRange, [ false, monthToStr(month-1), monthToStr(month-1) ]);
        runTest(dateRange, [ false, monthToStr(month+1), monthToStr(month-1) ]);

        runTest(dateRange, [ true, year, year ]);
        runTest(dateRange, [ true, year, year+1 ]);
        runTest(dateRange, [ true, year-1, year ]);
        runTest(dateRange, [ true, year-1, year+1 ]);
        runTest(dateRange, [ false, year-2, year-1 ]);
        runTest(dateRange, [ false, year+1, year+1 ]);
        runTest(dateRange, [ false, year+1, year+2 ]);
        runTest(dateRange, [ false, year+1, year-1 ]);

        runTest(dateRange, [ true, date, monthToStr(month) , date, monthToStr(month) ]);
        runTest(dateRange, [ true, yesterday[0], monthToStr(yesterday[1]) , date, monthToStr(month) ]);
        runTest(dateRange, [ false, yesterday[0], monthToStr(yesterday[1]) , yesterday[0], monthToStr(yesterday[1]) ]);
        runTest(dateRange, [ true, date, monthToStr(month) , tomorrow[0], monthToStr(tomorrow[1]) ]);
        runTest(dateRange, [ false, tomorrow[0], monthToStr(tomorrow[1]) , tomorrow[0], monthToStr(tomorrow[1]) ]);
        runTest(dateRange, [ true, yesterday[0], monthToStr(yesterday[1]) , tomorrow[0], monthToStr(tomorrow[1]) ]);
        runTest(dateRange, [ false, tomorrow[0], monthToStr(tomorrow[1]) , yesterday[0], monthToStr(yesterday[1]) ]);
    }

    {
        var lastMonth = incDate(new Date(), 'month', -1);
        var thisMonth = incDate(new Date(), 'month', 0);
        var nextMonth = incDate(new Date(), 'month', +1);
        runTest(dateRange, [ true, lastMonth[0], monthToStr(lastMonth[1]) , thisMonth[0], monthToStr(thisMonth[1]) ]);
        runTest(dateRange, [ true, thisMonth[0], monthToStr(thisMonth[1]) , nextMonth[0], monthToStr(nextMonth[1]) ]);
        runTest(dateRange, [ true, lastMonth[0], monthToStr(lastMonth[1]) , nextMonth[0], monthToStr(nextMonth[1]) ]);
        var date1 = incDate(new Date(), 'day', +1, 'month', -1);
        var date2 = incDate(new Date(), 'day', -1, 'month', +1);
        runTest(dateRange, [ true, date1[0], monthToStr(date1[1]) , nextMonth[0], monthToStr(nextMonth[1]) ]);
        runTest(dateRange, [ true, lastMonth[0], monthToStr(lastMonth[1]) , date2[0], monthToStr(date2[1]) ]);
        runTest(dateRange, [ false, nextMonth[0], monthToStr(nextMonth[1]) , lastMonth[0], monthToStr(lastMonth[1]) ]);
        var date3 = incDate(new Date(), 'day', +1, 'month', +1);
        var date4 = incDate(new Date(), 'day', +1, 'month', -1);
        runTest(dateRange, [ false, date3[0], monthToStr(date3[1]) , date4[0], monthToStr(date4[1]) ]);

        var date5 = incDate(new Date(), 'day', -1, 'month', -1);
        runTest(dateRange, [ false, date2[0], monthToStr(date2[1]) , date5[0], monthToStr(date5[1]) ]);

        runTest(dateRange, [ true, 1, "JAN", 31, "DEC" ]);
        runTest(dateRange, [ true, 2, "JAN", 1, "JAN" ]);

        var month = new Date().getMonth();
        runTest(dateRange, [ false, 1, monthToStr(month+1), 31, monthToStr(month+1) ]);
        runTest(dateRange, [ false, 1, monthToStr(month-1), 31, monthToStr(month-1) ]);
    }


    {
        var lastMonth = incDate(new Date(), 'month', -1);
        var thisMonth = incDate(new Date(), 'month', 0);
        var nextMonth = incDate(new Date(), 'month', +1);
        runTest(dateRange, [ true, monthToStr(thisMonth[1]), thisMonth[2], monthToStr(thisMonth[1]), thisMonth[2] ]);
        runTest(dateRange, [ true, monthToStr(lastMonth[1]), lastMonth[2], monthToStr(thisMonth[1]), thisMonth[2] ]);
        runTest(dateRange, [ true, monthToStr(thisMonth[1]), thisMonth[2], monthToStr(nextMonth[1]), nextMonth[2] ]);
        runTest(dateRange, [ true, monthToStr(lastMonth[1]), lastMonth[2], monthToStr(nextMonth[1]), nextMonth[2] ]);
        runTest(dateRange, [ true, monthToStr(0), year, monthToStr(11), year ]);

        runTest(dateRange, [ false, monthToStr(nextMonth[1]), nextMonth[2], monthToStr(lastMonth[1]), lastMonth[2] ]);
        runTest(dateRange, [ false, monthToStr(nextMonth[1]), nextMonth[2], monthToStr(nextMonth[1]), nextMonth[2] ]);
        runTest(dateRange, [ false, monthToStr(lastMonth[1]), lastMonth[2], monthToStr(lastMonth[1]), lastMonth[2] ]);

        var lastYear = incDate(new Date(), 'year', -1);
        var nextYear = incDate(new Date(), 'year', +1);

        runTest(dateRange, [ false, monthToStr(lastYear[1]), lastYear[2], monthToStr(lastMonth[1]), lastMonth[2] ]);
        runTest(dateRange, [ true, monthToStr(thisMonth[1]), thisMonth[2], monthToStr(nextYear[1]), nextYear[2] ]);

        var year = new Date().getFullYear();
        var month = new Date().getMonth();

        runTest(dateRange, [ true, monthToStr(month), year-1, monthToStr(month), year ]);
        runTest(dateRange, [ true, monthToStr(month), year-1, monthToStr(month), year+1 ]);
        runTest(dateRange, [ true, monthToStr(0), year, monthToStr(0), year+1 ]);
        runTest(dateRange, [ true, monthToStr(0), year-1, monthToStr(0), year+1 ]);
        runTest(dateRange, [ false, monthToStr(0), year-1, monthToStr(11), year-1 ]);
        runTest(dateRange, [ false, monthToStr(0), year+1, monthToStr(11), year+1 ]);
    }

    {
        var today = incDate(new Date(), 'day', 0);
        var yesterday = incDate(new Date(), 'day', -1);
        var tomorrow = incDate(new Date(), 'day', +1);
        runTest(dateRange, [ true,
            today[0], monthToStr(today[1]), today[2], today[0], monthToStr(today[1]), today[2] ]);
        runTest(dateRange, [ true,
            yesterday[0], monthToStr(yesterday[1]), yesterday[2], tomorrow[0], monthToStr(tomorrow[1]), tomorrow[2] ]);
    }

    {
        var dayLastMonth = incDate(new Date(), 'day', -1, 'month', -1);
        var dayNextMonth = incDate(new Date(), 'day', +1, 'month', +1);
        runTest(dateRange, [ true,
            dayLastMonth[0], monthToStr(dayLastMonth[1]), dayLastMonth[2], dayNextMonth[0], monthToStr(dayNextMonth[1]), dayNextMonth[2] ]);
    }

    {
        var dayLastYear = incDate(new Date(), 'day', -1, 'month', -1, 'year', -1);
        var dayNextYear = incDate(new Date(), 'day', +1, 'month', +1, 'year', +1);
        runTest(dateRange, [ true,
            dayLastYear[0], monthToStr(dayLastYear[1]), dayLastYear[2], dayNextYear[0], monthToStr(dayNextYear[1]), dayNextYear[2] ]);
    }

    {
        var dayLastYear = incDate(new Date(), 'day', +1, 'month', -1, 'year', -1);
        var dayNextYear = incDate(new Date(), 'day', +1, 'month', +1, 'year', +1);
        runTest(dateRange, [ true,
            dayLastYear[0], monthToStr(dayLastYear[1]), dayLastYear[2], dayNextYear[0], monthToStr(dayNextYear[1]), dayNextYear[2] ]);
    }

    {
        var tomorrow = incDate(new Date(), 'day', +1);
        var dayNextYear = incDate(new Date(), 'day', +1, 'month', +1, 'year', +1);
        runTest(dateRange, [ false,
            tomorrow[0], monthToStr(tomorrow[1]), tomorrow[2], dayNextYear[0], monthToStr(dayNextYear[1]), dayNextYear[2] ]);

    }

    {
        var nextMonth = incDate(new Date(), 'month', +1);
        var nextYear = incDate(new Date(), 'day', +1, 'month', +1, 'year', +1);
        runTest(dateRange, [ false,
            nextMonth[0], monthToStr(nextMonth[1]), nextMonth[2], nextYear[0], monthToStr(nextYear[1]), nextYear[2] ]);
    }

    {
        runTest(dateRange, [ true, 1, monthToStr(0), 0, 31, monthToStr(11), 100000 ]);
        runTest(dateRange, [ true, 1, monthToStr(0), year, 31, monthToStr(11), year ]);
        runTest(dateRange, [ true, 1, monthToStr(0), year-1, 31, monthToStr(11), year+1 ]);
        runTest(dateRange, [ false, 1, monthToStr(0), year-1, 31, monthToStr(11), year-1 ]);
        runTest(dateRange, [ false, 1, monthToStr(0), year+1, 31, monthToStr(11), year+1 ]);
     }

}

function testDateRange2() {

  var dates = [   
	new Date("January 31, 2011 3:33:33"),
	new Date("February 28, 2011 3:33:33"),
	new Date("February 29, 2012 3:33:33"),
	new Date("March 31, 2011 3:33:33"),
	new Date("April 30, 2011 3:33:33"),
	new Date("May 31, 2011 3:33:33"),
	new Date("June 30, 2011 3:33:33"),
	new Date("July 31, 2011 3:33:33"),
	new Date("August 31, 2011 3:33:33"),
	new Date("September 30, 2011 3:33:33"),
	new Date("October 31, 2011 3:33:33"),
	new Date("November 30, 2011 3:33:33"),
	new Date("December 31, 2011 3:33:33"),
]
  for (var i = 0; i < dates.length; i++)  {
      var current = dates[i];
      var today = incDate(current, 'day', 0);
      var yesterday = incDate(current, 'day', -1);
      var tomorrow = incDate(current, 'day', 1);
      var aYearFromNow = new Date(current.getFullYear()+1, current.getMonth()+1, current.getDate()+1);
      var later = [aYearFromNow.getDate(), aYearFromNow.getMonth(), aYearFromNow.getFullYear()];

      runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
        today[0], monthToStr(today[1]) , tomorrow[0], monthToStr(tomorrow[1]) ]);
      runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
        yesterday[0], monthToStr(yesterday[1]) , tomorrow[0], monthToStr(tomorrow[1]) ]);
      runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
        yesterday[0], monthToStr(yesterday[1]), yesterday[2], tomorrow[0], monthToStr(tomorrow[1]), tomorrow[2] ]);
      runTest(isDateInRange_internallForIcedTeaWebTesting, [ false, current,
        tomorrow[0], monthToStr(tomorrow[1]), tomorrow[2], later[0], monthToStr(later[1]), later[2] ]);
  }

}

function testDateRange3() {
  var dates = [   
	new Date("January 1, 2011 1:11:11"),
	new Date("February 1, 2011 1:11:11"),
	new Date("March 1, 2011 1:11:11"),
	new Date("April 1, 2011 1:11:11"),
	new Date("May 1, 2011 1:11:11"),
	new Date("June 1, 2011 1:11:11"),
	new Date("July 1, 2011 1:11:11"),
	new Date("August 1, 2011 1:11:11"),
	new Date("September 1, 2011 1:11:11"),
	new Date("October 1, 2011 1:11:11"),
	new Date("November 1, 2011 1:11:11"),
	new Date("December 1, 2011 1:11:11"),

    ]



  for (var i = 0; i < dates.length; i++)  {
    var current = dates[i];
    var yesterday = incDate(current,'day',-1);
    var today = incDate(current,'day',0);
    var tomorrow = incDate(current,'day',1);
    runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
      yesterday[0], monthToStr(yesterday[1]) , today[0], monthToStr(today[1]) ]);
    runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
      yesterday[0], monthToStr(yesterday[1]) , tomorrow[0], monthToStr(tomorrow[1]) ]);
    runTest(isDateInRange_internallForIcedTeaWebTesting, [ true, current,
      yesterday[0], monthToStr(yesterday[1]), yesterday[2], tomorrow[0], monthToStr(tomorrow[1]), tomorrow[2] ]);
  }
}

function testTimeRange() {
    var now = new Date();

    var hour = now.getHours();
    var min = now.getMinutes();
    var sec = now.getSeconds();

    function toHour(input) {
        if (input < 0) {
            while (input < 0) {
                input = input + 24;
            }
            return (input % 24);
        } else {
            return (input % 24);
        }
    }

    function toMin(input) {
        if (input < 0) {
            while (input < 0) {
                input = input + 60;
            }
            return (input % 60);
        } else {
            return (input % 60);
        }
    }

    tests = [
        [ true, hour ],
        [ false, toHour(hour+1)],
        [ false, toHour(hour-1)],

        [ true, hour, hour ],
        [ true, toHour(hour-1), hour ],
        [ true, hour, toHour(hour+1)],
        [ true, toHour(hour-1), toHour(hour+1)],
        [ true, toHour(hour+1), hour ],
        [ true, hour, toHour(hour-1) ],
        [ false, toHour(hour-2), toHour(hour-1)],
        [ false, toHour(hour+1), toHour(hour+2)],
        [ false, toHour(hour+1), toHour(hour-1) ],
        [ true, 0, 23 ],
        [ true, 12, 11 ],

        [ true, hour, min, hour, min ],
        [ true, hour, min, hour, toMin(min+1) ],
        [ true, hour, toMin(min-1), hour, min ],
        [ true, hour, toMin(min-1), hour, toMin(min+1) ],
        [ true, hour, toMin(min+2), hour, toMin(min+1) ],
        [ false, hour, toMin(min+1), hour, toMin(min+1) ],
        [ false, hour, toMin(min-1), hour, toMin(min-1) ],
        [ false, hour, toMin(min+1), hour, toMin(min-1) ],
        [ true, toHour(hour-1), min, hour, min ],
        [ true, hour, min, toHour(hour+1), min ],
        [ true, toHour(hour-1), min, toHour(hour+1), min ],
        [ true, 0, 0, 23, 59 ],
        [ true, 0, 1, 0, 0 ],

        [ true, 0, 1, 0, 0, 0, 0 ],
        [ true, hour, min, sec, hour, min, sec ],
        [ true, hour, min, sec, hour, min + 10, sec ],
        [ true, hour, min, sec - 10, hour, min, sec ],
        [ true, hour, min, sec, hour, min-1 , sec ],

    ];

    runTests(timeRange, tests);
}

main();
