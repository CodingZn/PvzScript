package src.api;

import java.io.File;
import java.io.Serializable;

public class Settings implements Serializable {
    // Request
    public int amf_interval;
    public int block_interval;
    public boolean proxy;
    public int proxy_port;
    public int req_interval;
    public int retry_count;
    public int retry_interval;
    public int timeout;
    public int wait302Time;

    // Battle
    public boolean kpfull;
    public int max_level;
    public int auto_book;
    public int auto_advbook;
    public int update_freq;

    // BuXie
    public double buxie_threshold;
    public int xp_di;
    public int xp_gao;
    public int xp_zhong;
    // public int cave_delta;
    
    public static final String PATH="userdata/settings";

    public static boolean save(Settings data){
        return Util.saveObject(data, PATH);
    }

    public static Settings load(){
        Settings c= (Settings) Util.loadObject(PATH,false);
        if (c==null){
            c=getCurrentSettings();
        }
        return c;
    }

    public static void apply(Settings data){
        if (data==null) { return;}
        Request.setAMFBlockTime(data.amf_interval);
        Request.setBlockTime(data.block_interval);
        Request.changeProxy(data.proxy);
        Request.setProxyPort(data.proxy_port);
        Request.setInterval(data.req_interval);
        Request.setRetry(data.retry_count, data.retry_interval);
        Request.setTimeout(data.timeout);
        Request.set302Wait(data.wait302Time);

        Battle.setKeepFull(data.kpfull);
        Battle.setMaxLevel(data.max_level);
        Battle.setAutoBook(data.auto_book);
        Battle.setAutoAdvBook(data.auto_advbook);
        Battle.setUpdateFreq(data.update_freq);

        BuXie.setThreshold(data.buxie_threshold);
        BuXie.setReserve(data.xp_di, data.xp_zhong, data.xp_gao);
    }

    public static void clear(){
        new File(PATH).delete();
    }

    private Settings(){}

    public static Settings getCurrentSettings(){
        Settings res= new Settings();
        res.amf_interval=Request.waitAmfTime;
        res.block_interval=Request.wait2441Time;
        res.proxy=Request.useProxy;
        res.proxy_port=Request.proxyPort;
        res.req_interval=Request.reqInterval;
        res.retry_count=Request.retryMaxCount;
        res.retry_interval=Request.retryInterval;
        res.timeout=Request.timeout;
        res.wait302Time=Request.wait302Time;

        res.kpfull=Battle.kpFull;
        res.max_level=Battle.maxLevel;
        res.auto_book=Battle.autobook;
        res.auto_advbook=Battle.autoAdvBook;
        res.update_freq=Battle.updateFreq;
        
        res.buxie_threshold=BuXie.getThreshold();
        res.xp_di=BuXie.low_reserve;
        res.xp_gao=BuXie.high_reserve;
        res.xp_zhong=BuXie.mid_reserve;
        return res;
    }

    // public void setCaveDelta(int v){
    //     this.cave_delta = v;
    // }

}
