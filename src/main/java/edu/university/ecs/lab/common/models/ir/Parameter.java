package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Parameter extends Node implements JsonSerializable {

    /**
     * Java class type of the class variable e.g. String
     */
    private String type;

    private Set<Annotation> annotations;

    public Parameter(String name, String packageAndClassName, String type, Set<Annotation> annotations) {
        this.name = name;
        this.packageAndClassName = packageAndClassName;
        this.type = type;
        this.annotations = annotations;
    }

    public Parameter(com.github.javaparser.ast.body.Parameter parameter, String packageAndClassName) {
        this.name = parameter.getNameAsString();
        this.type = parameter.getTypeAsString();
        this.packageAndClassName = packageAndClassName;
        this.annotations = parameter.getAnnotations().stream().map(annotationExpr -> new Annotation(annotationExpr, packageAndClassName)).collect(Collectors.toSet());
    }


    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("packageAndClassName", getPackageAndClassName());
        jsonObject.addProperty("type", getType());
        jsonObject.add("annotations", JsonSerializable.toJsonArray(annotations));

        return jsonObject;
    }
}
