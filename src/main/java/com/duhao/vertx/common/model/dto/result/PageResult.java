package com.duhao.vertx.common.model.dto.result;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * @author Hao Du
 * @version 1.0
 * @since 2021/9/18
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T extends Serializable> extends Result<T> {
    private static final long serialVersionUID = 9089995436521387770L;

    private Integer current;

    private Integer size;

    private Long total;

    private Integer totalPages;

    private Collection<T> items;

    protected PageResult() {
    }

    public PageResult(Integer current, Integer size, Long total, Integer totalPages, Collection<T> items) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.items = items;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Collection<T> getItems() {
        return items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    public static <E extends Serializable> PageResult<E> success(Collection<E> items, Integer current, Integer size, Long total, Integer totalPages) {
        PageResult<E> pageResult = new PageResult<>(current, size, total, totalPages, items);
        pageResult.setSuccess(true);
        return pageResult;
    }
}