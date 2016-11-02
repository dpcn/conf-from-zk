/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import java.io.IOException;
import java.util.Map;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import net.sf.cglib.beans.BeanCopier;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.Watcher.Event;

/**
 *
 * @author lfh
 * @param <T>
 */
@CommonsLog
@Data
public class ZkWatcher<T> implements TreeCacheListener {

    public static final String ENCODE = "UTF-8";
    private String hosts;
    private int timeout;
    private String path;
    private T wob;
    protected CuratorFramework zkclient = null;
    private BeanCopier copier = null;
    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    private TreeCache treeCache = null;

    public ZkWatcher(String hosts, int timeout, String path, T wob) throws IOException, KeeperException, InterruptedException, Exception {
        this.hosts = hosts;
        this.timeout = timeout;
        this.path = path;
        this.wob = wob;
        copier = BeanCopier.create(wob.getClass(), wob.getClass(), false);
        zkclient = CuratorFrameworkFactory.builder()
                    .connectString(hosts)
                    .sessionTimeoutMs(5000)
                    .retryPolicy(retryPolicy)
                    // .namespace(path)
                    .build();
        zkclient.start();
        treeCache = new TreeCache(zkclient, path);
        treeCache.getListenable().addListener(this);
        treeCache.start();
        log.debug("treeCache=" + treeCache);
        init();
    }

    public void init() throws KeeperException, InterruptedException, IOException, Exception {
        Object nmap = ZkWatherUtil.fromZkChildMain(this.zkclient, this.path, ENCODE);
        String ns = JsonUtil.toJsonString(nmap);
        Object nob = JsonUtil.toJavaBean(ns, this.wob.getClass());
        T nt = (T) nob;
        log.debug("nt=" + nt);
        if (nt != null) {
            copier.copy(nt, this.wob, null);
        }
    }

    public void refresh() throws KeeperException, InterruptedException, IOException {
        Object nmap = ZkWatherUtil.fromZkChildMain(this.treeCache, this.path, ENCODE);
        String ns = JsonUtil.toJsonString(nmap);
        Object nob = JsonUtil.toJavaBean(ns, this.wob.getClass());
        T nt = (T) nob;
        log.debug("nt=" + nt);
        copier.copy(nt, this.wob, null);
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        refresh();
    }

}
