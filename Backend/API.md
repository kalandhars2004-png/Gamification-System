# Payment Gamification System — API Documentation

Base URL: `http://localhost:3000`
Content-Type: `application/json`

---

## 1. Register a User

`POST /api/users/register`

Generates `referralId` as `REF` + userId. If `referredBy` is given, it is validated against an existing user's referral ID.

### Request
```json
{
  "name": "Kalandhar",
  "email": "kalandhar@gmail.com",
  "password": "1234",
  "accountNo": "ACC1001",
  "balance": 10000,
  "referredBy": "REF1002"
}
```

### Response — 200 OK
```json
{
  "message": "User registered successfully",
  "userId": 1,
  "referralId": "REF1001"
}
```

### Error — 400 (invalid referral ID)
```json
{
  "message": "Invalid referral ID: REF9999"
}
```

### Error — 400 (duplicate email)
```json
{
  "message": "Email already registered"
}
```

---

## 2. Login

`POST /api/users/login`

Simple email/password check (no JWT).

### Request
```json
{
  "email": "kalandhar@gmail.com",
  "password": "1234"
}
```

### Response — 200 OK
```json
{
  "message": "Login successful",
  "userId": 1,
  "name": "Kalandhar"
}
```

### Error — 400
```json
{
  "message": "Invalid password"
}
```

---

## 3. User Dashboard

`GET /api/users/{id}/dashboard`

### Response — 200 OK
```json
{
  "name": "Kalandhar",
  "accountNo": "ACC1001",
  "balance": 10500,
  "totalTransactions": 5,
  "totalRewards": 7,
  "referralId": "REF1001"
}
```

---

## 4. Make a Payment (main flow)

`POST /api/transactions/pay`

Flow: balance check → deduct → cashback (slab or service %) → first-transaction referral reward → goal reward → rewards recorded.

### Request — Normal transfer
```json
{
  "userId": 2,
  "amount": 500,
  "type": "TRANSFER",
  "toUserId": 1
}
```

### Response — 200 OK (first transaction, has referral → ₹250 + goal ₹50)
```json
{
  "message": "Payment successful",
  "amount": 500,
  "cashback": 35,
  "rewardType": "CASHBACK",
  "rewardValue": null,
  "goalReward": 50,
  "balance": 10585,
  "goal": "5 transactions completed"
}
```

### Request — Service payment (10% capped at ₹75)
```json
{
  "userId": 1,
  "amount": 1000,
  "type": "ELECTRICITY"
}
```

### Response — 200 OK (service cashback: 10% of 1000 = 100, capped to 75)
```json
{
  "message": "Payment successful",
  "amount": 1000,
  "cashback": 75,
  "rewardType": "CASHBACK",
  "rewardValue": null,
  "goalReward": 0,
  "balance": 9000,
  "goal": null
}
```

### Request — Small amount → coupon reward
```json
{
  "userId": 1,
  "amount": 5,
  "type": "TRANSFER"
}
```

### Response — 200 OK (no money credit, coupon returned)
```json
{
  "message": "Payment successful",
  "amount": 5,
  "cashback": 0,
  "rewardType": "COUPON",
  "rewardValue": "COUPON-FREESHIP",
  "goalReward": 0,
  "balance": 10000,
  "goal": null
}
```

### Error — 400 (insufficient balance)
```json
{
  "message": "Insufficient balance"
}
```

### Error — 400 (transfer without recipient)
```json
{
  "message": "toUserId is required for TRANSFER"
}
```

---

## 5. Transaction History (user)

`GET /api/users/{id}/transactions`

### Response — 200 OK
```json
[
  {
    "id": 2,
    "userId": 2,
    "toUserId": 1,
    "amount": 500,
    "type": "TRANSFER",
    "cashback": 35,
    "date": "2026-08-17T12:30:00"
  }
]
```

---

## 6. All Transactions

`GET /api/transactions`

Same shape as above, newest first.

---

## 7. All Rewards Earned (user)

`GET /api/users/{id}/rewards`

Every cashback, coupon, subscription, referral reward and goal reward is recorded here.

### Response — 200 OK
```json
[
  {
    "id": 4,
    "userId": 1,
    "rewardType": "CASHBACK",
    "rewardValue": null,
    "source": "REFERRAL_REFERRER",
    "amount": 500,
    "transactionId": 2,
    "date": "2026-08-17T12:30:05"
  },
  {
    "id": 3,
    "userId": 2,
    "rewardType": "CASHBACK",
    "rewardValue": null,
    "source": "REFERRAL_NEW_USER",
    "amount": 250,
    "transactionId": 2,
    "date": "2026-08-17T12:30:05"
  }
]
```

Reward `source` values: `TRANSACTION_CASHBACK`, `SERVICE_CASHBACK`, `REFERRAL_NEW_USER`, `REFERRAL_REFERRER`, `GOAL_REWARD`

