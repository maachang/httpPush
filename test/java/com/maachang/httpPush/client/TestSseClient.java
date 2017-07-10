package com.maachang.httpPush.client;

public class TestSseClient {
    protected TestSseClient(){}
    
    public static final void main(String[] args) throws Exception {
        String uuid = "0000015d-2890-fcf4-0000-000000000001";
        
        SseClient client = new SseClient("localhost",3332);
        
        client.connect(uuid, new HttpPushCallback() {
            public void onMessage(PushData data) {
                System.out.println(">data:" + data.getData());
            }
            public void onError(String message,Throwable t) {
                System.out.println(">error:" + message);
                t.printStackTrace();
            }
        });
        
        while(true) {
            Thread.sleep(100L);
        }
    }
}
