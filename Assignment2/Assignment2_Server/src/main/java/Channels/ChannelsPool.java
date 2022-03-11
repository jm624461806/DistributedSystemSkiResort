package Channels;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ChannelsPool {
    private GenericObjectPool<Channel> channelsPool;
    public static GenericObjectPoolConfig defaultConfig;

    /*
    static {
        defaultConfig = new GenericObjectPoolConfig();
        defaultConfig.setMaxTotal(500);
        defaultConfig.setBlockWhenExhausted(false);
    }*/

    public ChannelsPool() {
        this.channelsPool = new GenericObjectPool<Channel>(new ChannelsFactory("34.210.159.67"));
    }

    public Channel getChannel() {
        Channel channel = null;
        try {
            channel = this.channelsPool.borrowObject();
        }
         catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    public void returnChannel(Channel channel) {
        try {
            this.channelsPool.returnObject(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
