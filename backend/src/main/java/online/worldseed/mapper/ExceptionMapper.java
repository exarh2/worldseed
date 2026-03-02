package online.worldseed.mapper;

import online.worldseed.model.dto.exception.BadRequestDto;
import online.worldseed.model.dto.exception.BusinessErrorDto;
import online.worldseed.model.dto.exception.ServiceUnavailableDto;
import online.worldseed.model.exception.BadRequestException;
import online.worldseed.model.exception.BusinessProcessException;
import online.worldseed.model.exception.ServiceUnavailableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionMapper {

    public static BusinessErrorDto toBusinessErrorDto(BusinessProcessException exception) {
        var errorInfo = exception.getErrorInfo();
        return BusinessErrorDto.builder()
            .code(errorInfo.getCode())
            .message(errorInfo.getMessage())
            .reasons(exception.getCause() != null ? List.of(exception.getCause().getMessage()) : null)
            .build();
    }

    public static ServiceUnavailableDto toServiceUnavailableDto(ServiceUnavailableException exception) {
        var errorInfo = exception.getErrorInfo();
        return ServiceUnavailableDto.builder()
            .code(errorInfo.getCode())
            .message(errorInfo.getMessage())
            .build();
    }

    public static BadRequestDto toBadRequestDto(BadRequestException exception) {
        var errorInfo = exception.getErrorInfo();
        return BadRequestDto.builder()
            .code(errorInfo.getCode())
            .message(errorInfo.getMessage())
            .build();
    }

    public static String buildDetail(MethodArgumentNotValidException exception) {
        var errors = exception.getFieldErrors();
        return errors.stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .collect(Collectors.joining(",\n"));
    }
}
