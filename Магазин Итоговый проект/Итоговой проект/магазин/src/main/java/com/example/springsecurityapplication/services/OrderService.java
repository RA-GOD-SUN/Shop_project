package com.example.springsecurityapplication.services;

import com.example.springsecurityapplication.enumm.Status;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
//@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


@Autowired
    public List<Order> getAllOrder(){
        return orderRepository.findAll();
    }

    public Order getOrderById(int id){
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.orElse(null);
    }

    // Данный метод позволяет обновить данные Заказа
    @Transactional
    public void updateOrder(int id,Order order ){
        order.setId(id);
        orderRepository.save(order);
    }


    // Данный метод позволяет обновить инфо о заказе отменён
    @Transactional
    public void updateOrderCansel(Order order){
        order.setStatus(Status.Отменен);
        orderRepository.save(order);
    }

    @Transactional
    public List<Order>findByLastFourCharacters(String str){
        List<Order> orders = orderRepository.findByLastFourCharacters(str);
        return orders;
    }

    // Данный метод позволяет обновить инфо о заказе принят
    @Transactional
    public void updateOrderAccept(Order order ){
        order.setStatus(Status.Принят);
        orderRepository.save(order);
    }
    // Данный метод позволяет обновить инфо о заказе Оформлен
    @Transactional
    public void updateOrderRegister(Order order ){
        order.setStatus(Status.Оформлен);
        orderRepository.save(order);
    }
    // Данный метод позволяет обновить инфо о заказе Ожидает
    @Transactional
    public void updateOrderExpect(Order order ){
        order.setStatus(Status.Ожидает);
        orderRepository.save(order);
    }
    // Данный метод позволяет обновить инфо о заказе Получен
    @Transactional
    public void updateOrderGet(Order order ){
        order.setStatus(Status.Получен);
        orderRepository.save(order);
    }



//
}
