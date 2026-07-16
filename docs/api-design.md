# API Design Documentation

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
Currently unauthenticated (OAuth2/JWT planned — see Future Improvements).

---

## Endpoints

### Plans

#### `GET /plans`
Returns all active membership plans ordered by duration.

**Response 200:**
```json
[
  {
    "id": 1,
    "name": "Monthly",
    "durationMonths": 1,
    "price": 99.00,
    "active": true,
    "createdAt": "2024-01-01T00:00:00Z"
  },
  {
    "id": 2,
    "name": "Quarterly",
    "durationMonths": 3,
    "price": 249.00,
    "active": true,
    "createdAt": "2024-01-01T00:00:00Z"
  }
]
```

#### `GET /plans/{id}`
**Response 200:** Single plan object (same shape as above)  
**Response 404:** `PlanNotFoundException`

---

### Tiers

#### `GET /tiers`
Returns all active tiers ordered by priority (Platinum first).

**Response 200:**
```json
[
  {
    "id": 3,
    "name": "Platinum",
    "priority": 3,
    "discountPercentage": 20.00,
    "freeDelivery": true,
    "prioritySupport": true,
    "earlyAccess": true,
    "configuration": {
      "cashbackPercent": 5,
      "maxItemsPerOrder": 100,
      "dedicatedAccountManager": true,
      "loungeAccess": true
    },
    "active": true
  }
]
```

---

### Subscriptions

#### `POST /memberships`
Subscribe a user to a plan and tier.

**Request:**
```json
{
  "userId": 1,
  "planId": 2,
  "tierId": 1
}
```

**Response 201:**
```json
{
  "id": 42,
  "userId": 1,
  "plan": { "id": 2, "name": "Quarterly", ... },
  "tier": { "id": 1, "name": "Silver", ... },
  "status": "ACTIVE",
  "startDate": "2024-06-01",
  "expiryDate": "2024-09-01",
  "version": 0,
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T10:00:00Z"
}
```

**Error 409** — User already has active membership:
```json
{
  "timestamp": "2024-06-01T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "User 1 already has an active membership.",
  "path": "/api/v1/memberships",
  "traceId": "A1B2C3D4"
}
```

---

#### `PUT /memberships/{id}/upgrade`
Upgrade to a higher-priority tier.

**Request:**
```json
{
  "targetPlanId": 3,
  "targetTierId": 2
}
```

**Response 200:** Updated membership  
**Response 409:** Concurrent modification conflict  
**Response 422:** Target tier is not higher priority

---

#### `PUT /memberships/{id}/downgrade`
Downgrade to a lower-priority tier.

**Request:**
```json
{
  "targetPlanId": 1,
  "targetTierId": 1
}
```

**Response 200:** Updated membership  
**Response 422:** Target tier is not lower priority

---

#### `PUT /memberships/{id}/cancel`
Cancel a membership.

**Response 200:** Cancelled membership  
**Response 409:** Already cancelled or concurrent conflict

---

### User Membership

#### `GET /users/{userId}/membership`
Get the current active membership for a user.

**Response 200:** Full membership response  
**Response 404:** No active membership

---

#### `GET /users/{userId}/membership/status`
Get expiry status with countdown.

**Response 200:**
```json
{
  "membershipId": 42,
  "userId": 1,
  "status": "ACTIVE",
  "expiryDate": "2024-09-01",
  "daysUntilExpiry": 5,
  "expiringSoon": true,
  "expired": false
}
```

---

#### `POST /users/{userId}/tier/evaluate`
Run the Strategy Pattern tier evaluation engine.

**Request:**
```json
{
  "orderCount": 25,
  "monthlySpend": 2500.00,
  "cohort": "VIP"
}
```

**Response 200 (tier found):**
```json
{
  "userId": 1,
  "recommendedTier": { "id": 3, "name": "Platinum", ... },
  "tierFound": true,
  "message": "Based on your activity, you qualify for the Platinum tier."
}
```

**Response 200 (no tier):**
```json
{
  "userId": 1,
  "recommendedTier": null,
  "tierFound": false,
  "message": "No tier qualification found based on current activity metrics."
}
```

---

## Error Response Format

All errors return a consistent structure:

```json
{
  "timestamp": "2024-06-01T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Membership plan not found with id: 99",
  "path": "/api/v1/plans/99",
  "traceId": "A1B2C3D4"
}
```

| Field | Description |
|---|---|
| `timestamp` | ISO-8601 UTC timestamp |
| `status` | HTTP status code |
| `error` | HTTP status reason phrase |
| `message` | Human-readable error detail |
| `path` | Request path that triggered the error |
| `traceId` | Short ID for log correlation |

---

## Status Codes Summary

| Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Resource created |
| 400 | Validation failure |
| 404 | Resource not found |
| 409 | Conflict (duplicate subscription or concurrent modification) |
| 410 | Gone (expired membership) |
| 422 | Unprocessable (invalid upgrade/downgrade direction) |
| 500 | Internal error |
