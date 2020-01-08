package com.neepu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xyzzg
 * @version 1.0
 * @date 2019-12-10 9:15
 */
@Data
@AllArgsConstructor
public final class EsEntity<T> {

    private String id;
    private T data;
}
