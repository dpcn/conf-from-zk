/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 *
 * @author lfh
 */
@CommonsLog
public class ZkWatherUtil {

    /**
     * 监听节点
     *
     * @param zooKeeper
     * @param watcher
     * @param pPath
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static void watch(ZooKeeper zooKeeper, Watcher watcher, String pPath) throws KeeperException, InterruptedException {
        zooKeeper.exists(pPath, watcher);
        zooKeeper.getData(pPath, watcher, null);
        zooKeeper.getChildren(pPath, watcher);
    }

    /**
     * 递归监听所有子节点
     *
     * @param zooKeeper
     * @param watcher
     * @param pPath
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static void watchChilds(ZooKeeper zooKeeper, Watcher watcher, String pPath) throws KeeperException, InterruptedException {
        watch(zooKeeper, watcher, pPath);
        List<String> childs = zooKeeper.getChildren(pPath, null);
        if (childs != null && !childs.isEmpty()) {
            for (String child : childs) {
                log.debug("child=" + child);
                String cpatch = pPath + "/" + child;
                log.debug("cpatch=" + cpatch);
                watchChilds(zooKeeper, watcher, cpatch);
            }
        }
    }

    public static Object getData(TreeCache treeCache, String pPath, String encode) throws IOException, KeeperException, InterruptedException {
        log.debug("pPath=" + pPath);
        ChildData cdata = treeCache.getCurrentData(pPath);
        log.debug("cdata=" + cdata);
        byte[] data = cdata.getData();
        log.debug("data=" + data);
        String datas = new String(data, encode);
        Object ret = datas;
        if (datas != null && !datas.trim().isEmpty()) {
            datas = datas.trim();
            if (datas.startsWith("{") && datas.endsWith("}")) {
                Map<String, Object> map = JsonUtil.toJavaBean(datas, Map.class);
                ret = map;
            } else if (datas.startsWith("[") && datas.endsWith("]")) {
                Collection<Object> ocoll = JsonUtil.toJavaBean(datas, Collection.class);
                ret = ocoll;
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object fromZkChildMain(TreeCache treeCache, String pPath, String encode) throws KeeperException, InterruptedException, IOException {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        Map<String, ChildData> childs = treeCache.getCurrentChildren(pPath);
        log.debug("childs=" + childs);
        if (childs != null && !childs.isEmpty()) {
            ret = new HashMap<>();
            Map<String, Object> map = (Map<String, Object>) ret;
            for (Map.Entry<String, ChildData> childEN : childs.entrySet()) {
                String child = childEN.getKey();
                log.debug("child=" + child);
                String cpatch = pPath + "/" + child;
                log.debug("cpatch=" + cpatch);
                map.put(child, fromZkChild(treeCache, cpatch, encode));
            }
        }
        Object data = getData(treeCache, pPath, encode);
        //最后解析自身
        if (data != null) {
            if (data instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.putAll(map);
                } else {
                    ret = map;
                }
                log.debug("data instanceof Map  ret=" + ret);
            } else if (data instanceof Collection) {
                Collection<Object> ocoll = (Collection<Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                } else {
                    ret = ocoll;
                }
                log.debug(" else if (data instanceof Collection)   ret=" + ret);
            } else if (ret != null) {
                Map<String, Object> retMap = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                retMap.put(key, data);
            } else {
                ret = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                map.put(key, data);
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object fromZkChild(TreeCache treeCache, String pPath, String encode) throws KeeperException, InterruptedException, IOException {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        Map<String, ChildData> childs = treeCache.getCurrentChildren(pPath);
        log.debug("childs=" + childs);
        if (childs != null && !childs.isEmpty()) {
            ret = new HashMap<>();
            Map<String, Object> map = (Map<String, Object>) ret;
            for (Map.Entry<String, ChildData> childEN : childs.entrySet()) {
                String child = childEN.getKey();
                log.debug("child=" + child);
                String cpatch = pPath + "/" + child;
                log.debug("cpatch=" + cpatch);
                map.put(child, fromZkChild(treeCache, cpatch, encode));
            }
        }
        Object data = getData(treeCache, pPath, encode);
        //最后解析自身
        if (data != null) {
            if (data instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.putAll(map);
                } else {
                    ret = map;
                }
                log.debug("data instanceof Map  ret=" + ret);
            } else if (data instanceof Collection) {
                Collection<Object> ocoll = (Collection<Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                } else {
                    ret = ocoll;
                }
                log.debug(" else if (data instanceof Collection)   ret=" + ret);
            } else if (ret != null) {
                Map<String, Object> retMap = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                retMap.put(key, data);
            } else {
                ret = data;
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object getData(ZooKeeper zooKeeper, String pPath) throws IOException, KeeperException, InterruptedException {
        byte[] data = zooKeeper.getData(pPath, null, null);
        log.debug("data=" + data);
        String datas = new String(data);
        Object ret = datas;
        if (datas != null && !datas.trim().isEmpty()) {
            datas = datas.trim();
            if (datas.startsWith("{") && datas.endsWith("}")) {
                Map<String, Object> map = JsonUtil.toJavaBean(datas, Map.class);
                ret = map;
            } else if (datas.startsWith("[") && datas.endsWith("]")) {
                Collection<Object> ocoll = JsonUtil.toJavaBean(datas, Collection.class);
                ret = ocoll;
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object fromZkChildMain(ZooKeeper zooKeeper, String pPath) throws KeeperException, InterruptedException, IOException {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        List<String> childs = zooKeeper.getChildren(pPath, null);
        if (childs != null && !childs.isEmpty()) {
            ret = new HashMap<>();
            Map<String, Object> map = (Map<String, Object>) ret;
            for (String child : childs) {
                log.debug("child=" + child);
                String cpatch = pPath + "/" + child;
                log.debug("cpatch=" + cpatch);
                map.put(child, fromZkChild(zooKeeper, cpatch));
            }
        }
        Object data = getData(zooKeeper, pPath);
        //最后解析自身
        if (data != null) {
            if (data instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.putAll(map);
                } else {
                    ret = map;
                }
                log.debug("data instanceof Map  ret=" + ret);
            } else if (data instanceof Collection) {
                Collection<Object> ocoll = (Collection<Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                } else {
                    ret = ocoll;
                }
                log.debug(" else if (data instanceof Collection)   ret=" + ret);
            } else if (ret != null) {
                Map<String, Object> retMap = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                retMap.put(key, data);
            } else {
                ret = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                map.put(key, data);
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object fromZkChild(ZooKeeper zooKeeper, String pPath) throws KeeperException, InterruptedException, IOException {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        List<String> childs = zooKeeper.getChildren(pPath, null);
        if (childs != null && !childs.isEmpty()) {
            ret = new HashMap<>();
            Map<String, Object> map = (Map<String, Object>) ret;
            for (String child : childs) {
                log.debug("child=" + child);
                String cpatch = pPath + "/" + child;
                log.debug("cpatch=" + cpatch);
                map.put(child, fromZkChild(zooKeeper, cpatch));
            }
        }
        Object data = getData(zooKeeper, pPath);
        //最后解析自身
        if (data != null) {
            if (data instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.putAll(map);
                } else {
                    ret = map;
                }
                log.debug("data instanceof Map  ret=" + ret);
            } else if (data instanceof Collection) {
                Collection<Object> ocoll = (Collection<Object>) data;
                if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                } else {
                    ret = ocoll;
                }
                log.debug(" else if (data instanceof Collection)   ret=" + ret);
            } else if (ret != null) {
                Map<String, Object> retMap = (Map<String, Object>) ret;
                String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                log.debug(" else if (ret != null)   key=" + key);
                retMap.put(key, data);
            } else {
                ret = data;
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object getData(CuratorFramework zooKeeper, String pPath, String encode) throws IOException, KeeperException, InterruptedException, Exception {
        byte[] data = zooKeeper.getData().forPath(pPath);
        log.debug("data=" + data);
        String datas = new String(data, encode);
        log.debug("datas=" + datas);
        Object ret = datas;
        if (datas != null && !datas.trim().isEmpty()) {
            datas = datas.trim();
            if (datas.startsWith("{") && datas.endsWith("}")) {
                Map<String, Object> map = JsonUtil.toJavaBean(datas, Map.class);
                ret = map;
            } else if (datas.startsWith("[") && datas.endsWith("]")) {
                Collection<Object> ocoll = JsonUtil.toJavaBean(datas, Collection.class);
                ret = ocoll;
            }
        }
        log.debug("ret=" + ret);
        return ret;
    }

    public static Object fromZkChild(CuratorFramework zooKeeper, String pPath, String encode) throws KeeperException, InterruptedException, IOException, Exception {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        Stat exists = zooKeeper.checkExists().forPath(pPath);
        if (exists != null) {
            List<String> childs = zooKeeper.getChildren().forPath(pPath);
            if (childs != null && !childs.isEmpty()) {
                ret = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) ret;
                for (String child : childs) {
                    log.debug("child=" + child);
                    String cpatch = pPath + "/" + child;
                    log.debug("cpatch=" + cpatch);
                    map.put(child, fromZkChild(zooKeeper, cpatch, encode));
                }
            }
            Object data = getData(zooKeeper, pPath, encode);
            //最后解析自身
            if (data != null) {
                if (data instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) data;
                    if (ret != null) {
                        Map<String, Object> retMap = (Map<String, Object>) ret;
                        retMap.putAll(map);
                    } else {
                        ret = map;
                    }
                    log.debug("data instanceof Map  ret=" + ret);
                } else if (data instanceof Collection) {
                    Collection<Object> ocoll = (Collection<Object>) data;
                    if (ret != null) {
                        Map<String, Object> retMap = (Map<String, Object>) ret;
                        retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                    } else {
                        ret = ocoll;
                    }
                    log.debug(" else if (data instanceof Collection)   ret=" + ret);
                } else if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                    log.debug(" else if (ret != null)   key=" + key);
                    retMap.put(key, data);
                } else {
                    ret = data;
                }
            }
            log.debug("ret=" + ret);
        }
        return ret;
    }

    public static Object fromZkChildMain(CuratorFramework zooKeeper, String pPath, String encode) throws KeeperException, InterruptedException, IOException, Exception {
        Object ret = null;
        log.debug("path=" + pPath);
        //先解析子节点
        Stat exists = zooKeeper.checkExists().forPath(pPath);
        if (exists != null) {
            List<String> childs = zooKeeper.getChildren().forPath(pPath);
            if (childs != null && !childs.isEmpty()) {
                ret = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) ret;
                for (String child : childs) {
                    log.debug("child=" + child);
                    String cpatch = pPath + "/" + child;
                    log.debug("cpatch=" + cpatch);
                    map.put(child, fromZkChild(zooKeeper, cpatch, encode));
                }
            }
            Object data = getData(zooKeeper, pPath, encode);
            //最后解析自身
            if (data != null) {
                if (data instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) data;
                    if (ret != null) {
                        Map<String, Object> retMap = (Map<String, Object>) ret;
                        retMap.putAll(map);
                    } else {
                        ret = map;
                    }
                    log.debug("data instanceof Map  ret=" + ret);
                } else if (data instanceof Collection) {
                    Collection<Object> ocoll = (Collection<Object>) data;
                    if (ret != null) {
                        Map<String, Object> retMap = (Map<String, Object>) ret;
                        retMap.put(pPath.substring(pPath.lastIndexOf("/")), ocoll);
                    } else {
                        ret = ocoll;
                    }
                    log.debug(" else if (data instanceof Collection)   ret=" + ret);
                } else if (ret != null) {
                    Map<String, Object> retMap = (Map<String, Object>) ret;
                    String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                    log.debug(" else if (ret != null)   key=" + key);
                    retMap.put(key, data);
                } else {
                    ret = new HashMap<>();
                    Map<String, Object> map = (Map<String, Object>) ret;
                    String key = pPath.substring(pPath.lastIndexOf("/") + 1);
                    log.debug(" else if (ret != null)   key=" + key);
                    map.put(key, data);
                }
            }
            log.debug("ret=" + ret);
        }
        return ret;
    }

}
