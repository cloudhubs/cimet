package edu.university.ecs.lab.detection.architecture.models.interfaces;

import edu.university.ecs.lab.detection.architecture.models.AbstractUseCase;

import java.util.List;

public interface UseCaseInterface {
    /**
     * This method checks for instances of "this" UseCase and
     * returns a list of defined instances
     *
     * @return
     */
    public List<? extends AbstractUseCase> checkUseCase();


}
