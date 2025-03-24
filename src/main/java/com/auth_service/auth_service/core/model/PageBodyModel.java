package com.auth_service.auth_service.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageBodyModel {
    private String page;
    private String pageSize;
    private String totalPage;
    private String sortBy;
    private String sortDirection;
    private String total;
    private Long processTime;
    private String filter;

    public PageBodyModel convertToPageBodyModel(Pageable pageable, Page page) {
        this.setPage(String.valueOf(pageable == null ? "" : pageable.getPageNumber()));
        this.setPageSize(String.valueOf(pageable == null ? "" : pageable.getPageSize()));
        this.setSortBy(pageable == null ? "" : pageable.getSort().iterator().next().getProperty());
        this.setSortDirection(pageable == null ? "" : pageable.getSort().iterator().next().getDirection().name());
        this.setTotal(String.valueOf(page.getTotalElements()));
        this.setTotalPage(Math.toIntExact(page.getTotalElements()), page.getTotalPages());
        return this;
    }

    public void setTotalPage(Integer total, Integer pageSize) {
        this.totalPage = (String.valueOf(Math.ceil((total * 1.00) / (pageSize * 1.00))));
    }
}
