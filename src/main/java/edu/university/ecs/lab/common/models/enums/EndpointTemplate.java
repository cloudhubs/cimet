//package edu.university.ecs.lab.common.models.enums;
//
//import com.github.javaparser.ast.expr.AnnotationExpr;
//
//public enum EndpointTemplate {
//    GET_MAPPING("GetMapping", HttpMethod.GET),
//    POST_MAPPING("PostMapping", HttpMethod.POST),
//    DELETE_MAPPING("DeleteMapping", HttpMethod.DELETE),
//    PUT_MAPPING("PutMapping", HttpMethod.PUT);
//
//    final HttpMethod httpMethod;
//    final String name;
//
//    EndpointTemplate(AnnotationExpr annotation) {
//        switch (annotation.getNameAsString()) {
//            case "GetMapping":
//                httpMethod = HttpMethod.GET;
//                break;
//            case "PostMapping":
//                httpMethod = HttpMethod.POST;
//                break;
//            case "DeleteMapping":
//                httpMethod = HttpMethod.DELETE;
//                break;
//            case "PutMapping":
//                httpMethod = HttpMethod.PUT;
//                break;
//            case "PatchMapping":
//                httpMethod = HttpMethod.PATCH;
//                break;
//            case "RequestMapping":
//                if(annotation.getChildNodes().stream())
//                break;
//            default:
//        }
//
//        this.name = name;
//        this.httpMethod = httpMethod;
//    }
//
//}
