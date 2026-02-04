package stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import stack.ChowdeckStack;

public class ChowdeckApp {
    public static void main(final String[] args) {
        App app = new App();

        // Best Practice: Specify your AWS Account and Region explicitly
        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();

        new ChowdeckStack(app, "ChowdeckProductionStack", StackProps.builder()
                .env(env)
                .description("Production infrastructure for Chowdeck Microservices")
                .build());

        app.synth();
    }
}