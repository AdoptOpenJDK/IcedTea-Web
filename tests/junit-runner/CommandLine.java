/*
 * Copyright 2011 Red Hat, Inc.
 * Based on code from JUnit
 *
 * This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.internal.JUnitSystem;
import org.junit.internal.RealSystem;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class CommandLine extends JUnitCore {

    public static void main(String... args) {
        runMainAndExit(new RealSystem(), args);
    }

    public static void runMainAndExit(JUnitSystem system, String... args) {
        new CommandLine().runMain(system, args);
        System.exit(0);
    }

    public Result runMain(JUnitSystem system, String... args) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<Failure> missingClasses = new ArrayList<Failure>();
        for (String each : args) {
            try {
                if (each.contains("$")) continue;
                if (each.toLowerCase().endsWith(".jnlp")) continue;
                classes.add(Class.forName(each));
            } catch (ClassNotFoundException e) {
                system.out().println("ERROR: Could not find class: " + each);
            }
        }
        RunListener jXmlOutput = new JunitLikeXmlOutputListener(system, new File("tests-output.xml"));
        addListener(jXmlOutput);
        RunListener listener = new LessVerboseTextListener(system);
        addListener(listener);
        Result result = run(classes.toArray(new Class<?>[0]));
        for (Failure each : missingClasses) {
            result.getFailures().add(each);
        }
        return result;
    }

}
