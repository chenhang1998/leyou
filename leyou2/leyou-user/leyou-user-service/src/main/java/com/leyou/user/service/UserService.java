package com.leyou.user.service;

import com.leyou.user.pojo.User;

public interface UserService {

    /**
     * 校验数据是否已被使用
     * */
    Boolean checkUser(String data, Integer type);

    /**
     * 发送验证码和手机号到消息队列
     * */
    void sendVerifyCode(String phone);

    /**
     * 用户注册
     * */
    void register(User user, String code);

    /**
     *根据username和password查询user
     * */
    User queryUser(String username, String password);
}
