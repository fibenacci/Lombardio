# Google Cloud Platform (GKE) Cluster Foundation

This environment adapter is for GKE on Google Cloud.

## Expected Environment Variables (GCP)
- `GOOGLE_CREDENTIALS` (JSON string or path to service account key)
- `GCP_PROJECT_ID`
- `GCP_REGION`

## Terraform Scope
- GCP Project APIs
- VPC Network & Subnets
- GKE Cluster
- Cloud SQL (Optional managed DB)
