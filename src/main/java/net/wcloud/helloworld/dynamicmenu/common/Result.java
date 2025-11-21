package net.wcloud.helloworld.dynamicmenu.common;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一响应结构（Result）
 *
 * 设计说明：
 * ------------------------------------------------------------
 * - 这是全项目统一的接口返回格式，用于所有 Controller API。
 * - 泛型 T 表示实际业务数据内容，例如 UserVO、List<MenuVO> 等。
 * - 规范：
 * code = 0 → 表示成功
 * code != 0 → 表示失败或业务异常
 * message → 提示信息
 * data → 接口返回的数据主体
 *
 * 注意：
 * ------------------------------------------------------------
 * - Result 是纯数据结构，不包含业务逻辑。
 * - build 方法中加入少量 debug 日志，方便排查序列化问题。
 */
@Data
public class Result<T> {

    private static final Logger log = LoggerFactory.getLogger(Result.class);

    /** 响应码：0 表示成功，非 0 表示失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据内容 */
    private T data;

    /**
     * 成功返回
     *
     * @param data 响应实体
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMessage("OK");
        r.setData(data);

        if (log.isDebugEnabled()) {
            log.debug("[Result.success] 返回成功响应: code=0, message=OK, dataClass={}",
                    (data == null ? "null" : data.getClass().getName()));
        }

        return r;
    }

    /**
     * 失败返回
     *
     * @param code    错误码（非 0）
     * @param message 错误提示
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);

        if (log.isWarnEnabled()) {
            log.warn("[Result.fail] 返回失败响应: code={}, message={}", code, message);
        }

        return r;
    }
}
