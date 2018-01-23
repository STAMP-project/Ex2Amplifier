package fr.inria.stamp.catg;

import org.apache.commons.io.FileUtils;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
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
        try (BufferedReader buffer = new BufferedReader(new FileReader("isRealInput"))) {
            isRealInput = buffer.lines().findFirst().get().equals("true");
        } catch (Exception e) {
            isRealInput = true;
        }
        if (isRealInput) {
            try {
                FileUtils.copyDirectory(new File("inputs"), new File("inputs" + iteration));
                FileUtils.copyDirectory(new File("inputs"), new File("inputs.old"));
            } catch (Exception ignored) {
                //ignored
            }
        }
        try {
            FileUtils.copyDirectory(new File("history"), new File("history.old"));
        } catch (Exception ignored) {
            //ignored
        }
    }

    static List<List<CtLiteral<?>>> readOutPut(final Factory factory) {
        return Arrays.stream(new File(".").list(FILTER_INPUTS_FILES))
                .map(filename -> {
                    try (BufferedReader buffer = new BufferedReader(new FileReader(filename))) {
                        final List<CtLiteral<?>> literals = buffer.lines()
                                .map(factory::createLiteral)
                                .collect(Collectors.toList());
                        return literals;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    static final String COMMAND_LINE = "java -Xmx4096M -Xms2048M -Djanala.loggerClass=janala.logger.DirectConcolicExecution"
            + " -Djanala.conf=lib/catg.conf "
//                + jvmOpts +
            + " -javaagent:lib/catg-dev.jar -cp ";

    public static void eraseOldFiles() {
        Stream.concat(Arrays.stream(new String[]{
                        "inputs", "inputs.old",
                        "history", "history.old",
                        "isRealInput", "backtrackFlag"
                }),
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
