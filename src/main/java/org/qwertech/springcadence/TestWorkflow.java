package org.qwertech.springcadence;

import com.uber.cadence.workflow.QueryMethod;
import com.uber.cadence.workflow.SignalMethod;
import com.uber.cadence.workflow.WorkflowMethod;

public interface TestWorkflow {

  @WorkflowMethod(executionStartToCloseTimeoutSeconds = 60)
  String start();

  @SignalMethod
  void signal(String newState);

  @SignalMethod
  void signalWithActivity(String newState);

  @QueryMethod
  String query();
}
