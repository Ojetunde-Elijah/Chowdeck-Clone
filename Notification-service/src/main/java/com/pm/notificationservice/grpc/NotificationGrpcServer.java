package com.pm.notificationservice.grpc;

@GrpcService
public class NotificationGrpcServer extends NotificationInternalServiceGrpc.NotificationInternalServiceImplBase {

    private final NotificationRepository repository;
    private final MailSender mailSender;

    public NotificationGrpcServer(NotificationRepository repository, MailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    @Override
    public void sendOrderEmail(OrderNotificationRequest request,
                               StreamObserver<NotificationResponse> responseObserver) {
        String subject;
        String body;

        // Pick content based on the Enum Type
        switch (request.getType()) {
            case PAYMENT_SUCCESS:
                subject = "Payment Received! Order #" + request.getOrderId();
                body = "We've received your payment of ₦" + request.getTotalAmount() + ". Your food is being prepared!";
                break;
            case OUT_FOR_DELIVERY:
                subject = "Rider is on the way!";
                body = "Your order #" + request.getOrderId() + " has been picked up and is coming to you.";
                break;
            case ORDER_PLACED:
            default:
                subject = "Order Placed: #" + request.getOrderId();
                body = "Your order has been received. Please complete payment to start processing.";
                break;
        }

        // ... (rest of the SES sending logic remains the same)
    }
}