---

## 8. My Referrals (who joined with my code)

`GET /api/users/{id}/referrals`

### Response — 200 OK
```json
[
  {
    "id": 2,
    "name": "Sara",
    "email": "sara@gmail.com",
    "password": "1234",
    "accountNo": "ACC1002",
    "balance": 9750,
    "referralId": "REF1002",
    "referredBy": "REF1001"
  }
]
```

---

## 9. Add Balance (top-up)

`POST /api/users/{id}/add-balance`

### Request
```json
{
  "amount": 5000
}
```

### Response — 200 OK
```json
{
  "id": 1,
  "name": "Kalandhar",
  "email": "kalandhar@gmail.com",
  "password": "1234",
  "accountNo": "ACC1001",
  "balance": 15000,
  "referralId": "REF1001",
  "referredBy": null
}
```

---

# Generic Rule Management

All business rules live in the DB and are changeable at runtime — no code change needed.

## 10. Cashback Slab Rules

### `GET /api/rules/cashback`
```json
[
  {
    "id": 1,
    "minAmount": 0,
    "maxAmount": 9.99,
    "cashbackAmount": 0,
    "rewardType": "COUPON",
    "rewardValue": "COUPON-FREESHIP"
  },
  {
    "id": 2,
    "minAmount": 100,
    "maxAmount": 199.99,
    "cashbackAmount": 15,
    "rewardType": "CASHBACK",
    "rewardValue": null
  },
  {
    "id": 6,
    "minAmount": 5000,
    "maxAmount": null,
    "cashbackAmount": 100,
    "rewardType": "CASHBACK",
    "rewardValue": null
  }
]
```

### `POST /api/rules/cashback` — add new rule
```json
{
  "minAmount": 2000,
  "maxAmount": 2999.99,
  "cashbackAmount": 70,
  "rewardType": "CASHBACK",
  "rewardValue": null
}
```

### `PUT /api/rules/cashback/{id}` — update
### `DELETE /api/rules/cashback/{id}` — delete

---

## 11. Service Rules (percentage + cap)

### `GET /api/rules/services`
```json
[
  {
    "id": 1,
    "serviceType": "ELECTRICITY",
    "percentage": 10,
    "maxCap": 75,
    "rewardType": "CASHBACK",
    "rewardValue": null
  },
  {
    "id": 2,
    "serviceType": "MOBILE",
    "percentage": 5,
    "maxCap": 75,
    "rewardType": "CASHBACK",
    "rewardValue": null
  }
]
```

### `POST /api/rules/services` — add a new service type instantly (e.g. GAS)
```json
{
  "serviceType": "GAS",
  "percentage": 12,
  "maxCap": 100,
  "rewardType": "CASHBACK",
  "rewardValue": null
}
```

### `PUT /api/rules/services/{id}` — update
### `DELETE /api/rules/services/{id}` — delete

---

## 12. Goal Rules

### `GET /api/rules/goals`
```json
[
  {
    "id": 1,
    "transactionCount": 5,
    "reward": 50,
    "rewardType": "CASHBACK",
    "rewardValue": null
  },
  {
    "id": 2,
    "transactionCount": 10,
    "reward": 150,
    "rewardType": "CASHBACK",
    "rewardValue": null
  },
  {
    "id": 3,
    "transactionCount": 20,
    "reward": 300,
    "rewardType": "CASHBACK",
    "rewardValue": null
  }
]
```

### `POST /api/rules/goals` — add (goal reward can be a coupon/subscription too)
```json
{
  "transactionCount": 50,
  "reward": 0,
  "rewardType": "SUBSCRIPTION",
  "rewardValue": "1_MONTH_STREAMING"
}
```

### `PUT /api/rules/goals/{id}` — update
### `DELETE /api/rules/goals/{id}` — delete

---

## 13. App Config (referral amounts etc.)

### `GET /api/rules/config`
```json
[
  {
    "id": 1,
    "configKey": "referral.reward.newUser",
    "configValue": "250"
  },
  {
    "id": 2,
    "configKey": "referral.reward.referrer",
    "configValue": "500"
  }
]
```

### `PUT /api/rules/config/{key}` — change value at runtime (e.g. make referrer earn ₹1000)
```json
{
  "configValue": "1000"
}
```

### Response
```json
{
  "id": 2,
  "configKey": "referral.reward.referrer",
  "configValue": "1000"
}
```

---

## Quick Demo Script

1. Register A → `referralId = REF1001`
2. Register B with `referredBy: "REF1001"` → linked
3. B pays (first transaction) → B gets ₹250, A gets ₹500
4. `GET /api/users/1/rewards` → shows A's referral reward
5. B pays 5 times → `goalReward: 50` on the 5th payment
6. Add a service type via `POST /api/rules/services` → pay with it immediately
