/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import lombok.Data;

/**
 *
 * @author lfh
 */
@Data
public class ConfigBean {

    public ConfigBean(String hosts,
                String path,
                Class<?> valueType) {
        this.hosts = hosts;
        this.path = path;
        this.valueType = valueType;
    }

    String hosts;
    String path;
    Class<?> valueType;
}
