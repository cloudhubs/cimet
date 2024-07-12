package edu.university.ecs.lab.common.models.enums;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum EndpointTemplate {
    GET_MAPPING("GetMapping", HttpMethod.GET),
    POST_MAPPING("PostMapping", HttpMethod.POST),
    DELETE_MAPPING("DeleteMapping", HttpMethod.DELETE),
    PUT_MAPPING("PutMapping", HttpMethod.PUT);

    public static final List<String> ENDPOINT_ANNOTATIONS = Arrays.asList("RequestMapping", "GetMapping", "PutMapping", "PostMapping", "DeleteMapping", "PatchMapping");
    private final HttpMethod httpMethod;
    private final String name;
    private final String url;

    EndpointTemplate(String name, HttpMethod httpMethod) {
        this.name = name;
        this.httpMethod = httpMethod;
        this.url = "/" + name;


    }

    EndpointTemplate(AnnotationExpr ae) {
        HttpMethod finalHttpMethod = HttpMethod.NONE;

        // Set httpMethod
        if (ae.getNameAsString().equals("RequestMapping")) {
            if (ae instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr nae = (NormalAnnotationExpr) ae;
                if (nae.getPairs().isEmpty()) {
                    // This is a RequestMapping without parameters
                    finalHttpMethod = HttpMethod.ALL;
                } else {
                    for (MemberValuePair pair : nae.getPairs()) {
                        if (pair.getNameAsString().equals("method")) {
                            String methodValue = pair.getValue().toString();
                            finalHttpMethod = httpFromMapping(methodValue);
                            break;
                        }
                    }
                }
            } else {
                finalHttpMethod = HttpMethod.ALL;
            }
        } else {
            finalHttpMethod = httpFromMapping(ae.getNameAsString());
        }

        this.httpMethod = finalHttpMethod;
        this.name = ae.getNameAsString();
        this.url = ae.getNameAsString();
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
                return HttpMethod.NONE;
        }

    }


}
