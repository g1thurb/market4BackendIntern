# Implementation Plan

## Phase 1: Project Setup
- SecurityConfig
- application.yml
- HealthCheckApiController
- DB connection test

## Phase 2: User Auth
### Pages
- GET /user/login
- GET /user/sign-up

### APIs
- POST /api/user/login
- POST /api/user/sign-up
- GET /api/user/sign-up/check-id?id=
- GET /api/user/sign-up/check-nickname?nickname=

### DTO
- UserLoginRequest
- UserSignUpRequest
- AvailabilityResponse
- ErrorResponse

### Entity
- UserInfo
- UserLogin
- UserBasket

### Repository
- UserInfoRepository
- UserLoginRepository
- UserBasketRepository

### Service
- UserAuthService