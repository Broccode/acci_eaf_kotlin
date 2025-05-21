# Environment Variables Reference

This document lists all environment variables used by the EAF platform.

## General Variables

| Variable | Description | Default | Required | Used By |
|----------|-------------|---------|----------|---------|
| `EAF_ENV` | Environment name (`dev`, `test`, `prod`) | `dev` | No | All modules |
| `EAF_LOG_LEVEL` | Logging level | `INFO` | No | All modules |

## IAM Module

| Variable | Description | Default | Required | Used By |
|----------|-------------|---------|----------|---------|
| `EAF_IAM_JWT_SECRET_KEY` | Secret key for signing JWT tokens. Must be at least 32 characters (256 bits) for proper security. | `changeit` (UNSECURE) | Yes in production | `eaf-iam` |
| `EAF_IAM_JWT_EXPIRATION_MS` | Expiration time for JWT access tokens in milliseconds | `3600000` (1 hour) | No | `eaf-iam` |
| `EAF_IAM_JWT_REFRESH_EXPIRATION_MS` | Expiration time for JWT refresh tokens in milliseconds | `604800000` (7 days) | No | `eaf-iam` |
| `EAF_IAM_LOCKOUT_MAX_ATTEMPTS` | Maximum number of failed login attempts before account lockout | `5` | No | `eaf-iam` |
| `EAF_IAM_LOCKOUT_DURATION_MINUTES` | Duration in minutes for which an account remains locked after too many failed attempts | `15` | No | `eaf-iam` |

## Database Configuration

| Variable | Description | Default | Required | Used By |
|----------|-------------|---------|----------|---------|
| `EAF_DB_HOST` | Database host | `localhost` | No | All modules |
| `EAF_DB_PORT` | Database port | `5432` | No | All modules |
| `EAF_DB_NAME` | Database name | `eaf` | No | All modules |
| `EAF_DB_USER` | Database username | `eaf` | Yes in production | All modules |
| `EAF_DB_PASSWORD` | Database password | - | Yes in production | All modules |

## Security Recommendations

### JWT Secret Key

- The `EAF_IAM_JWT_SECRET_KEY` should be a cryptographically strong random value.
- Minimum length: 32 characters (256 bits) for HS256 signature algorithm.
- Should be unique per environment.
- Should be rotated periodically (quarterly or after incidents).
- Example generation (bash): `openssl rand -base64 32`

### Production Security

- Never use default values in production.
- Store secrets in a secure vault or environment variable management system.
- Restrict access to environment variable configurations.
