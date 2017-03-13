package com.finebi.cube.exception;

/**
 * This class created on 2016/3/21.
 *
 * @author Connery
 * @since 4.0
 */
public class BIStatusAbsentException extends Exception {

    private static final long serialVersionUID = 2501273737786952097L;

    public BIStatusAbsentException() {
    }

    public BIStatusAbsentException(String message) {
        super(message);
    }
}
