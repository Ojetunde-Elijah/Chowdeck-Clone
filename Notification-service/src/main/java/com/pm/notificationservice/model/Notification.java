package com.pm.notificationservice.model;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String recipientEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // QUEUED, SENT, DELIVERED, FAILED

    private String sesMessageId; // From AWS for tracking
    private Instant createdAt = Instant.now();
    private Instant sentAt;
}