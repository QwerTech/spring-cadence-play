package org.qwertech.springcadence;

import com.uber.cadence.activity.ActivityMethod;

public interface TestActivity {

  @ActivityMethod(scheduleToCloseTimeoutSeconds = 30)
  void activity(String newState);
}
