var applet = document.getElementById('applet')

function runSomeJS() {
    applet.print("Javascript URL string was evaluated.")
    applet.state = "HasRun";
}
