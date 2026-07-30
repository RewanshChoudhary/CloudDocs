# AWS Infrastructure Scaffold

This folder is reserved for the AWS resources in the README architecture:

- Private S3 bucket with `raw/` and `processed/` prefixes.
- S3 event notification wired to SQS.
- SQS standard queue plus dead-letter queue.
- EC2 IAM role with least-privilege S3 and SQS access.
- CloudWatch log groups for API and worker containers.

Terraform or CloudFormation can live here once the exact AWS account and naming conventions are chosen.
