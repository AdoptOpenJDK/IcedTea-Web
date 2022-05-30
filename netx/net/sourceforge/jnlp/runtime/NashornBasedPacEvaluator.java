package net.sourceforge.jnlp.runtime;

import jdk.nashorn.api.scripting.URLReader;
import net.sourceforge.jnlp.util.TimedHashMap;
import net.sourceforge.jnlp.util.logging.OutputController;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URL;

public class NashornBasedPacEvaluator implements PacEvaluator {

    private final TimedHashMap<String, String> cache;

    private static ScriptEngine engine = null;

    public NashornBasedPacEvaluator(URL pacUrl) {
        URL pacFuncsUrl = NashornBasedPacEvaluator.class.getResource("/net/sourceforge/jnlp/runtime/pac-funcs.js");
        JNLPPolicy.enablePac(pacUrl, pacFuncsUrl);

        engine = (new ScriptEngineManager()).getEngineByName("nashorn");
        try {
            engine.eval(new URLReader(pacFuncsUrl));
            engine.eval(new URLReader(pacUrl));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (engine == null) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Warning: No nashorn engine found");
        } else {
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Using the Nashorn based PAC evaluator for url " + pacUrl);
        }

        cache = new TimedHashMap<>();
    }

    public String getProxies(URL url) {
        String cachedResult = getFromCache(url);
        if (cachedResult != null) {
            return cachedResult;
        }

        String result = getProxiesWithoutCaching(url);
        addToCache(url, result);
        return result;
    }

    private String getFromCache(URL url) {
        String lookupString = url.getProtocol() + "://" + url.getHost();
        return cache.get(lookupString);
    }

    private void addToCache(URL url, String proxyResult) {
        String lookupString = url.getAuthority() + "://" + url.getHost();
        cache.put(lookupString, proxyResult);
    }

    protected String getProxiesWithoutCaching(final URL url) {
        try {
            return (String)((Invocable)engine).invokeFunction("FindProxyForURL", url.toString(), url.getHost());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "DIRECT";
    }
}
