insert into Human (id,fio, phone, mail, role)
values (1,'Артём Тучин','111111111', 'a@m.ru', 'Клиент'),
(2,'Артём Герасимов','111111111', 'a@m.ru', 'Работник'),
(3,'Дмитрий Емельянов','111111111', 'a@m.ru', 'Курьер');

insert into order_Status (id,type)
values
(1,'Новый'),
(2,'Выбран способ получения'),
(3,'Выбран способ оплаты'),
(4,'Оплачен онлайн'),
(5,'Оплачен курьеру'),
(6,'В работе'),
(7,'Готов к выдаче'),
(8,'Передан в доставку'),
(9,'Завершен');

insert into receive_Status (id,type)
values
(1,'Новый'),
(2,'Ожидает обработки'),
(3,'Выдан'),
(4,'Получено');

insert into delivery_Status (id,type)
values
(1,'Новый'),
(2,'Ожидает обработки'),
(3,'В работе'),
(4,'Доставлено'),
(5,'Получено');

insert into receive_Type (id,type, min_Sum)
values
(1,'Самовывоз', 0),
(2,'Доставка', 500);

insert into payment_Type (id,type, min_Sum)
values
(1,'Онлайн', 0),
(2,'При получении', 500);

insert into Product (id,name, count, cost)
values
    (1,'Карандаш', 10, 100),
    (2,'Нитка', 20, 50),
    (3,'Краска', 5, 250),
    (4,'Бумага', 100, 10);

DROP table product_In_Order;
DROP table pickup;
DROP table delivery;
DROP table orders;
DROP table product;
DROP table human;
DROP table delivery_Status;
DROP table receive_Status;
DROP table payment_Type;
DROP table order_Status;
DROP table receive_Type;