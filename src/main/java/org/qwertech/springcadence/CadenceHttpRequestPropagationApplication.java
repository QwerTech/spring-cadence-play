package org.qwertech.springcadence;

import static org.springframework.http.ResponseEntity.ok;

import com.google.common.collect.ImmutableList;
import com.uber.cadence.WorkflowExecution;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.client.WorkflowOptions.Builder;
import com.uber.cadence.context.ContextPropagator;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.Worker.FactoryOptions;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
@RestController

public class CadenceHttpRequestPropagationApplication {

  public static final ImmutableList<ContextPropagator> PROPAGATORS = ImmutableList.of(new IpPropagator());
  private final WorkflowClient workflowClient = WorkflowClient.newInstance("127.0.0.1", 17933, TEST_DOMAIN);
  private final String longWorkflowId = "someVeryLongWorkflowId_" + UUID.randomUUID();

  @GetMapping("/start")
  @SneakyThrows
  public ResponseEntity<String> start() {
    log.info("Started {}", longWorkflowId);
    WorkflowOptions options = new Builder(WORKFLOW_OPTIONS).setWorkflowId(longWorkflowId).build();
    TestWorkflow realWorkflow = workflowClient.newWorkflowStub(TestWorkflow.class, options);
    WorkflowExecution start = WorkflowClient.start(realWorkflow::start);
    RequestUtils.getClientIp();
    return ok("workflowId:" + start.getWorkflowId() + " runId:" + start.getRunId());
  }
  @GetMapping("/signalWithActivity")
  @SneakyThrows
  public ResponseEntity<Void> signalWithActivity() {
    TestWorkflow realWorkflow = workflowClient.newWorkflowStub(TestWorkflow.class, longWorkflowId);
    realWorkflow.signalWithActivity("signalWithActivity");
    return ok().build();
  }
  @GetMapping("/signal")
  @SneakyThrows
  public ResponseEntity<Void> signal() {
    TestWorkflow realWorkflow = workflowClient.newWorkflowStub(TestWorkflow.class, longWorkflowId);
    realWorkflow.signal(BYE_DATA);
    return ok().build();
  }

  public static final String TEST_DOMAIN = "test-domain";
  public static final String TASK_LIST = "HelloWorldTaskList";
  private static final WorkflowOptions WORKFLOW_OPTIONS = new Builder().setTaskList(TASK_LIST).setContextPropagators(PROPAGATORS).build();
  public static final String BYE_DATA = "ByeData";


  @SneakyThrows
  public static void main(String[] args) {

    Utils.createDomain(TEST_DOMAIN);
    FactoryOptions options = new FactoryOptions.Builder()
        .setContextPropagators(PROPAGATORS)
        .build();
    Worker.Factory factory = new Worker.Factory("127.0.0.1", 17933, TEST_DOMAIN, options);
    Worker worker = factory.newWorker(TASK_LIST);
    worker.registerWorkflowImplementationTypes(TestWorkflowImpl.class);

    worker.registerActivitiesImplementations(new TestActivityImpl());
    factory.start();
    SpringApplication.run(CadenceHttpRequestPropagationApplication.class, args);
  }


}
