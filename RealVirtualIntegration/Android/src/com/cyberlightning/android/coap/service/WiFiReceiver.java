package com.cyberlightning.android.coap.service;

import com.cyberlightning.android.coap.Application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

/*
* A BroadcastReceiver that notifies of important Wi-Fi p2p events.
*/
public class WiFiReceiver extends BroadcastReceiver {

   private WifiP2pManager mManager;
   private Channel mChannel;
   private Application mActivity;

   public WiFiReceiver(WifiP2pManager manager, Channel channel,Application activity) {
       super();
       this.mManager = manager;
       this.mChannel = channel;
       this.mActivity = activity;
   }

   @Override
   public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();

       if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
           // Check to see if Wi-Fi is enabled and notify appropriate activity
       } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
           // Call WifiP2pManager.requestPeers() to get a list of current peers
       } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
           // Respond to new connection or disconnections
       } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
           // Respond to this device's wifi state changing
       }
   }


}