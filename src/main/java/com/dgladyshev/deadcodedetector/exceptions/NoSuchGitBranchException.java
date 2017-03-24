package com.dgladyshev.deadcodedetector.exceptions;

import org.eclipse.jgit.api.errors.GitAPIException;

public class NoSuchGitBranchException extends GitAPIException {

    public NoSuchGitBranchException(String message) {
        super(message);
    }

}
