/**
 * Tongji Edu.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package edu.tongji.exception;

/**
 * 
 * @author Hanke Chen
 * @version $Id: FunctionErrorCode.java, v 0.1 2013-9-7 下午4:09:15 chench Exp $
 */
public enum FunctionErrorCode implements ErrorCode {

    /** 计算函数不支持对象为入参*/
    NOT_SUPPORT_OBJECT("计算函数不支持对象T为入参", "FUNC01001"),

    /** 计算函数不支持列表List<T>为入参*/
    NOT_SUPPORT_LIST("计算函数不支持列表List<T>为入参", "FUNC01002"),

    /** 计算函数不支持列表Lists<T>为入参*/
    NOT_SUPPORT_LISTS("计算函数不支持列表List<T>为入参", "FUNC01003"),

    /** 非法参数 */
    ILLEGAL_PARAMETER("非法参数", "FUNC02001"),

    /** 出现零值 */
    ZERO_OCCURS("零错误", "FUNC02002"),

    /** 空列表 */
    EMPTY_LIST("List为空，Item-based情况下，该Item从未被评价过", "FUNC02003"),

    /** 函数未定义 */
    NOT_DEFINITION("函数未定义", "FUNC03001");

    /** 异常描述 */
    private final String description;

    /** 异常编码 */
    private final String shortCode;

    private FunctionErrorCode(String description, String shortCode) {
        this.description = description;
        this.shortCode = shortCode;
    }

    /** 
     * @see edu.tongji.exception.ErrorCode#getType()
     */
    @Override
    public ExceptionType getType() {
        return ExceptionType.FUNCTION_ERROR;
    }

    /** 
     * @see edu.tongji.exception.ErrorCode#getCode()
     */
    @Override
    public String getCode() {
        return shortCode;
    }

    /** 
     * @see edu.tongji.exception.ErrorCode#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

}
