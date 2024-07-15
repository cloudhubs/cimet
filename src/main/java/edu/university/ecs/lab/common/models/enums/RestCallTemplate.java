package edu.university.ecs.lab.common.models.enums;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum to represent Spring methodName and HttpMethod combinations and determine HttpMethod from
 * methodName.
 */
@Getter
public class RestCallTemplate {
    public static final Set<String> REST_METHODS = Set.of("getForObject", "postForObject", "patchForObject", "put", "delete", "exchange");
    private final String url;
    private final HttpMethod httpMethod;
    private final CompilationUnit cu;

    public RestCallTemplate(MethodCallExpr mce, CompilationUnit cu) {
        this.cu = cu;
        this.url = parseURL(mce);
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
     * Find the URL from the given method call expression.
     *
     * @param mce the method call to extract url from
     * @return the URL found
     */
    private String parseURL(MethodCallExpr mce) {
        if (mce.getArguments().isEmpty()) {
            return "";
        }

        // Arbitrary index of the url parameter
        Expression exp = mce.getArguments().get(0);

        if (exp.isStringLiteralExpr()) {
            return StringParserUtils.removeOuterQuotations(exp.toString());
        } else if (exp.isFieldAccessExpr()) {
            return parseFieldValue(exp.asFieldAccessExpr().getNameAsString());
        } else if (exp.isNameExpr()) {
            return parseFieldValue(exp.asNameExpr().getNameAsString());
        } else if (exp.isBinaryExpr()) {
            return parseUrlFromBinaryExp(exp.asBinaryExpr());
        }

        return "";
    }

    private String parseUrlFromBinaryExp(BinaryExpr exp) {
        StringBuilder returnString = new StringBuilder();
        Expression left = exp.getLeft();
        Expression right = exp.getRight();

        if (left instanceof BinaryExpr) {
            returnString.append(parseUrlFromBinaryExp((BinaryExpr) left));
        } else if (left instanceof StringLiteralExpr) {
            returnString.append(formatURL(left.toString()));
        } else if (left instanceof NameExpr
                && !left.asNameExpr().getNameAsString().contains("url")
                && !left.asNameExpr().getNameAsString().contains("uri")) {
            returnString.append("/{?}");
        }

        // Check if right side is a binary expression
        if (right instanceof BinaryExpr) {
            returnString.append(parseUrlFromBinaryExp((BinaryExpr) right));
        } else if (right instanceof StringLiteralExpr) {
            returnString.append(formatURL(right.toString()));
        } else if (right instanceof NameExpr) {
            returnString.append("/{?}");
        }

        return returnString.toString(); // URL not found in subtree
    }

    private static String formatURL(String str) {
        str = str.replace("http://", "");
        str = str.replace("https://", "");

        int backslashNdx = str.indexOf("/");
        if (backslashNdx > 0) {
            str = str.substring(backslashNdx);
        }

        int questionNdx = str.indexOf("?");
        if (questionNdx > 0) {
            str = str.substring(0, questionNdx);
        }

        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }

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
}
