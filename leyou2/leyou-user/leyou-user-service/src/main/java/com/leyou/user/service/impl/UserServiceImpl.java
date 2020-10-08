package com.leyou.user.service.impl;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String key_prefix="user:verify:";

    /**
     * 校验数据是否已被使用
     * */
    @Override
    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if (type==1){
            user.setUsername(data);
        }else if (type==2){
            user.setPhone(data);
        }else {
            return null;
        }
        return this.userMapper.selectCount(user)==0;
    }

    /**
     * 发送验证码和手机号到消息队列,并且把验证码放入redis
     * */
    @Override
    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank(phone)){
            return;
        }
        //随机生成验证码
        String code = NumberUtils.generateCode(6);
        //将手机号和验证码封装成一个map传输
        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        try {
            this.amqpTemplate.convertAndSend("leyou.sms.exchange","verifycode.sms",msg);    //向rabbitmq发送消息队列
        }catch (AmqpException e) {
            e.printStackTrace();
        }
        //将验证码放入redis缓存中，设置过期时间为10分钟
        this.stringRedisTemplate.opsForValue().set(key_prefix+phone,code,10, TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     * */
    @Override
    public void register(User user, String code) {
        //取出redis中的验证码
        String redisCode = this.stringRedisTemplate.opsForValue().get(key_prefix + user.getPhone());
        //校验验证码
        if (!StringUtils.equals(code,redisCode)){
            return;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加盐加密
        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(password);
        //创建用户
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insert(user);
    }

    /**
     *根据username和password查询user
     * */
    @Override
    public User queryUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return null;
        }
        User user_query=new User();
        user_query.setUsername(username);
        User user = this.userMapper.selectOne(user_query);
        if(user==null){
            return null;
        }
        //给输入的密码加密比较
        password = CodecUtils.md5Hex(password, user.getSalt());
        if (StringUtils.equals(password,user.getPassword())){
            return user;
        }
        return null;
    }
}
