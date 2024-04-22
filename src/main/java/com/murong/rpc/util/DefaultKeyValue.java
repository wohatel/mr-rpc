package com.murong.rpc.util;


import lombok.Data;

/**
 * description
 *
 * @author yaochuang 2024/03/22 18:15
 */
@Data
public class DefaultKeyValue<K, V> {
    private K key;
    private V value;

    public DefaultKeyValue() {

    }

    public DefaultKeyValue(K k, V v) {
        this.key = k;
        this.value = v;
    }

}
