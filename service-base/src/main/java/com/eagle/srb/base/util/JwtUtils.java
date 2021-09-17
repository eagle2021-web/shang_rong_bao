package com.eagle.srb.base.util;

import com.eagle.common.exception.BusinessException;
import com.eagle.common.result.ResponseEnum;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * @author eagle2020
 */
@SuppressWarnings("unused")
public class JwtUtils {

    private static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000;
    private static final String TOKEN_SIGN_KEY = "A1t2g3uigu123456";

    private static Key getKeyInstance() {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] bytes = DatatypeConverter.parseBase64Binary(TOKEN_SIGN_KEY);
        return new SecretKeySpec(bytes, signatureAlgorithm.getJcaName());
    }

    public static String createToken(Long userId, String userName) {
        return Jwts.builder()
                .setSubject("SRB-USER")
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, getKeyInstance())
                .compressWith(CompressionCodecs.GZIP)
                .compact();
    }

    /**
     * 判断token是否有效
     *
     * @param token token字符串
     * @return token是否有效
     */
    public static boolean checkToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Long getUserId(String token) {
        Claims claims = getClaims(token);
        Integer userId = (Integer) claims.get("userId");
        return userId.longValue();
    }

    public static String getUserName(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("userName");
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }

    /**
     * 校验token并返回Claims
     *
     * @param token token字符串
     * @return token里专门存储自定义键值对的对象
     */
    private static Claims getClaims(String token) {
        if (StringUtils.isEmpty(token)) {
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}

