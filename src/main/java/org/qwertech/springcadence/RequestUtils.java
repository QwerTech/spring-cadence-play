package org.qwertech.springcadence;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@UtilityClass
public class RequestUtils {

  public static final String X_FORWARDED_FOR_HTTP_HEADER = "X-Forwarded-For";

  public static final String X_FORWARDED_FOR_HTTP_HEADER_SEPARATOR = ",";

  public String getClientIp() {
    String ip = Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()))
        .map(ServletRequestAttributes::getRequest)
        .map(RequestUtils::getIpAddressFromRequest)
        .orElse("");
    Validate.notEmpty(ip);
    log.info(ip);
    return ip;
  }

  private static String getIpAddressFromRequest(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(X_FORWARDED_FOR_HTTP_HEADER))
        .filter(StringUtils::isNotBlank)
        .map(header -> StringUtils.substringBefore(header, X_FORWARDED_FOR_HTTP_HEADER_SEPARATOR))
        .orElse(request.getRemoteAddr());
  }

}
