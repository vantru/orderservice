package com.appsdeveloperblog.estore.ordersservice.data;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "outbox_event")
@Data
public class OutboxEventEntity {
    @Id
    private UUID id;
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "payload", nullable = false, length = 10000)
    private String payload;
    @Column(name = "created_at", nullable = false )
    private LocalDateTime createdAt;

    @PrePersist
    private void  prePersits(){
        if(id == null) this.id = UUID.randomUUID();
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
