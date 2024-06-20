package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the intermediate structure of a microservice system.
 */
@Data
@AllArgsConstructor

@EqualsAndHashCode
public class MicroserviceSystem implements JsonSerializable {
    /**
     * The name of the system
     */
    private String name;

    /**
     * The commit ID of the system
     */
    private String commitID;

    /**
     * Set of microservices in the system
     */
    private Set<Microservice> microservices;

    /**
     * Set of present classes who have no microservice
     */
    private Set<JClass> orphans;

    /**
     * see {@link JsonSerializable#toJsonObject()}
     */
    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("commitID", commitID);
        jsonObject.add("microservices", JsonSerializable.toJsonArray(microservices));
        jsonObject.add("orphans", JsonSerializable.toJsonArray(orphans));

        return jsonObject;
    }

    /**
     * Returns the microservice that matches the passed name
     *
     * @param name the name to search for
     * @return microservice whose name matches or null if not found
     */
    public Microservice findMicroserviceByName(String name) {
        return getMicroservices().stream().filter(microservice -> microservice.getName().equals(name)).findFirst().orElse(null);
    }


    /**
     * Given an existing microservice, if it must now be orphanized
     * then all JClasses belonging to that service will be added to
     * the system's pool of orphans for later use
     *
     * @param microservice the microservice to orphanize
     */
    public void orphanize(Microservice microservice) {
        orphans.addAll(microservice.getControllers());
        orphans.addAll(microservice.getServices());
        orphans.addAll(microservice.getRepositories());
        orphans.addAll(microservice.getEntities());
    }

    /**
     * Given a new or modified microservice, we must adopt awaiting
     * orphans based on their file paths containing the microservices
     * (folder) path
     *
     * @param microservice the microservice adopting orphans
     */
    public void adopt(Microservice microservice) {
        Set<JClass> updatedOrphans = new HashSet<>(getOrphans());

        for (JClass jClass : getOrphans()) {
            // If the microservice is in the same folder as the path to the microservice
            if (jClass.getPath().contains(microservice.getPath())) {
                microservice.addJClass(jClass);
                updatedOrphans.remove(jClass);
            }

        }

        setOrphans(updatedOrphans);

    }

    public JClass findClass(String Path){
        return getMicroservices().stream().flatMap(m -> m.getClasses().stream()).filter(c -> c.getPath().equals(Path)).findFirst().orElse(null);
    }

}
