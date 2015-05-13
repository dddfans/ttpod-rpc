package test;

import com.ttpod.rpc.InnerBindUtil;
import com.ttpod.rpc.RequestBean;
import com.ttpod.rpc.ResponseBean;
import com.ttpod.rpc.client.ClientHandler;
import com.ttpod.rpc.client.OutstandingContainer;
import com.ttpod.rpc.netty.Client;
import com.ttpod.rpc.netty.client.DefaultClientHandler;
import com.ttpod.rpc.netty.client.DefaultClientInitializer;
import com.ttpod.rpc.netty.pool.CloseableChannelFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO Comment here.
 * date: 14-2-14 下午7:02
 *
 * @author: yangyang.cong@ttpod.com
 */
public class RealSearch {

    public static void main(String[] args) throws Exception {
        CloseableChannelFactory client = new Client(
                new InetSocketAddress("192.168.8.12", 16000), new DefaultClientInitializer());
        // Read commands from the stdin.
        final ClientHandler handler = client.newChannel().pipeline().get(DefaultClientHandler.class);
        final ClientHandler handler2 = client.newChannel().pipeline().get(DefaultClientHandler.class);
        final ClientHandler handler3 = client.newChannel().pipeline().get(DefaultClientHandler.class);
        final ClientHandler handler4 = client.newChannel().pipeline().get(DefaultClientHandler.class);
        ClientHandler[] handlers = {handler,handler2,handler3,handler4};
//      final ClientHandler handler = client.newChannel().pipeline().get(DefaultClientHandler.class);
        Executor exe = Executors.newFixedThreadPool(4);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            String line = in.readLine().trim();

            if(line.matches("^\\d+$")){
                System.out.println("println begin " + line );
                long l = System.currentTimeMillis();
                int code = 0;
                for(int i = 0 , total =Integer.parseInt(line) ; i < total;i++) {
                    RequestBean<String> req = new RequestBean<>((byte) 1, (short) 1, (short) 10, "she");
                    code += handlers[i&3].rpc(req).getCode();
                }
                System.out.println("end ,cost  " + ( System.currentTimeMillis() - l) + "ms,code : " +code );
            }else {
                RequestBean<String> req = new RequestBean<>((byte) 1, (short) 1, (short) 10, line);
                ResponseBean res = handler.rpc(req);
                System.out.println(line + "  ->  rpc[" + InnerBindUtil.id(req) + "] -> " + res);
            }
            if ("bye".equals(line)) {
                client.shutdown();
                break;
            }
        }

    }
}
