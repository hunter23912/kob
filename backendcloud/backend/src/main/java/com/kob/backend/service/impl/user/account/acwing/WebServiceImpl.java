package com.kob.backend.service.impl.user.account.acwing;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.user.account.acwing.utils.HttpClientUtil;
import com.kob.backend.service.user.account.acwing.WebService;
import com.kob.backend.utils.JwtUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class WebServiceImpl implements WebService {
    private static final String appId = "123"; // todo
    private static final String appSecret = "abc"; // todo

    @Value("${acwing.redirect_uri}")
    private String redirectUri; // 点击同意后跳转的地址
    private static final String applyAccessTokenUrl = "https://www.acwing.com/third_party/api/oauth2/access_token/"; // 申请 access_token 的接口
    private static final String getUserInfoUrl = "https://www.acwing.com/third_party/api/meta/identity/getinfo/"; // 获取用户信息的接口
    private static final Random random = new Random();

    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public JSONObject applyCode() {
        JSONObject resp = new JSONObject();
        String encodeUrl; // 用户点击同意后跳转的地址，通常是项目前端地址X，在X中再次请求后端接口，后端在从第三方获取授权码，openid等信息

        encodeUrl = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        StringBuilder state = new StringBuilder(); // 自己生成的随机状态值，防止 CSRF 攻击。传递给第三方，第三方会原封不动地传回给自己
        for (int i = 0; i < 10; i++) {
            state.append((char)(random.nextInt(10) + '0'));
        }
        resp.put("result", "success");
        redisTemplate.opsForValue().set(state.toString(), "true");
        redisTemplate.expire(state.toString(), Duration.ofMinutes(10)); // state 有效期 10 分钟

        String applyCodeUrl = "https://www.acwing.com/third_party/api/oauth2/web/authorize/?" +
                "appid=" + appId +
                "&redirect_uri=" + encodeUrl + // 点击同意后跳转的地址，由第三方决定接口名 redirect_uri，具体地址自己设置，第三方会返回授权码code和原封不动的状态码state
                "&scope=userinfo" +
                "&state=" + state;

        resp.put("apply_code_url", applyCodeUrl); // 选择同意或拒绝的页面

        return resp;
    }

    @Override
    public JSONObject receiCode(String code, String state) {
        JSONObject resp = new JSONObject();
        resp.put("result", "failed");
        if (code == null || state == null) return resp;
        if(Boolean.FALSE.equals(redisTemplate.hasKey(state))) return resp; // 如果state不对，可能是 CSRF 攻击
        redisTemplate.delete(state); // state 只能用一次，防止重放攻击
        List<NameValuePair> nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("appid", appId)); // 应用 ID
        nameValuePairs.add(new BasicNameValuePair("secret", appSecret)); // 应用密钥
        nameValuePairs.add(new BasicNameValuePair("code", code)); // 授权码

        // 将授权码、appid,secret发送给第三方，并从第三方获取 access_token 和 openid
        String getString = HttpClientUtil.get(applyAccessTokenUrl, nameValuePairs); // 发送 GET 请求，一个异步操作
        if (getString == null) return resp;
        JSONObject getResp = JSONObject.parseObject(getString); // 将字符串转换为 JSON 对象

        // 从第三方返回的信息中提取 access_token 和 openid
        String accessToken = getResp.getString("access_token"); // 返回访问令牌和openid
        String openid = getResp.getString("openid");
        if (accessToken == null || openid == null) return resp;

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        List<User> users = userMapper.selectList(queryWrapper); // 查询数据库中是否存在该 openid 的用户

        // 如果用户存在，直接生成 JWT 并返回
        if (!users.isEmpty()) {
            User user  = users.getFirst();
            String jwt = JwtUtil.createJWT(user.getId().toString());

            resp.put("result", "success");
            resp.put("jwt_token", jwt);
            return resp;
        }

        // 如果用户不存在，从第三方获取用户信息
        nameValuePairs = new LinkedList<>();
        nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
        nameValuePairs.add(new BasicNameValuePair("openid", openid));
        getString = HttpClientUtil.get(getUserInfoUrl, nameValuePairs); // 携带令牌和用户的openid，从第三方服务器获取用户信息
        if (getString == null) return resp;

        // 解析用户信息
        getResp = JSONObject.parseObject(getString);
        String username = getResp.getString("username");
        String photo = getResp.getString("photo");

        if (username == null || photo == null) return resp;

        // 通过第三方登录的用户自动注册时，确保用户名不重复
        for (int i = 0; i < 100; i++) {
            QueryWrapper<User> usernamequeryWrapper = new QueryWrapper<>();
            usernamequeryWrapper.eq("username", username);
            if (userMapper.selectList(usernamequeryWrapper).isEmpty()) break;
            username += (char)(random.nextInt(10) + '0');
            if(i == 99) return resp;
        }

        // 创建新用户并插入数据库
        User user = new User(
                null,
                username,
                null,
                photo,
                1500,
                openid
        );
        userMapper.insert(user);
        String jwt = JwtUtil.createJWT(user.getId().toString());
        resp.put("result", "success");
        resp.put("jwt_token", jwt);
        return resp;
    }
}
