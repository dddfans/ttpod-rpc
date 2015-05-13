package com.ttpod.rpc.netty.pool.impl;

import com.ttpod.rpc.pool.GroupManager;
import com.ttpod.rpc.pool.GroupMemberObserver;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *
 * Not Support Retry When KeeperState.Expired .
 *
 * @see com.ttpod.rpc.netty.pool.impl.CuratorGroupManager
 *
 * date: 14-2-13 下午6:40
 *
 * @author: yangyang.cong@ttpod.com
 */
@Deprecated
public class DefaultGroupManager implements Runnable,GroupManager {


    static final Logger logger = LoggerFactory.getLogger(DefaultGroupManager.class);

    ZooKeeper zk;
    String groupName;
    GroupMemberObserver montior;
    ExecutorService exe;

    public DefaultGroupManager(String zkAddress, String groupName){
        this(Zoo.connect(zkAddress),groupName,null);
    }
    public DefaultGroupManager(ZooKeeper zk, String groupName,GroupMemberObserver montior) {
        this.zk = zk;
        this.groupName = Zoo.flipPath(groupName);
        this.montior = montior;
        boolean successCreate =   Zoo.syncCreate(this.groupName,zk);

        assert successCreate;
        if(montior != null){
            exe = Executors.newSingleThreadExecutor();
            exe.execute(this);
//            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//                public void run() {
//                    exe.shutdownNow();
//                }
//            }));
        }
    }

    public String join(String memberName, byte[] data){
        String path =  buildPath( memberName );
         try {
             return zk.create(path,
                     data,
                     ZooDefs.Ids.OPEN_ACL_UNSAFE,
                     CreateMode.EPHEMERAL);
         } catch (KeeperException | InterruptedException e) {
             logger.error(memberName+ " join group " + groupName +" Faild !!!",e);
             throw new RuntimeException(memberName+ " join group " + groupName +" Faild !!!",e);
         }
     }

    @Override
    public void leave(String memberName) throws Exception {
        zk.delete(buildPath(memberName),-1);
    }

    @Override
    public String name() {
        return groupName;
    }

    @Override
    public void shutdown() {
        if( null != exe ) exe.shutdownNow();
        if(null != zk) try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


//    void beginMontior() throws InterruptedException {
//        zk.getChildren(groupName,false, new AsyncCallback.ChildrenCallback() {
//            @Override
//            public void processResult(int rc, String path, Object ctx, List<String> children) {
//                if(rc == 0){
//                    montior.onChange(children);
//                    semaphore.release();
//                }
//            }
//        },null);
//    }


    private void montior() throws InterruptedException {

        try {
            List<String> children = zk.getChildren(groupName, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        semaphore.release();
                    }
                }
            });
            montior.onChange(children);
            if (children.isEmpty()) {
                logger.error("No members in group {} . Service maybe Unavailable !!!! ", groupName);
            }
        } catch (KeeperException e) {
            logger.error("GroupMemberObserver catch KeeperException from montior group: " + groupName,e);
            throw new RuntimeException("list catch KeeperException",e);
        }

    }
    private Semaphore semaphore = new Semaphore(1);

    public void run(){
        try {
            semaphore.acquire();
            while (true) {
                montior();
                semaphore.acquire();
            }
        } catch (InterruptedException e) {
            logger.error("GroupMember Montior maybe canceled of group: "+  groupName,e);
        }

    }

    void deleteAllMember()
            throws KeeperException, InterruptedException {
        try {
            List<String> children = zk.getChildren(groupName, false);
            for (String child : children) {
                zk.delete(buildPath( child), -1);
            }
//            zk.delete(path, -1);
        }
        catch (KeeperException.NoNodeException e) {
            logger.error("Group {} does not exist !!! ",  groupName);
//            System.out.printf("Group %s does not exist\n", groupName);
        }
    }

    protected String buildPath(String memberName){
        return groupName + "/" + memberName;
    }


}
