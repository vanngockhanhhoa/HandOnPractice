//package org.example.ash.job;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.example.ash.client.ServiceBClient;
//import org.example.ash.service.CategoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CategoryJob {
//    private final CategoryService categoryService;
//
//    private final ServiceBClient serviceBClient;
//
//    private int page = 1;
//
//    @Scheduled(fixedRate = 300000)
//    public void run() {
//        log.info("==== START CATEGORY JOB (page {}) ====", page);
//        categoryService.processBatch(page);
////        serviceBClient.getUserById(12L);
//        page++;
//    }
//}
//
//
