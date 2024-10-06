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
     * Returns the microservice whose path is the start of the passed path
     *
     * @param path the path to search for
     * @return microservice instance of matching path or null
     */
    public Microservice findMicroserviceByPath(String path) {
        return getMicroservices().stream().filter(microservice -> path.startsWith(microservice.getPath())).findFirst().orElse(null);
    }


    /**
     * Given an existing microservice, if it must now be orphanized
     * then all JClasses belonging to that service will be added to
     * the system's pool of orphans for later use
     *
     * @param microservice the microservice to orphanize
     */
    public void orphanize(Microservice microservice) {
        Set<JClass> classes = microservice.getClasses();
        classes.forEach(c -> c.updateMicroserviceName(""));
        orphans.addAll(classes);
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
                    JClass jClass = (JClass) file;
                    jClass.updateMicroserviceName(microservice.getName());
                    microservice.addJClass(jClass);
                    updatedOrphans.remove(file);
                } else {
                    microservice.getFiles().add((ConfigFile) file);
                }
            }

        }

        setOrphans(updatedOrphans);

    }

    /**
     * Get the class of a given endpoint
     * 
     * @param path endpoint 
     * @return class that endpoint is in
     */
    public JClass findClass(String path){
        JClass returnClass = null;
        returnClass = getMicroservices().stream().flatMap(m -> m.getClasses().stream()).filter(c -> c.getPath().equals(path)).findFirst().orElse(null);
        if(returnClass == null){
            returnClass = getOrphans().stream().filter(c -> c instanceof JClass).filter(c -> c.getPath().equals(path)).map(c -> (JClass) c).findFirst().orElse(null);
        }

        return returnClass;
    }

    /**
     * Get the file of a given endpoint
     * 
     * @param path endpoint
     * @return file that endpoint is in
     */
    public ProjectFile findFile(String path){
        ProjectFile returnFile = null;
        returnFile = getMicroservices().stream().flatMap(m -> m.getAllFiles().stream()).filter(c -> c.getPath().equals(path)).findFirst().orElse(null);
        if(returnFile == null){
            returnFile = getOrphans().stream().filter(c -> c.getPath().equals(path)).findFirst().orElse(null);
        }

        return returnFile;
    }

    /**
     * This method returns the name of the microservice associated with
     * a file that exists in the system. Note this method will not work
     * if the file is not present somewhere in the system
     *
     * @param path the ProjectFile path
     * @return string name of microservice or "" if it does not exist
     */
    public String getMicroserviceFromFile(String path){
        for(Microservice microservice : getMicroservices()) {
            for(ProjectFile file : microservice.getFiles()) {
                if(file.getPath().equals(path)) {
                    return microservice.getName();
                }
            }
        }

        return "";
    }

    public void orphanizeAndAdopt(Microservice microservice) {
        orphanize(microservice);
        for(Microservice m : getMicroservices()){
            adopt(m);
        }
    }


}
