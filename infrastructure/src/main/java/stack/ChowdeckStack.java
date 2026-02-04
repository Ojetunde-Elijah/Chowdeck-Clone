package stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChowdeckStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;
    private final PrivateDnsNamespace dnsNamespace;
    private final Map<String, FargateService> services = new HashMap<>();

    public ChowdeckStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        // 1. Networking
        this.vpc = Vpc.Builder.create(this, "ChowdeckVPC").maxAzs(2).build();
        this.dnsNamespace = PrivateDnsNamespace.Builder.create(this, "Namespace")
                .name("chowdeck.local").vpc(vpc).build();
        this.ecsCluster = Cluster.Builder.create(this, "ChowdeckCluster").vpc(vpc).build();

        // 2. Shared Database (RDS)
        DatabaseInstance sharedDb = DatabaseInstance.Builder.create(this, "SharedDB")
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_16).build()))
                .vpc(vpc)
                .databaseName("chowdeck_main")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .removalPolicy(RemovalPolicy.DESTROY)
                .credentials(Credentials.fromGeneratedSecret("postgres"))
                .build();

        // 3. Application Secrets
        Secret appSecrets = Secret.Builder.create(this, "AppSecrets")
                .secretName("ChowdeckAppSecrets")
                .secretObjectValue(Map.of(
                        "JWT_SECRET", SecretValue.unsafePlainText(""),
                        "PAYSTACK_SECRET_KEY", SecretValue.unsafePlainText("")
                )).build();

        // 4. Microservices (Ports updated to your log outputs)
        services.put("auth", createService("Auth", "auth-service", 7005, 9090, sharedDb,
                Map.of("JWT_EXPIRATION", "86400000",
                        "USER_SERVICE_ADDRESS", "user.chowdeck.local",
                        "USER_SERVICE_GRPC_PORT", "7090"),
                Map.of("JWT_SECRET", software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(appSecrets, "JWT_SECRET"))));

        services.put("user", createService("User", "User-service", 7006, 7090, sharedDb, null, null));
        services.put("restaurant", createService("Restaurant", "Restaurant-service", 7007, 7091, sharedDb, null, null));
        services.put("payment", createService("Payment", "Payment-service", 7011, 9090, sharedDb, null,
                Map.of("PAYSTACK_SECRET_KEY", software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(appSecrets, "PAYSTACK_SECRET_KEY"))));

        services.put("order", createService("Order", "Order-service", 7008, 7092, sharedDb, Map.of(
                "GRPC_CLIENT_USER_SERVICE_ADDRESS", "static://user.chowdeck.local:7090",
                "GRPC_CLIENT_USER_SERVICE_NEGOTIATION_TYPE", "PLAINTEXT",


                // This maps directly to @GrpcClient("restaurant-service")
                "grpc.client.restaurant-service.address", "static://restaurant.chowdeck.local:7091",
                "grpc.client.restaurant-service.negotiation-type", "PLAINTEXT"
        ), null));

        // 5. Public Gateway
        ApplicationLoadBalancedFargateService gatewaySvc = createApiGateway(Map.of(
                "AUTH_SERVICE_URL", "http://auth.chowdeck.local:7005",
                "USER_SERVICE_URL", "http://user.chowdeck.local:7006",
                "RESTAURANT_SERVICE_URL", "http://restaurant.chowdeck.local:7007",
                "PAYMENT_SERVICE_URL", "http://payment.chowdeck.local:7011",
                "ORDER_SERVICE_URL", "http://order.chowdeck.local:7008"
        ));

        setupUniversalNetworking(gatewaySvc.getService(), sharedDb);
    }

    private FargateService createService(String id, String imageName, int webPort, int grpcPort, DatabaseInstance db,
                                         Map<String, String> extraEnv, Map<String, software.amazon.awscdk.services.ecs.Secret> extraSecrets) {

        FargateTaskDefinition taskDef = FargateTaskDefinition.Builder.create(this, id + "Task")
                .cpu(512).memoryLimitMiB(1024).build();

        Map<String, String> env = new HashMap<>();

        env.put("GRPC_SERVER_PORT", String.valueOf(grpcPort));
        Map<String, software.amazon.awscdk.services.ecs.Secret> secrets = new HashMap<>();

        if (db != null) {
            // Using the 'public' schema bypasses the need for schema creation SQL
            // Hibernate will simply create the tables (users, orders, etc) in the public space.
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/chowdeck_main",
                    db.getDbInstanceEndpointAddress(), db.getDbInstanceEndpointPort());

            env.put("SPRING_DATASOURCE_URL", jdbcUrl);
            env.put("SPRING_JPA_DATABASE_PLATFORM", "org.hibernate.dialect.PostgreSQLDialect");
            env.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");

            // REMOVED all HIKARI_CONNECTION_INIT and SCHEMA properties to avoid pool sealing errors.

            secrets.put("SPRING_DATASOURCE_USERNAME", software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(db.getSecret(), "username"));
            secrets.put("SPRING_DATASOURCE_PASSWORD", software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(db.getSecret(), "password"));
        }

        if (extraEnv != null) env.putAll(extraEnv);
        if (extraSecrets != null) secrets.putAll(extraSecrets);

        taskDef.addContainer(id + "Container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromAsset("../" + imageName))
                .environment(env).secrets(secrets)
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix(id)
                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                .retention(RetentionDays.ONE_DAY).removalPolicy(RemovalPolicy.DESTROY).build()).build()))
                .portMappings(List.of(
                        PortMapping.builder().containerPort(webPort).protocol(Protocol.TCP).build(),
                        PortMapping.builder().containerPort(grpcPort).protocol(Protocol.TCP).build()
                )).build());

        return FargateService.Builder.create(this, id + "Svc")
                .cluster(ecsCluster).taskDefinition(taskDef).serviceName(id.toLowerCase() + "-service")
                .cloudMapOptions(CloudMapOptions.builder().name(id.toLowerCase()).cloudMapNamespace(dnsNamespace).build())
                .assignPublicIp(true)
                .build();
    }

    private ApplicationLoadBalancedFargateService createApiGateway(Map<String, String> gatewayEnv) {
        ApplicationLoadBalancedFargateService gateway = ApplicationLoadBalancedFargateService.Builder.create(this, "PublicGateway")
                .cluster(ecsCluster).cpu(512).memoryLimitMiB(1024)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromAsset("../Api-gateway"))
                        .environment(gatewayEnv).containerPort(4004).build())
                .publicLoadBalancer(true).build();

        gateway.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .path("/actuator/health").port("4004").healthyHttpCodes("200,404").build());

        return gateway;
    }

    private void setupUniversalNetworking(FargateService gateway, DatabaseInstance db) {
        db.getConnections().allowFrom(Peer.ipv4(vpc.getVpcCidrBlock()), Port.tcp(5432), "Allow DB access");
        for (FargateService service : services.values()) {
            service.getConnections().allowFrom(gateway, Port.allTcp(), "Allow Gateway traffic");
            service.getConnections().allowFrom(Peer.ipv4(vpc.getVpcCidrBlock()), Port.allTcp(), "Allow VPC traffic");
            service.getConnections().allowToAnyIpv4(Port.allTcp());
        }
        gateway.getConnections().allowTo(Peer.ipv4(vpc.getVpcCidrBlock()), Port.allTcp(), "Gateway to services");
    }
}