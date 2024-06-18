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
     * <p>ex: "abcde" --> abcde
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
     * Merge the given class and method paths into a single path.
     *
     * <p>ex: /abc/def and ghi/jkl --> abc/def/ghi/jkl
     *
     * @param classPath  the class base (api) path
     * @param methodPath the method (api) path
     * @return the merged path
     */
    public static String mergePaths(String classPath, String methodPath) {
        if (classPath.startsWith("/")) classPath = classPath.substring(1);
        if (methodPath.startsWith("/")) methodPath = methodPath.substring(1);

        String path =
                FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(classPath, methodPath), true);
        if (!path.startsWith("/")) path = "/" + path;

        return path;
    }

    /**
     * Find the package name in the given compilation unit.
     *
     * @param cu the compilation unit
     * @return the package name else null if not found
     */
    public static String findPackage(CompilationUnit cu) {
        for (PackageDeclaration pd : cu.findAll(PackageDeclaration.class)) {
            return pd.getNameAsString();
        }
        return null;
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
