package edu.university.ecs.lab.detection.models.results.antipatterns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract implementation of an Antipattern should be the parent
 * of all system Antipatterns
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({@JsonSubTypes.Type(value = CyclicDependency.class, name = "CyclicDependency"),
        @JsonSubTypes.Type(value = GreedyMicroservice.class, name = "GreedyMicroservice"),
        @JsonSubTypes.Type(value = HubLikeMicroservice.class, name = "HubLikeMicroservice"),
        @JsonSubTypes.Type(value = NoApiGateway.class, name = "NoApiGateway"),
        @JsonSubTypes.Type(value = NoHealthcheck.class, name = "NoHealthcheck"),
        @JsonSubTypes.Type(value = ServiceChain.class, name = "ServiceChain"),
        @JsonSubTypes.Type(value = WobblyServiceInteraction.class, name = "WobblyServiceInteraction"),
        @JsonSubTypes.Type(value = WrongCuts.class, name = "WrongCuts")})
public abstract class AbstractAntiPattern {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnore
    protected abstract String getName();
    @JsonIgnore
    protected abstract String getDescription();
    @JsonIgnore
    protected abstract JsonNode getMetaData();
}
