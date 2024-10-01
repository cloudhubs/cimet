package edu.university.ecs.lab.common.models.ir;

import com.github.javaparser.ast.expr.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an annotation in Java
 */
@Data
@EqualsAndHashCode
public class Annotation extends Node {

    private Map<String, String> attributes;

    public Annotation(AnnotationExpr annotationExpr, String packageAndClassName) {
        this.name = annotationExpr.getNameAsString();
        this.packageAndClassName = packageAndClassName;
        this.attributes = parseAttributes(annotationExpr);
    }

    public Annotation(String name, String packageAndClassName, HashMap<String, String> attributes) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.attributes = attributes;
    }

    public String getContents() {
        return getAttributes().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(","));
    }

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        JsonElement attributesJson = gson.toJsonTree(attributes, new TypeToken<Map<String, String>>(){}.getType());
        jsonObject.add("attributes", attributesJson);

        return jsonObject;
    }

    private static HashMap<String, String> parseAttributes(AnnotationExpr annotationExpr) {
        HashMap<String, String> attributes = new HashMap<>();

        if(annotationExpr instanceof MarkerAnnotationExpr) {
            return attributes;
        } else if (annotationExpr instanceof SingleMemberAnnotationExpr smAnnotationExpr) {
            if(smAnnotationExpr.getMemberValue() instanceof StringLiteralExpr sle) {
                attributes.put("default", sle.asString());
            } else {
                return attributes;
            }
        } else if (annotationExpr instanceof NormalAnnotationExpr nAnnotationExpr) {
            for(MemberValuePair mvp : nAnnotationExpr.getPairs()) {
                if(mvp.getValue() instanceof StringLiteralExpr sle) {
                    attributes.put(mvp.getNameAsString(), sle.asString());
                }
            }
        }

        return attributes;
    }
}
