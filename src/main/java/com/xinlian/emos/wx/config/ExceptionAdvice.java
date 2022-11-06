package com.xinlian.emos.wx.config;

import com.xinlian.emos.wx.common.util.R;
import com.xinlian.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public R validExceptionHandler(Exception e) {
        log.error("执行异常", e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            return R.error(exception.getBindingResult().getFieldError().getDefaultMessage());
        } else if (e instanceof EmosException) {
            EmosException exception = (EmosException) e;
            return R.error(exception.getMessage());
        } else if(e instanceof UnauthorizedException) {
            return R.error("你不具备相关权限");
        } else {
            return R.error("后端执行异常");
        }
    }
}
