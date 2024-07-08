package edu.university.ecs.lab.common.models.enums;

import lombok.Getter;

/**
 * Enum to represent Spring methodName and HttpMethod combinations and determine HttpMethod from
 * methodName.
 */
@Getter
public enum RestCallTemplate {
    GET_FOR_OBJECT("getForObject", HttpMethod.GET),
    GET_FOR_ENTITY("getForEntity", HttpMethod.GET),
    POST_FOR_OBJECT("postForObject", HttpMethod.POST),
    POST_FOR_ENTITY("postForEntity", HttpMethod.POST),
    PUT("put", HttpMethod.PUT),
    EXCHANGE("exchange", HttpMethod.NONE), // exchange can be many types, so we do not specify here
    DELETE("delete", HttpMethod.DELETE), // TODO: delete doesn't work
    ;

    private final String methodName;
    private final HttpMethod httpMethod;

    RestCallTemplate(String methodName, HttpMethod httpMethod) {
        this.methodName = methodName;
        this.httpMethod = httpMethod;
    }

    /**
     * Find the RestTemplate by the method name.
     *
     * @param methodName the method name
     * @return the RestTemplate found (null if not found)
     */
    public static HttpMethod findHttpMethodByName(String methodName) {
        for (RestCallTemplate template : RestCallTemplate.values()) {
            if (template.getMethodName().equals(methodName)) {
                return template.getHttpMethod();
            }
        }

        return HttpMethod.NONE;
    }

    /**
     * Get the HTTP method for the JSF exchange() method call.
     *
     * @param arguments the arguments of the exchange() method
     * @return the HTTP method extracted
     */
    public static HttpMethod getHttpMethodForExchange(String arguments) {
        if (arguments.contains("HttpMethod.POST")) {
            return HttpMethod.POST;
        } else if (arguments.contains("HttpMethod.PUT")) {
            return HttpMethod.PUT;
        } else if (arguments.contains("HttpMethod.DELETE")) {
            return HttpMethod.DELETE;
        } else {
            return HttpMethod.GET; // default
        }
    }
}
