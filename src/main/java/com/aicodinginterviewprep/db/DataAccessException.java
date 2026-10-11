package com.aicodinginterviewprep.db;

import com.aicodinginterviewprep.errors.PersistenceException;

public class DataAccessException extends PersistenceException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
