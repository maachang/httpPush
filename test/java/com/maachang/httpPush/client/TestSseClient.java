package com.maachang.httpPush.client;

import com.maachang.httpPush.data.PushData;

public class TestSseClient {
    protected TestSseClient(){}
    
    public static final void main(String[] args) throws Exception {
        String uuid = "0000015d-386e-bea4-0000-000000000001";
        
        SseClient client = new SseClient(true,"push.maachang.com");
        
        client.connect(uuid, new SseCallback() {
            public void onMessage(PushData data) {
                System.out.println(">data:" + data.getData());
            }
            public void onError(String message,Throwable t) {
                System.out.println(">error:" + message);
                t.printStackTrace();
            }
        });
        
        while(client.isConnect()) {
            Thread.sleep(100L);
        }
    }
}
