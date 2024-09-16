package edu.university.ecs.lab.common.models.enums;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import javassist.expr.Expr;
import lombok.Getter;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enum to represent Spring methodName and HttpMethod combinations and determine HttpMethod from
 * methodName.
 */
@Getter
public class RestCallTemplate {
    public static final Set<String> REST_OBJECTS = Set.of("RestTemplate", "OAuth2RestOperations", "OAuth2RestTemplate");
    public static final Set<String> REST_METHODS = Set.of("getForObject", "postForObject", "patchForObject", "put", "delete", "exchange");
    private static final String UNKNOWN_VALUE = "{?}";

    private final String url;
    private final HttpMethod httpMethod;
    private final CompilationUnit cu;
    private final MethodCallExpr mce;

    public RestCallTemplate(MethodCallExpr mce, CompilationUnit cu) {
        this.cu = cu;
        this.mce = mce;
        this.url = mce.getArguments().get(0).toString().isEmpty() ? "" : cleanURL(parseURL(mce.getArguments().get(0)));
        this.httpMethod = getHttpFromName(mce);
    }

    /**
     * Find the RestTemplate by the method name.
     *
     * @param methodName the method name
     * @return the RestTemplate found (null if not found)
     */
    public HttpMethod getHttpFromName(MethodCallExpr mce) {
        String methodName = mce.getNameAsString();
        switch (methodName) {
            case "getForObject":
                return HttpMethod.GET;
            case "postForObject":
                return HttpMethod.POST;
            case "patchForObject":
                return HttpMethod.PATCH;
            case "put":
                return HttpMethod.PUT;
            case "delete":
                return HttpMethod.DELETE;
            case "exchange":
                return getHttpMethodForExchange(mce.getArguments().stream().map(Node::toString).collect(Collectors.joining()));
        }

        return HttpMethod.NONE;
    }

    /**
     * Get the HTTP method for the JSF exchange() method call.
     *
     * @param arguments the arguments of the exchange() method
     * @return the HTTP method extracted
     */
    public HttpMethod getHttpMethodForExchange(String arguments) {
        if (arguments.contains("HttpMethod.POST")) {
            return HttpMethod.POST;
        } else if (arguments.contains("HttpMethod.PUT")) {
            return HttpMethod.PUT;
        } else if (arguments.contains("HttpMethod.DELETE")) {
            return HttpMethod.DELETE;
        } else if (arguments.contains("HttpMethod.PATCH")) {
            return HttpMethod.PATCH;
        } else {
            return HttpMethod.GET; // default
        }
    }

    /**
     * Find the URL from the given expression.
     *
     * @param exp the expression to extract url from
     * @return the URL found
     */
    private String parseURL(Expression exp) {
        if (exp.isStringLiteralExpr()) {
            return exp.asStringLiteralExpr().asString();
        } else if (exp.isFieldAccessExpr()) {
            return parseFieldValue(exp.asFieldAccessExpr().getNameAsString());
        } else if (exp.isBinaryExpr()) {
            String left = parseURL(exp.asBinaryExpr().getLeft());
            String right = parseURL(exp.asBinaryExpr().getRight());
            return left + right;
        } else if(exp.isEnclosedExpr()) {
            return parseURL(exp.asEnclosedExpr());
        // Base case, if we are a method call or a u
        } else if(exp.isMethodCallExpr()) {
            // Here we may try to find a modified url in a method call expr
            return backupParseURL(exp).isEmpty() ? UNKNOWN_VALUE : backupParseURL(exp);
        } else if(exp.isNameExpr()) {
            // Special case
            if(exp.asNameExpr().getNameAsString().contains("uri") || exp.asNameExpr().getNameAsString().contains("url")) {
                return "";
            }
            return UNKNOWN_VALUE;
        }

        // If all fails, try to find some viable url
        return backupParseURL(exp);
    }

    /**
     * Find the URL from the given expression.
     *
     * @param exp the expression to extract url from
     * @return the URL found
     */
    private String backupParseURL(Expression exp) {
        // Regular expression to match the first instance of "/.*"
        String regex = "\".*(/.+?)\"";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher for the input string
        Matcher matcher = pattern.matcher(exp.toString());

        // Find the first match
        if (matcher.find()) {
            // Extract the first instance of "/.*"
            String extracted = matcher.group(0).replace("\"", "");  // Group 1 corresponds to the part in parentheses (captured group)

            // Replace string formatters if they are present
            extracted = extracted.replaceAll("%[sdif]", UNKNOWN_VALUE);

            return cleanURL(extracted);
        }

        return "";

    }

    private static String cleanURL(String str) {
        str = str.replace("http://", "");
        str = str.replace("https://", "");

        // Remove everything before the first /
        int backslashNdx = str.indexOf("/");
        if (backslashNdx > 0) {
            str = str.substring(backslashNdx);
        }

//        int questionNdx = str.indexOf("?");
//        if (questionNdx > 0) {
//            str = str.substring(0, questionNdx);
//        }

        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }

        // Remove trailing / (does not affect functionality, trailing /'s are ignored in Spring)
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    private String parseFieldValue(String fieldName) {
        for (FieldDeclaration fd : cu.findAll(FieldDeclaration.class)) {
            if (fd.getVariables().toString().contains(fieldName)) {
                Expression init = fd.getVariable(0).getInitializer().orElse(null);
                if (init != null) {
                    return StringParserUtils.removeOuterQuotations(init.toString());
                }
            }
        }

        return "";
    }

//    private String parseNameValue(Expression mceScope, String name) {
//        for (VariableDeclarationExpr vdc : mceScope.findAll(VariableDeclarationExpr.class)) {
//            for(VariableDeclarator vd : vdc.getVariables()) {
//                if(vd.getNameAsString().equals(name)) {
//
//                }
//            }
//        }
//
//        return "";
//    }
}
