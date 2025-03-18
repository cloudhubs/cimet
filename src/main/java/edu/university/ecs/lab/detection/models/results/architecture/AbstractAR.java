package edu.university.ecs.lab.detection.models.results.architecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;

import edu.university.ecs.lab.detection.models.results.DetectionResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Architectural Rule class template for all architectural rules.
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({@JsonSubTypes.Type(value = AR1.class, name = "AR1"),
        @JsonSubTypes.Type(value = AR3.class, name = "AR3"),
        @JsonSubTypes.Type(value = AR4.class, name = "AR4"),
        @JsonSubTypes.Type(value = AR6.class, name = "AR6"),
        @JsonSubTypes.Type(value = AR7.class, name = "AR7"),
        @JsonSubTypes.Type(value = AR20.class, name = "AR20"),
        @JsonSubTypes.Type(value = AR21.class, name = "AR21"),
        @JsonSubTypes.Type(value = AR22.class, name = "AR22"),
        @JsonSubTypes.Type(value = AR23.class, name = "AR23"),
        @JsonSubTypes.Type(value = AR24.class, name = "AR24")})
@Data
public abstract class AbstractAR extends DetectionResult {
    protected String oldCommitID;
    protected String newCommitID;
    protected JsonNode metaData;

    /**
     * Get the name of the Architectural Rule
     *
     * @return string name of the Architectural Rule
     */
    @JsonIgnore
    public abstract String getName();

    /**
     * Get the description of the Architectural Rule
     *
     * @return string description of the Architectural Rule
     */
    @JsonIgnore
    public abstract String getDescription();

    /**
     * Get the weight of the Architectural Rule
     *
     * @return double weight of the Architectural Rule
     */
    @JsonIgnore
    public abstract double getWeight();

    /**
     * Get the new commitID
     *
     * @return string new commitID
     */
    @JsonIgnore
    public abstract String getType();
}
