package tuchin_emelianov.blps_lab_1.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tuchin_emelianov.blps_lab_1.dto.OrderDTO;
import tuchin_emelianov.blps_lab_1.exceptions.ElementNotFoundException;
import tuchin_emelianov.blps_lab_1.exceptions.EntityNotFoundException;
import tuchin_emelianov.blps_lab_1.jpa.entity.*;
import tuchin_emelianov.blps_lab_1.jpa.repository.*;
import tuchin_emelianov.blps_lab_1.request.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductInOrderRepository productInOrderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ReceiveTypeRepository receiveTypeRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final ModelMapper modelMapper;
    private final PickupService pickupService;
    private final DeliveryService deliveryService;
    private final TransactionTemplate transactionTemplate;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ProductInOrderRepository productInOrderRepository, OrderStatusRepository orderStatusRepository, ReceiveTypeRepository receiveTypeRepository, PaymentTypeRepository paymentTypeRepository, ModelMapper modelMapper, PickupService pickupService, DeliveryService deliveryService, PlatformTransactionManager platformTransactionManager) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productInOrderRepository = productInOrderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.receiveTypeRepository = receiveTypeRepository;
        this.paymentTypeRepository = paymentTypeRepository;
        this.modelMapper = modelMapper;
        this.pickupService = pickupService;
        this.deliveryService = deliveryService;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    public void checkOrder(Long id) {
        if (!orderRepository.existsById(id))
            throw new ElementNotFoundException("Заказ с id=%s не найден".formatted(id));
    }

    public OrderDTO getOrderDTO(Long id) {
        Orders order = orderRepository.findOrderById(id);
        if (order == null) throw new EntityNotFoundException("Заказ с id=%s не найден".formatted(id));
        return modelMapper.map(order, OrderDTO.class);
    }

    public Page<OrderDTO> getOrdersDTO(Pageable pageable) {
        Page<Orders> ordersPage = orderRepository.findAll(pageable);
        return ordersPage.map(orders -> modelMapper.map(orders, OrderDTO.class));
    }

    public Orders getOrder(Long id) {
        return orderRepository.findOrderById(id);
    }


