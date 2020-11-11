package org.qwertech.springcadence;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class TestWorkflowImpl implements TestWorkflow {

  private static final Logger wfLog = Workflow.getLogger(TestWorkflowImpl.class);

  private final TestActivity activities = Workflow.newActivityStub(TestActivity.class,
      new ActivityOptions.Builder()
          .setContextPropagators(CadenceHttpRequestPropagationApplication.PROPAGATORS).build());
  private String state = "";

  @Override
  public String start() {
    wfLog.info("started");
    RequestUtils.getClientIp();
    Workflow.await(() -> state.equals(CadenceHttpRequestPropagationApplication.BYE_DATA));
    wfLog.info("finished");
    return state;
  }

  @Override
  public void signalWithActivity(String newState) {
    wfLog.info("signalWithActivity: starting: New data {}", newState);
    RequestUtils.getClientIp();
    activities.activity(newState);
    wfLog.info("signalWithActivity: finished: New data {}", newState);
  }

  @Override
  public void signal(String newState) {
    state = newState;
    RequestUtils.getClientIp();
    wfLog.info("signal: New data {}", newState);
  }

  @Override
  public String query() {
    //TODO this is not working
//    RequestUtils.getClientIp();
    log.info("query: data {}", state);
    return state;
  }
}
