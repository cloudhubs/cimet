package edu.university.ecs.lab.common.models.sdg;

import lombok.Getter;
import lombok.Setter;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;

/**
 * Represents an edge in a network graph schema to model a microservice system.
 * Each edge object contains details about the connection between nodes.
 */
@Setter
@Getter
public class EndpointCallEdge extends DefaultWeightedEdge {
    /**
     * The endpoint of the target node that is accessed.
     */
    private String endpoint;

    /**
     * Determines whether another object is "equal to" this edge.
     * Two edges are considered equal if their source, target, and endpoint are all equal.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointCallEdge edge = (EndpointCallEdge) o;
        return Objects.equals(this.getSource(), edge.getSource()) && Objects.equals(this.getTarget(), edge.getTarget()) && Objects.equals(endpoint, edge.endpoint);
    }

    /**
     * Returns a hash code value for the edge. This method is supported for the benefit
     * of hash tables such as those provided by HashMap.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getSource(), this.getTarget(), endpoint);
    }
}
