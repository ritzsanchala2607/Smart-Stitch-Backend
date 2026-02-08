package com.stitcho.beta.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.RateRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.Repository.WorkerRatingRepository;
import com.stitcho.beta.dto.CreateWorkerRequest;
import com.stitcho.beta.dto.WorkerResponse;
import com.stitcho.beta.dto.WorkerStatsResponse;
import com.stitcho.beta.dto.WorkerTaskResponse;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Rate;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecureWorkerService {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final RateRepository rateRepository;
    private final TaskRepository taskRepository;
    private final RoleRepository roleRepository;
    private final OwnerRepository ownerRepository;
    private final WorkerRatingRepository workerRatingRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createWorker(Long userId, CreateWorkerRequest request) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Shop shop = owner.getShop();

        // Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Create user
        Role workerRole = roleRepository.findById(request.getUser().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Validate that the role is actually WORKER
        if (!"WORKER".equalsIgnoreCase(workerRole.getRoleName())) {
            throw new IllegalArgumentException("Invalid role. Must be WORKER role (roleId=2).");
        }

        User user = new User();
        user.setName(request.getUser().getName());
        user.setEmail(request.getUser().getEmail());
        user.setContactNumber(request.getUser().getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setProfilePicture(request.getUser().getProfilePicture());
        user.setRole(workerRole);
        user = userRepository.save(user);

        // Create worker profile
        Worker worker = new Worker();
        worker.setUser(user);
        worker.setShop(shop);
        worker.setWorkType(request.getWorker().getWorkType());
        worker.setExperience(request.getWorker().getExperience());
        worker.setRatings(null);
        worker = workerRepository.save(worker);

        // Create rates
        for (var rateReq : request.getRates()) {
            Rate rate = new Rate();
            rate.setWorker(worker);
            rate.setWorkType(rateReq.getWorkType());
            rate.setRate(rateReq.getRate());
            rateRepository.save(rate);
        }
    }

    public List<WorkerResponse> getMyShopWorkers(Long userId, String name) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();

        List<Worker> workers;
        if (name != null && !name.trim().isEmpty()) {
            workers = workerRepository.findByShopIdAndUserNameContaining(shopId, name);
        } else {
            workers = workerRepository.findByShop_ShopId(shopId);
        }

        return workers.stream()
                .map(this::mapToWorkerResponse)
                .collect(Collectors.toList());
    }

    public List<Task> getMyTasks(Long userId) {
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        
        return taskRepository.findByWorker_Id(worker.getId());
    }

    public WorkerResponse getMyProfile(Long userId) {
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        
        return mapToWorkerResponse(worker);
    }

    @Transactional
    public void deleteWorker(Long userId, Long workerId) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        // Get worker and verify it belongs to owner's shop
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        
        if (!worker.getShop().getShopId().equals(owner.getShop().getShopId())) {
            throw new RuntimeException("Access denied: Worker does not belong to your shop");
        }

        // Delete worker (cascade will handle rates, tasks, etc.)
        workerRepository.delete(worker);
        
        // Delete associated user account
        userRepository.delete(worker.getUser());
    }

    private WorkerResponse mapToWorkerResponse(Worker worker) {
        WorkerResponse response = new WorkerResponse();
        response.setWorkerId(worker.getId());
        response.setUserId(worker.getUser().getId());
        response.setName(worker.getUser().getName());
        response.setEmail(worker.getUser().getEmail());
        response.setContactNumber(worker.getUser().getContactNumber());
        response.setWorkType(worker.getWorkType());
        response.setExperience(worker.getExperience());
        response.setRatings(worker.getRatings());

        List<Rate> rates = rateRepository.findByWorker_Id(worker.getId());
        List<WorkerResponse.RateInfo> rateInfos = rates.stream()
                .map(rate -> new WorkerResponse.RateInfo(rate.getWorkType(), rate.getRate()))
                .collect(Collectors.toList());
        response.setRates(rateInfos);

        return response;
    }

    // ==================== WORKER DASHBOARD METHODS ====================

    public WorkerStatsResponse getWorkerStats(Long userId) {
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        
        List<Task> allTasks = taskRepository.findByWorker_Id(worker.getId());
        
        int totalTasks = allTasks.size();
        int pendingTasks = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .count();
        int inProgressTasks = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        int completedTasks = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        
        // Calculate average rating
        List<com.stitcho.beta.entity.WorkerRating> ratings = workerRatingRepository.findByWorker_Id(worker.getId());
        Double averageRating = ratings.isEmpty() ? 0.0 : 
                ratings.stream()
                        .mapToDouble(com.stitcho.beta.entity.WorkerRating::getRating)
                        .average()
                        .orElse(0.0);
        
        return new WorkerStatsResponse(
                totalTasks,
                pendingTasks,
                inProgressTasks,
                completedTasks,
                Math.round(averageRating * 10.0) / 10.0, // Round to 1 decimal
                ratings.size()
        );
    }

    public List<WorkerTaskResponse> getWorkerTasks(Long userId) {
        Worker worker = workerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        
        List<Task> tasks = taskRepository.findByWorker_Id(worker.getId());
        
        return tasks.stream()
                .map(this::mapToWorkerTaskResponse)
                .collect(Collectors.toList());
    }

    private WorkerTaskResponse mapToWorkerTaskResponse(Task task) {
        WorkerTaskResponse response = new WorkerTaskResponse();
        response.setTaskId(task.getTaskId());
        response.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : null);
        response.setStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
        response.setAssignedAt(task.getAssignedAt());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());
        
        if (task.getOrder() != null) {
            WorkerTaskResponse.OrderInfo orderInfo = new WorkerTaskResponse.OrderInfo();
            orderInfo.setOrderId(task.getOrder().getOrderId());
            orderInfo.setDeadline(task.getOrder().getDeadline());
            orderInfo.setOrderStatus(task.getOrder().getStatus() != null ? 
                    task.getOrder().getStatus().name() : "NEW");
            
            if (task.getOrder().getCustomer() != null && 
                task.getOrder().getCustomer().getUser() != null) {
                orderInfo.setCustomerName(task.getOrder().getCustomer().getUser().getName());
            }
            
            response.setOrder(orderInfo);
        }
        
        return response;
    }
}
