package openerp.openerpresourceserver.wms.exception;

import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalHandlerException {
    @ExceptionHandler(DataNotFoundException.class)
    public ApiResponse<Void> handleDataNotFoundException(DataNotFoundException e) {
        log.error(e.getMessage());
        return ApiResponse.<Void>
                builder().code(404).
                message(e.getMessage()).
                build();
    }

    @ExceptionHandler(DataExistedException.class)
    public ApiResponse<Void> handleDataExistedException(DataExistedException e) {
        log.error(e.getMessage());
        return ApiResponse.<Void>
                builder().code(400).
                message(e.getMessage()).
                build();
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error(e.getMessage());
        return ApiResponse.<Void>
                builder().code(500).
                message("Internal server error").
                build();
    }
}
