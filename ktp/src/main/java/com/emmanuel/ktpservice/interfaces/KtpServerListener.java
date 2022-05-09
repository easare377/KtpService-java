package com.emmanuel.ktpservice.interfaces;


import com.emmanuel.ktpservice.KtpClient;

public interface KtpServerListener {
    void onClientConnected(KtpClient client);
}
