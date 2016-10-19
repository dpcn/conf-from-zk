# conf-from-zk
一个从zookeeper获取数据转为指定javabean，并根据zookeeper时间自动更新本地javabean的工具。
# 使用方法
@Data

public class TestBean {

    private String testzk ;
    
    private String t1 ; 
    
}

ConfigFromZk CFZK = ConfigFromZk.getInstance();

TestBean wob = CFZK.getConfig("127.0.0.1:2181", "/testzk", TestBean.class);

如果zookeeper对应的路径变动，那么wob会自动同步，无需额外代码同步。

自测结果：
zookeeper中的数据：
/testzk 1
/testzk/t1 2

 runner wob=TestBean(testzk=1, t1=2) 
 
 修改zookeeper中的数据后：
 /testzk 3
/testzk/t1 4
 
runner wob=TestBean(testzk=3, t1=4) 