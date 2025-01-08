package br.com.granzoto.media_compressor.workflow;

import java.io.IOException;

public class WorkflowCompressionStepException extends Exception {

    public WorkflowCompressionStepException(String message){
        super(message);
    }

    public WorkflowCompressionStepException(String message, Throwable cause){
        super(message, cause);
    }
}
