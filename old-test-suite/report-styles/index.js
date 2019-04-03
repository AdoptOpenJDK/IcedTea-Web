function showhideMethods(inElement,toValue) {
	var e = document.getElementById(inElement);
	methods=e.getElementsByClassName("method");
	for ( var i = 0; i < methods.length; i++ ) {
		methods[i].style.display=toValue
	}
}
function openAnchor() {
	anchor=self.document.location.hash;
	if (anchor==null || anchor=="") return;
	stub=anchor.substring(1);
	var logs=getLogsArray(stub);
	logs[0].style.display="inline";
	logs[1].style.display="inline";
	recalcArraysWidth(logs);
	window.location.hash=stub;
}

function negateIdDisplay(which) {
	var e = document.getElementById(which);
	if (e.style.display=="block") {
		e.style.display="none"
	} else {
		e.style.display="block"
	}
}

function negateIdDisplayInline(which) {
	var e = document.getElementById(which);
	if (e.style.display=="inline") {
		e.style.display="none"
	} else {
		e.style.display="inline"
	}
}

function setClassDisplay(which,what) {
	var e = document.getElementsByClassName(which);
	for ( var i = 0; i < e.length; i++ ) {
		e[i].style.display=what
	}
}

function negateClassBlocDisplay(which) {
	var e = document.getElementsByClassName(which);
	for ( var i = 0; i < e.length; i++ ) {
		if (e[i].style.display=="block") {
			e[i].style.display="none"
		} else {
			e[i].style.display="block"
		}
	}
}

function negateClassBlocDisplayIn(where,which) {
	var parent = document.getElementById(where);
	var e = parent.getElementsByClassName(which);
	for ( var i = 0; i < e.length; i++ ) {
		if (e[i].style.display=="block") {
			e[i].style.display="none"
		} else {
			e[i].style.display="block"
		}
	}
}

function getLogsArray(stub) {
	return new Array(document.getElementById(stub+".out"),document.getElementById(stub+".err"),document.getElementById(stub+".all"));
}

function recalcLogsWidth(stub) {
	var logs=getLogsArray(stub)
	recalcArraysWidth(logs);
}
function showAllLogs() {
	var e = document.getElementsByClassName("method");
	for ( var i = 0; i < e.length; i++ ) {
		stub=e[i].id;
		var logs=getLogsArray(stub)
		logs[0].style.display="none";
		logs[1].style.display="none"
		logs[2].style.display="inline"
		recalcArraysWidth(logs);
		
	}
}

function recalcArraysWidth(logs) {
	visible=0;
	for ( var i = 0; i < logs.length; i++ ) {
		if (logs[i].style.display!="none"){
			visible++;
		}
	}
	if (visible==0) return;
	nwWidth=90/visible;
	for ( var i = 0; i < logs.length; i++ ) {
		if (logs[i].style.display!="none"){
			logs[i].style.width=nwWidth+"%";
		}
	}
}
