package com.xianzhi.activiti.demo;

import com.xianzhi.activiti.demo.vo.RequestVO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Payload;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.ProcessInstanceMeta;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.model.impl.ProcessDefinitionMetaImpl;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/seller")
public class SellerController {

  Logger logger = LoggerFactory.getLogger(SellerController.class);

  @Autowired
  ProcessRuntime processRuntime;

  @Autowired
  TaskRuntime taskRuntime;

  @PostMapping("/loanRequest")
  public String submitNewRequest(HttpServletRequest request, @RequestParam int amount,
      @RequestParam String username) {
    request.getUserPrincipal().getName();
    ProcessInstance processInstance = processRuntime.start(
        ProcessPayloadBuilder.start().withProcessDefinitionId("loan-request").build());
    logger.info("process started : {}" , processInstance);
    return processInstance.getId();
  }

  @PostMapping("/loanRequest/complete")
  public String complete(String processInstanceId) {
    Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 1),
        TaskPayloadBuilder.tasks().withProcessInstanceId(processInstanceId).build());
    Task task = tasks.getContent().get(0);
    logger.info("task created by process {}", task);
    task = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
    logger.info("task claimed by user {}", task);
    task = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
    logger.info("task completed buy user {}", task);
    return "task complete!";
  }

  @PostMapping("/completeAll")
  public String completeAll() {
    Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
    tasks.getContent().forEach(task -> {
      logger.info("task created by process {}", task);
      if(null == task.getAssignee()) {
        task = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        logger.info("task claimed by user {}", task);
      }
      task = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
      logger.info("task completed buy user {}", task);
    });
    return "finish";
  }

  /**
   * 什么都能干的方法
   */
  @PostMapping("super")
  public String superMethod() {
    Page<ProcessDefinition> processDefinitionPage = processRuntime
        .processDefinitions(Pageable.of(0, 10));
    processDefinitionPage.getContent().forEach(processDefinition -> {
      logger.info(processDefinition.toString());
    });
    ProcessDefinitionMetaImpl processDefinitionMeta = (ProcessDefinitionMetaImpl) processRuntime
        .processDefinitionMeta("loan-request:1:2ce46abd-587b-11ea-9538-acde48001122");
    ProcessDefinitionUtil
        .getProcessDefinition("loan-request:1:2ce46abd-587b-11ea-9538-acde48001122", true);
    logger.info(processDefinitionMeta.getProcessDefinitionKey());
    logger.info("{}", processDefinitionMeta.getConnectorsIds());
    logger.info("{}", processDefinitionMeta.getGroupIds());
    logger.info("{}", processDefinitionMeta.getUsersIds());
    return "I am a super method, I can do all thing";
  }

}
