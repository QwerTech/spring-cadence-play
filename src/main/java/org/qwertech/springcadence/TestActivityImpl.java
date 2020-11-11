package org.qwertech.springcadence;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

@Slf4j
@AllArgsConstructor
public class TestActivityImpl implements TestActivity {

  @Override
  public void activity(String newState) {
    RequestUtils.getClientIp();
    log.info(newState);
  }
}
