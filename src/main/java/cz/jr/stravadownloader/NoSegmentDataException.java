package cz.jr.stravadownloader;

/**
 * Created by Jiří Rýdel on 4/17/20, 1:42 PM
 */
public class NoSegmentDataException extends Exception {

    public NoSegmentDataException() {
    }

    public NoSegmentDataException(String message) {
        super(message);
    }

    public NoSegmentDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSegmentDataException(Throwable cause) {
        super(cause);
    }
}
