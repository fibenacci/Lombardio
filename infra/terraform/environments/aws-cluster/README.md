# Amazon Web Services (EKS) Cluster Foundation

This environment adapter is for EKS on AWS.

## Expected Environment Variables (AWS)
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

## Terraform Scope
- VPC, Subnets, IGW, NAT Gateway
- EKS Cluster & Managed Node Groups
- RDS (Optional managed DB)
