package edu.university.ecs.lab.delta.models.enums;

import org.eclipse.jgit.diff.DiffEntry;

/**
 * Enumerated type for defining the types of changes used by jgit
 */
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
