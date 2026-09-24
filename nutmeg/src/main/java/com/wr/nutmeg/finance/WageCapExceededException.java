package com.wr.nutmeg.finance;

/**
 * Thrown when a transfer or signing would push a club's total wage bill
 * above its wage cap.
 */
public class WageCapExceededException extends RuntimeException {

    public WageCapExceededException(String message) {
        super(message);
    }
}
