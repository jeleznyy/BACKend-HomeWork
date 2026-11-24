# Лабораторная работа 3

ФИО: Иванов Олег Петрович

Группа: Б9124-02.03.01мо

HTTP-сервис проката велосипедов. Java 21, Maven, Spring Boot, PostgreSQL 18.

Flyway создаёт таблицы при запуске. Данные проката пока хранятся в памяти.

Запуск базы в Docker — `docker compose up -d --wait db`

Запуск — `mvn spring-boot:run`

Проверка API —`bash requests.sh` в Git Bash на свежем сервере.

Запуск тестов — `mvn test`