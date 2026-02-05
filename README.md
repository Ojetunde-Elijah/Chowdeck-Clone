🍔 Chowdeck Microservices Backend
gRPC-Powered Distributed System on AWS
This project is a high-performance, cloud-native food ordering backend architected with a microservices mindset. It leverages Java 21, Spring Boot, and gRPC to achieve ultra-low latency service-to-service communication, all orchestrated on AWS using Infrastructure as Code (CDK).

🏗 System Architecture
The system is designed to handle high-concurrency ordering flows by decoupling core business domains:

Auth Service: Manages JWT-based security and identity.

Order Service: The orchestrator for order placement, communicating with User and Restaurant services.

Restaurant Service: Manages menus, availability, and restaurant metadata.

User Service: Handles profile management and address data.

API Gateway: A public-facing entry point routing external REST traffic to internal gRPC/REST services.

Infrastructure (AWS)
Networking: Custom VPC with isolated public/private subnets.

Compute: AWS ECS Fargate (Serverless Container Orchestration).

Database: Amazon RDS (PostgreSQL) with automated provisioning.

Discovery: AWS Cloud Map for internal service discovery (*.chowdeck.local).

Security: AWS Secrets Manager for sensitive credential injection.

🛠 Technical Stack
Category	Technology
Language	Java 21
Framework	Spring Boot 3.x
Communication	gRPC, Protobuf, REST
Infrastructure	AWS CDK (Java)
Containerization	Docker
Database	PostgreSQL
🚀 Key Technical Exploits
⚡ Sub-100ms gRPC Communication
Unlike standard REST-to-REST microservices, this system uses gRPC (HTTP/2) for internal calls. By using binary serialization with Protocol Buffers, payload sizes are reduced by up to 60%, significantly lowering latency during the order checkout flow.

🛡 Infrastructure as Code (IaC)
The entire AWS environment is defined in Java using AWS CDK. This ensures 100% reproducible environments.

Java
// Example: Provisioning the ECS Cluster and VPC in CDK
Vpc vpc = Vpc.Builder.create(this, "ChowdeckVPC").maxAzs(2).build();
Cluster cluster = Cluster.Builder.create(this, "ChowdeckCluster").vpc(vpc).build();
🔒 Zero-Trust Networking
Services are tucked away in private subnets. Only the Application Load Balancer (ALB) is exposed to the internet. Internal service-to-service communication is restricted via AWS Security Groups to ensure "Least Privilege" access.

📦 Local Development
Prerequisites
JDK 21

Docker & Docker Compose

AWS CLI & CDK Bootstrap

Setup
Clone the repo:

Bash
git clone https://github.com/your-username/chowdeck-backend.git
Build the Protobuf definitions:

Bash
mvn clean install
Run via Docker Compose (Local Testing):

Bash
docker-compose up --build
🌍 Deployment
To deploy the entire stack to AWS:

Bash
cd cdk-stack
cdk deploy
👤 Author
Ojetunde Elijah Oluwaseun

Backend Lead @ GDG on Campus UI

LinkedIn | Twitter
