package com.kob.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        Map<String, String> map = new HashMap<>();
        if (username == null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password == null || confirmedPassword == null) {
            map.put("error_message", "密码不能为空");
            return map;
        }
        username = username.trim(); // 删除用户名两端的空格
        if(username.isEmpty()) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password.isEmpty() || confirmedPassword.isEmpty()) {
            map.put("error_message", "密码不能为空");
            return map;
        }
        if(username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }
        if(password.length() > 100 || confirmedPassword.length() > 100) {
            map.put("error_message", "密码长度不能大于100");
            return map;
        }
        if(!password.equals(confirmedPassword)) {
            map.put("error_message", "两次输入的密码不一致");
            return map;
        }

        // MyBatis-Plus的查询构造器，用于构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper);
        if (!users.isEmpty()){
            map.put("error_message", "用户名已存在");
            return map;
        }

        String encodedPassword = passwordEncoder.encode(password);

        // 默认头像
        String photo = "https://picx.zhimg.com/v2-ad67b1a83ab788f4407a79786b2462bf_1440w.jpg";

        User user = new User(null, username, encodedPassword, photo, 1500);
        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}
