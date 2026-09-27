-- =============================================================
-- Greenhill Food Co-op Weekly Ordering System
-- Full database script: schema creation + sample data
-- Target: MySQL 8.0, database greenhill_coop
--
-- Usage:
--   mysql -u coop_user -p < db/seed.sql
--
-- Note: the application also updates the schema automatically at
-- start-up (spring.jpa.hibernate.ddl-auto=update). Running this
-- script simply pre-creates the same structure and loads the
-- sample data below.
-- =============================================================

CREATE DATABASE IF NOT EXISTS greenhill_coop CHARACTER SET utf8mb4;
USE greenhill_coop;

-- -------------------------------------------------------------
-- Clean up (child tables first)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_rounds;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS members;

-- -------------------------------------------------------------
-- Schema
-- -------------------------------------------------------------

-- Member households of the co-operative
CREATE TABLE members (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    name      VARCHAR(120) NOT NULL,
    email     VARCHAR(160) NOT NULL,
    phone     VARCHAR(30)  NULL,
    joined_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_members_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Catalogue products; UNIT = sold as whole items, WEIGHT = sold by kg
CREATE TABLE products (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(140)  NOT NULL,
    description  VARCHAR(400)  NULL,
    pricing_type VARCHAR(10)   NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    active       TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- One weekly ordering round per week
CREATE TABLE order_rounds (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    name      VARCHAR(80) NOT NULL,
    status    VARCHAR(10) NOT NULL,
    opens_at  DATETIME    NOT NULL,
    closes_at DATETIME    NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- One order per member per round
CREATE TABLE orders (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    member_id  BIGINT   NOT NULL,
    round_id   BIGINT   NOT NULL,
    placed_at  DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_member_round (member_id, round_id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_orders_round  FOREIGN KEY (round_id)  REFERENCES order_rounds (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Product lines inside an order; weight products may use decimals
CREATE TABLE order_items (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   DECIMAL(10,3)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- -------------------------------------------------------------
-- Sample data
-- -------------------------------------------------------------

-- Member households
INSERT INTO members (id, name, email, phone, joined_at) VALUES
(1, 'Nguyen household',  'nguyen@example.com',  '0400 111 222', '2024-03-05 10:15:00'),
(2, 'Chen household',    'chen@example.com',    '0400 222 333', '2024-06-18 14:40:00'),
(3, 'O''Brien household', 'obrien@example.com', '0400 333 444', '2025-02-11 09:05:00'),
(4, 'Patel household',   'patel@example.com',   '0400 444 555', '2025-05-27 16:30:00'),
(5, 'Smith household',   'smith@example.com',   '0400 555 666', '2026-01-20 11:00:00'),
(6, 'Walker household',  'walker@example.com',  '0400 666 777', '2026-08-03 13:45:00');

-- Catalogue
INSERT INTO products (id, name, description, pricing_type, price, active) VALUES
(1, 'Free-range Eggs (box of 12)', 'Local Lockyer Valley farm',            'UNIT',   7.50, 1),
(2, 'Olive Oil 500ml',             'Cold pressed, Kooralbyn grove',        'UNIT',  14.00, 1),
(3, 'Local Honey 1kg jar',         'Raw bush honey from the Scenic Rim',   'UNIT',   9.80, 1),
(4, 'Rolled Oats',                 'Organic rolled oats, sold by weight',  'WEIGHT', 4.20, 1),
(5, 'Brown Rice',                  'Long grain brown rice, sold by weight','WEIGHT', 3.80, 1),
(6, 'Wholemeal Flour',             'Stone ground, sold by weight',         'WEIGHT', 3.20, 1),
(7, 'Carrots',                     'Seasonal mix, sold by weight',         'WEIGHT', 2.60, 1),
(8, 'Apples',                      'Granite Belt apples, sold by weight',  'WEIGHT', 4.50, 1);

-- Weekly rounds: week 38 finished, week 39 currently open
INSERT INTO order_rounds (id, name, status, opens_at, closes_at) VALUES
(1, 'Week 38', 'PACKED', '2026-09-14 08:00:00', '2026-09-20 20:00:00'),
(2, 'Week 39', 'OPEN',   '2026-09-21 08:00:00', '2026-09-27 20:00:00');

-- Orders for week 38 (packed and collected)
INSERT INTO orders (id, member_id, round_id, placed_at, updated_at) VALUES
(1, 1, 1, '2026-09-15 18:20:00', '2026-09-17 07:45:00'),
(2, 3, 1, '2026-09-16 20:05:00', '2026-09-16 20:05:00');

INSERT INTO order_items (id, order_id, product_id, quantity) VALUES
(1, 1, 1, 2.000),
(2, 1, 4, 1.500),
(3, 2, 3, 1.000),
(4, 2, 7, 2.000);

-- Orders for week 39 (open round)
INSERT INTO orders (id, member_id, round_id, placed_at, updated_at) VALUES
(3, 1, 2, '2026-09-22 19:10:00', '2026-09-24 08:30:00'),
(4, 2, 2, '2026-09-23 12:00:00', '2026-09-23 12:00:00'),
(5, 4, 2, '2026-09-24 09:15:00', '2026-09-25 21:40:00'),
(6, 5, 2, '2026-09-25 17:05:00', '2026-09-25 17:05:00');

INSERT INTO order_items (id, order_id, product_id, quantity) VALUES
(5,  3, 1, 1.000),
(6,  3, 4, 2.500),
(7,  4, 5, 1.200),
(8,  4, 1, 2.000),
(9,  5, 8, 3.500),
(10, 5, 2, 1.000),
(11, 6, 4, 0.800),
(12, 6, 7, 1.500);
