package org.example.ash.mapper;

import org.example.ash.dto.OrderDTO;
import org.example.ash.dto.request.OrderRequest;
import org.example.ash.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order, OrderRequest> {
}