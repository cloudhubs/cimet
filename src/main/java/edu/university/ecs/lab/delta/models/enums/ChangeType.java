package edu.university.ecs.lab.delta.models.enums;

import org.eclipse.jgit.diff.DiffEntry;

public enum ChangeType {
    ADD,
    MODIFY,
    DELETE;

    public static ChangeType fromDiffEntry(DiffEntry entry) {
        switch (entry.getChangeType()) {
            case ADD:
                return ADD;
            case MODIFY:
                return MODIFY;
            case DELETE:
                return DELETE;
            default:
                throw new IllegalArgumentException("Unknown change type: " + entry.getChangeType());
        }
    }
}
