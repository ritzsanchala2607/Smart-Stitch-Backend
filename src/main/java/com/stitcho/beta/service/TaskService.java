package com.stitcho.beta.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final OrderService orderService;

    @Transactional
    public void startTask(Long taskId, Long workerId) {
        Task task = taskRepository.findByTaskIdAndWorker_Id(taskId, workerId)
                .orElseThrow(() -> new RuntimeException("Task not found or does not belong to this worker"));

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Task is not in PENDING state");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Update order status
        orderService.updateOrderStatus(task.getOrder().getOrderId());
    }

    @Transactional
    public void completeTask(Long taskId, Long workerId) {
        Task task = taskRepository.findByTaskIdAndWorker_Id(taskId, workerId)
                .orElseThrow(() -> new RuntimeException("Task not found or does not belong to this worker"));

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task is not in IN_PROGRESS state");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Update order status
        orderService.updateOrderStatus(task.getOrder().getOrderId());
    }
}
