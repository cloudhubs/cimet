package edu.university.ecs.lab.intermediate.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.apache.commons.io.FilenameUtils;

/**
 * Utility class for parsing strings.
 */
public class StringParserUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private StringParserUtils() {
    }

    /**
     * Remove start/end quotations from the given string.
     *
     *
     * @param s the string to remove quotations from
     * @return the string with quotations removed
     */
    public static String removeOuterQuotations(String s) {
        if (s != null && s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Simplifies all path arguments to {?}.
     *
     * @param url the endpoint URL
     * @return the simplified endpoint URL
     */
    public static String simplifyEndpointURL(String url) {
        return url.replaceAll("\\{[^{}]*\\}", "{?}");
    }
}