//    public Page<Orders> getOrders(Pageable pageable) {
//        return orderRepository.findAll(pageable);
//    }


    public Page<tuchin_emelianov.blps_lab_1.jpa.entity.Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public tuchin_emelianov.blps_lab_1.jpa.entity.Product getProduct(Long id) {
        return productRepository.findProductById(id);
    }

    public ResultMessage addOrder(Human user, List<Product> products) {
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Orders order = new Orders();
            order.setClient(user);
            order.setDate(new Date());
            order.setLastStatusDate(new Date());
            order.setOrderStatus(orderStatusRepository.findByType("Новый"));

            StringBuilder message = new StringBuilder();
            List<String> names = new ArrayList<>();
            for (Product value : products) {
                if (names.contains(value.getName())) {
                    message.append("Товары не должны повторяться");
                    continue;
                }
                names.add(value.getName());
                tuchin_emelianov.blps_lab_1.jpa.entity.Product product = productRepository.findProductByName(value.getName());
                if (product == null) {
                    message.append("Товар ").append(value.getName()).append(" не найден\n");
                    continue;
                }
                if (product.getCount() < value.getCount()) {
                    message.append("Недостаточно товара ").append(product.getName()).append(" на складе\n");
                }
            }
            if (!message.isEmpty()) {
                return new ResultMessage(0, message.toString());
            }
            order = orderRepository.save(order);

            for (Product value : products) {
                tuchin_emelianov.blps_lab_1.jpa.entity.Product product = productRepository.findProductByName(value.getName());
                product.setCount(product.getCount() - value.getCount());
                product = productRepository.save(product);
                ProductInOrder productInOrder = new ProductInOrder();
                productInOrder.setProduct(product);
                productInOrder.setOrders(order);
                productInOrder.setCount(value.getCount());
                productInOrderRepository.save(productInOrder);
            }

            return new ResultMessage(order.getId(), "Заказ успешно создан! Вам необходимо выбрать способ получения.");
        });
    }

    private int sumOrder(Orders order) {
        List<ProductInOrder> products = productInOrderRepository.findAllProductInOrderByOrders(order);

        int total = 0;

        for (ProductInOrder product : products) {
            total += productRepository.findProductByName(product.getProduct().getName()).getCost() * product.getCount();
        }

        return total;
    }

    public ResultMessage setReceiveType(Long id, String type, String address) {
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Orders order = orderRepository.findOrderById(id);

            if (order.getOrderStatus() != orderStatusRepository.findByType("Новый")) {
                return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
            }

            ReceiveType receiveType = receiveTypeRepository.findByType(type);
            int total = sumOrder(order);
            if (total < receiveType.getMinSum()) {
                return new ResultMessage(0, "Для '" + type + "' заказ должен быть больше этой суммы: " + receiveType.getMinSum() + ", сейчас: " + total);
            }

            order.setReceiveType(receiveType);
            order.setLastStatusDate(new Date());
            order.setOrderStatus(orderStatusRepository.findByType("Выбран способ получения"));

            order = orderRepository.save(order);

            if (type.equals("Самовывоз")) {
                pickupService.addOrder(order);
            } else {
                deliveryService.addOrder(order, address);
            }

            return new ResultMessage(order.getId(), "Cпособ получения успешно выбран! Вам необходимо выбрать способ оплаты.");
        });
    }

    public ResultMessage setPaymentType(Long id, String type) {
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Orders order = orderRepository.findOrderById(id);

            if (order.getOrderStatus() != orderStatusRepository.findByType("Выбран способ получения")) {
                return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
            }

            PaymentType paymentType = paymentTypeRepository.findByType(type);
            int total = sumOrder(order);
            if (total < paymentType.getMinSum()) {
                return new ResultMessage(0, "Для оплаты '" + type + "' заказ должен быть больше этой суммы: " + paymentType.getMinSum() + ", сейчас: " + total);
            }

            order.setPaymentType(paymentType);
            order.setLastStatusDate(new Date());
            order.setOrderStatus(orderStatusRepository.findByType("Выбран способ оплаты"));

            orderRepository.save(order);

            if (type.equals("Онлайн")) {
                return new ResultMessage(order.getId(), "Cпособ оплаты успешно выбран! Вам необходимо оплатить заказ.");
            } else {
                return new ResultMessage(order.getId(), "Cпособ оплаты успешно выбран! Оплатить заказ нужно будет курьеру.");
            }
        });
    }

    public ResultMessage payOnline(Long id) {
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            Orders order = orderRepository.findOrderById(id);
            if (order.getOrderStatus() == orderStatusRepository.findByType("Выбран способ оплаты") && order.getPaymentType() == paymentTypeRepository.findByType("Онлайн")) {
                order.setLastStatusDate(new Date());
                order.setOrderStatus(orderStatusRepository.findByType("Оплачен онлайн"));
                orderRepository.save(order);
                return new ResultMessage(order.getId(), "Оплата успешна произведена");
            } else {
                return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
            }
        });
    }

    public ResultMessage payDelivery(Orders order) {
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            if (order.getOrderStatus() == orderStatusRepository.findByType("Передан в доставку") && order.getPaymentType() == paymentTypeRepository.findByType("При получении")) {
                order.setLastStatusDate(new Date());
                order.setOrderStatus(orderStatusRepository.findByType("Оплачен курьеру"));
                orderRepository.save(order);
                return new ResultMessage(order.getId(), "Оплата успешна произведена");
            } else {
                return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
            }
        });

    }

    public ResultMessage work(Long id, Human user) {
        transactionTemplate.setIsolationLevelName("ISOLATION_REPEATABLE_READ");
        return transactionTemplate.execute(status -> {
            Orders order = orderRepository.findOrderById(id);

            if (
                    order.getOrderStatus().getType().equals("Оплачен онлайн")
                            || (
                            order.getOrderStatus().getType().equals("Выбран способ оплаты")
                                    && order.getReceiveType().getType().equals("Доставка")
                    )) {
                order.setWorker(user);
                order.setLastStatusDate(new Date());
                order.setOrderStatus(orderStatusRepository.findByType("В работе"));
                orderRepository.save(order);
                return new ResultMessage(order.getId(), "Заказ взят в работу сотрудником: " + user.getFio());
            } else {
                return new ResultMessage(0, "Нельзя взять заказ в работу");
            }
        });
    }

    public ResultMessage done(Long id, Human user) {
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate.execute(status -> {
            Orders order = orderRepository.findOrderById(id);

            if (!order.getOrderStatus().getType().equals("В работе")) {
                return new ResultMessage(0, "Недопустимый статус заказа: " + order.getOrderStatus().getType());
            }
            if (!order.getWorker().equals(user)) {
                return new ResultMessage(0, "Заказ в работе у другого сотрудника");
            }
            order.setLastStatusDate(new Date());

            if (order.getReceiveType().equals(receiveTypeRepository.findByType("Доставка"))) {
                order.setOrderStatus(orderStatusRepository.findByType("Передан в доставку"));
            } else {
                order.setOrderStatus(orderStatusRepository.findByType("Готов к выдаче"));
            }
            orderRepository.save(order);
            if (order.getReceiveType().getType().equals("Самовывоз")) {
                pickupService.updateOrder(order);
            } else {
                deliveryService.updateOrder(order);
            }
            return new ResultMessage(order.getId(), "Заказ собран сотрудником: " + user.getFio());
        });
    }
}
