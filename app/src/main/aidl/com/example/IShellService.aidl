package com.example;

interface IShellService {
    String exec(String command);
    int getUid();
    void destroy();
}
