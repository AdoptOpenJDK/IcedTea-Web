
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
	testWeekdayRange();
	testDateRange();
	testTimeRange();

	java.lang.System.out.println("Test results: passed: " + testsPassed + "; failed: " + testsFailed + ";");
}

function runTests(name, tests) {

	var undefined_var;

	for ( var i = 0; i < tests.length; i++) {

		var expectedVal = tests[i][0];
		var args = tests[i].slice(1);
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
}

function testDateRange() {

    function incDate(date) {
        return (date + 1 - 1) % 31 +1 ;
    }

    function decDate(date) {
        return (date - 1 - 1 + 31) % 31 + 1;
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
            default: throw "Invalid Month";
        }
    }

    var today = new Date();
    var date = today.getDate();
    var month = today.getMonth();
    var year = today.getYear();

    var tests = [
        [ true, date ],
        [ false, incDate(date) ],
        [ false, decDate(date) ],

        [ true, monthToStr(month) ],
        [ false, monthToStr(month+1) ],
        [ false, monthToStr(month-1) ],

        [ true, year ],
        [ false, year - 1],
        [ false, year + 1],

        [ true, date, date ],
        [ true, date, incDate(date) ],
        [ true, decDate(date), date ],
        [ true, decDate(date), incDate(date) ],
        [ false, incDate(date), decDate(date) ],
        [ false, decDate(decDate(date)), decDate(date) ],
        [ false, incDate(date), incDate(incDate(date)) ],

        [ true, monthToStr(month), monthToStr(month) ],
        [ true, monthToStr(month), monthToStr(month+1) ],
        [ true, monthToStr(month-1), monthToStr(month) ],
        [ true, monthToStr(month-1), monthToStr(month+1) ],
        [ true, "JAN", "DEC" ],
        [ true, "DEC", "NOV" ],
        [ true, "JUL", "JUN"],
        [ false, monthToStr(month+1), monthToStr(month+1) ],
        [ false, monthToStr(month-1), monthToStr(month-1) ],
        [ false, monthToStr(month+1), monthToStr(month-1) ],

        [ true, year, year ],
        [ true, year, year+1 ],
        [ true, year-1, year ],
        [ true, year-1, year+1 ],
        [ false, year-2, year-1 ],
        [ false, year+1, year+1 ],
        [ false, year+1, year+2 ],
        [ false, year+1, year-1 ],

        [ true, date, monthToStr(month) , date, monthToStr(month) ],
        [ true, decDate(date), monthToStr(month) , date, monthToStr(month) ],
        [ false, decDate(date), monthToStr(month) , decDate(date), monthToStr(month) ],
        [ true, date, monthToStr(month) , incDate(date), monthToStr(month) ],
        [ false, incDate(date), monthToStr(month) , incDate(date), monthToStr(month) ],
        [ true, decDate(date), monthToStr(month) , incDate(date), monthToStr(month) ],
        [ false, incDate(date), monthToStr(month) , decDate(date), monthToStr(month) ],
        [ true, date, monthToStr(month-1) , date, monthToStr(month) ],
        [ true, date, monthToStr(month) , date, monthToStr(month+1) ],
        [ true, date, monthToStr(month-1) , date, monthToStr(month+1) ],
        [ true, incDate(date), monthToStr(month-1) , date, monthToStr(month+1) ],
        [ true, date, monthToStr(month-1) , decDate(date), monthToStr(month+1) ],
        [ false, date, monthToStr(month+1) , date, monthToStr(month-1) ],
        [ false, incDate(date), monthToStr(month+1) , incDate(date), monthToStr(month-1) ],
        [ false, decDate(date), monthToStr(month+1) , decDate(date), monthToStr(month-1) ],
        [ true, 1, "JAN", 31, "DEC" ],
        [ true, 2, "JAN", 1, "JAN" ],
        [ false, 1, monthToStr(month+1), 31, monthToStr(month+1) ],
        [ false, 1, monthToStr(month-1), 31, monthToStr(month-1) ],

        [ true, monthToStr(month), year, monthToStr(month), year ],
        [ true, monthToStr(month-1), year, monthToStr(month), year ],
        [ true, monthToStr(month), year, monthToStr(month+1), year ],
        [ true, monthToStr(month-1), year, monthToStr(month+1), year ],
        [ true, monthToStr(0), year, monthToStr(11), year ],
        [ false, monthToStr(month+1), year, monthToStr(month-1), year ],
        [ false, monthToStr(month+1), year, monthToStr(month+1), year ],
        [ false, monthToStr(month-1), year, monthToStr(month-1), year ],
        [ false, monthToStr(month), year-1, monthToStr(month-1), year ],
        [ true, monthToStr(month), year, monthToStr(month), year + 1 ],
        [ true, monthToStr(month), year-1, monthToStr(month), year ],
        [ true, monthToStr(month), year-1, monthToStr(month), year+1 ],
        [ true, monthToStr(0), year, monthToStr(0), year+1 ],
        [ true, monthToStr(0), year-1, monthToStr(0), year+1 ],
        [ false, monthToStr(0), year-1, monthToStr(11), year-1 ],
        [ false, monthToStr(0), year+1, monthToStr(11), year+1 ],

        [ true, date, monthToStr(month), year, date, monthToStr(month), year ],
        [ true, decDate(date), monthToStr(month), year, incDate(date), monthToStr(month), year ],
        [ true, decDate(date), monthToStr(month-1), year, incDate(date), monthToStr(month+1), year ],
        [ true, decDate(date), monthToStr(month-1), year-1, incDate(date), monthToStr(month+1), year+1 ],
        [ true, incDate(date), monthToStr(month-1), year-1, incDate(date), monthToStr(month+1), year+1 ],
        [ false, incDate(date), monthToStr(month), year, incDate(date), monthToStr(month+1), year+1 ],
        [ false, date, monthToStr(month+1), year, incDate(date), monthToStr(month+1), year+1 ],
        [ true, 1, monthToStr(0), 0, 31, monthToStr(11), 100000 ],
        [ true, 1, monthToStr(0), year, 31, monthToStr(11), year ],
        [ true, 1, monthToStr(0), year-1, 31, monthToStr(11), year+1 ],
        [ false, 1, monthToStr(0), year-1, 31, monthToStr(11), year-1 ],
        [ false, 1, monthToStr(0), year+1, 31, monthToStr(11), year+1 ],

    ];

    runTests(dateRange, tests);

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
