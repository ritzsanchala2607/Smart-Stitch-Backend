package com.stitcho.beta.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final WorkerRepository workerRepository;
    private final OrderService orderService;

    @Transactional
    public void startTask(Long taskId, Long userId) {
        // Get worker from userId
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found for this user"));

        Task task = taskRepository.findByTaskIdAndWorker_Id(taskId, worker.getId())
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
    public void completeTask(Long taskId, Long userId) {
        // Get worker from userId
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found for this user"));

        Task task = taskRepository.findByTaskIdAndWorker_Id(taskId, worker.getId())
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
