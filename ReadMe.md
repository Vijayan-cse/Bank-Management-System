API Endpoints:

AUTH (public)
POST   /api/auth/register
POST   /api/auth/login

ACCOUNTS (JWT required)
POST   /api/accounts               → create account
GET    /api/accounts               → get all my accounts
GET    /api/accounts/{accNo}       → get single account
POST   /api/accounts/deposit       → deposit
POST   /api/accounts/withdraw      → withdraw
POST   /api/accounts/transfer      → transfer
GET    /api/accounts/{accNo}/transactions → transaction history

USER (JWT required)
GET    /api/users/profile          → get profile
PUT    /api/users/profile          → update profile

*********************************************************
Feature added newly by me:
1. Feature 1 — Minimum Balance Validation - updated in withdraw method

2. Password change API

3. Pagination for Transactions

4. Swagger UI
"http://localhost:8080/swagger-ui/index.html"
