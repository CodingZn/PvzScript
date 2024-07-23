package src.api;

import java.io.Serializable;

public class Settings implements Serializable {
    public int amf_interval;
    public int block_interval;
    public double buxie_threshold;
    public boolean kpfull;
    public int max_level;
    public boolean proxy;
    public int proxy_port;
    public int req_interval;
    public int retry_count;
    public int retry_interval;
    public int timeout;
    public int xp_di;
    public int xp_gao;
    public int xp_zhong;
    
    public static final String PATH="userdata/settings";

    public static boolean save(Settings data){
        return Util.saveObject(data, PATH);
    }

    public static Settings load(){
        Settings c= (Settings) Util.loadObject(PATH);
        if (c==null){
            c=getCurrentSettings();
        }
        return c;
    }

    private Settings(){}

    public static Settings getCurrentSettings(){
        Settings res= new Settings();
        res.amf_interval=Request.waitAmfTime;
        res.block_interval=Request.wait2441Time;
        res.buxie_threshold=BuXie.getThreshold();
        res.kpfull=Battle.kpFull;
        res.max_level=Battle.maxLevel;
        res.proxy=Request.useProxy;
        res.proxy_port=Request.proxyPort;
        res.req_interval=Request.reqInterval;
        res.retry_count=Request.retryMaxCount;
        res.retry_interval=Request.retryInterval;
        res.timeout=Request.timeout;
        res.xp_di=BuXie.low_reserve;
        res.xp_gao=BuXie.high_reserve;
        res.xp_zhong=BuXie.mid_reserve;
        return res;
    }
}
