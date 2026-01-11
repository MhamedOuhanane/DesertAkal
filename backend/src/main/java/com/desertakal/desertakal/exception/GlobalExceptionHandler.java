package com.desertakal.desertakal.exception;

import com.desertakal.desertakal.exception.base.BusinessException;
import com.desertakal.desertakal.model.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorResponse<String> error = ErrorResponse.<String>builder()
                .timestamp(LocalDateTime.now())
                .status(exception.getStatus().value())
                .error(exception.getStatus().getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(exception.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        var errorsValid = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null
                                ? fe.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing + "; " + replacement
                ));

        var error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(errorsValid)
                .message("Validation failed")
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(
            DisabledException exception,
            HttpServletRequest request
    ) {
        var error = ErrorResponse.<String>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Account Disabled")
                .message("Sorry, this account is not activated. Please check your email or contact the administration.")
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(
            LockedException exception,
            HttpServletRequest request
    ) {
        var error = ErrorResponse.<String>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Account Locked")
                .message("This account has been banned. Please contact administration for support.")
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handeGeneraleException(
            Exception exception,
            HttpServletRequest request
    ) {
        var error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please contact support." + exception.getMessage())
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
