# Market4 Backend

E-commerce backend built with Java, Spring Boot, JPA and PostgreSQL,
focused on transactional order processing and inventory concurrency.

## Key Features
- Direct and basket checkout
- Multi-seller order creation
- Coupon validation and discount snapshots
- Immutable item / price / delivery-address snapshots
- Atomic inventory decrement to prevent overselling
- Transaction rollback when any basket item fails
- Concurrent last-item purchase integration test

## Tech Stack
Java, Spring Boot, JPA/Hibernate, PostgreSQL, Gradle, Docker
Local Kubernetes deployment with Minikube

## Concurrency & Transaction Design
간단하게 atomic UPDATE와 rollback 선택 이유 5~10줄

## Running
./gradlew test
docker compose up ...
Minikube는 짧게

## Project Structure
service / repository / entity / controller 정도
