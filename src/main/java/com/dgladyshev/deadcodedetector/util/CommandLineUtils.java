package com.dgladyshev.deadcodedetector.util;

import com.dgladyshev.deadcodedetector.exceptions.ExecProcessException;
import lombok.extern.slf4j.Slf4j;
import org.buildobjects.process.ExternalProcessFailureException;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.StartupException;
import org.buildobjects.process.TimeoutException;

@Slf4j
public final class CommandLineUtils {

    private CommandLineUtils() {
    }

    /**
     * Executes shell command with given arguments.
     * @param cmd command
     * @param args command arguments
     * @throws ExecProcessException if shell command failed to be executed or return non-zero error code
     */
    public static String execProcess(String cmd, long timeoutMillis, String... args) throws ExecProcessException {
        try {
            return new ProcBuilder(cmd)
                    .withArgs(args)
                    .withTimeoutMillis(timeoutMillis)
                    .withExpectedExitStatuses(0)
                    .run()
                    .getOutputString();
        } catch (StartupException | TimeoutException ex)  {
            throw new ExecProcessException("Failed to execute shell command", ex);
        } catch (ExternalProcessFailureException ex) {
            throw new ExecProcessException(ex.getStderr(), ex);
        }
    }
}