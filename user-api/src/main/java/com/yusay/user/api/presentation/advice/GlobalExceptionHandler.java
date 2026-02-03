package com.yusay.user.api.presentation.advice;

import com.yusay.user.api.domain.exception.DeleteAllNotAllowedException;
import com.yusay.user.api.domain.exception.DuplicateUserException;
import com.yusay.user.api.domain.exception.UserNotFoundException;
import com.yusay.user.api.presentation.constant.ErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("User not found");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateUser(DuplicateUserException ex, WebRequest request) {
        // セキュリティ: 具体的な値を公開せず、フィールド名のみを示す
        String detail = "指定された" + ex.getFieldName() + "は既に使用されています";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problemDetail.setTitle("Duplicate user");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        problemDetail.setProperty("field", ex.getFieldName());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        problemDetail.setTitle("Validation error");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(DeleteAllNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleDeleteAllNotAllowed(DeleteAllNotAllowedException ex, WebRequest request) {
        // セキュリティ: 内部情報（削除上限値等）を公開せず、汎用的なメッセージを返す
        // 例外メッセージの詳細はログに記録されるが、クライアントには安全なメッセージのみを返す
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ErrorMessages.DELETE_ALL_NOT_ALLOWED);
        problemDetail.setTitle("Delete all not allowed");
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }
}
