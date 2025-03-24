package com.auth_service.auth_service.core.model;

import lombok.Data;

@Data
public class ResponseBodyModel<T> {
	private boolean status;
	private String code;
	private String title;
	private String message;
	private T objectValue;
	private PageBodyModel pageValue;

	public void setOperationError(String code, String message, T object) {
		this.status = false;
		this.code = code;
		this.message = message;
		this.objectValue = object;
	}

	public void setOperationSuccess(String code, String message, T object) {
		this.status = true;
		this.code = code;
		this.message = message;
		this.objectValue = object;
	}

}
