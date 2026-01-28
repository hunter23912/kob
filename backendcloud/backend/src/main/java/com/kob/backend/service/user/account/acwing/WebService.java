package com.kob.backend.service.user.account.acwing;

import com.alibaba.fastjson.JSONObject;

public interface WebService {
    JSONObject applyCode();
    JSONObject receiCode(String code, String state);
}
