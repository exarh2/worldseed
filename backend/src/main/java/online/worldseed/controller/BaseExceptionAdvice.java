package online.worldseed.generator.utils;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import online.worldseed.generator.model.dto.exception.BadRequestDto;
import online.worldseed.generator.model.dto.exception.BusinessErrorDto;
import online.worldseed.generator.model.dto.exception.ServiceErrorDto;
import online.worldseed.generator.model.dto.exception.ServiceUnavailableDto;
import online.worldseed.generator.model.exception.BadRequestException;
import online.worldseed.generator.model.exception.BusinessProcessException;
import online.worldseed.generator.model.exception.EntityNotFoundException;
import online.worldseed.generator.model.exception.ForbiddenException;
import online.worldseed.generator.model.exception.ServiceErrorException;
import online.worldseed.generator.model.exception.ServiceUnavailableException;
import online.worldseed.generator.model.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static online.worldseed.generator.mapper.ExceptionMapper.toBadRequestDto;
import static online.worldseed.generator.mapper.ExceptionMapper.toBusinessErrorDto;
import static online.worldseed.generator.mapper.ExceptionMapper.toServiceUnavailableDto;
import static online.worldseed.generator.model.Errors.REQUEST_VALIDATION_ERROR;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@Hidden
@ControllerAdvice
public class BaseExceptionAdvice {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BadRequestDto> handle(BadRequestException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return badRequest().body(toBadRequestDto(exception));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Void> handle(UnsupportedOperationException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(NOT_IMPLEMENTED).build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handle(UnauthorizedException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(UNAUTHORIZED).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handle(BadCredentialsException exception) {
        log.warn(exception.getClass().getName() + ": " + exception.getMessage());
        return status(UNAUTHORIZED).build();
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Void> handle(ForbiddenException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(FORBIDDEN).build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handle(AccessDeniedException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(FORBIDDEN).build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handle(EntityNotFoundException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return notFound().build();
    }

    @ExceptionHandler(BusinessProcessException.class)
    public ResponseEntity<BusinessErrorDto> handle(BusinessProcessException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return unprocessableEntity().body(toBusinessErrorDto(exception));
    }

    @ExceptionHandler(ServiceErrorException.class)
    public ResponseEntity<ServiceErrorDto> handle(ServiceErrorException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        var errorText = exception.getError();
        return status(INTERNAL_SERVER_ERROR)
                .body(errorText == null ? null : new ServiceErrorDto(errorText));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ServiceUnavailableDto> handle(ServiceUnavailableException exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(SERVICE_UNAVAILABLE).body(toServiceUnavailableDto(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceErrorDto> handle(Exception exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return status(INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<BadRequestDto> handleValidationError(Exception exception) {
        log.error(exception.getClass().getName() + ": ", exception);
        return badRequest().body(BadRequestDto.builder()
                .code(REQUEST_VALIDATION_ERROR.getCode())
                .message(REQUEST_VALIDATION_ERROR.getMessage())
                .reasons(List.of(exception.getMessage()))
                .build());
    }
}
