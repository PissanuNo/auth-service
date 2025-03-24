package com.auth_service.auth_service.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestBodyModel<T> {

	private @Getter @Setter T requestObject;

	private @Getter @Setter
    PageBodyModel pageValue;
}
