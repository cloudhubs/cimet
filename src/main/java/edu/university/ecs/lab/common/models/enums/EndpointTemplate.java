package edu.university.ecs.lab.common.models.enums;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Factory class for generating an endpoint template from annotations
 */
@Getter
public class EndpointTemplate {
//    GET_MAPPING("GetMapping", HttpMethod.GET),
//    POST_MAPPING("PostMapping", HttpMethod.POST),
//    DELETE_MAPPING("DeleteMapping", HttpMethod.DELETE),
//    private static final EndpointTemplate PUT_MAPPING = new EndpointTemplate("PutMapping", HttpMethod.PUT);

    public static final List<String> ENDPOINT_ANNOTATIONS = Arrays.asList("RequestMapping", "GetMapping", "PutMapping", "PostMapping", "DeleteMapping", "PatchMapping");
    private final HttpMethod httpMethod;
    private final String name;
    private final String url;



    public EndpointTemplate(AnnotationExpr requestMapping, AnnotationExpr endpointMapping) {
        HttpMethod finalHttpMethod = HttpMethod.ALL;

        String preUrl = "";
        if(requestMapping != null) {
            if (requestMapping instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr nae = (NormalAnnotationExpr) requestMapping;
                for (MemberValuePair pair : nae.getPairs()) {
                    if (pair.getNameAsString().equals("value")) {
                        preUrl = pair.getValue().toString();
                    }
                }
            } else if (requestMapping instanceof SingleMemberAnnotationExpr) {
                preUrl = requestMapping.asSingleMemberAnnotationExpr().getMemberValue().toString();
            }
        }

        String url = "";
        if (endpointMapping instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr nae = (NormalAnnotationExpr) endpointMapping;
            for (MemberValuePair pair : nae.getPairs()) {
                if (pair.getNameAsString().equals("method")) {
                    String methodValue = pair.getValue().toString();
                    finalHttpMethod = httpFromMapping(methodValue);
                } else if(pair.getNameAsString().equals("path") || pair.getNameAsString().equals("value")) {
                    url = pair.getValue().toString();
                }
            }
        } else if (endpointMapping instanceof SingleMemberAnnotationExpr) {
            url = endpointMapping.asSingleMemberAnnotationExpr().getMemberValue().toString();
        }

        if(finalHttpMethod == HttpMethod.ALL) {
            finalHttpMethod = httpFromMapping(endpointMapping.getNameAsString());
        }


        this.httpMethod = finalHttpMethod;
        this.name = endpointMapping.getNameAsString();
        this.url = (preUrl.replace("\"", "") + simplifyEndpointURL(url.replace("\"", ""))).replace("//", "/");
    }



    private static HttpMethod httpFromMapping(String mapping) {
        switch (mapping) {
            case "GetMapping":
            case "RequestMethod.GET":
                return HttpMethod.GET;
            case "PostMapping":
            case "RequestMethod.POST":
                return HttpMethod.POST;
            case "DeleteMapping":
            case "RequestMethod.DELETE":
                return HttpMethod.DELETE;
            case "PutMapping":
            case "RequestMethod.PUT":
                return HttpMethod.PUT;
            case "PatchMapping":
            case "RequestMethod.PATCH":
                return HttpMethod.PATCH;
            default:
                return HttpMethod.ALL;
        }

    }

    public static String getPathFromAnnotation(AnnotationExpr ae, String url) {
        // Annotations of type @Mapping("/endpoint")
        if (ae.isSingleMemberAnnotationExpr()) {
            url = url + StringParserUtils.simplifyEndpointURL(
                    StringParserUtils.removeOuterQuotations(
                            ae.asSingleMemberAnnotationExpr().getMemberValue().toString()));
        }

        // Annotations of type @Mapping(path="/endpoint")
        else if (ae.isNormalAnnotationExpr() && !ae.asNormalAnnotationExpr().getPairs().isEmpty()) {
            for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
                if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
                    url = url + StringParserUtils.simplifyEndpointURL(
                            StringParserUtils.removeOuterQuotations(mvp.getValue().toString()));
                    break;
                }
            }
        }
        return url;
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
