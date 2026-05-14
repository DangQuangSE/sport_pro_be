package com.sport_pro_be.modules.order.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.modules.order.domain.Order;
import com.sport_pro_be.modules.order.domain.OrderReturn;
import com.sport_pro_be.modules.order.dto.ReturnRequest;
import com.sport_pro_be.modules.order.dto.ReturnResponse;
import com.sport_pro_be.modules.order.dto.UpdateReturnStatusRequest;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.enums.ReturnStatus;
import com.sport_pro_be.modules.order.interfaces.IReturnService;
import com.sport_pro_be.modules.order.repository.OrderRepository;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import com.sport_pro_be.modules.order.repository.OrderReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReturnService implements IReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    @Loggable(action = "REQUEST_RETURN", module = "ORDER_RETURN")
    public ReturnResponse requestReturn(Long userId, ReturnRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(OrderMessageConstant.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException(OrderMessageConstant.INVALID_ORDER_FOR_RETURN);
        }

        if (returnRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException(OrderMessageConstant.RETURN_ALREADY_EXISTS);
        }

        OrderReturn orderReturn = OrderReturn.builder()
                .order(order)
                .reason(request.getReason())
                .reasonDetail(request.getReasonDetail())
                .bankAccountInfo(request.getBankAccountInfo())
                .images(request.getImages())
                .status(ReturnStatus.PENDING)
                .refundAmount(order.getTotalAmount()) // Default to full refund
                .build();

        orderReturn = returnRepository.save(orderReturn);
        
        // Update order status
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        return mapToResponse(orderReturn);
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_RETURN_STATUS", module = "ORDER_RETURN")
    public ReturnResponse updateReturnStatus(Long returnId, UpdateReturnStatusRequest request) {
        OrderReturn orderReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.RETURN_NOT_FOUND));

        orderReturn.setStatus(request.getStatus());
        if (request.getAdminNote() != null) {
            orderReturn.setAdminNote(request.getAdminNote());
        }

        // Sync with Order status
        Order order = orderReturn.getOrder();
        if (request.getStatus() == ReturnStatus.RECEIVED) {
            order.setStatus(OrderStatus.RETURNED);
        } else if (request.getStatus() == ReturnStatus.REFUNDED) {
            order.setStatus(OrderStatus.REFUNDED);
        } else if (request.getStatus() == ReturnStatus.REJECTED) {
            // Revert order status if rejected? 
            // Usually, we might want to set it back to DELIVERED or a specific state.
            order.setStatus(OrderStatus.DELIVERED);
        }

        orderRepository.save(order);
        orderReturn = returnRepository.save(orderReturn);

        return mapToResponse(orderReturn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnResponse> getAllReturns(Pageable pageable) {
        return returnRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnResponse getReturnDetails(Long returnId) {
        return returnRepository.findById(returnId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.RETURN_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnResponse getReturnByOrderId(Long userId, Long orderId) {
        OrderReturn orderReturn = returnRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.RETURN_NOT_FOUND));
        
        if (!orderReturn.getOrder().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(OrderMessageConstant.RETURN_NOT_FOUND);
        }
        
        return mapToResponse(orderReturn);
    }

    private ReturnResponse mapToResponse(OrderReturn orderReturn) {
        return ReturnResponse.builder()
                .id(orderReturn.getId())
                .orderId(orderReturn.getOrder().getId())
                .status(orderReturn.getStatus())
                .reason(orderReturn.getReason())
                .reasonDetail(orderReturn.getReasonDetail())
                .refundAmount(orderReturn.getRefundAmount())
                .adminNote(orderReturn.getAdminNote())
                .bankAccountInfo(orderReturn.getBankAccountInfo())
                .images(orderReturn.getImages())
                .createdAt(orderReturn.getCreatedAt())
                .build();
    }
}
