/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import java.io.IOException;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.zookeeper.KeeperException;

/**
 *
 * @author lfh
 */
@CommonsLog
public class ZkWatcherTest implements Runnable {

    private ConfigFromZk CFZK = ConfigFromZk.getInstance();
    TestBean wob = null;

    public ZkWatcherTest(String hosts, int timeout, String path) throws IOException, KeeperException, InterruptedException, InstantiationException, IllegalAccessException {
        wob = CFZK.getConfig(hosts, path, TestBean.class);
    }

    public static void main(String args[]) throws InterruptedException, IOException, KeeperException, InstantiationException, IllegalAccessException {
        String host = "192.168.2.215:2181,192.168.2.216:2181,192.168.2.217:2181";
        int timeout = 3000;
        String path = "/testzk";
        ZkWatcherTest zkw = new ZkWatcherTest(host, timeout, path);
        TestBean tb = zkw.get();
        Thread th = new Thread(zkw);
        th.start();
        while (true) {
            Thread.sleep(1000);
            System.out.println(" ext tb=" + tb);
        }
    }

    public TestBean get() {
        return wob;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println(" runner wob=" + wob);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                log.error(ex);
            }
        }
    }

}
