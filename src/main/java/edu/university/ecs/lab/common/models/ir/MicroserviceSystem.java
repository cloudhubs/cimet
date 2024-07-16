package edu.university.ecs.lab.common.models.ir;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.enums.FileType;
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
     * Set of present files (class or configurations) who have no microservice
     */
    private Set<ProjectFile> orphans;

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
    public Microservice findMicroserviceByPath(String name) {
        return getMicroservices().stream().filter(microservice -> microservice.getPath().equals(name)).findFirst().orElse(null);
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
        Set<ProjectFile> updatedOrphans = new HashSet<>(getOrphans());

        for (ProjectFile file : getOrphans()) {
            // If the microservice is in the same folder as the path to the microservice
            if (file.getPath().contains(microservice.getPath())) {
                if(file.getFileType().equals(FileType.JCLASS)) {
                    microservice.addJClass((JClass) file);
                    updatedOrphans.remove(file);
                } else {
                    microservice.getFiles().add((ConfigFile) file);
                }
            }

        }

        setOrphans(updatedOrphans);

    }

    public JClass findClass(String path){
        JClass returnClass = null;
        returnClass = getMicroservices().stream().flatMap(m -> m.getClasses().stream()).filter(c -> c.getPath().equals(path)).findFirst().orElse(null);
        if(returnClass == null){
            returnClass = getOrphans().stream().filter(c -> c instanceof JClass).filter(c -> c.getPath().equals(path)).map(c -> (JClass) c).findFirst().orElse(null);
        }

        return returnClass;
    }

    public void orphanizeAndAdopt(Microservice microservice) {
        orphanize(microservice);
        for(Microservice m : getMicroservices()){
            adopt(m);
        }
    }

}
