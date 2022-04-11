# DistributedSystemSkiResort
- Implemented RESTful APIs that support recording of ski ride events.
- Built back-end server in Java that utilizes asynchronous messaging system RabbitMQ and consumes messages by storeing data in Redis. Server replicas were deployed on AWS EC2 instances and traffic was distributed by AWS Application Load Balancer.
- Built multi-thread clients to stress test and evaluate the performance by measuring throughput, mean/median response time, P99.
