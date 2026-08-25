#!/bin/sh
set -eu
awslocal sqs create-queue --queue-name payment-events
awslocal sqs create-queue --queue-name notification-events
