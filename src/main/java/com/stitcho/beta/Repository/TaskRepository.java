package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.TaskType;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOrder_OrderId(Long orderId);
    Optional<Task> findByTaskIdAndWorker_Id(Long taskId, Long workerId);
    List<Task> findByOrder_OrderIdAndStatus(Long orderId, TaskStatus status);
    List<Task> findByOrder_OrderIdAndTaskTypeAndStatus(Long orderId, TaskType taskType, TaskStatus status);
}
