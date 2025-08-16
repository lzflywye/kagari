
INSERT INTO services(id, name, description, price)
VALUES ('00000000-0000-0000-0000-000000000000', 'Test Service', '', 1000);

INSERT INTO users(id, username, password, role)
VALUES ('00000000-0000-0000-0000-000000000000', 'admin', '$2a$10$dE/4wyjAM9hncjKDoQ.IIeTY9zsel55iwJhSW3GIP0s5TeKHGjVzi', 'admin'),
       ('00000000-0000-0000-0000-000000000001', 'user', '$2a$10$5HIkJHdYjNLm45NtjjdMqu/YFoTC0PZIbkTXclKgQ9e3f1IpL/nCe', 'user');

INSERT INTO reservations(id, service_id, reserved_date, start_time, customer_name, customer_phone, description, status)
VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000', CURRENT_DATE, '14:00:00', 'Test Customer', 'xxx-xxxx-xxxx', '', 'pending');

INSERT INTO reservations(id, service_id, reserved_date, start_time, customer_name, customer_phone, description, status)
VALUES ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000000', CURRENT_DATE + INTERVAL '1 day', '14:00:00', 'Test Customer', 'xxx-xxxx-xxxx', '', 'pending');

INSERT INTO reservations(id, service_id, reserved_date, start_time, customer_name, customer_phone, description, status)
VALUES ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000000', CURRENT_DATE + INTERVAL '2 day', '14:00:00', 'Test Customer', 'xxx-xxxx-xxxx', '', 'confirmed');

INSERT INTO reservations(id, service_id, reserved_date, start_time, customer_name, customer_phone, description, status)
VALUES ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000000', CURRENT_DATE + INTERVAL '3 day', '14:00:00', 'Test Customer', 'xxx-xxxx-xxxx', '', 'cancelled');
