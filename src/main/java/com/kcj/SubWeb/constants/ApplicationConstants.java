package com.kcj.SubWeb.constants;

public final class ApplicationConstants {
    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = "lj2Iw+WI1Pwg3JvahJ433PEEmNgi+9IDxMJI+cGR2+Xm6vVtp7vitYm1/WNOGuFwgg1xwJdZfqKeTqGBj0calA==";
    //openssl rand -hex 64 0-9, a-z
    //openssl rand -base64 64 모든 문자
    public static final String JWT_HEADER = "Authorization";
}
