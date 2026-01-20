package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.RemoveService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {
    private final BotMapper botMapper;

    public RemoveServiceImpl(BotMapper botMapper) {
        this.botMapper = botMapper;
    }

    @Override
    public Map<String, String> remove(Map<String, String> data) {

        // 根据token获取用户信息
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = null;
        if (authenticationToken != null) {
            loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        }

        User user = null;
        if (loginUser != null) {
            user = loginUser.getUser();
        }

        int bot_id = Integer.parseInt(data.get("bot_id"));
        Bot bot = botMapper.selectById(bot_id);
        Map<String, String> map = new HashMap<>();

        if(bot == null) {
            map.put("error_message", "Bot不存在或已被删除");
            return map;
        }

        if (user != null && !bot.getUserId().equals(user.getId())) {
            map.put("error_message", "没有权限删除该Bot");
            return map;
        }

        botMapper.deleteById(bot_id);

        map.put("error_message", "success");

        return map;
    }
}
