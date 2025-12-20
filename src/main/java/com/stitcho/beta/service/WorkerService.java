package com.stitcho.beta.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.RateRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.CreateWorkerRequest;
import com.stitcho.beta.dto.RateDto;
import com.stitcho.beta.dto.WorkerResponse;
import com.stitcho.beta.entity.Rate;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopRepository shopRepository;
    private final WorkerRepository workerRepository;
    private final RateRepository rateRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createWorker(Long shopId, CreateWorkerRequest request) {
        // 1️⃣ Validate shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 2️⃣ Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 3️⃣ Create user
        Role workerRole = roleRepository.findById(request.getUser().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getUser().getName());
        user.setEmail(request.getUser().getEmail());
        user.setContactNumber(request.getUser().getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setRole(workerRole);
        user.setProfilePicture(request.getUser().getProfilePicture());

        user = userRepository.save(user);

        // 4️⃣ Create worker profile
        Worker worker = new Worker();
        worker.setUser(user);
        worker.setShop(shop);
        worker.setWorkType(request.getWorker().getWorkType());
        worker.setExperience(request.getWorker().getExperience());
        worker.setRatings(null);

        worker = workerRepository.save(worker);

        // 5️⃣ Create rates
        for (RateDto rateReq : request.getRates()) {
            Rate rate = new Rate();
            rate.setWorker(worker);
            rate.setWorkType(rateReq.getWorkType());
            rate.setRate(rateReq.getRate());

            rateRepository.save(rate);
        }
    }

    public List<WorkerResponse> getWorkersByShop(Long shopId, String name) {
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
}
