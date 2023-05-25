/*
 * ApiRestResponseExceptionHandler.java
 * Copyright (c) UEG.
 *
 *
 *
 *
 */
package br.ueg.prog.webi.api.exception;

import br.ueg.prog.webi.api.util.Util;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe handler responsável por interceptar e tratar as exceções de forma
 * amigavel para o client.
 * 
 * @author UEG
 */
public abstract class ApiRestResponseExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	/**
	 * Retorna o código da mensagem padrão para erros internos da aplicação.
	 * 
	 * @return -
	 */
	protected MessageCode getCodeInternalServerError(){
		return ApiMessageCode.ERRO_INESPERADO;
	};

	/**
	 * Método handle referente a exceção {@link BusinessException}.
	 * 
	 * @param e  -
	 * @return -
	 */
	@ExceptionHandler({ BusinessException.class })
	public ResponseEntity<Object> handleBusinessException(final BusinessException e) {
		ResponseEntity<Object> response = null;

		if (e.getCode() != null) {
			Object[] params = null;

			if (e.hasParameters()) {
				params = e.getParameters();

				if (e.isConcat()) {
					String paramsConcat = Util.getValorConcatenado(", ", params);

					params = new Object[1];
					params[0] = paramsConcat;
				}
			}

			String message = getMessage(e.getCode(), params);
			logger.error(message, e);

			response = response(e.getCode(), message, e.getParameters());
		} else if (e.getResponse() != null) {
			response = ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
		} else if (e.getCause() != null) {
			response = handleException(e.getCause());
		}
		return response;
	}

	/**
	 * Método handle global de tratamento de exceção.
	 * 
	 * @param e  -
	 * @return -
	 */
	@ExceptionHandler({ RuntimeException.class, Exception.class })
	public ResponseEntity<Object> handleException(final Throwable e) {
		MessageCode code = getCodeInternalServerError();
		String message = getMessage(code, e.getMessage());
		logger.error(message, e);
		return response(code, message, null);
	}

	/**
	 * Método handle responsável por tratar a exceção
	 * {@link InvalidParameterException}.
	 * 
	 * @param e  -
	 * @return -
	 */
	@ExceptionHandler({ InvalidParameterException.class })
	public ResponseEntity<MessageResponse> handleInvalidParameter(final InvalidParameterException e) {
		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		response.addAttribute(new FieldResponse(e.getField(), e.getDefaultMessage()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
	}

	/**
	 * Método handle responsável por tratar a exceção
	 * {@link ConstraintViolationException}.
	 * 
	 * @param e -
	 * @return -
	 */
	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<MessageResponse> handleConstraintViolation(final ConstraintViolationException e) {

		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

		e.getConstraintViolations().forEach(constraint -> {
			List<String> parts = new ArrayList<>();
			constraint.getPropertyPath().forEach(node -> parts.add(node.getName()));

			String attribute = parts.get(parts.size() - BigDecimal.ONE.intValue());
			response.addAttribute(new FieldResponse(attribute, constraint.getMessage()));
		});
		return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
	}


	/**
	 * Método handle responsável por tratar a exceção {@link AccessDeniedException}.
	 *
	 * @param e -
	 * @return -
	 */
	@ExceptionHandler({ AccessDeniedException.class })
	public ResponseEntity<MessageResponse> handleAccessDeniedException(final AccessDeniedException e) {
		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
		response.setMessage(e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(response);
	}

	/**
	 * Método handle responsável por tratar a exceção {@link AccessDeniedException}.
	 *
	 * @param e -
	 * @return -
	 */
	@ExceptionHandler({ TokenExpiredException.class })
	public ResponseEntity<MessageResponse> handleAccessDeniedException(final TokenExpiredException e) {
		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
		response.setMessage(e.getMessage());
		logger.error(e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(response);
	}

	/**
	 * @see ResponseEntityExceptionHandler#handleMethodArgumentNotValid(MethodArgumentNotValidException,
	 *      HttpHeaders,
	 *      HttpStatusCode,
	 *      WebRequest)
	 */
	@Override
	@Nullable
	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException e,
																  HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {

		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

		BindingResult result = e.getBindingResult();

		result.getAllErrors().forEach(error -> {
			FieldError fieldError = (FieldError) error;
			response.addAttribute(new FieldResponse(fieldError.getField(), error.getDefaultMessage()));
		});
		return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
	}

	/**
	 * @see ResponseEntityExceptionHandler#handleMissingServletRequestParameter(MissingServletRequestParameterException,
	 *      HttpHeaders,
	 *      HttpStatusCode,
	 *      WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
            final MissingServletRequestParameterException e, final HttpHeaders headers, final HttpStatusCode status,
            final WebRequest request) {

		MessageResponse response = new MessageResponse();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		response.addAttribute(new FieldResponse(e.getParameterName(), "não pode estar vazio"));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
	}

	/**
	 * @see ResponseEntityExceptionHandler#handleHttpMessageNotReadable(HttpMessageNotReadableException,
	 *      HttpHeaders,
	 *      HttpStatusCode,
	 *      WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException e,
                                                                  final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
		ResponseEntity<Object> responseEntity = null;

		if (e.getCause() instanceof InvalidFormatException) {
			InvalidFormatException ex = (InvalidFormatException) e.getCause();
			String attribute = ex.getPath().stream().map(path -> path.getFieldName()).findFirst().orElse(null);

			MessageResponse response = new MessageResponse();
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
			response.addAttribute(new FieldResponse(attribute, "valor inválido"));
			responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
		} else {
			responseEntity = super.handleHttpMessageNotReadable(e, headers, status, request);
		}
		return responseEntity;
	}

	/**
	 * @see ResponseEntityExceptionHandler#handleTypeMismatch(TypeMismatchException,
	 *      HttpHeaders,
	 *      HttpStatusCode,
	 *      WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers,
                                                        final HttpStatusCode status, final WebRequest request) {
		return handleException(ex);
	}

	/**
	 * Retorna a mensagem informado com os parâmetros.
	 * 
	 * @param code    -
	 * @param params -
	 * @return -
	 */
	private String getMessage(final MessageCode code, final Object... params) {
		return messageSource.getMessage(code.toString(), params, LocaleContextHolder.getLocale());
	}

	/**
	 * Retorna a instância de {@link ResponseEntity} conforme os parâmetros
	 * informados.
	 * 
	 * @param code -
	 * @param params  -
	 * @param message -
	 * @return -
	 */
	private ResponseEntity<Object> response(final MessageCode code, final String message, final Object[] params) {
		MessageResponse response = new MessageResponse();
		response.setCode(code.toString());
		response.setParameters(params);
		response.setMessage(message);

		HttpStatus status = HttpStatus.resolve(code.getStatus());
		response.setError(status.getReasonPhrase());
		response.setStatus(status.value());

		return ResponseEntity.status(status).body(response);
	}
}
