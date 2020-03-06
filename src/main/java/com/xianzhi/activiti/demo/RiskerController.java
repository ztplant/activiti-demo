package com.xianzhi.activiti.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risker")
public class RiskerController {

  Logger logger = LoggerFactory.getLogger(RiskerController.class);

  @Autowired
  TaskRuntime taskRuntime;

  @GetMapping
  public List<Task> getAllWaitTask() {
    //获取前五个任务
    Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 5));
    return tasks.getContent();
  }

  @PostMapping("/examine")
  public String examineTask(String taskId) {
    Task task = taskRuntime.task(taskId);
    logger.info("risker get a task {} ", task);
    task = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(taskId).build());
    logger.info("risker claim a task {} ", task);
    return "begin to exam";
  }

  @PostMapping("/examine/{commit}")
  public String commitExamineResult(String taskId
      , @PathVariable("commit") boolean pass) {
    Task task = taskRuntime.task(taskId);
    Map<String, Object> map = new HashMap<>();
    map.put("approved", pass);
    CompleteTaskPayload examineResult = TaskPayloadBuilder.complete().withTaskId(task.getId())
        .withVariable("content", map).build();
    task = taskRuntime.complete(examineResult);
    logger.info("risker complete a task {} ", task);
    return pass ? "congratulation" : "check and commit it again!";
  }

  @PostMapping("/examine/passAll")
  public String passAll() {
    Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
    Map<String, Object> map = new HashMap<>();
    map.put("approved", true);
    tasks.getContent().forEach(task -> {
      if(null == task.getAssignee()){
        task = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        logger.info("risker claim a task {} ", task);
      }
      task = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId())
          .withVariable("content", map).build());
      logger.info("risker complete a task {} ", task);
    });

    return "finished";
  }
}
