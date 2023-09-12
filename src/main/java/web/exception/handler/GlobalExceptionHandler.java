package main.java.web.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import main.java.web.exception.FileException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/04/18:19
 **/
@Slf4j
@Component
@RestControllerAdvice
@Order
public class GlobalExceptionHandler {
    @ExceptionHandler(value = FileException.class)
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Object> handleFileException(FileException e) {
        log.error(e.getMessage(), e.getClass());
        return new HashMap(){{put("msg",e.getMessage());}};
    }
}

