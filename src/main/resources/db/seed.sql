-- Sample seed data for the Greenhill Food Co-op ordering system.
-- Run manually against the greenhill_coop database when desired.

INSERT INTO products (name, description, pricing_type, price, active) VALUES
('Free-range Eggs (box of 12)', 'Local Lockyer Valley farm, box of twelve', 'UNIT', 7.50, 1),
('Olive Oil 500ml', 'Cold pressed, Kooralbyn grove', 'UNIT', 14.00, 1),
('Rolled Oats', 'Organic rolled oats, sold by weight', 'WEIGHT', 4.20, 1),
('Brown Rice', 'Long grain brown rice, sold by weight', 'WEIGHT', 3.80, 1),
('Carrots', 'Seasonal mix, sold by weight', 'WEIGHT', 2.60, 1);
