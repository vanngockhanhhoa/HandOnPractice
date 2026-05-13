package org.example.ash.controller;

import lombok.RequiredArgsConstructor;
import org.example.ash.dto.OrderDTO;
import org.example.ash.dto.request.OrderRequest;
import org.example.ash.dto.response.BaseResponse;
import org.example.ash.entity.OrderStatus;
import org.example.ash.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderDTO>> create(@RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.created(orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.ok(orderService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<OrderDTO>>> getAll() {
        return ResponseEntity.ok(BaseResponse.ok(orderService.getAll()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<List<OrderDTO>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(BaseResponse.ok(orderService.getByUserId(userId)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse<OrderDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(BaseResponse.ok(orderService.updateStatus(id, status)));
    }
}
