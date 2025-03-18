package edu.university.ecs.lab.detection.models.results;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.ecs.lab.detection.models.results.antipatterns.*;
import edu.university.ecs.lab.detection.models.results.architecture.AbstractAR;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({@JsonSubTypes.Type(value = AbstractAntiPattern.class, name = "AbstractAntiPattern"),
        @JsonSubTypes.Type(value = AbstractAR.class, name = "AbstractAR"),})
public abstract class DetectionResult {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected String description;
    protected String location;
    protected Map<String, Object> additionalData;

    public String toString() {
        return description + " " + location;
    }
}
