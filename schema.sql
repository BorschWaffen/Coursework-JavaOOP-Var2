\encoding UTF8

CREATE TABLE IF NOT EXISTS departments (
                                           id SERIAL PRIMARY KEY,
                                           name VARCHAR(120) NOT NULL UNIQUE,
    location VARCHAR(150) NOT NULL
    );

CREATE TABLE IF NOT EXISTS employees (
                                         id SERIAL PRIMARY KEY,
                                         full_name VARCHAR(150) NOT NULL,
    position VARCHAR(100) NOT NULL,
    salary NUMERIC(12, 2) NOT NULL CHECK (salary >= 0),
    hire_date DATE NOT NULL,
    department_id INTEGER NOT NULL REFERENCES departments(id) ON DELETE RESTRICT
    );

INSERT INTO departments (name, location) VALUES
                                             ('Розробка ПЗ', 'Корпус ЕлІТ, ауд. 301'),
                                             ('Аналітика та QA', 'Корпус ЕлІТ, ауд. 305'),
                                             ('Інфраструктура', 'Корпус ЕлІТ, ауд. 210')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO employees (full_name, position, salary, hire_date, department_id)
SELECT 'Коваленко Андрій Олександрович', 'Senior Java Developer', 65000.00, DATE '2020-03-15', id
FROM departments WHERE name = 'Розробка ПЗ'
    ON CONFLICT DO NOTHING;

INSERT INTO employees (full_name, position, salary, hire_date, department_id)
SELECT 'Мельник Олена Сергіївна', 'Java Developer', 48000.00, DATE '2022-09-01', id
FROM departments WHERE name = 'Розробка ПЗ'
    ON CONFLICT DO NOTHING;

INSERT INTO employees (full_name, position, salary, hire_date, department_id)
SELECT 'Бондаренко Максим Ігорович', 'QA Engineer', 42000.00, DATE '2021-06-10', id
FROM departments WHERE name = 'Аналітика та QA'
    ON CONFLICT DO NOTHING;

INSERT INTO employees (full_name, position, salary, hire_date, department_id)
SELECT 'Ткаченко Ірина Вікторівна', 'Data Analyst', 50000.00, DATE '2023-02-20', id
FROM departments WHERE name = 'Аналітика та QA'
    ON CONFLICT DO NOTHING;

INSERT INTO employees (full_name, position, salary, hire_date, department_id)
SELECT 'Сидоренко Олег Петрович', 'DevOps Engineer', 58000.00, DATE '2019-11-05', id
FROM departments WHERE name = 'Інфраструктура'
    ON CONFLICT DO NOTHING;