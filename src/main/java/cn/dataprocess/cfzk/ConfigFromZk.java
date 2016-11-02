/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.zookeeper.KeeperException;

/**
 *
 * @author lfh
 */
public class ConfigFromZk {

    public static int TimeOut = 3000;

    private Map<ConfigBean, ZkWatcher> configPool = null;

    private ConfigFromZk() {
        configPool = new HashMap<>();
    }

    private static class ConfigFromZkHolder {

        private static final ConfigFromZk INSTANCE = new ConfigFromZk();
    }

    public static final ConfigFromZk getInstance() {
        return ConfigFromZkHolder.INSTANCE;
    }

    private synchronized <T> void initConfig(ConfigBean cb, Class<T> valueType)
                throws InstantiationException, IllegalAccessException,
                IOException, KeeperException, InterruptedException, Exception {
        if (configPool.containsKey(cb)) {
            return;
        }
        T obj = valueType.newInstance();
        ZkWatcher zkw = new ZkWatcher(cb.getHosts(), TimeOut, cb.getPath(), obj);
        configPool.put(cb, zkw);
    }

    public <T> T getConfig(String host, String path, Class<T> valueType)
                throws InstantiationException, IllegalAccessException, KeeperException,
                IOException, InterruptedException, Exception {
        T ret = null;
        ConfigBean cb = new ConfigBean(host, path, valueType);
        ZkWatcher zkw = configPool.get(cb);
        if (zkw != null) {
            ret = (T) zkw.getWob();
        } else {
            initConfig(cb, valueType);
            zkw = configPool.get(cb);
            ret = (T) zkw.getWob();
        }
        return ret;
    }

}
