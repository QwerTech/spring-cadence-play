package org.qwertech.springcadence;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uber.cadence.context.ContextPropagator;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class IpPropagator implements ContextPropagator {

  private static final Gson gson = (new GsonBuilder()).create();
  public static final String HTTP_REQUEST_KEY = "httpRequest";

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public Map<String, byte[]> serializeContext(Object context) {
    if (context == null) {
      return Collections.emptyMap();
    } else {
      try {
        return ImmutableMap.of(HTTP_REQUEST_KEY, gson.toJson(context).getBytes(StandardCharsets.UTF_8));
      } catch (Exception var3) {
        log.error("Context serialization error! Context: {}", context, var3);
        return Collections.emptyMap();
      }
    }
  }

  @Override
  public Object deserializeContext(Map<String, byte[]> context) {
    if (context != null && !context.isEmpty()) {
      try {
        return Optional.ofNullable(context.get(HTTP_REQUEST_KEY))
            .map(b -> new String(b, StandardCharsets.UTF_8))
            .map(s -> gson.fromJson(s, String.class))
            .orElse(null);
      } catch (Exception var3) {
        log.error("Context deserialization error! Context map: {}", context, var3);
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public Object getCurrentContext() {
    return RequestUtils.getClientIp();
  }

  public static class CustomRequestFacade extends RequestFacade {

    private String ip;

    public CustomRequestFacade(String ip) {
      super(null);
      this.ip = ip;
    }

    @Override
    public String getRemoteAddr() {
      return ip;
    }

    @Override
    public String getHeader(String name) {
      if (name.equals(RequestUtils.X_FORWARDED_FOR_HTTP_HEADER)) {
        return ip;
      }
      return super.getHeader(name);
    }
  }

  @Override
  public void setCurrentContext(Object context) {
    RequestFacade request1 = new CustomRequestFacade((String) context);
    ServletRequestAttributes attributes = new ServletRequestAttributes(request1);
    RequestContextHolder.setRequestAttributes(attributes);

  }
}
