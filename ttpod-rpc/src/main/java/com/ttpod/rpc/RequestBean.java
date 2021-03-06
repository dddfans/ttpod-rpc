package com.ttpod.rpc;

/**
 * date: 14-1-28 上午11:31
 *
 * @author: yangyang.cong@ttpod.com
 */
public class RequestBean<Data> {



    public static final byte NAME_SERVICE = 0;
    public static final byte FIRST_SERVICE = 1;
    public static final byte SECOND_SERVICE = 2;
    public static final byte THIRD_SERVICE = 3;



    public static final short UNSIGNED_BYTE_MAX_SIZE = 0xff;
    public static final short DEFAULT_SIZE = (short)Math.max(5,Integer.getInteger("RequestBean.size",5)&UNSIGNED_BYTE_MAX_SIZE);

    byte service ;
    short page  = 1;// unsigned byte. MAX VALUE =0xff
    short size  = DEFAULT_SIZE;// unsigned byte.
    short _req_id;
    Data data ;

    public RequestBean(){
    }

    public RequestBean(Enum service, short page, short size, Data data) {
        this((byte) service.ordinal(),page,size,data);
    }

    public RequestBean(byte service, short page, short size, Data data) {
        this();
        this.service = service;
        this.page = page;
        this.size = size;
        this.data = data;
    }

    public byte getService() {
        return service;
    }

    public void setService(byte service) {
        this.service = service;
    }

    public short getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = (short)page;
    }
    public void setPage(short page) {
        this.page = page;
    }

    public short getSize() {
        return size;
    }

    public void setSize(short size) {
        this.size = size;
    }
    public void setSize(int size) {
        this.size = (short)size;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RequestBean{" +
                "_req_id=" + _req_id +
                ", service=" + service +
                ", page=" + page +
                ", size=" + size +
                ", data='" + data + '\'' +
                '}';
    }

    public int safePage(){
        return Math.max(page,1);
    }

    public int safeSize(){
        return Math.max(size,1);
    }


    public static <Data>RequestBean<Data>  req(Enum service){
        RequestBean<Data> req = new RequestBean<>();
        req.setService((byte)service.ordinal());
        return req;
    }

}
