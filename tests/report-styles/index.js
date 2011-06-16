
function negateIdDisplay(which) {
	var e = document.getElementById(which);
	if (e.style.display=="block") {
		e.style.display="none"
	} else {
		e.style.display="block"
	}
}


function setClassDisplay(which,what) {
	var e = document.getElementsByClassName(which);
	for ( var i = 0; i < e.length; i++ ) {
		e[i].style.display=what
	}
}



