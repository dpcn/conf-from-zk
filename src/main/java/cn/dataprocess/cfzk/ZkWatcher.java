/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import java.io.IOException;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import net.sf.cglib.beans.BeanCopier;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

/**
 *
 * @author lfh
 * @param <T>
 */
@CommonsLog
@Data
public class ZkWatcher<T> implements Watcher {
    
    private String hosts;
    private int timeout;
    private String path;
    private T wob;
    protected ZooKeeper zooKeeper;
    private BeanCopier copier = null;

    public ZkWatcher(String hosts, int timeout, String path, T wob) throws IOException, KeeperException, InterruptedException {
        this.hosts = hosts;
        this.timeout = timeout;
        this.path = path;
        this.wob = wob;
        copier = BeanCopier.create(wob.getClass(), wob.getClass(), false);
        zooKeeper = new ZooKeeper(hosts, timeout, this); 
        ZkWatherUtil.watchChilds(zooKeeper, this, path);
        refresh();
    }

    public void refresh() throws KeeperException, InterruptedException, IOException {
        Object nmap = ZkWatherUtil.fromZkChild(this.zooKeeper, this.path);
        String ns = JsonUtil.toJsonString(nmap);
        Object nob = JsonUtil.toJavaBean(ns, this.wob.getClass());
        T nt = (T) nob;
        copier.copy(nt, this.wob, null);
    }
    
    public void checkConnection(WatchedEvent event) throws IOException{
        KeeperState zkstate = event.getState(); 
         if( KeeperState.Expired ==  zkstate ) {
             this.zooKeeper = new ZooKeeper(hosts, timeout, this);
         }
         else  if( KeeperState.Disconnected ==  zkstate ) {
             this.zooKeeper = new ZooKeeper(hosts, timeout, this);
         }
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            checkConnection(event);
        } catch (IOException ex) {
            log.error(ex);
        }
        log.debug("event=" + event.getType());
        Event.EventType eType = event.getType();
        if (eType.equals(Event.EventType.None)) {
            return;
        }
        //先判断路径
        String cPath = event.getPath();
        if (!cPath.contains(this.path)) {
            return;
        }
        try {
            checkConnection(event); 
            //重新注册
            ZkWatherUtil.watchChilds(zooKeeper, this, path);
            refresh();
        } catch (KeeperException | InterruptedException | IOException ex) {
            log.error(ex);
        }
    }

}
