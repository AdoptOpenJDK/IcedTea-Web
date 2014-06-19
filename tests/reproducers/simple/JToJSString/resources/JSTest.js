function doTest(funcCallback, applet) {
	if (applet.init != null) {
		funcCallback();
	} else {
		setTimeout(function(){doTest(funcCallback, applet)}, 100);
	}
}
