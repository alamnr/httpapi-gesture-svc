package info.ejava.examples.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import info.ejava.examples.app.svc.ClientErrorException.InvalidInputException;
import info.ejava.examples.app.svc.ClientErrorException.NotFoundException;
import info.ejava.examples.app.svc.ServerErrorException.InternalErrorException;
import lombok.extern.slf4j.Slf4j;


/*
 * This class provide custom error handling for exceptions thrown by the 
 * controller. It is one of several techniques offered by spring and selected 
 * primarily because we retain full control over the response and response headers 
 * returned to the caller
 * 
 */

@RestControllerAdvice(basePackageClasses=GesturesController.class)
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handle(NotFoundException ex){
            return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<String> handle(InvalidInputException ex){        
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<String> handle(InternalErrorException ex){
        log.warn("{}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex){
        log.warn("{}", ex.getMessage(), ex);
        String text = String.format("unexpected error executing request : %s",ex.toString());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, text);
    }


    protected ResponseEntity<String> buildResponse(HttpStatus status, String text){
        return ResponseEntity.status(status).body(text);
    }
}
