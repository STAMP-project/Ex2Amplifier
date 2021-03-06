package eu.stamp_project.ex2amplifier.catg;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/01/18
 */
public class CATGUtils {


    public static final FilenameFilter FILTER_INPUTS_FILES = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return Pattern.compile("inputs(\\d)+").matcher(name).matches();
        }
    };

    static void copyIfNeeded(int iteration) {
        boolean isRealInput = true;
        try (BufferedReader buffer = new BufferedReader(
                new FileReader(CATGExecutor.getPrefixWorkingDirectory() + "isRealInput"))) {
            isRealInput = buffer.lines().findFirst().get().equals("true");
        } catch (Exception e) {
            isRealInput = true;
        }
        if (isRealInput) {
            try {
                FileUtils.copyFile(
                        new File(CATGExecutor.getPrefixWorkingDirectory() + "inputs"),
                        new File(CATGExecutor.getPrefixWorkingDirectory() + "inputs" + iteration)
                );
                FileUtils.copyFile(new File(CATGExecutor.getPrefixWorkingDirectory() + "inputs"),
                        new File(CATGExecutor.getPrefixWorkingDirectory() + "inputs.old")
                );
            } catch (Exception ignored) {
                //ignored
            }
        }
        try {
            FileUtils.copyFile(
                    new File(CATGExecutor.getPrefixWorkingDirectory() + "history"),
                    new File(CATGExecutor.getPrefixWorkingDirectory() + "history.old"));
        } catch (Exception ignored) {
            //ignored
        }
    }

    static List<List<String>> readOutPut() {
        return Arrays.stream(new File(
                CATGExecutor.getPrefixWorkingDirectory().isEmpty() ?
                        "." : CATGExecutor.getPrefixWorkingDirectory())
                .list(FILTER_INPUTS_FILES))
                .map(filename -> {
                    try (BufferedReader buffer = new BufferedReader(new FileReader(filename))) {
                        final List<String> literals = buffer
                                .lines()
                                .collect(Collectors.toList());
                        return literals;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    static final String COMMAND_LINE = "java -Xmx4096M -Xms2048M -Djanala.loggerClass=janala.logger.DirectConcolicExecution";

    static final String CONF_FILE_OPT = " -Djanala.conf=";
    static final String CONF_FILE_NAME = "lib/catg.conf ";

    static final String AGENT_OPT = " -javaagent:";
    static final String AGENT_FILE_NAME = "lib/catg-dev.jar ";

    public static void eraseOldFiles() {
        Stream.concat(Arrays.stream(new String[]{
                        "formula", "formula.tmp", "coverage.catg",
                        "inputs", "inputs.old", "inputs.bak",
                        "history", "history.old", "history.bak",
                        "isRealInput", "backtrackFlag", "instrumented"
                }).map(file -> CATGExecutor.getPrefixWorkingDirectory() + file),
                Arrays.stream(new File(".").list(FILTER_INPUTS_FILES))
        ).forEach(CATGUtils::tryToErase);
    }

    private static void tryToErase(String filename) {
        try {
            FileUtils.forceDelete(new File(filename));
        } catch (Exception e) {
            //ignored
        }
    }
}
