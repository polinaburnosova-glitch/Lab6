package common.network;

import java.io.Serializable;

public enum ResponseStatus implements Serializable {
    OK,
    NOT_FOUND,
    VALIDATION_ERROR,
    SERVER_ERROR,
    UNKNOWN_COMMAND,
    WARNING,
    UNAUTHORIZED,
    FORBIDDEN;
}
