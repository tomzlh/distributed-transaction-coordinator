package com.ops.sc.compensator.service;


public interface CleanService {
    void cleanTransData(Long tid);

    void cleanGroupDataAsync(String groupId);
}
