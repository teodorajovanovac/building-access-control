package com.buildingaccess.exception;

/** Status-guard konflikt — npr. izmena propusnice koja više nije ACTIVE, brisanje zgrade sa aktivnim podacima. */
public class InvalidStatusException extends RuntimeException {

    public InvalidStatusException(String message) {
        super(message);
    }
}
