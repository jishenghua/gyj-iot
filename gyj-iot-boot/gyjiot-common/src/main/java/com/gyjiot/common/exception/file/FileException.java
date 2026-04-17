package com.gyjiot.common.exception.file;

import com.gyjiot.common.exception.base.BaseException;

/**
 * 文件信息异常类
 *
 * @author jishenghua
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
