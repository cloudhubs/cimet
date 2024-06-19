package edu.university.ecs.lab.detection.architecture.models;

import edu.university.ecs.lab.detection.architecture.models.enums.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class UseCase1 extends UseCase {
    protected static final String NAME = "UseCase1";
    protected static final Scope SCOPE = Scope.CLASS;
    protected static final String DESC = "This is describing the class";

    private UseCase1() {}



    @Override
    public List<? extends UseCase> checkUseCase() {
        ArrayList<UseCase1> useCases = new ArrayList<>();

        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public Scope getScope() {
        return SCOPE;
    }

    @Override
    public double getWeight() {
        return 0;
    }
}
