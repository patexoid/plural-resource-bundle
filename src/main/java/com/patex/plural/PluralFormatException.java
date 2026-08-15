package com.patex.plural;

/**
 * Thrown when a pattern passed to {@link PluralMessageFormat#format} contains
 * a malformed {@code {n}} placeholder or {@code <p:word>} plural tag, or when
 * the arguments supplied don't match what the pattern requires (missing
 * index, wrong type).
 */
public class PluralFormatException extends IllegalArgumentException {

    PluralFormatException(String message) {
        super(message);
    }
}