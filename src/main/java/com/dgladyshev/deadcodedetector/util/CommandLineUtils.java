package com.dgladyshev.deadcodedetector.util;

import org.buildobjects.process.ProcBuilder;

public final class CommandLineUtils {

    private CommandLineUtils() {}

    public static String execProcess(String cmd, String... args) {
        return new ProcBuilder(cmd)
                .withArgs(args)
                .withTimeoutMillis(15000)
                .withExpectedExitStatuses(0)
                .run()
                .getOutputString();
    }
}


