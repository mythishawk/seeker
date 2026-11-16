package com.job.seeker.discovery;

/** A job board could not be read (network error, unknown company, bad response). */
public class BoardFetchException extends RuntimeException {

    public BoardFetchException(String message) {
        super(message);
    }

    public BoardFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
