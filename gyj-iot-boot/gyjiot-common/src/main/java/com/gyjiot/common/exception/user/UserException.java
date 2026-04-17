package com.gyjiot.common.exception.user;

import com.gyjiot.common.exception.base.BaseException;

/**
 * 用户信息异常类
 * 
 * @author jishenghua
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
