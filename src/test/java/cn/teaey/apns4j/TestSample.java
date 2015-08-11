package cn.teaey.apns4j;

import cn.teaey.apns4j.keystore.KeyStoreWrapper;
import cn.teaey.apns4j.network.SecurityConnection;
import cn.teaey.apns4j.protocol.NotifyPayload;
import cn.teaey.feva.common.Interval;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Teaey
 * @date 13-8-30
 */
public class TestSample {
    //设置一个私钥密码
    private final String keyStorePasswd = "xxxx";
    //设置自己的需要推送的devicetoken（测试用）
    private final String deviceTokenString = "968b6a792359fdaef520f7089cb717a63a77961affe311d0aae6d0b1290b9d58";

    /**
     * Very Simple to use APNS4j
     */
    @Test
    public void rawInvoke() {
        //create & init a notify payload
        NotifyPayload notifyPayload = Apns4j.buildNotifyPayload()
                .alert(Long.toString(System.currentTimeMillis()))
                .badge(2);
        //notifyPayload.sound("default").alertBody("Pushed By \\\" apns4j").alertActionLocKey("Button Text");

        //get a keystore
        KeyStoreWrapper keyStoreWrapper = Apns4j.buildKeyStoreWraper("iphone_dev.p12", keyStorePasswd);
        //create a ssl socket
        SecurityConnection connection = Apns4j.buildSecurityConnection(keyStoreWrapper, Apns4j.SERVER_DEVELOPMENT);
        Assert.assertNotNull(connection);
        connection.sendAndFlush(deviceTokenString, notifyPayload);
        connection.close();
    }

    @Test
    public void asyncService() throws InterruptedException {
        KeyStoreWrapper keyStoreWrapper = Apns4j.buildKeyStoreWraper("iphone_dev.p12", keyStorePasswd);
        final APNSAsynService asynService = Apns4j.buildAPNSAsynService(4, keyStoreWrapper, Apns4j.SERVER_DEVELOPMENT);
        List<Thread> tList = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1; i++) {
                        NotifyPayload notifyPayload = Apns4j.buildNotifyPayload()
                                .alert("" + System.currentTimeMillis())
                                .badge((int) System.currentTimeMillis() % 100);
                        asynService.sendAndFlush(deviceTokenString, notifyPayload);
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            tList.add(t);
        }
        for (Thread each : tList) {
            each.start();
        }
        for (Thread each : tList) {
            each.join();
        }
        asynService.shutdown();
    }

    @Test
    public void performanceTest() {
        long takeMills = Interval.interval(new Runnable() {
            public void run() {
                try {
                    NotifyPayload notifyPayload = Apns4j.buildNotifyPayload()
                            //.alert("TEST1")
                            //.badge(2)
                            //.sound("default")
                            .alertBody("TEST")
                            .alertActionLocKey("TEST");

                    notifyPayload.toJsonBytes();
                    notifyPayload.toJsonString();
                } catch (Exception e) {
                }
            }
        }, 10000);
        System.out.println("take " + takeMills + "ms");
    }

    /**
     * build KeyStore
     */
    @Test
    public void loadKeyStoreWithClassPath() {
        KeyStoreWrapper keyStore = Apns4j.buildKeyStoreWraper("iphone_dev.p12", keyStorePasswd);
        Assert.assertNotNull(keyStore);
    }

    @Test
    public void loadKeyStoreWithSystemPath() {
        KeyStoreWrapper keyStore = Apns4j.buildKeyStoreWraper("D:/iphone_dev.p12", keyStorePasswd);
        Assert.assertNotNull(keyStore);
    }

    @Test
    public void loadKeyStoreByFile() {
        File f = new File("D:/iphone_dev.p12");
        KeyStoreWrapper keyStore = Apns4j.buildKeyStoreWraper(f, keyStorePasswd);
        Assert.assertNotNull(keyStore);
    }

    @Test
    public void loadKeyStoreByInputStream() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("iphone_dev.p12");
        KeyStoreWrapper keyStore = Apns4j.buildKeyStoreWraper(in, keyStorePasswd);
        Assert.assertNotNull(keyStore);
    }

    @Test
    public void loadKeyStoreByByteArray() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("iphone_dev.p12");
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        KeyStoreWrapper keyStore = Apns4j.buildKeyStoreWraper(bytes, keyStorePasswd);
        Assert.assertNotNull(keyStore);
    }

    /**
     * How to get Connection
     */
    @Test
    public void getConnection() {
        KeyStoreWrapper keyStoreWrapper = Apns4j.buildKeyStoreWraper("iphone_dev.p12", keyStorePasswd);
        SecurityConnection connection = Apns4j.buildSecurityConnection(keyStoreWrapper, Apns4j.SERVER_DEVELOPMENT);
        Assert.assertNotNull(connection);
        connection.close();
    }


}
