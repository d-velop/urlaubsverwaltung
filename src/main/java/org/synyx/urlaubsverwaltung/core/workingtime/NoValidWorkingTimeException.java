package org.synyx.urlaubsverwaltung.core.workingtime;

/**
 * Exception that is thrown when no valid {@link WorkingTime} can be found for a period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class NoValidWorkingTimeException extends IllegalStateException {

    private final String message;

    public NoValidWorkingTimeException(String message) {

        super(message);

        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }
}
