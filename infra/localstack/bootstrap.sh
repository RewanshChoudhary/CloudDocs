#!/bin/sh
set -e

BUCKET_NAME="${MEDIA_S3_BUCKET:-clouddocs-media-local}"
QUEUE_NAME="${MEDIA_SQS_QUEUE_NAME:-clouddocs-media-events}"
DLQ_NAME="${MEDIA_SQS_DLQ_NAME:-clouddocs-media-events-dlq}"

awslocal s3 mb "s3://${BUCKET_NAME}" 2>/dev/null || true

DLQ_URL="$(awslocal sqs create-queue --queue-name "${DLQ_NAME}" --query QueueUrl --output text)"
DLQ_ARN="$(awslocal sqs get-queue-attributes --queue-url "${DLQ_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"

QUEUE_URL="$(awslocal sqs create-queue \
  --queue-name "${QUEUE_NAME}" \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"${DLQ_ARN}\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}" \
  --query QueueUrl \
  --output text)"
QUEUE_ARN="$(awslocal sqs get-queue-attributes --queue-url "${QUEUE_URL}" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)"

cat >/tmp/s3-notification.json <<EOF
{
  "QueueConfigurations": [
    {
      "QueueArn": "${QUEUE_ARN}",
      "Events": ["s3:ObjectCreated:*"],
      "Filter": {
        "Key": {
          "FilterRules": [
            {
              "Name": "prefix",
              "Value": "raw/"
            }
          ]
        }
      }
    }
  ]
}
EOF

awslocal s3api put-bucket-notification-configuration \
  --bucket "${BUCKET_NAME}" \
  --notification-configuration file:///tmp/s3-notification.json

echo "LocalStack media resources ready: bucket=${BUCKET_NAME}, queue=${QUEUE_URL}, dlq=${DLQ_URL}"
